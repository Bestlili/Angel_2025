package org.example.angelbacked.controller;

import org.example.angelbacked.entity.User;
import org.example.angelbacked.service.UserService;
import org.example.angelbacked.util.JwtUtil;
import org.example.angelbacked.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated // 开启数据验证
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    // 创建一个内部类用于接收注册请求数据
    public static class RegisterRequest {
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, message = "用户名长度不能小于3")
        private String username;

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        private String email;

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, message = "密码长度不能小于6")
        private String password;

        // Getters and setters
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
    
    // 创建一个内部类用于接收登录请求数据
    public static class LoginRequest {
        @NotBlank(message = "用户名或邮箱不能为空")
        private String identifier;

        @NotBlank(message = "密码不能为空")
        private String password;

        // Getters and setters
        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    /**
     * 注册接口（对应前端的/api/auth/register）
     */
    @PostMapping("/register")
    public Result register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            userService.register(registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getPassword());
            return Result.success("注册成功");
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 登录接口（对应前端的/api/auth/login）
     * 支持使用用户名或邮箱登录
     */
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.login(loginRequest.getIdentifier(), loginRequest.getPassword());
            // 生成JWT token
            String token = jwtUtil.generateToken(user.getUsername());
            
            System.out.println("Generated token for user '" + user.getUsername() + "': " + token);
            
            // 创建返回数据，包括用户信息和token
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("user", user);
            responseData.put("token", token);
            
            return Result.success("登录成功").setData(responseData);
        } catch (Exception e) {
            System.err.println("Login failed: " + e.getMessage());
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 登出接口（对应前端的/api/auth/logout）
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        // 对于JWT token，通常前端会删除token来实现登出
        // 这里可以添加额外的登出逻辑，如将token加入黑名单等
        try {
            // 获取请求头中的token
            final String requestTokenHeader = request.getHeader("Authorization");
            String jwtToken = null;
            
            // JWT Token的格式为 "Bearer token"
            if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                jwtToken = requestTokenHeader.substring(7);
                // 可以在这里添加将token加入黑名单的逻辑
                // 例如: tokenBlacklistService.addToBlacklist(jwtToken);
            }
            
            return Result.success("登出成功");
        } catch (Exception e) {
            return Result.error("登出失败: " + e.getMessage());
        }
    }
}