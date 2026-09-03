package com.example.elderly_Platform.api.service;

import com.example.elderly_Platform.core.dto.ProfileUpdateDTO;
import com.example.elderly_Platform.core.entity.User;
import com.example.elderly_Platform.core.exception.BusinessException;
import com.example.elderly_Platform.core.mapper.UserMapper;
import com.example.elderly_Platform.core.vo.ProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 个人资料服务 —— 处理会员个人信息查询与修改业务逻辑
 */
@Service
@RequiredArgsConstructor
public class ProfileService {
    private final UserMapper userMapper;

    /**
     * 获取个人信息
     *
     * @param userId 用户ID
     * @return 个人资料 VO
     * @throws BusinessException 当用户不存在时抛出异常
     */
    public ProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        ProfileVO vo = new ProfileVO();
        vo.setId(user.getId());
        vo.setPhone(user.getPhone());
        vo.setRealName(user.getRealName());
        vo.setGender(user.getGender());
        vo.setBirthDate(user.getBirthDate() != null ? user.getBirthDate().toLocalDate() : null);
        vo.setHeight(user.getHeight());
        vo.setAvatar(user.getAvatar());
        vo.setEmergencyContact(user.getEmergencyContact());
        vo.setMemberLevel(user.getMemberLevel());
        vo.setPoints(user.getPoints());
        vo.setStatus(user.getStatus());
        vo.setCreateTime(user.getCreateTime());
        return vo;
    }

    /**
     * 修改个人信息
     *
     * @param userId 用户ID
     * @param dto    需要更新的个人资料数据
     * @throws BusinessException 当用户不存在时抛出异常
     */
    public void updateProfile(Long userId, ProfileUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        User update = new User();
        update.setId(userId);
        update.setRealName(dto.getRealName());
        update.setGender(dto.getGender());
        if (dto.getBirthDate() != null) {
            update.setBirthDate(dto.getBirthDate().atStartOfDay());
        }
        update.setHeight(dto.getHeight());
        update.setAvatar(dto.getAvatar());
        update.setEmergencyContact(dto.getEmergencyContact());
        update.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(update);
    }
}
