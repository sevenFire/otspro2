package com.baosight.xinsight.ots.rest.service;

import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.OtsIndex;
import com.baosight.xinsight.ots.client.OtsTable;
import com.baosight.xinsight.ots.client.metacfg.Index;
import com.baosight.xinsight.ots.common.secondaryindex.SecondaryIndexColumn;
import com.baosight.xinsight.ots.common.secondaryindex.SecondaryIndexInfo;
import com.baosight.xinsight.ots.common.util.ColumnsUtil;
import com.baosight.xinsight.ots.constants.TableConstants;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.ErrorMode;
import com.baosight.xinsight.ots.rest.constant.RestErrorCode;
import com.baosight.xinsight.ots.rest.model.index.request.IndexCreateBody;
import com.baosight.xinsight.ots.rest.model.index.response.IndexInfoListResponseBody;
import com.baosight.xinsight.ots.rest.model.index.response.IndexNamesResponseBody;
import com.baosight.xinsight.ots.rest.model.index.same.IndexColumnsBody;
import com.baosight.xinsight.ots.rest.model.index.same.IndexInfoBody;
import com.baosight.xinsight.ots.rest.model.table.response.TableInfoBody;
import com.baosight.xinsight.ots.rest.util.ConfigUtil;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;
import com.baosight.xinsight.ots.rest.util.TableConfigUtil;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                                        IndexCreateBody postBody) throws OtsException, IOException {

        LOG.info("Create index " + tableName + "." + indexName);
        long errorCode = 0;
        ErrorMode rMode = new ErrorMode(errorCode);

        String indexType = postBody.getIndexType();
        List<IndexColumnsBody> indexKey = postBody.getIndexKey();
        if (indexKey == null || indexKey.size() ==0){
            throw new OtsException(OtsErrorCode.EC_OTS_INDEX_INVALID_PARAM,"index_key至少包含一列");
        }

        //get OtsTable
        OtsTable otsTable = ConfigUtil.getInstance().getOtsAdmin().getOtsTableByUniqueKey(userInfo.getTenantId(), tableName);
        //check table
        if(otsTable == null){
            errorCode = OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST;
            rMode.setError_code(errorCode);
            rMode.setErrinfo("Table " + tableName + " no exist!");
        }

        //set in permission
        if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
            PermissionUtil.GetInstance().checkEditPermission(userInfo, otsTable.getTableId());
        }

        if (TableConstants.OTS_INDEX_TYPE_HBASE_STRING.equalsIgnoreCase(indexType)) {//HBase索引
            //todo lyh 存储于数据库内的数据格式为：(col1:type1)
        	ObjectMapper mapper = new ObjectMapper();
            ArrayNode schema_tableColumns = (ArrayNode)mapper.readTree(otsTable.getInfo().getTableColumns());
            Map<String,String> schema_tableColumnsMap = ColumnsUtil.dealWithTableColumns(schema_tableColumns);

            IndexInfoBody indexInfoBody = new IndexInfoBody(indexName,indexType);

            for (int i = 0; i < indexKey.size(); i++) {
                IndexColumnsBody columnBody = indexKey.get(i);

                String colName = columnBody.getColName();
                //有些类别必须写明maxLen，有些类型可以没有，采用默认值。
                Integer colMaxLen = columnBody.getColMaxLen();
                //列类型要从表schema中拿
                if (schema_tableColumnsMap.containsKey(colName)) {
                    String colType = schema_tableColumnsMap.get(colName);
                    //获取最终的最大长度
                    Integer realColMaxLen = dealWithColMaxLen(colType,colMaxLen);

                    indexInfoBody.addColumn(colName,colType,realColMaxLen);
                }else{
                    throw new OtsException(OtsErrorCode.EC_OTS_INDEX_INVALID_PARAM,"该列名"+ colName+"不匹配表结构中的任何一列");
                }
            }

            //create index, 包含RDB中和HBase中
            Index index = indexInfoBody.fromBodyToIndex();
            otsTable.createIndexHBase(index);
//                    .rebuild(OtsConstants.OTS_MAPREDUCE_JAR_PATH, false);
        }else{
            //todo lyh 以后要加的类型是ES索引
//            createIndexES()
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

//    /**
//     * 获取参数的值，包含校验
//     * @param body
//     * @param propName 属性名
//     * @param must 是否必填
//     * @return
//     */
//    private static JsonNode getRestParam(IndexCreateBody body, String propName, boolean must) throws OtsException {
//        if (!body.has(propName)){
//            if(must){
//                throw new OtsException(ParamErrorCode.EC_OTS_REST_PARAM_INVALID,propName);
//            }else{
//                return null;
//            }
//        }
//
//        return body.get(propName);
//    }

    /**
     * 获取某小表的所有索引
     * @param userInfo
     * @param tableName
     * @return
     */
    public static IndexInfoListResponseBody getIndexInfoList(PermissionCheckUserInfo userInfo, String tableName) throws OtsException {
        IndexInfoListResponseBody indexInfoListResponseBody = new IndexInfoListResponseBody();

        OtsTable tableInfo;
        try {
            tableInfo = ConfigUtil.getInstance().getOtsAdmin().getOtsTableByUniqueKey(userInfo.getTenantId(), tableName);

            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
                PermissionUtil.GetInstance().checkReadPermission(userInfo, tableInfo.getTableId());
            }

            List<OtsIndex> indexList = tableInfo.getAllIndexInfo();
            List<IndexInfoBody> indexInfoBodies = new ArrayList<>();
            for (int i = 0; i < indexList.size(); i++) {
                indexInfoBodies.add(fromIndex(indexList.get(i)));
            }

            indexInfoListResponseBody.setIndexInfoList(indexInfoBodies);

        } catch (OtsException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw new OtsException(RestErrorCode.EC_OTS_REST_QUERY_INDEX, "failed to query table:" + tableName + " indexes");
        }


        return indexInfoListResponseBody;
    }

    /**
     * 查询单个索引的信息
     * @param userInfo
     * @param tableName
     * @param indexName
     * @return
     */
    public static IndexInfoBody getIndexInfo(PermissionCheckUserInfo userInfo, String tableName, String indexName) throws OtsException {
        IndexInfoBody indexInfoBody;

        OtsTable tableInfo;
        try {
            tableInfo = ConfigUtil.getInstance().getOtsAdmin().getOtsTableByUniqueKey(userInfo.getTenantId(), tableName);

            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
                PermissionUtil.GetInstance().checkReadPermission(userInfo, tableInfo.getTableId());
            }

            OtsIndex otsIndex = tableInfo.getOtsIndex(indexName);
            indexInfoBody = fromIndex(otsIndex);
            indexInfoBody.setErrcode(0L);

        } catch (OtsException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw new OtsException(RestErrorCode.EC_OTS_REST_QUERY_INDEX, "failed to query table:" + tableName + " indexes");
        }

        return indexInfoBody;
    }


    /**
     * 获取索引名的列表
     * @param userInfo
     * @param tableName
     * @return
     * @throws OtsException
     */
    public static IndexNamesResponseBody getIndexNameList(PermissionCheckUserInfo userInfo, String tableName) throws OtsException {
        IndexNamesResponseBody indexNamesResponseBody = new IndexNamesResponseBody();


        OtsTable tableInfo;
        try {
            tableInfo = ConfigUtil.getInstance().getOtsAdmin().getOtsTableByUniqueKey(userInfo.getTenantId(), tableName);

            if (userInfo.getTenantId() != null && userInfo.getUserId() != null) {
                PermissionUtil.GetInstance().checkReadPermission(userInfo, tableInfo.getTableId());
            }

            List<OtsIndex> indexList = tableInfo.getAllIndexInfo();
            List<String> indexNames = new ArrayList<>();
            for (int i = 0; i < indexList.size(); i++) {
                indexNames.add(indexList.get(i).getIndexName());
            }

            indexNamesResponseBody.setIndexNames(indexNames);

        } catch (OtsException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw new OtsException(RestErrorCode.EC_OTS_REST_QUERY_INDEX, "failed to query table:" + tableName + " indexes");
        }

        return indexNamesResponseBody;
    }


    /**
     * 将参数传入返回的body中
     * @param otsIndex
     */
    public static IndexInfoBody fromIndex(OtsIndex otsIndex) {
        IndexInfoBody indexInfoBody = new IndexInfoBody();

        Index index = otsIndex.getInfo();

        indexInfoBody.setIndexId(index.getIndexId());
        indexInfoBody.setIndexName(index.getIndexName());
        indexInfoBody.setIndexType(index.getIndexType());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            List<IndexColumnsBody> tableColumnsBodies = objectMapper.readValue(index.getIndexKey(), new TypeReference<List<IndexColumnsBody>>() {});
            indexInfoBody.setIndexKey(tableColumnsBodies);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 时间格式化的格式
        indexInfoBody.setCreateTime(sDateFormat.format(index.getCreateTime()));
        indexInfoBody.setModifyTime(sDateFormat.format(index.getModifyTime()));
        indexInfoBody.setCreator(index.getCreator());
        indexInfoBody.setModifier(index.getModifier());

        return indexInfoBody;
    }

}
