package com.baosight.xinsight.ots.client;

import com.baosight.xinsight.ots.OtsConfiguration;
import com.baosight.xinsight.ots.OtsConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.Database.HBase.HBaseTableProvider;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.client.exception.PermissionSqlException;
import com.baosight.xinsight.ots.client.metacfg.Configurator;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.client.util.HBaseConnectionUtil;
import com.baosight.xinsight.ots.constants.TableConstants;
import com.baosight.xinsight.ots.exception.OtsException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HConstants;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.log4j.Logger;

import java.io.IOException;
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
    public void createTable(Long userId,
                                Long tenantId,
                                String tableName,
                                Table table) throws OtsException {

        boolean hBaseFailed2DelPost;

        try {
            //在pg中创建表。
            createRDBTableIfNotExist(userId,tenantId,tableName,table);
            //在HBase中创建大表
            hBaseFailed2DelPost = createHBaseTableIfNotExist(tenantId);

            //HBase创建失败，需要回滚RDB中数据
            if (hBaseFailed2DelPost){
                delRDBTable(userId,tableName);
            }

//            return new OtsTable(table, tenantId, this.conf);

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
     * @param table
     */
    public void updateTable(Long userId, Long tenantId, String tableName, Table table) throws OtsException {
        try {
            //在pg中更新表。
            updateRDBTableIfExist(userId,tenantId,tableName,table);
        } catch (OtsException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void updateRDBTableIfExist(Long userId, Long tenantId, String tableName, Table table) throws OtsException {
        Configurator configurator = new Configurator();

        //查询表是否存在
        if (!configurator.ifExistTable(tenantId, tableName)) {//不存在，则抛出异常给上层方法
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_EXIST,
                    String.format("tenant (tenantId:%d) doesn't own table(tableName:%s)!", tenantId, tableName));
        }

        //查询更新后的表是否冲突
        if (configurator.ifExistTable(tenantId, table.getTableName())) {//不存在，则抛出异常给上层方法
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_EXIST,
                    String.format("tenant (tenantId:%d) already own table(tableName:%s)!", tenantId, table.getTableName()));
        }

        //更新表
        try {
            configurator.updateTable(userId, tenantId, tableName, table);
        } catch (ConfigException e) {//更新失败
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_CREATE,
                    String.format("user(userId:%d) in tenant(tenantId:%d) update table(tableName:%s) failed!", userId, tenantId, tableName));
        }finally {
            configurator.release();
        }
    }

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

        //插入表
        table.setUserId(userId);
        table.setTenantId(tenantId);
        table.setTableName(tableName);
        table.setCreator(userId);
        table.setModifier(userId);
        table.setCreateTime(new Date());
        table.setModifyTime(table.getCreateTime());

        long tableId;
        try {
            tableId = configurator.addTable(table);
        } catch (ConfigException e) {//插入失败，则抛出异常给上层方法，因为有联动。
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_CREATE,
                    String.format("user(userId:%d) in tenant(tenantId:%d) add table(tableName:%s) failed!", userId, tenantId, tableName));
        }finally {
            configurator.release();
        }
        table.setTableId(tableId);
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

    /**
     * 在pg中删除表
     * @param userId
     * @param tableName
     * @throws OtsException
     */
    private void delRDBTable(Long userId, String tableName) throws OtsException {
        Configurator configurator = new Configurator();

        try {
            configurator.delTable(userId,tableName);
        } catch (ConfigException e) {//删除失败
            e.printStackTrace();
            throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_CREATE,
                    String.format("user:%s delete table:%s failed!", userId, tableName));
        }finally {
            configurator.release();
        }
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
     * 根据表名在RDB中查询表
     * @param tenantId
     * @param tableName
     * @return
     */
    public Table getRDBTable(Long tenantId, String tableName) throws ConfigException {
        Configurator configurator = new Configurator();

        try {
            Table table = configurator.queryTable(tenantId, tableName);
            return table;
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        }finally {
            configurator.release();
        }
    }

    /**
     * 在RDB中查询所有表，返回表名的列表
     * @param tenantId
     * @Param tableName
     * @param limit
     * @param offset
     * @param fuzzy 是否需要模糊查询
     * @return
     */
    public List<String> getRDBTableNameList(Long tenantId, String tableName, long limit, long offset, Boolean fuzzy) throws ConfigException {
        Configurator configurator = new Configurator();

        try {
            List<String> tableNameList = configurator.queryTableNameList(tenantId,tableName,limit,offset,fuzzy);
            return tableNameList;
        } catch (ConfigException e) {
            e.printStackTrace();
            throw e;
        }finally {
            configurator.release();
        }
    }


}
