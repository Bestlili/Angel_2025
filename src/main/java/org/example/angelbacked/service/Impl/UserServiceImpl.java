package org.example.angelbacked.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.angelbacked.entity.User;
import org.example.angelbacked.mapper.UserMapper;
import org.example.angelbacked.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    //密码加密器
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();

    @Override
    public void register(String username, String email, String password){
        //1.校验用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        if (baseMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        //2.校验邮箱是否已存在
        queryWrapper.clear();
        queryWrapper.eq(User::getEmail, email);
        if (baseMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("邮箱已存在");
        }

        //3.密码加密
        String encodedPassword = bCryptPasswordEncoder.encode(password);

        //4.保存用户信息
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        user.setEmail(email);
        user.setRegistrationDate(LocalDateTime.now());
        user.setStatus(1); // 1表示active状态
        baseMapper.insert(user);
    }
    
    @Override
    public User login(String identifier, String password) {
        // 1. 根据用户名或邮箱查询用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 判断identifier是邮箱还是用户名
        if (StringUtils.hasText(identifier)) {
            if (identifier.contains("@")) {
                // 如果包含@符号，按邮箱查询
                queryWrapper.eq(User::getEmail, identifier);
            } else {
                // 否则按用户名查询
                queryWrapper.eq(User::getUsername, identifier);
            }
        } else {
            throw new RuntimeException("用户名或邮箱不能为空");
        }
        
        User user = baseMapper.selectOne(queryWrapper);
        
        // 2. 如果用户不存在，抛出异常
        if (user == null) {
            throw new RuntimeException("用户名或邮箱不存在");
        }
        
        // 3. 校验密码
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        // 4. 更新最后登录时间
        user.setLastLoginDate(LocalDateTime.now());
        baseMapper.updateById(user);
        
        // 5. 返回用户信息（不包含密码）
        user.setPassword(null);
        return user;
    }
    
    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return baseMapper.selectOne(queryWrapper);
    }
    
    @Override
    public boolean updateProfile(User user) {
        // 根据用户名更新用户资料
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        return baseMapper.update(user, queryWrapper) > 0;
    }
    
    @Override
    public boolean changePassword(String username, String currentPassword, String newPassword) {
        // 1. 获取用户信息
        User user = getByUsername(username);
        if (user == null) {
            return false;
        }
        
        // 2. 验证当前密码是否正确
        if (!bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("当前密码错误");
        }
        
        // 3. 加密新密码
        String encodedNewPassword = bCryptPasswordEncoder.encode(newPassword);
        
        // 4. 更新密码
        user.setPassword(encodedNewPassword);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return baseMapper.update(user, queryWrapper) > 0;
    }
    
    @Override
    public boolean deleteAccount(String username) {
        // 这里实现软删除，将用户状态设置为已注销
        User user = new User();
        user.setStatus(0); // 0表示deleted状态
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        return baseMapper.update(user, queryWrapper) > 0;
    }
}