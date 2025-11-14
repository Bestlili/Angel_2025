package org.example.angelbacked.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.angelbacked.entity.User;
import org.example.angelbacked.entity.UserFollow;
import org.example.angelbacked.util.Result;

import java.util.List;

public interface UserRelationService extends IService<UserFollow> {
    
    Result followUser(Integer targetUserId, Integer currentUserId);
    
    Result getRecommendedUsers(Integer currentUserId);
    
    List<User> enrichUsers(List<User> users, Integer currentUserId);
}