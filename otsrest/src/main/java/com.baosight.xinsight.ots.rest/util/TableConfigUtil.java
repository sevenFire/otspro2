package com.baosight.xinsight.ots.rest.util;

import com.baosight.xinsight.common.CommonConstants;
import com.baosight.xinsight.ots.client.OtsTable;
import com.baosight.xinsight.ots.client.exception.ConfigException;
import com.baosight.xinsight.ots.rest.model.table.response.TableInfoBody;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liyuhui
 * @date 2018/12/16
 * @description
 */
public class TableConfigUtil {
    private static Map<String, Map<String, TableInfoBody>> tbConfig = new HashMap<>();

    /**
     * 添加tableInfo
     * @param userId
     * @param tenantId
     * @param info
     * @throws ConfigException
     */
    public static void addTableConfig(long userId, long tenantId, TableInfoBody info) throws ConfigException {
        String key = generateConfigKey(userId,tenantId);

        if (tbConfig.containsKey(key)) {
            tbConfig.get(key).put(info.getTableName(), info);
            return;
        }

        Map<String, TableInfoBody> tbConfigMap = new HashMap<>();
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
    public static TableInfoBody getTableConfig(Long userId, Long tenantId, String tableName) {
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
            OtsTable otsTable = ConfigUtil.getInstance().getOtsAdmin().getTableInfo(tenantId, tableName);
            if (otsTable == null) {
                return;
            }

            TableInfoBody info = new TableInfoBody(tableName);
            info.setTableId(otsTable.getTableId());
            info.setPrimaryKey(otsTable.getPrimaryKey());
            info.setTableColumns(otsTable.getTableColumns());

            addTableConfig(userId, tenantId, info);
        } catch (ConfigException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 删除
     * @param tenantId
     * @param userId
     * @param tableName
     */
    public static synchronized void syncDel(long tenantId, long userId, String tableName) {
        String key = String.valueOf(tenantId) + CommonConstants.DEFAULT_DOMAIN_SPLIT + String.valueOf(userId);
        if (tbConfig.containsKey(key)) {
            if (tbConfig.get(key).containsKey(tableName)) {
                tbConfig.get(key).remove(tableName);
            }
        }
    }

    /**
     * 更新
     * @param tenantId
     * @param userId
     * @param tableName
     * @throws ConfigException
     * @throws Exception
     */
    public static synchronized void syncUpdate(long tenantId, long userId, String tableName) throws Exception {
        String key = String.valueOf(tenantId) + CommonConstants.DEFAULT_DOMAIN_SPLIT + String.valueOf(userId);
        if (tbConfig.containsKey(key)) {
            if (tbConfig.get(key).containsKey(tableName)) {
                tbConfig.get(key).remove(tableName);

                syncAdd(tenantId, userId, tableName);
            }
        }
    }



}
