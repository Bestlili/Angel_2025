package org.example.angelbacked.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String email;
    private String role;
    
    // 用户资料字段
    @TableField("nickname")
    private String nickname;
    
    @TableField("bio")
    private String bio;
    
    @TableField("introduction")
    private String introduction;
    
    // 安全设置字段
    @TableField("remember_login")
    private Boolean rememberLogin;
    
    @TableField("auto_login")
    private Boolean autoLogin;
    
    // 隐私设置字段
    @TableField("show_diaries")
    private Boolean showDiaries;
    
    @TableField("show_test_results")
    private Boolean showTestResults;
    
    // 通知设置字段
    @TableField("community_notifications")
    private Boolean communityNotifications;
    
    @TableField("tests_notifications")
    private Boolean testsNotifications;
    
    // 账户信息字段
    @TableField("registration_date")
    private LocalDateTime registrationDate;
    
    @TableField("last_login_date")
    private LocalDateTime lastLoginDate;
    
    @TableField("status")
    private Integer status;
    
    // 管理员功能扩展字段
    @TableField(exist = false)
    private List<String> permissions;
    
    // 扩展字段
    @TableField(exist = false)
    private String avatar; // 头像URL
    
    // 添加name字段，便于前端直接访问
    @TableField(exist = false)
    private String name;
    
    public String getName() {
        return this.username;
    }
}