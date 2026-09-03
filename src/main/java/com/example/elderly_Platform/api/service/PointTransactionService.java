package com.example.elderly_Platform.api.service;

import com.example.elderly_Platform.core.entity.PointTransaction;
import com.example.elderly_Platform.core.mapper.PointTransactionMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 积分流水服务 —— 处理会员积分流水的分页查询业务逻辑
 */
@Service
@RequiredArgsConstructor
public class PointTransactionService {
    private final PointTransactionMapper pointTransactionMapper;

    /**
     * 分页查询用户的积分流水
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @param type     积分流水类型（可选）
     * @param userId   用户ID
     * @return 分页的积分流水 PageInfo
     */
    public PageInfo<PointTransaction> selectPoints(Integer pageNum, Integer pageSize,String type,Long userId) {
        PageHelper.startPage(pageNum,pageSize);

        PointTransaction pt=new PointTransaction();
        pt.setUserId(userId);
        pt.setType(type);
        List<PointTransaction> list =pointTransactionMapper.selectByCondition(pt);
        return new PageInfo<>(list);
    }
}
