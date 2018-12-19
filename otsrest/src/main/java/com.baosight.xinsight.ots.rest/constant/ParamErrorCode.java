package com.baosight.xinsight.ots.rest.constant;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class ParamErrorCode extends RestErrorCode {
    public static final long EC_OTS_REST_PARAM_BASE_NO = 173100;

    public static final long PARAM_RECORDS_IS_NULL = EC_OTS_REST_PARAM_BASE_NO + 1;
    public static final long PARAM_PRIMARY_KEY_IS_NULL = EC_OTS_REST_PARAM_BASE_NO + 2;
    //参数非法
    public static final long EC_OTS_REST_PARAM_INVALID = EC_OTS_REST_PARAM_BASE_NO + 3;
}
