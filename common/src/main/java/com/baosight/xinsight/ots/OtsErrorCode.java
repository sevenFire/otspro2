package com.baosight.xinsight.ots;

public class OtsErrorCode {
	//////////////////////////////////////table
	public static final int EC_OTS_STORAGE_BASE_NO = 174000;

	//对表的操作
	public static final long EC_OTS_STORAGE_TABLE_CREATE = EC_OTS_STORAGE_BASE_NO + 1;
	public static final long EC_OTS_STORAGE_TABLE_DELETE = EC_OTS_STORAGE_BASE_NO + 2;
	public static final long EC_OTS_STORAGE_TABLE_UPDATE = EC_OTS_STORAGE_BASE_NO + 3;
	public static final long EC_OTS_STORAGE_TABLE_EXIST = EC_OTS_STORAGE_BASE_NO + 4;
	public static final long EC_OTS_STORAGE_TABLE_NOTEXIST = EC_OTS_STORAGE_BASE_NO + 5;

	public static final long EC_OTS_STORAGE_SAVE_SQLTABLE_CONFIG = EC_OTS_STORAGE_BASE_NO + 6;

	//对记录的操作
	public static final long EC_OTS_STORAGE_RECORD_INSERT = EC_OTS_STORAGE_BASE_NO + 7;
	public static final long EC_OTS_STORAGE_RECORD_DELETE = EC_OTS_STORAGE_BASE_NO + 8;
	public static final long EC_OTS_STORAGE_RECORD_QUERY = EC_OTS_STORAGE_BASE_NO + 9;
	public static final long EC_OTS_STORAGE_RECORD_FILE_GET = EC_OTS_STORAGE_BASE_NO + 10;
	public static final long EC_OTS_STORAGE_RECORD_FILE_INSERT = EC_OTS_STORAGE_BASE_NO + 11;

	//非法参数
	public static final long EC_OTS_STORAGE_INVALID_RECQUERY_RANGE = EC_OTS_STORAGE_BASE_NO + 12;
	public static final long EC_OTS_STORAGE_INVALID_RECQUERY_KEY = EC_OTS_STORAGE_BASE_NO + 13;
	public static final long EC_OTS_STORAGE_INVALID_RECDELETE_RANGE = EC_OTS_STORAGE_BASE_NO + 14;
	public static final long EC_OTS_STORAGE_INVALID_RECDELETE_KEY = EC_OTS_STORAGE_BASE_NO + 15;
	public static final long EC_OTS_STORAGE_INVALID_REGEX = EC_OTS_STORAGE_BASE_NO + 16;
	//空参数
	public static final long EC_OTS_STORAGE_NULL_PARAMETER = EC_OTS_STORAGE_BASE_NO + 17;

	public static final long EC_OTS_STORAGE_DELETE_RECORD_NOMATCH = EC_OTS_STORAGE_BASE_NO + 17;

	public static final long EC_OTS_STORAGE_INDEX_DUPLICATE_COLUMN = EC_OTS_STORAGE_BASE_NO + 18;
	public static final long EC_OTS_STORAGE_INDEX_REBUILD = EC_OTS_STORAGE_BASE_NO + 19;
	public static final long EC_OTS_STORAGE_INDEX_NO_EXIST = EC_OTS_STORAGE_BASE_NO + 20;
	public static final long EC_OTS_STORAGE_NO_RUNNING_HBASE_MASTER = EC_OTS_STORAGE_BASE_NO + 21;
	public static final long EC_OTS_STORAGE_OPERATE_RECORD_NOSERVINGREGINON = EC_OTS_STORAGE_BASE_NO + 22;
	public static final long EC_OTS_STORAGE_OPERATE_RECORD_REGINONTOOBUSY = EC_OTS_STORAGE_BASE_NO + 23;
	public static final long EC_OTS_STORAGE_FAILED_CONN_ZK = EC_OTS_STORAGE_BASE_NO + 24;
	public static final long EC_OTS_STORAGE_INVALID_TENANT = EC_OTS_STORAGE_BASE_NO + 25;
	public static final long EC_OTS_STORAGE_JSON2OBJECT = EC_OTS_STORAGE_BASE_NO + 26;
	public static final long EC_OTS_STORAGE_INVALID_INITPARAM_HBASE = EC_OTS_STORAGE_BASE_NO + 27;
	public static final long EC_OTS_STORAGE_INVALID_INITPARAM_RDS = EC_OTS_STORAGE_BASE_NO + 28;
	public static final long EC_OTS_STORAGE_INVALID_INITPARAM_INDEX = EC_OTS_STORAGE_BASE_NO + 29;
	public static final long EC_OTS_STORAGE_INVALID_CHECK_TABLESTATUS = EC_OTS_STORAGE_BASE_NO + 30;
	
	/////////////////////////////////////index
	public static final int EC_OTS_INDEX_BASE_NO = 175000;
	public static final int EC_OTS_INDEX_INVALID_COLUMN_NAME = EC_OTS_INDEX_BASE_NO + 1;
	public static final int EC_OTS_INDEX_INVALID_COLUMN_TYPE = EC_OTS_INDEX_BASE_NO + 2;
	public static final int EC_OTS_INDEX_INVALID_COLUMN_NUM = EC_OTS_INDEX_BASE_NO + 3;
	public static final int EC_OTS_INDEX_FAILED_GEN_MOLPHINE_MAPPER = EC_OTS_INDEX_BASE_NO + 4;
	public static final int EC_OTS_INDEX_FAILED_GEN_MOLPHINE_FILE = EC_OTS_INDEX_BASE_NO + 5;
	public static final int EC_OTS_INDEX_NO_EXIST_CONFIG_TEMPLATE = EC_OTS_INDEX_BASE_NO + 6;
	public static final int EC_OTS_INDEX_FAILED_COPY_CONFIG_TEMPLATE = EC_OTS_INDEX_BASE_NO + 7;
	public static final int EC_OTS_INDEX_NO_EXIST_COLLECTION_SCHEMA = EC_OTS_INDEX_BASE_NO + 8;
	public static final int EC_OTS_INDEX_FAILED_PARSE_COLLECTION_SCHEMA = EC_OTS_INDEX_BASE_NO + 9;
	public static final int EC_OTS_INDEX_FAILED_MODIFY_COLLECTION_SCHEMA = EC_OTS_INDEX_BASE_NO + 10;
	public static final int EC_OTS_INDEX_FAILED_DEL_CONFIG_FROM_ZK = EC_OTS_INDEX_BASE_NO + 11;
	public static final int EC_OTS_INDEX_FAILED_ACCESS_SOLR_CLOUD_SERVER = EC_OTS_INDEX_BASE_NO + 12;
	public static final int EC_OTS_INDEX_INVALID_SOLR_SERVER_ADDR = EC_OTS_INDEX_BASE_NO + 13;
	public static final int EC_OTS_INDEX_FAILED_CREATE_COLLECTION = EC_OTS_INDEX_BASE_NO + 14;
	public static final int EC_OTS_INDEX_FAILED_DELETE_COLLECTION = EC_OTS_INDEX_BASE_NO + 15;
	public static final int EC_OTS_INDEX_FAILED_CREATE_HBASE_INDEXER = EC_OTS_INDEX_BASE_NO + 16;
	public static final int EC_OTS_INDEX_ALREADY_EXIST = EC_OTS_INDEX_BASE_NO + 17;
	public static final int EC_OTS_INDEX_BUILDING = EC_OTS_INDEX_BASE_NO + 18;
	public static final int EC_OTS_INDEX_FAILED_BUILD = EC_OTS_INDEX_BASE_NO + 19;
	public static final int EC_OTS_INDEX_FAILED_CLEAR = EC_OTS_INDEX_BASE_NO + 20;
	public static final int EC_OTS_INDEX_FAILED_DELETE_CONFIG_FILE = EC_OTS_INDEX_BASE_NO + 21;
	public static final int EC_OTS_INDEX_FAILED_DELETE = EC_OTS_INDEX_BASE_NO + 22;
	public static final int EC_OTS_INDEX_FAILED_UPLOAD_CONFIG_TO_ZK = EC_OTS_INDEX_BASE_NO + 23;
	public static final int EC_OTS_INDEX_FAILED_CREATE = EC_OTS_INDEX_BASE_NO + 24;
	public static final int EC_OTS_INDEX_FAILED_CONN_TO_ZK = EC_OTS_INDEX_BASE_NO + 25;
	public static final int EC_OTS_INDEX_FAILED_UPDATE = EC_OTS_INDEX_BASE_NO + 26;
	public static final int EC_OTS_INDEX_FAILED_QUERY = EC_OTS_INDEX_BASE_NO + 27;
	public static final int EC_OTS_INDEX_NO_EXIST = EC_OTS_INDEX_BASE_NO + 28;
	public static final int EC_OTS_INDEX_INVALID_SHARE_NUM = EC_OTS_INDEX_BASE_NO + 29;
	public static final int EC_OTS_INDEX_INVALID_REPLICATION = EC_OTS_INDEX_BASE_NO + 30;
	public static final int EC_OTS_INDEX_INVALID_PATTERN = EC_OTS_INDEX_BASE_NO + 31;
	
	/////////////////////////////////////secondary index
	public static final int EC_OTS_SEC_INDEX_BASE_NO = 175500;
	public static final int EC_OTS_SEC_INDEX_ALREADY_EXIST = EC_OTS_SEC_INDEX_BASE_NO+1;
	public static final int EC_OTS_SEC_INDEX_CREATE_INDEX_TABLE_FAILED = EC_OTS_SEC_INDEX_BASE_NO+2;
	public static final int EC_OTS_SEC_INDEX_COLUMN_NO_EXIST = EC_OTS_SEC_INDEX_BASE_NO+3;
	public static final int EC_OTS_SEC_INDEX_COLUMN_INVALID_TYPE = EC_OTS_SEC_INDEX_BASE_NO+4;
	public static final int EC_OTS_SEC_INDEX_INDEX_IS_BUILDING = EC_OTS_SEC_INDEX_BASE_NO+5;
	public static final int EC_OTS_SEC_INDEX_INDEX_TABLE_DISABLED = EC_OTS_SEC_INDEX_BASE_NO+6;
	public static final int EC_OTS_SEC_INDEX_COLUMN_ALREADY_EXIST = EC_OTS_SEC_INDEX_BASE_NO+7;
	public static final int EC_OTS_SEC_INDEX_INVALID_COLUMN_TYPE  = EC_OTS_SEC_INDEX_BASE_NO+8;
	public static final int EC_OTS_SEC_INDEX_INVALID_COLUMN_NAME = EC_OTS_SEC_INDEX_BASE_NO+9;
	public static final int EC_OTS_SEC_INDEX_INVALID_MAX_LEN = EC_OTS_SEC_INDEX_BASE_NO+10;
	public static final int EC_OTS_SEC_INDEX_INVALID_INDEX_INFO = EC_OTS_SEC_INDEX_BASE_NO+11;
	public static final int EC_OTS_SEC_INDEX_FAILED_GET_INDEX_INFO = EC_OTS_SEC_INDEX_BASE_NO+12;
	public static final int EC_OTS_SEC_INDEX_INVALID_TYPE_LEN = EC_OTS_SEC_INDEX_BASE_NO+13;
	public static final int EC_OTS_SEC_INDEX_FAILED_DELETE = EC_OTS_SEC_INDEX_BASE_NO+14;
	public static final int EC_OTS_SEC_INDEX_DELETE_INDEX_TABLE_FAILED = EC_OTS_SEC_INDEX_BASE_NO + 15;
	public static final int EC_OTS_SEC_INDEX_UPDATE_INDEX_TABLE_FAILED = EC_OTS_SEC_INDEX_BASE_NO + 16;
	public static final int EC_OTS_SEC_INDEX_INVALID_COLUMN_RANGE = EC_OTS_SEC_INDEX_BASE_NO + 17;
	public static final int EC_OTS_SEC_INDEX_INVALID_VALUE = EC_OTS_SEC_INDEX_BASE_NO + 18;
	
	/////////////////////////////rdb
	public static final int EC_RDS_BASE_NO = 176500;
	public static final int EC_RDS_ZERO_QUROM = EC_RDS_BASE_NO + 1;
	public static final int EC_RDS_FAILED_LOAD_JDBC_DRIVER = EC_RDS_BASE_NO + 2;
	public static final int EC_RDS_FAILED_CLOSE_CONN = EC_RDS_BASE_NO + 3;
	public static final int EC_RDS_FAILED_CONN = EC_RDS_BASE_NO + 4;
	public static final int EC_RDS_FAILED_CREATE_TABLE = EC_RDS_BASE_NO + 5;
	public static final int EC_RDS_FAILED_ROLLBACK = EC_RDS_BASE_NO + 6;
	public static final int EC_RDS_FAILED_DEL_TABLE = EC_RDS_BASE_NO + 7;
	public static final int EC_RDS_FAILED_UPDATE_TABLE = EC_RDS_BASE_NO + 8;
	public static final int EC_RDS_FAILED_QUERY_TABLE = EC_RDS_BASE_NO + 9;
	public static final int EC_RDS_FAILED_ADD_INDEX = EC_RDS_BASE_NO + 10;
	public static final int EC_RDS_FAILED_DEL_INDEX = EC_RDS_BASE_NO + 11;
	public static final int EC_RDS_FAILED_QUERY_INDEX = EC_RDS_BASE_NO + 12;
	public static final int EC_RDS_FAILED_UPDATE_INDEX = EC_RDS_BASE_NO + 13;
	public static final int EC_RDS_FAILED_ADD_TABLE_PROFILE = EC_RDS_BASE_NO + 14;
	public static final int EC_RDS_FAILED_DEL_TABLE_PROFILE = EC_RDS_BASE_NO + 15;
	public static final int EC_RDS_FAILED_QUERY_TABLE_PROFILE = EC_RDS_BASE_NO + 16;
	public static final int EC_RDS_FAILED_UPDATE_TABLE_PROFILE = EC_RDS_BASE_NO + 17;

	public static final int EC_RDS_FAILED_ADD_INDEX_PROFILE = EC_RDS_BASE_NO + 18;
	public static final int EC_RDS_FAILED_DEL_INDEX_PROFILE = EC_RDS_BASE_NO + 19;
	public static final int EC_RDS_FAILED_QUERY_INDEX_PROFILE = EC_RDS_BASE_NO + 20;
	public static final int EC_RDS_FAILED_UPDATE_INDEX_PROFILE = EC_RDS_BASE_NO + 21;
	
	public static final int EC_RDS_FAILED_QUERY_ALL_TID = EC_RDS_BASE_NO + 22;
	public static final int EC_RDS_FAILED_QUERY_TENANT = EC_RDS_BASE_NO + 23;
	
	/////////////////////////////permission
	public static final int EC_PERMISSION_NO = 176600;	
	public static final long EC_OTS_PERMISSION_MANAGE_INSTANCE_ADD_FAILED = EC_PERMISSION_NO + 1;
	public static final long EC_OTS_PERMISSION_NO_PERMISSION_FAULT = EC_PERMISSION_NO + 2;
	public static final long EC_OTS_PERMISSION_GROUPNAME_EMPTY = EC_PERMISSION_NO + 3;
	public static final long EC_OTS_PERMISSION_GET_GROUP_LIST_FAILED = EC_PERMISSION_NO + 4;
	public static final long EC_OTS_PERMISSION_OBJECTID_EMPTY = EC_PERMISSION_NO + 5;
	public static final long EC_OTS_PERMISSION_PARAMERTERS_NOT_COMPLETE = EC_PERMISSION_NO + 6;
	public static final long EC_OTS_ADD_PERMISSION_SQL_LABEL = EC_PERMISSION_NO + 7;
	public static final long EC_OTS_QUERY_PERMISSION_SQL_LABEL = EC_PERMISSION_NO + 8;
	public static final long EC_OTS_QUERY_PERMISSION_RESULT_EMPTY = EC_PERMISSION_NO + 9;
	public static final long EC_OTS_DELETE_PERMISSION_FAILED = EC_PERMISSION_NO + 10;
	public static final long EC_OTS_AAS_RESPONSE_RETURN_UNAVAILABLE = EC_PERMISSION_NO + 11;
	public static final long EC_OTS_AAS_REQUEST_UNAVAILABLE = EC_PERMISSION_NO + 12;
	public static final long EC_OTS_AAS_SUPER_ACCOUNT_TOKEN_UNAVAILABLE = EC_PERMISSION_NO + 13;
	public static final long EC_OTS_AAS_REGISTER_RESSOURCE_FAILED = EC_PERMISSION_NO + 14;
}

