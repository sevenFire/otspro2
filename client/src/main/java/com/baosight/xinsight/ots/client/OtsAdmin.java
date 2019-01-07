package com.baosight.xinsight.ots.client;

import com.baosight.xinsight.ots.OtsConfiguration;
import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.Database.HBase.HBaseMetricsProvider;
import com.baosight.xinsight.ots.client.Database.HBase.HBaseRecordProvider;
import com.baosight.xinsight.ots.client.Database.HBase.HBaseTableProvider;
import com.baosight.xinsight.ots.client.Database.HBase.RecordQueryOption;
import com.baosight.xinsight.ots.client.Database.HBase.RecordResult;
import com.baosight.xinsight.ots.client.Database.HBase.RowRecord;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.client.exception.PermissionSqlException;
import com.baosight.xinsight.ots.client.exception.TableException;
import com.baosight.xinsight.ots.client.metacfg.Configurator;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.client.util.HBaseConnectionUtil;
import com.baosight.xinsight.ots.constants.TableConstants;
import com.baosight.xinsight.ots.exception.OtsException;

import org.apache.commons.codec.DecoderException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/13
 * @description
 */
public class OtsAdmin {
    private static final Logger LOG = Logger.getLogger(OtsAdmin.class);

    private OtsConfiguration conf = null;

    public OtsAdmin(OtsConfiguration conf) throws IOException, NumberFormatException, OtsException {
        this.conf = conf;

        try {
            //ZK
            String ZOOKEEPER_QUORUM = conf.getProperty(OtsConstants.ZOOKEEPER_QUORUM);
            String ZOOKEEPER_TIMEOUT = conf.getProperty(OtsConstants.ZOOKEEPER_TIMEOUT);
            if (ZOOKEEPER_QUORUM == null || ZOOKEEPER_TIMEOUT == null) {
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_INVALID_INITPARAM_HBASE,
                        "invalid input parameter for zookeeper:" + ZOOKEEPER_QUORUM + ZOOKEEPER_TIMEOUT);
            }

            //RDB
            String POSTGRES_QUORUM = conf.getProperty(OtsConstants.POSTGRES_QUORUM);
            String POSTGRES_PORT = conf.getProperty(OtsConstants.POSTGRES_PORT);
            String POSTGRES_DBNAME = conf.getProperty(OtsConstants.POSTGRES_DBNAME);
            String POSTGRES_USERNAME = conf.getProperty(OtsConstants.POSTGRES_USERNAME);
            String POSTGRES_PASSWORD = conf.getProperty(OtsConstants.POSTGRES_PASSWORD);
            if (POSTGRES_QUORUM == null || POSTGRES_DBNAME == null
                    || POSTGRES_USERNAME == null || POSTGRES_PASSWORD == null) {
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_INVALID_INITPARAM_RDS,
                        "invalid input parameter for postgres:"
                                + POSTGRES_QUORUM + "," + POSTGRES_PORT
                                + "," + POSTGRES_DBNAME + "," + POSTGRES_USERNAME);
            }
            if (POSTGRES_PORT == null) {
                LOG.warn(OtsConstants.POSTGRES_PORT + "not set postgres port, try to use 5432 default port");
                POSTGRES_PORT = "5432";
            }

            // init HBase
            Configuration hbase_cfg = HBaseConfiguration.create();
            hbase_cfg.setStrings(HConstants.ZOOKEEPER_QUORUM, ZOOKEEPER_QUORUM);
            hbase_cfg.setStrings(HConstants.ZK_SESSION_TIMEOUT, ZOOKEEPER_TIMEOUT);
            HBaseConnectionUtil.init(hbase_cfg);

            // init rdb
            Configurator.init(POSTGRES_QUORUM, Integer.valueOf(POSTGRES_PORT),
                    POSTGRES_DBNAME, POSTGRES_USERNAME, POSTGRES_PASSWORD);

            //HBase-indexer
            //todo lyh index相关
//            String INDEX_CONFIG_HOME = conf.getProperty(OtsConstants.INDEX_CONFIG_HOME);
//            String HBASE_INDEXER_QUORUM = conf.getProperty(OtsConstants.HBASE_INDEXER_QUORUM);
//            if (INDEX_CONFIG_HOME == null || HBASE_INDEXER_QUORUM == null) {
//                LOG.warn("no input parameter for indexer:" + INDEX_CONFIG_HOME + "," + HBASE_INDEXER_QUORUM);
//            } else {
//                IndexConfigurator.Init(ZOOKEEPER_QUORUM, Integer.valueOf(ZOOKEEPER_TIMEOUT),
//                        INDEX_CONFIG_HOME, HBASE_INDEXER_QUORUM);
//            }

            //redis


        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw e;
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    //todo lyh
    public void finalize() {
        HBaseConnectionUtil.getInstance().stop();
//        IndexConfigurator.Release();
//        OtsIndex.Release();
    }


    //===============================================CRUD====================================
    /**
     * 创建表
     * 1.首先在pg中创建表
     * 2.判断该用户所在的租户在hbase中是否已经有对应的大表。
     * 3.如果HBase中创建失败，需要回滚刚刚在pg中的数据。
     * @param userId
     * @param tenantId
     * @param tableName
     * @Param table
     * @return
     */
    public OtsTable createTable(Long userId,
                                Long tenantId,
                                String tableName,
                                Table table) throws OtsException {

        boolean hBaseFailed2DelPost;

        try {
            //在pg中创建表，如果存在则报异常。
            createRDBTableIfNotExist(userId,tenantId,tableName,table);
            //在HBase中创建大表，如果该租户下已经有大表，则不再创建
            hBaseFailed2DelPost = createHBaseTableIfNotExist(tenantId);

            //HBase创建失败，需要回滚RDB中数据
            if (hBaseFailed2DelPost){
                delTableByUniqueKey(tenantId,tableName);
            }
            return new OtsTable(table, tenantId, this.conf);

        }  catch (OtsException e) {
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * 更新表
     * @param userId
     * @param tenantId
     * @param tableName
     * @param tableNew
     */
    public OtsTable updateTable(Long userId, Long tenantId, String tableName, Table tableNew) throws OtsException {
        try {
            //在pg中更新表。
            tableNew = updateRDBTableIfExist(userId,tenantId,tableName,tableNew);
        } catch (OtsException e) {
            e.printStackTrace();
            throw e;
        }
        return new OtsTable(tableNew,this.conf);
    }

    /**
     * 删除表
     * @param tenantId
     * @param tableName
     */
    public OtsTable deleteTable(Long tenantId, String tableName) throws OtsException, IOException {

        boolean hBaseFailed2DelPost;

        try {
            //先备份pg中旧数据
            OtsTable otsTable = getTableInfo(tenantId,tableName);
            if (otsTable == null){
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_DELETE,
                        String.format("table(table_name %s) in tenant(tenant_id:%d) is not exist!\n", tableName, tenantId));
            }
            //在pg中删除表
            delRDBTableByTableId(otsTable.getInfo().getTableId());

            //在HBase中删除该小表对应的记录
            hBaseFailed2DelPost = deleteAllRecordByTableId(tenantId,otsTable.getTableId());

            //删除小表的索引 //todo lyh



            //HBase删除失败，需要回滚RDB中数据，并报异常
            if (hBaseFailed2DelPost){
                recoveryRDBTable(otsTable.getInfo());
            }

            return otsTable;
        }  catch (OtsException e) {
            e.printStackTrace();
            throw e;
        }
    }


    /**
     * 获取表，过滤掉无权限的。
     * @param tenantId
     * @return
     */
    public List<String> getTableNameListWithPermission(Long tenantId) throws ConfigException {
        List<String> tableNameList;

        Configurator configurator = new Configurator();
        try {
            tableNameList = configurator.queryPermissionTableNames(tenantId);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } finally {
            configurator.release();
        }

        return tableNameList;
    }

    /**
     * 根据表名在RDB中查询表
     * @param tenantId
     * @param tableName
     * @return
     */
    public OtsTable getOtsTableByUniqueKey(Long tenantId, String tableName) throws ConfigException {
        Configurator configurator = new Configurator();

        try {
            Table table = configurator.queryTable(tenantId, tableName);
            if (table != null) {
                return new OtsTable(table,this.conf);
            }
            return null;
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        }finally {
            configurator.release();
        }
    }

    private Table getRDBTableByTableId(Long tableId) throws ConfigException {
        Configurator configurator = new Configurator();

        try {
            Table table = configurator.queryTableById(tableId);
            if (table != null) {
                return table;
            }
            return null;
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        }finally {
            configurator.release();
        }
    }

    public OtsTable getTableInfo(Long tenantId,
                              String tableName) throws ConfigException {

        return getTableInfoWithPermission(tenantId,tableName,null);
    }

    /**
     * 根据表名在RDB中查询表
     * @param tenantId
     * @param tableName
     * @return
     */
    public OtsTable getTableInfoWithPermission(Long tenantId,
                                               String tableName,
                                               List<Long> noGetPermissionList) throws ConfigException {
        Configurator configurator = new Configurator();

        try {
            Table table = configurator.queryTableWithPermission(tenantId, tableName, noGetPermissionList);
            if (table != null) {
                return new OtsTable(table, tenantId, this.conf);
            }
            return null;
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        }finally {
            configurator.release();
        }
    }

    /**
     * 获取所有的表，包含权限筛选。
     * @param tenantId
     * @param permittedIds
     * @return
     */
    public List<OtsTable> getAllOtsTablesWithPermission(Long tenantId,
                                                        long limit,
                                                        long offset,
                                                        List<Long> permittedIds) throws ConfigException {
       return getAllOtsTablesWithPermission(tenantId,null,limit,offset,true,permittedIds);
    }


    /**
     * 查询出表，如果没有get权限的表，不查出来。
     * @param tenantId
     * @param tableName
     * @param limit
     * @param offset
     * @param fuzzy
     * @param noGetPermittedIds
     * @return
     * @throws ConfigException
     */
    public List<OtsTable> getAllOtsTablesWithPermission(Long tenantId,
                                          String tableName,
                                          long limit,
                                          long offset,
                                          Boolean fuzzy,
                                          List<Long> noGetPermittedIds) throws ConfigException {
        List<OtsTable> otsTableList = new ArrayList<>();
        Configurator configurator = new Configurator();

        try {
            List<Table> lstTables = configurator.queryAllTablesWithPermission(tenantId,tableName,limit,offset,fuzzy,noGetPermittedIds);
            for (Table table: lstTables) {
                otsTableList.add(new OtsTable(table, tenantId, this.conf));
            }
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        }finally {
            configurator.release();
        }
        return otsTableList;
    }

    //====================================HBase==================================================

    /**
     * 在HBase中创建表，一个租户共用一张表
     * 如果该租户下已经有表，则不创建。
     * @param tenantId
     */
    private boolean createHBaseTableIfNotExist(Long tenantId) throws OtsException {
        Boolean HBaseFailed2DelPost = false;
        try {
            if(!isHBaseTableExist(tenantId)){
                createHBaseTable(tenantId);
            }
        } catch (OtsException ex) {
            HBaseFailed2DelPost = true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            return HBaseFailed2DelPost;
        }
    }


    /**
     * 在HBase中插入表
     * @param tenantId
     * @throws OtsException
     */
    private void createHBaseTable(long tenantId) throws OtsException {
        Admin admin = null;

        try {
            admin = HBaseConnectionUtil.getInstance().getAdmin();
            HBaseTableProvider.createHBaseTable(admin, String.valueOf(tenantId));
        } catch (MasterNotRunningException e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_NO_RUNNING_HBASE_MASTER,
                    "Failed to create hbasetable because hbase master no running!\n" + e.getMessage());
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_FAILED_CONN_ZK,
                    "Failed to create hbasetable because can not connecto to zookeeper!\n" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_CREATE,
                    "Failed to create hbasetable!\n" + e.getMessage());
        } finally {
            try {
                if (admin != null) {
                    admin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }










    //===========================HBASE===================================
    /**
     * 删除所有属于某表的记录
     * @param tenantId
     * @param tableId
     * @return
     */
    private boolean deleteAllRecordByTableId(Long tenantId, Long tableId) {

        Boolean HBaseFailed2DelPost = false;
        Admin admin = null;

        try {
            admin = HBaseConnectionUtil.getInstance().getAdmin();
            String tableName = new StringBuilder().append(TableConstants.HBASE_TABLE_PREFIX).append(tenantId).toString();
            org.apache.hadoop.hbase.client.Table table = HBaseConnectionUtil.getInstance().getTable(tableName);
            HBaseRecordProvider.deleteAllRecordByTableId(table,tableId);
        } catch (OtsException ex) {
            HBaseFailed2DelPost = true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (admin != null) {
                    admin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return HBaseFailed2DelPost;
        }
    }

    /**
     * 清空小表对应的记录
     * @param
     */
    public void truncate(Long tenantId, Long tableId) throws OtsException {
        deleteAllRecordByTableId(tenantId,tableId);
    }



    /**
     * 判定HBase中的表是否已经存在
     * @param tenantId
     * @return
     * @throws Exception
     */
    private boolean isHBaseTableExist(long tenantId) throws Exception {

        Admin admin = null;
        try {
            admin = HBaseConnectionUtil.getInstance().getAdmin();
            String tableName = new StringBuilder().append(TableConstants.HBASE_TABLE_PREFIX).append(tenantId).toString();
            return HBaseTableProvider.isHBaseTableExist(admin, TableName.valueOf(tableName));
        } catch (MasterNotRunningException e) {
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_NO_RUNNING_HBASE_MASTER,
                    "Failed to check table exist or not because hbase master no running!\n" + e.getMessage());
        } catch (ZooKeeperConnectionException e) {
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_FAILED_CONN_ZK,
                    "Failed to check table exist or not because can not connecto to zookeeper!\n" + e.getMessage());
        } catch (IOException e) {
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST,
                    "Failed to check table exist or not!\n" + e.getMessage());
        } finally {
            try {
                if (admin != null) {
                    admin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 判定namespace是否存在
     * @param tenantId
     * @return
     */
    public boolean isNamespaceExist(long tenantId) throws OtsException {
        Admin admin = null;
        try {
            admin = HBaseConnectionUtil.getInstance().getAdmin();
            return HBaseMetricsProvider.isNamespaceExist(admin, String.valueOf(tenantId));
        } catch (MasterNotRunningException e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_NO_RUNNING_HBASE_MASTER, "Failed to check if namespace exist because hbase master no running!");
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_FAILED_CONN_ZK,	"Failed to check if namespace exist because can not connecto to zookeeper!");
        } catch (IOException e) {
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_INVALID_TENANT, "Failed to check if namespace exist!");
        } finally {
            try {
                if (admin != null) {
                    admin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 在表中插入数据
     * @param tenantId
     * @param records
     */
    public void insertRecords(Long tenantId, List<RowRecord> records) throws IOException, TableException {
        String tableName = new StringBuilder().append(TableConstants.HBASE_TABLE_PREFIX).append(tenantId).toString();
        HBaseRecordProvider.insertRecords(TableName.valueOf(tableName),records);
    }

    /**
     * 更新记录
     * @param tenantId
     * @param records
     */
    public void updateRecords(Long tenantId, List<RowRecord> records) throws IOException, TableException {
        String tableName = new StringBuilder().append(TableConstants.HBASE_TABLE_PREFIX).append(tenantId).toString();
        HBaseRecordProvider.updateRecords(TableName.valueOf(tableName),records);
    }


    //===================================RDB============================================

    /**
     * 在pg中创建表，如果已经存在则报异常。
     * @param userId
     * @param tenantId
     * @param tableName
     * @param table
     */
    public void createRDBTableIfNotExist(Long userId, Long tenantId, String tableName, Table table) throws OtsException {
        Configurator configurator = new Configurator();

        //查询表是否存在
        if (configurator.ifExistTable(tenantId, tableName)) {//已存在，则抛出异常给上层方法，因为有联动。
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_EXIST,
                    String.format("tenant(tenantId:%d) already owned table(tableName:%s)!", tenantId, tableName));
        }

        //表的其他信息
        table.setUserId(userId);
        table.setTenantId(tenantId);
        table.setTableName(tableName);
        table.setCreator(userId);
        table.setModifier(userId);
        table.setCreateTime(new Date());
        table.setModifyTime(table.getCreateTime());
        table.setPermission(true);
        table.setEnable(true);

        long tableId;
        try {
            tableId = configurator.addTable(table);
        } catch (ConfigException e) {//插入失败，则抛出异常给上层。
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_CREATE,
                    String.format("user(userId:%d) in tenant(tenantId:%d) add table(tableName:%s) failed!", userId, tenantId, tableName));
        }finally {
            configurator.release();
        }
        table.setTableId(tableId);
    }

    /**
     * 在pg中根据表名删除表
     * @param tableId
     */
    private void delRDBTableByTableId(Long tableId) throws OtsException {
        Configurator configurator = new Configurator();

        try {
            configurator.delTableByTableId(tableId);
        } catch (ConfigException e) {//删除失败
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_CREATE,
                    String.format("Delete table:%s failed!", tableId));
        }finally {
            configurator.release();
        }

    }

    /**
     * 在pg中删除表
     * @param tenantId
     * @param tableName
     * @throws OtsException
     */
    private void delTableByUniqueKey(Long tenantId, String tableName) throws OtsException {
        Configurator configurator = new Configurator();

        try {
            configurator.delTableByUniqueKey(tenantId,tableName);
        } catch (ConfigException e) {//删除失败
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_CREATE,
                    String.format("user:%s delete table:%s failed!", tenantId, tableName));
        }finally {
            configurator.release();
        }
    }

    /**
     * 更新表（带校验）
     * @param userId
     * @param tenantId
     * @param tableName 更新后的表名
     * @param tableNew
     * @throws OtsException
     */
    private Table updateRDBTableIfExist(Long userId, Long tenantId, String tableName, Table tableNew) throws OtsException {
        Configurator configurator = new Configurator();

        //查询表是否存在
        Table table = configurator.queryTable(tenantId,tableName);
        if (table == null){
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_EXIST,
                    String.format("tenant (tenantId:%d) doesn't own table(tableName:%s)!", tenantId, tableName));
        }
//        //查询表是否存在
//        if (!configurator.ifExistTable(tenantId, tableName)) {//不存在，则抛出异常给上层方法
//            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_EXIST,
//                    String.format("tenant (tenantId:%d) doesn't own table(tableName:%s)!", tenantId, tableName));
//        }

        //新旧表名相同
        if (tableName.equals(tableNew.getTableName())){
            return table;
        }

        //查询更新后的表是否冲突
        if (configurator.ifExistTable(tenantId, tableNew.getTableName())) {//不存在，则抛出异常给上层方法
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_EXIST,
                    String.format("tenant (tenantId:%d) already own table(tableName:%s)!", tenantId, tableNew.getTableName()));
        }

        //更新表
        try {
            configurator.updateTable(userId, tenantId, tableName, tableNew);
            if (tableNew.getTableName() != null){
                table.setTableName(tableNew.getTableName());
            }
            if (tableNew.getTableDesc() != null){
                table.setTableDesc(tableNew.getTableDesc());
            }
        } catch (ConfigException e) {//更新失败
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_CREATE,
                    String.format("user(userId:%d) in tenant(tenantId:%d) update table(tableName:%s) failed!", userId, tenantId, tableName));
        }finally {
            configurator.release();
        }

        return table;
    }


    /**
     * 恢复被删除的表
     * @param table
     */
    private void recoveryRDBTable(Table table) throws OtsException {
        Configurator configurator = new Configurator();

        try {
            configurator.addTable(table);
        } catch (ConfigException e) {//删除失败
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_CREATE,
                    String.format("recovery table:%s failed!", table.getTableId()));
        }finally {
            configurator.release();
        }
    }


    //=================================权限相关==================================

    public boolean checkTablePermitted(long tableId) throws ConfigException, PermissionSqlException {
        Configurator configurator = new Configurator();
        try {
            return configurator.checkPermitted(tableId);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } catch (PermissionSqlException e) {
            e.printStackTrace();
            throw e;
        } finally {
            configurator.release();
        }
    }

    public void setTablePermission(long tableId) throws ConfigException, PermissionSqlException {
        Configurator configurator = new Configurator();
        try {
            configurator.setTablePermission(tableId);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } catch (PermissionSqlException e) {
            e.printStackTrace();
            throw e;
        } finally {
            configurator.release();
        }
    }


    /**
     * 获取表，过滤掉无权限的。
     * @param tenantId
     * @return
     */
    public List<OtsTable> getPermissionTables(Long tenantId) throws ConfigException {
        List<OtsTable> otsTableList = new ArrayList<>();

        Configurator configurator = new Configurator();
        try {
            List<Table> tableList = configurator.queryPermissionTables(tenantId);
            for (Table table: tableList) {
                otsTableList.add(new OtsTable(table, tenantId, this.conf));
            }
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } finally {
            configurator.release();
        }

        return otsTableList;
    }


    /**
     * 获取表，过滤掉无权限的。
     * @param tenantId
     * @return
     */
    public OtsTable getPermissionTable(Long tenantId, String tableName) throws ConfigException {

        Configurator configurator = new Configurator();
        try {
            Table table = configurator.queryPermissionTable(tenantId,tableName);
            if (table == null){
                return null;
            }
            return new OtsTable(table,this.conf);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } finally {
            configurator.release();
        }

    }


    /**
     * 获取表的Id，过滤掉无权限的。
     * @param tenantId
     * @return
     */
    public List<Long> getPermissionTableIds(Long tenantId) throws ConfigException {
        List<Long> IdList;

        Configurator configurator = new Configurator();
        try {
            IdList = configurator.queryPermissionTableIds(tenantId);
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        } finally {
            configurator.release();
        }

        return IdList;
    }

    //========================records=================================

    /**
     * 查询记录
     * @param tenantId
     * @param startKey
     * @param endKey
     * @return
     */
    public RecordResult getRecords(Long tenantId, RecordQueryOption query, byte[] startKey, byte[] endKey) throws OtsException {

        try {
            org.apache.hadoop.hbase.client.Table hTable =
                    HBaseConnectionUtil.getInstance().getTable(generateHBaseTableName(tenantId));
            return HBaseRecordProvider.getRecordsByRange(hTable, query, startKey, endKey);
        }catch (MasterNotRunningException e) {
            e.printStackTrace();
            LOG.error("Failed to query records because hbase master no running!\n" + e.getMessage());
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_NO_RUNNING_HBASE_MASTER, "Failed to query records because hbase master no running!\n" + e.getMessage());
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
            LOG.error("Failed to query because can not connecto to zookeeper!\n" + e.getMessage());
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_FAILED_CONN_ZK,	"Failed to query because can not connecto to zookeeper!\n" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Failed to query records!\n" + e.getMessage());
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_RECORD_QUERY, "Failed to query records!\n" + e.getMessage());
        } catch (TableException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
            throw e;
        } catch (DecoderException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_RECORD_QUERY, "Failed to query records!\n" + e.getMessage());
        }

    }


    /**
     * 根据rowKeys批量查询记录
     * @param rowKeyBatch
     * @param query
     */
    public RecordResult getRecordsBatch(Long tenantId, List<byte[]> rowKeyBatch, RecordQueryOption query) throws OtsException {
        try {
            org.apache.hadoop.hbase.client.Table hTable =
                    HBaseConnectionUtil.getInstance().getTable(generateHBaseTableName(tenantId));

            return HBaseRecordProvider.getRecordsByKeys(hTable, rowKeyBatch, query);
        } catch (MasterNotRunningException e) {
            e.printStackTrace();
            LOG.error("Failed to query records because hbase master no running!\n" + e.getMessage());
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_NO_RUNNING_HBASE_MASTER, "Failed to query records because hbase master no running!\n" + e.getMessage());
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
            LOG.error("Failed to query because can not connecto to zookeeper!\n" + e.getMessage());
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_FAILED_CONN_ZK,	"Failed to query because can not connecto to zookeeper!\n" + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            LOG.error("Failed to query records!\n" + e.getMessage());
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_RECORD_QUERY, "Failed to query records!\n" + e.getMessage());
        } catch (TableException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
            throw e;
        }

    }


    /**
     * 根据租户id拼接生成HBase中真正的表名
     * 1:ots_${tenantId}
     * @param tenantId
     * @return
     */
    private String generateHBaseTableName(Long tenantId) {
        return (TableConstants.HBASE_TABLE_PREFIX + String.valueOf(tenantId));
    }



    //============================================index===========================================

}
