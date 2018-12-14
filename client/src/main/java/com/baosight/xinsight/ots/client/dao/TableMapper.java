package com.baosight.xinsight.ots.client.dao;

import com.baosight.xinsight.ots.client.pojo.Table;

public interface TableMapper {
    int deleteByPrimaryKey(Long tableId);

    int insert(Table record);

    int insertSelective(Table record);

    Table selectByPrimaryKey(Long tableId);

    int updateByPrimaryKeySelective(Table record);

    int updateByPrimaryKey(Table record);
}