package org.example.angelbacked.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.angelbacked.entity.User;
import org.example.angelbacked.entity.UserFollow;
import org.example.angelbacked.mapper.UserFollowMapper;
import org.example.angelbacked.mapper.UserMapper;
import org.example.angelbacked.service.UserRelationService;
import org.example.angelbacked.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserRelationServiceImpl extends ServiceImpl<UserFollowMapper, UserFollow> implements UserRelationService {
    
    @Autowired
    private UserFollowMapper userFollowMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    @Transactional
    public Result followUser(Integer targetUserId, Integer currentUserId) {
        if (targetUserId.equals(currentUserId)) {
            return Result.error("不能关注自己");
        }
        
        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null) {
            return Result.error("目标用户不存在");
        }
        
        boolean isFollowing = userFollowMapper.isFollowing(currentUserId, targetUserId);
        
        if (isFollowing) {
            // 取消关注
            userFollowMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<UserFollow>()
                    .eq("follower_id", currentUserId)
                    .eq("following_id", targetUserId));
        } else {
            // 关注
            UserFollow userFollow = new UserFollow();
            userFollow.setFollowerId(currentUserId);
            userFollow.setFollowingId(targetUserId);
            userFollowMapper.insert(userFollow);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("isFollowing", !isFollowing); // 状态已切换
        
        return Result.success().setData(result);
    }
    
    @Override
    public Result getRecommendedUsers(Integer currentUserId) {
        // 简单实现：获取一些用户作为推荐（排除当前用户）
        List<User> users = userMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .last("LIMIT 10"));
        
        if (currentUserId != null) {
            users = users.stream()
                    .filter(user -> !user.getId().equals(currentUserId))
                    .collect(Collectors.toList());
        }
        
        users = enrichUsers(users, currentUserId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        
        return Result.success().setData(result);
    }
    
    @Override
    public List<User> enrichUsers(List<User> users, Integer currentUserId) {
        return users.stream().map(user -> {
            boolean isFollowing = userFollowMapper.isFollowing(currentUserId, user.getId());
            // 可以添加更多用户信息
            return user;
        }).collect(Collectors.toList());
    }
}