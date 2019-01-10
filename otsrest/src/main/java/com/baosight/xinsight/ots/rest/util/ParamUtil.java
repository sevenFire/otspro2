package com.baosight.xinsight.ots.rest.util;

import com.baosight.xinsight.ots.rest.constant.RestConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class ParamUtil {
    private static final Logger LOG = Logger.getLogger(ParamUtil.class);

    /**
     * 对offset参数的校验和转换
     * @param offset
     * @param offset
     */
    public static long dealWithOffset(String offset) {
        if (StringUtils.isBlank(offset) || Long.parseLong(offset) < 0){
            offset = String.valueOf(RestConstants.DEFAULT_QUERY_OFFSET);
        }

        return Long.parseLong(offset);
    }

    public static long dealWithLimit(String limit) {
        if (StringUtils.isBlank(limit) || Long.parseLong(limit) < 0){
            limit = String.valueOf(RestConstants.DEFAULT_QUERY_LIMIT);
        }

        if (limit != null && Integer.parseInt(limit) > RestConstants.DEFAULT_QUERY_MAX_LIMIT) {
            LOG.info("limit is too large, set to " + RestConstants.DEFAULT_QUERY_MAX_LIMIT);
            limit = String.valueOf(RestConstants.DEFAULT_QUERY_MAX_LIMIT);
        }

        return Long.parseLong(limit);
    }


    public static String dealWithCursorMark(String cursorMark) {
        if (StringUtils.isBlank(cursorMark)){
            cursorMark = "*";
        }
        return cursorMark;
    }
}
