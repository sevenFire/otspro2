package com.baosight.xinsight.ots.client.metacfg;

import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.client.exception.PermissionSqlException;
import com.baosight.xinsight.ots.constants.TableConstants;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hsqldb.persist.HsqlDatabaseProperties.indexName;

/**
 * @author liyuhui
 * @date 2018/12/13
 * @description
 */
public class Configurator {

    private static final Logger LOG = Logger.getLogger(Configurator.class);

    private Connection conn;
    private static String []urls;
    private static String user;
    private static String passwd;
    public static final int TIMEOUT = 3;//second

    public static void init(String quorum, int port, String dbname, String strUser, String strPasswd) throws ConfigException {
        user = strUser;
        passwd = strPasswd;
        String[] hosts = quorum.split(",");
        if(hosts.length <= 0) {
            throw new ConfigException(OtsErrorCode.EC_RDS_ZERO_QUROM, "Invalid postgresql quorum format, too less option!");
        }
        urls = new String[hosts.length];
        for (int i = 0; i < hosts.length; i++) {
            urls[i] = String.format("jdbc:postgresql://%s:%d/%s", hosts[i], port, dbname);
        }

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_LOAD_JDBC_DRIVER, e.getMessage());
        }
    }

    public void release() throws ConfigException {
        try {
            if(null != conn)
                conn.close();
        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_CLOSE_CONN, e.getMessage());
        }
    }

    public void connect() throws ConfigException {
        for (int i = 0; i < urls.length; i++) {
            try {
                if(null == conn || conn.isClosed()) {
                    conn = DriverManager.getConnection(urls[i], user, passwd);
                }

                if(!conn.isValid(TIMEOUT)) {
                    conn.close();
                    conn = DriverManager.getConnection(urls[i], user, passwd);
                } else {
                    break;
                }
            } catch (SQLException e) {
                if (i < urls.length - 1) {
                    continue;
                } else {
                    throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_CONN, "Failed to connect to database!");
                }
            }
        }
    }

    public void disConnect() throws ConfigException {
        try {
            conn.close();
        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_CLOSE_CONN, e.getMessage());
        }
    }


    //============================================表=========================================================

    /**
     * 新增表
     */
    public long addTable(Table table) throws ConfigException{
        long tableId = 0;


        try {
            connect();
            conn.setAutoCommit(false);

			String sql = String.format("insert into ots_user_table " +
							" (\"user_id\", \"tenant_id\", \"table_name\", \"table_desc\", \"primary_key\", \"table_columns\", \"create_time\", \"modify_time\", \"creator\", \"modifier\") " +
							" values ('%d', '%d', '%s', '%s', '%s', '%s', ? , ? , ? , ?) returning table_id; ",
					table.getUserId(), table.getTenantId(), table.getTableName(), table.getTableDesc(),  table.getPrimaryKey(), table.getTableColumns());
			LOG.debug(sql);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setTimestamp(1, new Timestamp(table.getCreateTime().getTime()));
			pstmt.setTimestamp(2, new Timestamp(table.getModifyTime().getTime()));
			pstmt.setLong(3, table.getCreator());
            pstmt.setLong(4, table.getModifier());

			LOG.debug(pstmt.toString());

			pstmt.execute();
			ResultSet rs = pstmt.getResultSet();
			if(rs.next()){
				tableId = rs.getLong("table_id");
			}

			conn.commit();
			conn.setAutoCommit(true);
			pstmt.close();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_ROLLBACK, "Failed to add new table and failed to rollback db!" + e.getMessage());
            }
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_CREATE_TABLE, "Failed to add new table " + table.getTableName() +"!\n" + e.getMessage());
        }

        return tableId;
    }

    /**
     * 更新表
     * @param userId
     * @param tableName
     * @param tableNew
     */
    public void updateTable(Long userId, Long tenantId, String tableName, Table tableNew) throws ConfigException{

        try {
            connect();
            conn.setAutoCommit(false);

            StringBuilder sqlSB = new StringBuilder(" update ots_user_table set modify_time = ? , modifier = ? ");
            if (tableNew.getTableName() != null){
                sqlSB.append( " , table_name = '"+ tableNew.getTableName() + "'");
            }
            if (tableNew.getTableDesc() != null){
                sqlSB.append( " , table_desc = '"+ tableNew.getTableDesc() + "'");
            }
            String sql = sqlSB.append(" where tenant_id = " + tenantId + " and table_name = '" + tableName + "'").toString();

            LOG.debug(sql);

            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setTimestamp(1, new Timestamp(new Date().getTime()));
            pstmt.setLong(2, userId);

            LOG.debug(pstmt.toString());

            pstmt.execute();

            conn.commit();
            conn.setAutoCommit(true);
            pstmt.close();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_ROLLBACK, "Failed to update table and failed to rollback db!" + e.getMessage());
            }
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_UPDATE_TABLE, "Failed to update table " + tableName +"!\n" + e.getMessage());
        }
    }


    /**
     * 根据tenantId和表名删除表。
     * 注意：同一个租户下不能有同名表，所以(tenantId,tableName)是唯一键。
     * @param tenantId
     * @param tableName
     * @throws ConfigException
     */
    public void delTableByUniqueKey(long tenantId, String tableName) throws ConfigException {

        try {
            connect();

            Statement st = conn.createStatement();
            String sql = String.format("delete from ots_user_table where tenant_id = '%d' and table_name = '%s';", tenantId, tableName);
            LOG.debug(sql);

            st.execute(sql);
            st.close();
        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_DEL_TABLE, "Failed to delete table " + tableName + "!\n" + e.getMessage());
        }

    }

    /**
     * 根据表id删除表
     * @param tableId
     * @throws ConfigException
     */
    public void delTableByTableId(long tableId) throws ConfigException {

        try {
            connect();

            Statement st = conn.createStatement();
            String sql = String.format("delete from ots_user_table where ots_user_table.table_id = '%d';", tableId);
            LOG.debug(sql);

            st.execute(sql);
            st.close();
        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_DEL_TABLE, "Failed to delete table " + tableId + "!\n" + e.getMessage());
        }

    }

    /**
     * 查询并返回表的详细信息
     * @param tenantId
     * @param tableName
     * @return
     * @throws ConfigException
     */
    public Table queryTable(long tenantId, String tableName) throws ConfigException{
        return queryTableWithPermission(tenantId,tableName,null);
    }

    /**
     * 判定表是否存在
     * @param tenantId
     * @param tableName
     * @return
     * @throws ConfigException
     */
    public Boolean ifExistTable(long tenantId, String tableName) throws ConfigException {
        Statement st = null;
        try {
            connect();

            st = conn.createStatement();
            String sql = String.format("select table_id from ots_user_table where ots_user_table.table_name = '%s' and ots_user_table.tenant_id = '%d';", tableName, tenantId);
            LOG.debug(sql);

            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                return true;
            } else return false;

        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_QUERY_TABLE, "Failed to query table " + tableName + "!\n" + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    //=======================================表=============================================


    //=======================================权限=============================================
    /**
     * 查询并返回表的详细信息，带权限校验
     * @param tenantId
     * @param tableName
     * @param noGetPermissionList
     * @return
     */
    public Table queryTableWithPermission(Long tenantId,
                                          String tableName,
                                          List<Long> noGetPermissionList) throws ConfigException {

        Table table = null;

        try {
            connect();
            Statement st = conn.createStatement();

            String sql;
            if (CollectionUtils.isEmpty(noGetPermissionList)) {
                sql = String.format("select * from ots_user_table where table_name = '%s' and tenant_id = '%d';", tableName, tenantId);
            }else {
                String list2String = StringUtils.join(noGetPermissionList.toArray(), ",");
                StringBuilder permittedIdString = new StringBuilder().append("(").append(list2String).append(")");
                sql = String.format("select * from ots_user_table where tenant_id = '%d' and table_id not in %s order by table_id;",
                        tenantId, permittedIdString);
            }
            LOG.debug(sql);

            ResultSet rs = st.executeQuery(sql);
            if(rs.next()) {
                table = resultSetToTable(rs);
            }
            st.close();
        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_QUERY_TABLE, "Failed to query table " + tableName + "!\n" + e.getMessage());
        }

        return table;
    }

    /**
     * 查询某租户下的所有表， 带权限筛选
     * @param tenantId
     * @param tableName
     * @param limit
     * @param offset
     * @param fuzzy 是否模糊查询
     * @return 返回表的列表
     */
    public List<Table> queryAllTablesWithPermission(Long tenantId,
                                                    String tableName,
                                                    Long limit,
                                                    Long offset,
                                                    Boolean fuzzy,
                                                    List<Long> noGetPermittedIds) throws ConfigException {
        List<Table> tableList = new ArrayList<>();

        try {
            connect();

            Statement st = conn.createStatement();
            String sql;
            if (!CollectionUtils.isEmpty(noGetPermittedIds)) {
                sql = getSqlWithPermission(tenantId,tableName,limit,offset,fuzzy,noGetPermittedIds);
            }else {
                sql = getSqlWithoutPermission(tenantId,tableName,limit,offset,fuzzy);
            }

            ResultSet rs = st.executeQuery(sql);
            while (rs.next()){
                Table table = resultSetToTable(rs);
                tableList.add(table);
            }
            st.close();
        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_QUERY_TABLE, "Failed to query table !\n" + e.getMessage());
        }

        return tableList;

    }

    private String getSqlWithoutPermission(Long tenantId,
                                           String tableName,
                                           Long limit,
                                           Long offset,
                                           Boolean fuzzy) {
        String sql;
        StringBuilder sqlSB = new StringBuilder(" select * from ots_user_table where tenant_id = '%d' ");


        if (!StringUtils.isBlank(tableName)) {//带条件查询
            sqlSB.append(" and table_name ");
            if (fuzzy){
                sqlSB.append(" ~ '%s' ");
            }else {
                sqlSB.append(" = '%s' ");
            }
            sqlSB.append(" limit '%d' offset '%d' " );
            sql = String.format(sqlSB.toString(),tenantId, tableName,limit,offset);
        }else if(limit != null && offset != null){
            sqlSB.append(" limit '%d' offset '%d' " );
            sql = String.format(sqlSB.toString(), tenantId, limit, offset);
        }else{
            sql = String.format(sqlSB.toString(), tenantId);
        }

        LOG.debug(sql);

        return sql;
    }

    private String getSqlWithPermission(Long tenantId,
                                        String tableName,
                                        Long limit,
                                        Long offset,
                                        Boolean fuzzy,
                                        List<Long> noGetPermittedIds) {
        String sql;
        StringBuilder sqlSB = new StringBuilder(" select * from ots_user_table where tenant_id = '%d' ");

        String list2String = StringUtils.join(noGetPermittedIds.toArray(), ",");
        StringBuilder permittedIdString = new StringBuilder().append("(").append(list2String).append(")");
        sqlSB.append(" and table_id not in  %s ");

        if (!StringUtils.isBlank(tableName)) {//带条件查询
            sqlSB.append(" and table_name ");
            if (fuzzy){
                sqlSB.append(" ~ '%s' ");
            }else {
                sqlSB.append(" = '%s' ");
            }
            sqlSB.append(" limit '%d' offset '%d' " );
            sql = String.format(sqlSB.toString(),tenantId,permittedIdString, tableName,limit,offset);
        }else if(limit != null && offset != null){
            sqlSB.append(" limit '%d' offset '%d' " );
            sql = String.format(sqlSB.toString(), tenantId, permittedIdString, limit, offset);
        }else{
            sql = String.format(sqlSB.toString(), tenantId, permittedIdString);
        }

        LOG.debug(sql);

        return sql;
    }

    /**
     * 判定表是否被设置过权限
     * @param tableId
     * @return
     * @throws PermissionSqlException
     * @throws ConfigException
     */
    public boolean checkPermitted(long tableId) throws PermissionSqlException, ConfigException {
        boolean permitted = false;
        try {
            connect();
            Statement st = conn.createStatement();
            String sql = "select permission from ots_user_table where table_id =" + tableId;
            ResultSet rs = st.executeQuery(sql);
            if(rs.next()){
                permitted = rs.getBoolean("permission");
            }
            st.close();
        } catch(ConfigException e){
            e.getStackTrace();
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            throw e;
        } catch (SQLException e) {
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            throw new PermissionSqlException(OtsErrorCode.EC_OTS_QUERY_PERMISSION_SQL_LABEL,
                    (new StringBuilder()).append("Failed to query the specified value of permission label fields ").toString());
        }
        return permitted;
    }

    /**
     * 查询被设置过权限的表的Id（被设置过权限的话，permission=t）
     * @param tenantId
     * @return
     */
    public List<Long> queryPermittedTableIds(Long tenantId) throws ConfigException {

        List<Long> returnObjectList = new ArrayList<>();
        try {
            connect();
            Statement st = conn.createStatement();
            String sql = String.format("select table_id from ots_user_table where permission='t' and tenant_id = '%d'", tenantId);
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                returnObjectList.add(rs.getLong(TableConstants.TABLE_ID));
            }
            st.close();
        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_QUERY_TABLE, "Failed to query all table!\n" + e.getMessage());
        }
        return returnObjectList;
    }

    /**
     * 查询被设置过权限的表的Id（被设置过权限的话，permission=t）
     * @param tenantId
     * @return
     */
    public List<Long> queryPermittedTableId(Long tenantId, String tableName) throws ConfigException {
        List<Long> returnObjectList = new ArrayList<>();
        try {
            connect();
            Statement st = conn.createStatement();
            String sql = String.format("select table_id from ots_user_table where permission='t' and tenant_id = '%d' and table_name = '%s' ", tenantId, tableName);
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                returnObjectList.add(rs.getLong(TableConstants.TABLE_ID));
            }
            st.close();
        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_QUERY_TABLE, "Failed to query all table!\n" + e.getMessage());
        }
        return returnObjectList;
    }

    /**
     * 对表进行授权
     * @param tableId
     * @throws PermissionSqlException
     * @throws ConfigException
     */
    public void setTablePermission(long tableId) throws PermissionSqlException, ConfigException {
        try {
            connect();
            Statement st = conn.createStatement();
            String sql = "update ots_user_table set permission = true where table_id =" + tableId;
            st.execute(sql);
            st.close();
        }  catch(ConfigException e){
            e.getStackTrace();
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            throw e;
        }	catch (SQLException e) {
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            throw new PermissionSqlException(OtsErrorCode.EC_OTS_ADD_PERMISSION_SQL_LABEL, (new StringBuilder()).append("Failed to add the permitted field to the relevant table").toString());
        }
    }

    //=================权限===========================


    //=================索引===========================

    /**
     * 查询索引详细信息
     * @param tableId
     * @param indexName
     * @return
     */
    public Index queryIndex(long tableId, String indexName) throws ConfigException {
        Index index = null;

        try {
            connect();

            Statement st = conn.createStatement();
            String sql = String.format(" select * from ots_table_index where table_id = '%d' and index_name = '%s' ;", tableId, indexName);
            LOG.debug(sql);

            ResultSet rs = st.executeQuery(sql);
            if(rs.next()) {
                index = resultSetToIndex(rs);
            }
            st.close();
        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_QUERY_INDEX, "Failed to query index, tableId ="
                    + tableId + " indexName=" + indexName +"!\n" + e.getMessage());
        }

        return index;
    }

    /**
     * 新增index
     * @param index
     * @return
     */
    public long addIndex(Index index) throws ConfigException {
        long indexId = 0;

        try {
            connect();
            conn.setAutoCommit(false);

            String sql = String.format(" insert into ots_table_index " +
                            " (\"table_id\", \"user_id\",\"index_type\",\"index_name\", \"table_name\", \"index_key\", \"shard\", \"replication\", \"create_time\", \"modify_time\", \"creator\",\"modifier\") " +
                            " values ('%d', '%d', '%s', '%s','%s','%s','%d','%d', ?, ?, ?, ?) returning index_id; ",
                    index.getTableId(), index.getUserId(), index.getIndexType(), index.getIndexName(), index.getTableName(),
                    index.getIndexKey(),index.getShard(),index.getReplication());

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, new Timestamp(index.getCreateTime().getTime()));
            pstmt.setTimestamp(2, new Timestamp(index.getModifyTime().getTime()));
            pstmt.setLong(3, index.getCreator());
            pstmt.setLong(4, index.getModifier());

            LOG.debug(pstmt.toString());

            pstmt.execute();
            ResultSet rs = pstmt.getResultSet();
            if(rs.next()){
                indexId = rs.getLong("index_id");
            }
            conn.commit();
            conn.setAutoCommit(true);
            pstmt.close();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_ADD_INDEX, "Failed to add index " + index.getIndexName() + " and rollback!\n" + e.getMessage() + e1.getMessage());
            }
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_ADD_INDEX, "Failed to add index " + index.getIndexName() + "!\n" + e.getMessage());
        }

        return indexId;

    }

    /**
     * 查询该表拥有的所有index
     * @param tableId
     * @return
     */
    public List<Index> queryRDBIndexByTableId(long tableId)  throws ConfigException {
        List<Index> indexList = new ArrayList<>();

        try {
            connect();
            Statement st = conn.createStatement();

            String sql = String.format("select * from ots_table_index where table_id = '%d' order by index_id;", tableId);
            LOG.debug(sql);

            ResultSet rs = st.executeQuery(sql);
            while(rs.next()) {
                Index index = resultSetToIndex(rs);
                indexList.add(index);
            }
            st.close();
        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_QUERY_INDEX, "Failed to query table index of table(tableId=" + tableId +")!\n" + e.getMessage());
        }

        return indexList;
    }

    /**
     * 判定该表是否已经有了该索引
     * @param tableId
     * @param indexName
     * @return
     */
    public boolean ifExistIndex(Long tableId, String indexName) throws ConfigException {
        Statement st = null;
        try {
            connect();

            st = conn.createStatement();
            String sql = String.format("select index_id from ots_table_index where index_name = '%s' and table_id = '%d';", indexName, tableId);
            LOG.debug(sql);

            ResultSet rs = st.executeQuery(sql);
            if (rs.next()) {
                return true;
            } else return false;

        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_QUERY_INDEX, "Failed to query index " + indexName + "!\n" + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void delIndexByIndexId(long indexId) throws ConfigException {
        Statement st = null;
        try {
            connect();

            st = conn.createStatement();
            String sql = String.format("delete from ots_table_index where index_id = '%d' ", indexId);
            LOG.debug(sql);

            st.execute(sql);
            st.close();
        } catch (SQLException e) {
            throw new ConfigException(OtsErrorCode.EC_RDS_FAILED_QUERY_INDEX,
                    "Failed to delete index " + indexId + "!\n" + e.getMessage());
        } finally {
            try {
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 将查询的结果存入index对象中
     * @param rs
     * @return
     */
    private Index resultSetToIndex(ResultSet rs) throws SQLException {
        Index index = new Index();
        index.setUserId(rs.getLong(TableConstants.USER_ID));
        index.setTableId(rs.getLong(TableConstants.TABLE_ID));

        index.setIndexType(rs.getString(TableConstants.INDEX_TYPE));
        index.setTableName(rs.getString(TableConstants.TABLE_NAME));
        index.setIndexName(rs.getString(TableConstants.INDEX_NAME));
        index.setIndexKey(rs.getString(TableConstants.INDEX_KEY));

        index.setShard(rs.getInt(TableConstants.SHARD));
        index.setReplication(rs.getInt(TableConstants.REPLICATION));

        index.setCreateTime(rs.getTimestamp(TableConstants.CREATE_TIME));
        index.setModifyTime(rs.getTimestamp(TableConstants.MODIFY_TIME));
        index.setCreator(rs.getLong(TableConstants.CREATOR));
        index.setModifier(rs.getLong(TableConstants.MODIFIER));

        return index;
    }

    /**
     * 即将废弃
     * 将查询的结果存入table对象中
     * @param rs
     * @return
     */
    private Table resultSetToTable(ResultSet rs) throws SQLException {
        Table table = new Table();
        table.setUserId(rs.getLong(TableConstants.USER_ID));
        table.setTableId(rs.getLong(TableConstants.TABLE_ID));
        table.setTenantId(rs.getLong(TableConstants.TENANT_ID));

        table.setTableName(rs.getString(TableConstants.TABLE_NAME));
        table.setTableDesc(rs.getString(TableConstants.TABLE_DESC));

        table.setPrimaryKey(rs.getString(TableConstants.PRIMARY_KEY));
        table.setTableColumns(rs.getString(TableConstants.TABLE_COLUMNS));

        table.setCreateTime(rs.getTimestamp(TableConstants.CREATE_TIME));
        table.setModifyTime(rs.getTimestamp(TableConstants.MODIFY_TIME));
        table.setCreator(rs.getLong(TableConstants.CREATOR));
        table.setModifier(rs.getLong(TableConstants.MODIFIER));

        table.setPermission(rs.getBoolean(TableConstants.PERMISSION));
        table.setEnable(rs.getBoolean(TableConstants.ENABLE));

        return table;
    }

}
