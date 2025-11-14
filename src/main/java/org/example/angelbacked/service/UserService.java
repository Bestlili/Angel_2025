package org.example.angelbacked.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.angelbacked.entity.User;

public interface UserService extends IService<User> {
    // 注册方法
    void register(String username, String email, String password);

    // 登录方法，支持使用用户名或邮箱登录
    User login(String identifier, String password);
    
    // 通过用户名获取用户
    User getByUsername(String username);
    
    // 更新用户个人资料
    boolean updateProfile(User user);
    
    // 修改用户密码
    boolean changePassword(String username, String currentPassword, String newPassword);
    
    // 注销账户
    boolean deleteAccount(String username);
}