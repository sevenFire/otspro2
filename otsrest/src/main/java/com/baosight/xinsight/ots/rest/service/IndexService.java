package com.baosight.xinsight.ots.rest.service;

import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.OtsIndex;
import com.baosight.xinsight.ots.client.OtsTable;
import com.baosight.xinsight.ots.common.secondaryindex.SecondaryIndexColumn;
import com.baosight.xinsight.ots.common.secondaryindex.SecondaryIndexInfo;
import com.baosight.xinsight.ots.constants.ParamConstant;
import com.baosight.xinsight.ots.constants.ParamErrorCode;
import com.baosight.xinsight.ots.constants.TableConstants;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.ErrorMode;
import com.baosight.xinsight.ots.rest.util.ConfigUtil;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;

import java.io.IOException;

/**
 * @author liyuhui
 * @date 2018/12/20
 * @description
 */
public class IndexService {
    private static final Logger LOG = Logger.getLogger(IndexService.class);

    /**
     * 创建索引
     * 被选为索引列的列有顺序，通过index_key构建rowKey，然后cell中存储的是primary_key构建的rowKey。
     * @param userInfo
     * @param tableName
     * @param indexName
     * @param postBody
     * @return
     */
    public static ErrorMode createIndex(PermissionCheckUserInfo userInfo,
                                        String tableName,
                                        String indexName,
                                        JsonNode postBody) throws OtsException, IOException {

        LOG.info("Create index " + tableName + "." + indexName);
        long errorCode = 0;
        ErrorMode rMode = new ErrorMode(errorCode);

        //todo lyh 校验postBody的格式
        String indexType = getRestParam(postBody, ParamConstant.KEY_INDEX_TYPE, true).asText();
        ArrayNode indexKey = (ArrayNode)getRestParam(postBody, ParamConstant.KEY_INDEX_KEY, true);

        //get table, otsTable中含有conf的信息
        OtsTable otsTable = ConfigUtil.getInstance().getOtsAdmin().getOtsTableByUniqueKey(userInfo.getTenantId(), tableName);

        //set in permission
        if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
            PermissionUtil.GetInstance().checkEditPermission(userInfo, otsTable.getTableId());
        }

        //check table
        if(otsTable == null){
            errorCode = OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST;
            rMode.setError_code(errorCode);
            rMode.setErrinfo("Table " + tableName + " no exist!");
        }

        //get index
        OtsIndex otsIndex = otsTable.getOtsIndex(indexName);

        //check index
        if (otsIndex != null) {
            LOG.error("Index '" + indexName + "' of table '" + tableName + "' in tenant '" + userInfo.getTenantId() + "' has exist!");
            throw new OtsException(OtsErrorCode.EC_OTS_INDEX_ALREADY_EXIST,
                    "Index '" + indexName + "' of table '" + tableName + "' in tenant '" + userInfo.getTenantId() + "' has exist!");
        }

        if (TableConstants.OTS_INDEX_TYPE_HBASE_STRING.equalsIgnoreCase(indexType)) {//HBase索引
            //todo lyh 存储于数据库内的数据格式为：(col1:type1)
        	ObjectMapper mapper = new ObjectMapper();
            ArrayNode schema_tableColumns = (ArrayNode)mapper.readTree(otsTable.getInfo().getTableColumns()); 

            SecondaryIndexInfo secIndexInfo = new SecondaryIndexInfo(indexName);
            for (int i = 0; i < indexKey.size(); i++) {
                JsonNode columnJSONObject = indexKey.get(i);

                String colName = getRestParam(columnJSONObject, ParamConstant.KEY_COL_NAME, true).asText();
                //有些类别必须写明maxLen，有些类型可以没有，采用默认值。
                Integer colMaxLen = getRestParam(columnJSONObject, ParamConstant.KEY_COL_MAXLEN, false).asInt();
                //列类型要从表schema中拿
                String colType = schema_tableColumns.get(colName).asText().toLowerCase();
                Integer realColMaxLen = dealWithColMaxLen(colType,colMaxLen);

                //构建SecondaryIndexInfo
                secIndexInfo.addColumn(colName, SecondaryIndexColumn.ValueType.valueOf(colType),realColMaxLen);
            }

            //create secondaryIndex
//            otsTable.createSecondaryIndex(secIndexInfo).rebuild(OtsConstants.OTS_MAPREDUCE_JAR_PATH, false);
        }else{
            throw new OtsException(OtsErrorCode.EC_OTS_SEC_INDEX_INVALID_VALUE,"not suitable type");
        }

        LOG.debug("RETURN:" + rMode.toString());
        return rMode;
    }

    /**
     * todo lyh other situation
     * 处理列的maxLen
     * @param colType
     * @param colMaxLen
     * @return
     */
    private static Integer dealWithColMaxLen(String colType, Integer colMaxLen) throws OtsException {
        switch (colType){
            case "string":
            case "blob":
                if (colMaxLen == null || colMaxLen <= 0) {
                    throw new OtsException(OtsErrorCode.EC_OTS_SEC_INDEX_INVALID_MAX_LEN, "Invalid type maxLen");
                }
                return colMaxLen;
            case "int32":
                return OtsConstants.OTS_SEC_INDEX_TYPE_INT_LEN;
            case "float32":
                return OtsConstants.OTS_SEC_INDEX_TYPE_FLOAT_LEN;
            case "int64":
                return OtsConstants.OTS_SEC_INDEX_TYPE_LONG_LEN;
            case "float64":
                return OtsConstants.OTS_SEC_INDEX_TYPE_DOUBLE_LEN;
            default:
                throw new OtsException(OtsErrorCode.EC_OTS_SEC_INDEX_INVALID_MAX_LEN, "Invalid type maxLen");
        }

    }

    /**
     * 获取参数的值，包含校验
     * @param body
     * @param propName 属性名
     * @param must 是否必填
     * @return
     */
    private static JsonNode getRestParam(JsonNode body, String propName, boolean must) throws OtsException {
        if (!body.has(propName)){
            if(must){
                throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID,propName);
            }else{
                return null;
            }
        }

        return body.get(propName);
    }
}
