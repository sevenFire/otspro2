package com.baosight.xinsight.ots.client.metacfg;

import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.client.exception.PermissionSqlException;
import com.baosight.xinsight.ots.constants.TableConstants;
import com.mchange.v2.sql.SqlUtils;

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
import java.util.List;

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


    //============================================CURD=========================================================
    //新增表
    public long addTable(Table table) throws ConfigException	{
        long tableId = 0;

        try {
            connect();
            conn.setAutoCommit(false);

			String sql = String.format("insert into ots_user_table " +
							" (\"table_id\", \"user_id\", \"tenant_id\", \"table_name\", \"table_desc\", \"primary_key\", \"table_columns\", \"create_time\", \"modify_time\", \"creator\", \"modifier\") " +
							" values ('%d', '%d', '%d', '%s', '%s', '%s', '%s', '%d', '%d', ? , ? , ? , ? , ? , ?) returning table_id;",
					table.getTableId(), table.getUserId(), table.getTenantId(), table.getTableName(), table.getTableDesc(),  table.getPrimaryKey(), table.getTableColumns(), table.getCreateTime(), table.getModifyTime(),table.getCreator(),table.getModifier());
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
     * 根据userId和表名删除表。
     * 注意：同一个用户下不能有同名表，所以(userId,tableName)是唯一键。
     * @param userId
     * @param tableName
     * @throws ConfigException
     */
    public void delTable(long userId, String tableName) throws ConfigException {

        try {
            connect();

            Statement st = conn.createStatement();
            String sql = String.format("delete from ots_user_table where ots_user_table.user_id = '%d' and ots_user_table.table_name = '%s';", userId, tableName);
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
    public void delTable(long tableId) throws ConfigException {

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
     * @param userId
     * @param tableName
     * @return
     * @throws ConfigException
     */
    public Table queryTable(long userId, String tableName) throws ConfigException{
        Table table = null;

        try {
            connect();

            Statement st = conn.createStatement();
            String sql = String.format("select * from ots_user_table where ots_user_table.table_name = '%s' and ots_user_table.userid = '%d';", tableName, userId);
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
     * 判定表是否存在
     * @param userId
     * @param tableName
     * @return
     * @throws ConfigException
     */
    public Boolean ifExistTable(long userId, String tableName) throws ConfigException {
        Statement st = null;
        try {
            connect();

            st = conn.createStatement();
            String sql = String.format("select count(1) from ots_user_table where ots_user_table.table_name = '%s' and ots_user_table.user_id = '%d';", tableName, userId);
            LOG.debug(sql);

            ResultSet rs = st.executeQuery(sql);
            if (rs.next() && rs.getInt(1)>0) {
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

    /**
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

        return table;
    }

    /**
     * 判定表权限
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
            String sql = "select permission from public.ots_user_table where public.ots_user_table.id =" + tableId;
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
            throw new PermissionSqlException(OtsErrorCode.EC_OTS_QUERY_PERMISSION_SQL_LABEL, (new StringBuilder()).append("Failed to query the specified value of permission label fields ").toString());
        }
        return permitted;
    }


    /**
     * 设置表权限
     * @param tableId
     * @throws PermissionSqlException
     * @throws ConfigException
     */
    public void setTablePermission(long tableId) throws PermissionSqlException, ConfigException {
        try {
            connect();
            Statement st = conn.createStatement();
            String sql = "update public.ots_user_table set permission = true where public.ots_user_table.id =" + tableId;
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
}
