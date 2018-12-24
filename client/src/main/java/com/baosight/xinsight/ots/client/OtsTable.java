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
import com.cloudera.org.codehaus.jackson.map.ObjectMapper;

import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Date;

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
                return configurator.queryTable(getTableId(), getTableName());
            } catch (ConfigException e) {
                e.printStackTrace();
                throw e;
            } finally {
                configurator.release();
            }
        }
    }

    /**
     * 查询表是否拥有该索引
     * @param indexName
     * @return
     */
    public OtsIndex getOtsIndex(String indexName) throws ConfigException {
        Configurator configurator = new Configurator();

        try {
            //唯一键为(tenantId,tableName,indexName)
            Index index = configurator.queryIndex(getTenantId(), getTableName(), indexName);
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
     * 创建二级索引
     * @param secondaryIndexInfo
     * @returns
     */
    public OtsSecondaryIndex createSecondaryIndex(SecondaryIndexInfo secondaryIndexInfo) throws OtsException {
        Admin admin = null;

        //校验索引列
        if(secondaryIndexInfo.getColumns().size() <= 0) {
            throw new SecondaryIndexException(OtsErrorCode.EC_OTS_INDEX_INVALID_COLUMN_NUM, "Failed to create index, because lack index column def!");
        }
        if (secondaryIndexInfo.checkColumnsDuplicateAndEmpty()) {
            LOG.warn("create index + '" + secondaryIndexInfo.getIndexName() + "' in table '"+ getTableName() + "' failed for duplicate column or empty column name!");
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_INDEX_DUPLICATE_COLUMN,
                    "create index + '" + secondaryIndexInfo.getIndexName() + "' in table '"+ getTableName() + "' failed for duplicate column or empty column name!");
        }

        //表名为 1:ots_1
        String tableFullName = TableNameUtil.generateHBaseTableName(tenantId);
        //index对应的索引表名
        TableName indexTableName = TableNameUtil.generateHBaseIndexTableName(tableFullName,secondaryIndexInfo.getIndexName());

        Configurator configurator = new Configurator();
        long indexId = 0;

//        try {
//            // add to rdb
//            Index index = fromSecondaryIndexInfoToIndex(secondaryIndexInfo);
//            indexId = configurator.addIndex(index);
//
//            //add to HBase
//            admin = HBaseConnectionUtil.getInstance().getAdmin();
//            TableName userTableName = TableName.valueOf(tableFullName);
//            HTableDescriptor userTableDescriptor = admin.getTableDescriptor(userTableName);
//            HColumnDescriptor userTableColumnDescriptor = userTableDescriptor.getFamily(Bytes.toBytes(OtsConstants.DEFAULT_FAMILY_NAME));
//
//            //注册协处理器
//            if (0 == userTableDescriptor.getCoprocessors().size()) {
//                HBaseTableProvider.setTableEnableStatus(admin, userTableName, false);
//                addCorProcessors(userTableDescriptor);
//                HBaseTableProvider.setTableEnableStatus(admin, userTableName, true);
//            }
//
//            //check index exist
//            List<SecondaryIndexInfo> indexes = SecondaryIndexUtil.parseIndexes(userTableDescriptor.getValue(OtsConstants.OTS_INDEXES));
//            if (indexes.size() > 0 && SecondaryIndexUtil.containsIndex(indexes, secondaryIndexInfo.getIndexName())) {
//                throw new SecondaryIndexException(OtsErrorCode.EC_OTS_SEC_INDEX_ALREADY_EXIST, "Index already exist!");
//            }
//
//            if (hasSecondaryIndexBuilding()) {
//                throw new SecondaryIndexException(OtsErrorCode.EC_OTS_SEC_INDEX_INDEX_IS_BUILDING,
//                        "Failed to delete secondary index, because some secondary index of table is building, please try again!");
//            }
//
//            indexes.add(secIndex);
//
//            //after rdb success, add hbase info; if hbase operate error, clean rdb info
//            String newOtsIndexes = SecondaryIndexUtil.indexesToString(indexes);
//            SecondaryIndexUtil.parseIndexes(newOtsIndexes);
//            userTableDescriptor.setValue(OtsConstants.OTS_INDEXES, newOtsIndexes);
//
//            //modifies the existing table's descriptor.
//            admin.modifyTable(userTable, userTableDescriptor);
//
//            HTableDescriptor indexTableDesc = new HTableDescriptor(indexTableName);
//            HColumnDescriptor columnDescriptor = new HColumnDescriptor(Bytes.toBytes(OtsConstants.DEFAULT_FAMILY_NAME));
//
//            columnDescriptor.setScope(OtsConstants.DEFAULT_REPLICATED_SCOPE);
//            columnDescriptor.setCompressionType(userTableColumnDescriptor.getCompressionType());
//            columnDescriptor.setMaxVersions(1);
//            //columnDescriptor.setMobEnabled(userTableColumnDescriptor.isMobEnabled());
//            //columnDescriptor.setMobThreshold(userTableColumnDescriptor.getMobThreshold());
//
//            indexTableDesc.addFamily(columnDescriptor);
//            indexTableDesc.setMaxFileSize(Long.MAX_VALUE);
//            indexTableDesc.setValue(HTableDescriptor.SPLIT_POLICY, "org.apache.hadoop.hbase.regionserver.ConstantSizeRegionSplitPolicy");
//
//            admin.createTable(indexTableDesc);
//
//            return new OtsSecondaryIndex(getId(), idxid, tableFullName, secIndex, this.conf);
//
//        } catch (IOException e) {
//            try {
//                admin.getTableDescriptor(indexTableName);
//            } catch (TableNotFoundException tnfe) {
//                configurator.delIndex(idxid);
//            }
//            throw new SecondaryIndexException(OtsErrorCode.EC_OTS_SEC_INDEX_CREATE_INDEX_TABLE_FAILED, e.getMessage());
//
//        } finally {
//            TableProvider.setTableEnableStatus(admin, userTable, true);
//
//            try {
//                if (admin != null) {
//                    admin.close();
//                }
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//            if(null != configurator) {
//                configurator.release();
//            }
//        }
            return null;
            //todo lyh

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


    /**
     * 填充参数到实体Index中
     * @param secondaryIndexInfo
     * @return
     */
    private Index fromSecondaryIndexInfoToIndex(SecondaryIndexInfo secondaryIndexInfo) throws IOException, ConfigException {
        Index index = new Index();
        index.setTableId(getTableId());
        index.setUserId(getUserId());
        index.setTenantId(getTenantId());

        index.setIndexType(OTS_INDEX_TYPE_HBASE_STRING);
        index.setIndexName(secondaryIndexInfo.getIndexName());
        index.setTableName(getTableName());

        index.setShard(0);
        index.setReplication(0);

        index.setCreateTime(new Date());
        index.setModifyTime(new Date());
        index.setCreator(getUserId());
        index.setModifier(getUserId());

//        index.setIndexKey(JSON.toJSON(secondaryIndexInfo.getColumns()).toString());

        ObjectMapper objectMapper = new ObjectMapper();
        index.setIndexKey( objectMapper.writeValueAsString(secondaryIndexInfo.getColumns()));
        return index;
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
