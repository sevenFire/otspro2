package com.baosight.xinsight.ots.constants;

/**
 * @author liyuhui
 * @date 2018/12/11
 * @description
 */
public class TableConstants {
    public static final String TABLE_ID = "table_id";
    public static final String USER_ID = "user_id";
    public static final String TENANT_ID = "tenant_id";
    public static final String TABLE_NAME = "table_name";
    public static final String TABLE_DESC = "table_desc";
    public static final String PRIMARY_KEY = "primary_key";
    public static final String TABLE_COLUMNS = "table_columns";
    public static final String CREATE_TIME = "create_time";
    public static final String MODIFY_TIME = "modify_time";
    public static final String CREATOR = "creator";
    public static final String MODIFIER = "modifier";
    public static final String ENABLE = "enable";
    public static final String PERMISSION = "permission";

    public static final String INDEX_NAME = "index_name";
    public static final String INDEX_TYPE = "index_type";
    public static final String SHARD = "shard";
    public static final String REPLICATION = "replication";
    public static final String INDEX_KEY = "index_key";


    public static final String T_OTS_USER_TABLE = "ots_user_table";  //表名
    public static final String E_OTS_USER_TABLE = "Table";   //实体名

    public static final String INSERT = "insert";
    public static final String QUERY = "query";
    public static final String DELETE = "delete";
    public static final String UPDATE = "update";


    //HBase中ots的表名前缀
    public static final String HBASE_TABLE_PREFIX = "1:ots__";

    //HBase中ots的cell名 //todo lyh
    public static final String HBASE_TABLE_CELL = "all";

    public static final String OTS_INDEX_TYPE_HBASE_STRING = "HBase";
}
