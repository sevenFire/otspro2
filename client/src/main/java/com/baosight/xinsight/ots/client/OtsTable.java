package com.baosight.xinsight.ots.client;

import com.baosight.xinsight.ots.OtsConfiguration;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.client.exception.SecondaryIndexException;
import com.baosight.xinsight.ots.client.metacfg.Configurator;
import com.baosight.xinsight.ots.client.metacfg.Index;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.client.util.TableNameUtil;
import com.baosight.xinsight.ots.common.secondaryindex.SecondaryIndexInfo;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.utils.JsonUtil;
import com.cloudera.org.codehaus.jackson.map.ObjectMapper;

import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.log4j.Logger;
import org.noggit.JSONUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.baosight.xinsight.ots.constants.TableConstants.OTS_INDEX_TYPE_HBASE_STRING;

/**
 * @author liyuhui
 * @date 2018/12/15
 * @description
 */
public class OtsTable {
    private static final Logger LOG = Logger.getLogger(OtsTable.class);

    private Long userId;
    private Long tenantId;
    private String tableName;
    private Table info = null;
    private OtsConfiguration conf = null;

    public OtsTable() {
    }

    public OtsTable(Table info, Long tenantId, OtsConfiguration conf) {
        this.info = info;
        this.tenantId = tenantId;
        this.conf = conf;
        if (info != null) {
            this.userId = info.getUserId();
            this.tableName = info.getTableName();
        }
    }



    public OtsTable(Table info, OtsConfiguration conf) {
        this.info = info;
        this.conf = conf;
        if (info != null) {
            this.userId = info.getUserId();
            this.tableName = info.getTableName();
            this.tenantId = info.getTenantId();
        }
    }

    public OtsTable(Long userId, Long tenantId, String tableName, OtsConfiguration conf) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.conf = conf;
    }

    public Table getInfo() throws ConfigException, IOException {
        if (this.info != null) {
            return this.info;
        } else { //no safe mode
            Configurator configurator = new Configurator();

            try {
                return configurator.queryTable(getTenantId(), getTableName());
            } catch (ConfigException e) {
                e.printStackTrace();
                throw e;
            } finally {
                configurator.release();
            }
        }
    }

    /**
     * 查询表的某索引的详情
     * @param indexName
     * @return
     */
    public OtsIndex getOtsIndex(String indexName) throws ConfigException, IOException {
        Configurator configurator = new Configurator();

        try {
            //唯一键为(tenantId,tableName,indexName)
            Index index = configurator.queryIndex(getTableId(), indexName);
            if (index != null) {
                return new OtsIndex(index, this.conf);
            }
            return null;

        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } finally {
            configurator.release();
        }
    }

    /**
     * 查询该表拥有的所有索引
     * @return
     */
    public List<OtsIndex> getAllIndexInfo() throws ConfigException, IOException {
        List<OtsIndex> lstIndex = new ArrayList<>();

        Configurator configurator = new Configurator();
        try {
            List<Index> indexList = configurator.queryRDBIndexByTableId(getTableId());
            for (Index index: indexList) {
                lstIndex.add(new OtsIndex(index, this.conf));
            }
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } finally {
            configurator.release();
        }

        return lstIndex;

    }


    /**
     * 创建HBase二级索引
     * @param index
     * @returns
     */
    public OtsIndex createIndexHBase(Index index) throws OtsException, IOException {
        boolean hBaseFailed2DelPost;

        try {
            //在pg中创建索引记录，如果存在则报异常。
            createRDBIndexIfNotExist(getTableId(),index);
            //在HBase中创建索引表及索引
            hBaseFailed2DelPost = createHBaseIndexIfNotExist(index.getIndexName());

            //HBase创建失败，需要回滚RDB中数据
            if (hBaseFailed2DelPost){
                delRDBIndexByIndexId(index.getIndexId());
            }
            return new OtsIndex(index, this.conf);

        }  catch (OtsException e) {
            e.printStackTrace();
            throw e;
        }

    }

    /**
     * 删除RDB中的索引记录
     * @param indexId
     */
    private void delRDBIndexByIndexId(long indexId) throws OtsException {
        Configurator configurator = new Configurator();
        try {
            configurator.delIndexByIndexId(indexId);
        } catch (ConfigException e) {//插入失败，则抛出异常给上层。
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_CREATE,
                    String.format("user(userId:%d) in tenant(tenantId:%d) add index(indexId:%d) failed!", userId, tenantId, indexId));
        }finally {
            configurator.release();
        }


    }

    /**
     * 没有问题返回false
     * @param indexName
     * @return
     */
    private boolean createHBaseIndexIfNotExist(String indexName) {
        String tableFullName = TableNameUtil.generateHBaseTableName(tenantId);
        //index对应的索引表名
        TableName indexTableName = TableNameUtil.generateHBaseIndexTableName(tableFullName,indexName);


        //todo lyh
        return false;

    }

    /**
     * 在RDB中创建索引
     * @param index
     */
    private void createRDBIndexIfNotExist(Long tableId, Index index) throws OtsException {
        Configurator configurator = new Configurator();

        //查询索引是否存在
        if (configurator.ifExistIndex(tableId, index.getIndexName())) {//已存在，则抛出异常给上层方法，因为有联动。
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_INDEX_EXIST,
                    String.format("table(tableId:%d) already owned index(indexName:%s)!", tableId, index.getIndexName()));
        }

        //索引的其他信息
        index.setUserId(userId);
        index.setTableName(tableName);
        index.setTableId(tableId);
        index.setCreator(userId);
        index.setModifier(userId);
        index.setCreateTime(new Date());
        index.setModifyTime(index.getCreateTime());

        //插入索引
        long indexId;
        try {
            indexId = configurator.addIndex(index);
        } catch (ConfigException e) {//插入失败，则抛出异常给上层。
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_CREATE,
                    String.format("user(userId:%d) in tenant(tenantId:%d) add index(indexName:%s) failed!",
                            userId, tenantId, index.getIndexName()));
        }finally {
            configurator.release();
        }
        index.setIndexId(indexId);
    }

    /**
     * 是否build
     * @return
     * @throws IOException
     * @throws ConfigException
     */
    public boolean hasSecondaryIndexBuilding() throws IOException, ConfigException {
//        Configurator configurator = new Configurator();
//
//        try {
//            YarnAppUtil yarnAppUtil = new YarnAppUtil(conf.getProperty(com.baosight.xinsight.config.ConfigConstants.YARN_RM_HOST),
//                    conf.getProperty(com.baosight.xinsight.config.ConfigConstants.REDIS_QUORUM));
//            List<Index> indexes = configurator.queryTable(getTenantId(), getTableName());
//            for(Index index:indexes) {
//                if(SecondaryIndexUtil.isIndexBuilding(yarnAppUtil, getId(), getName(), index.getId(), index.getName())) {
//                    return true;
//                }
//            }
//
//            return false;
//
//        } finally {
//            configurator.release();
//        }
        return false;
        //todo lyh
    }


    /**
     * 注册协处理器
     * @param htd
     * @throws IOException
     */
    private void addCorProcessors(HTableDescriptor htd) throws IOException {
        htd.addCoprocessor("com.baosight.xinsight.ots.coprocessor.IndexRegionObserver");
    }


    public Long getUserId() {
        return userId;
    }

    public long getTableId() throws IOException, ConfigException {
        return getInfo().getTableId();
    }

    public String getTableName() {
        return this.tableName;
    }

    public long getTenantId() {
        return this.tenantId;
    }

    public String getTenantIdAsString() throws IOException, ConfigException {
        return String.valueOf(getTenantId());
    }

    public String getTableDesc() throws IOException, ConfigException {
        return getInfo().getTableDesc();
    }

    public Date getCreateTime() throws ConfigException, IOException {
        return getInfo().getCreateTime();
    }

    public Date getModifyTime() throws ConfigException, IOException {
        return getInfo().getModifyTime();
    }

    public long getModifier() throws ConfigException, IOException {
        return getInfo().getModifier();
    }

    public long getCreator() throws ConfigException, IOException {
        return getInfo().getCreator();
    }

    public String getPrimaryKey() throws ConfigException, IOException {
        return getInfo().getPrimaryKey();
    }

    public String getTableColumns() throws ConfigException, IOException {
        return getInfo().getTableColumns();
    }


}
