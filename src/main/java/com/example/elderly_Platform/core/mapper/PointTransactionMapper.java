package com.example.elderly_Platform.core.mapper;

import com.example.elderly_Platform.core.entity.PointTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PointTransactionMapper {

    PointTransaction selectById(Long id);

    /**
     * 动态条件查询积分流水
     */
    List<PointTransaction> selectByCondition(PointTransaction tx);

    int insert(PointTransaction tx);

    int updateById(PointTransaction tx);

    /**
     * 逻辑删除
     */
    int deleteById(Long id);

    /**
     * 查询用户可用的积分批次（FIFO：按获得时间升序）
     */
    List<PointTransaction> selectAvailableBatches(@Param("userId") Long userId);

    /**
     * 查询已过期的积分批次（remain_amount > 0 且 expire_time < NOW()）
     */
    List<PointTransaction> selectExpiredBatches();
}
