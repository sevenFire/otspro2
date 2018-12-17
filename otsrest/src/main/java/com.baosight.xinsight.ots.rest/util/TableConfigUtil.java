package com.baosight.xinsight.ots.rest.util;

import com.baosight.xinsight.common.CommonConstants;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.client.metacfg.Table;
import com.baosight.xinsight.ots.rest.model.table.vo.TableInfoVo;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liyuhui
 * @date 2018/12/16
 * @description
 */
public class TableConfigUtil {
    private static Map<String, Map<String, TableInfoVo>> tbConfig = new HashMap<>();


    /**
     * 添加tableInfo
     * @param userId
     * @param tenantId
     * @param info
     * @throws ConfigException
     */
    public static void addTableConfig(long userId, long tenantId, TableInfoVo info) throws ConfigException {
        String key = generateConfigKey(userId,tenantId);

        if (tbConfig.containsKey(key)) {
            tbConfig.get(key).put(info.getTableName(), info);
            return;
        }

        Map<String, TableInfoVo> tbConfigMap = new HashMap<>();
        tbConfigMap.put(info.getTableName(), info);
        tbConfig.put(key, tbConfigMap);
    }

    private static String generateConfigKey(long userId, long tenantId) {
        // 10001@2
        return String.valueOf(tenantId) + CommonConstants.DEFAULT_DOMAIN_SPLIT + String.valueOf(userId);
    }


    /**
     * 获取tableInfo
     * @param userId
     * @param tenantId
     * @param tableName
     * @return
     */
    public static TableInfoVo getTableConfig(Long userId, Long tenantId, String tableName) {
        String key = generateConfigKey(userId,tenantId);

        if (tbConfig.containsKey(key)) {
            if (tbConfig.get(key).containsKey(tableName)) {
                return tbConfig.get(key).get(tableName);
            }
        }
        return null;
    }

    /**
     * 删除tableInfo
     * @param userId
     * @param tenantId
     * @param tableName
     * @throws ConfigException
     */
    public static void deleteTableConfig(long userId, long tenantId, String tableName) throws ConfigException {
        String key = generateConfigKey(userId,tenantId);
        if (tbConfig.containsKey(key)) {
            if (tbConfig.get(key).containsKey(tableName)) {
                tbConfig.get(key).remove(tableName);
            }
        }
    }

    /**
     * 新增
     * @param userId
     * @param tenantId
     * @param tableName
     * @throws ConfigException
     * @throws Exception
     */
    public static synchronized void syncAdd(long userId, long tenantId, String tableName) throws ConfigException, Exception {
        try {
            Table table = ConfigUtil.getInstance().getOtsAdmin().getTableInfo(tenantId, tableName);
            if (table == null) {
                return;
            }

            TableInfoVo info = new TableInfoVo(tableName);
            info.setTableId(table.getTableId());
            info.setPrimaryKey(table.getPrimaryKey());
            info.setTableColumns(table.getTableColumns());

            addTableConfig(userId, tenantId, info);
        } catch (ConfigException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 删除
     * @param tenantid
     * @param userid
     * @param tablename
     */
    public static synchronized void syncDel(long tenantid, long userid, String tablename) {
        String key = String.valueOf(tenantid) + CommonConstants.DEFAULT_DOMAIN_SPLIT + String.valueOf(userid);
        if (tbConfig.containsKey(key)) {
            if (tbConfig.get(key).containsKey(tablename)) {
                tbConfig.get(key).remove(tablename);
            }
        }
    }

    /**
     * 更新
     * @param tenantid
     * @param userid
     * @param tablename
     * @throws ConfigException
     * @throws Exception
     */
    public static synchronized void syncUpdate(long tenantid, long userid, String tablename) throws ConfigException, Exception {
        String key = String.valueOf(tenantid) + CommonConstants.DEFAULT_DOMAIN_SPLIT + String.valueOf(userid);
        if (tbConfig.containsKey(key)) {
            if (tbConfig.get(key).containsKey(tablename)) {
                tbConfig.get(key).remove(tablename);

                syncAdd(tenantid, userid, tablename);
            }
        }
    }



}
