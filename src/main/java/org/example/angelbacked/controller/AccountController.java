package org.example.angelbacked.controller;

import org.example.angelbacked.entity.User;
import org.example.angelbacked.service.UserService;
import org.example.angelbacked.util.JwtUtil;
import org.example.angelbacked.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AccountController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    // 获取当前用户信息
    @GetMapping("/user")
    public Result getCurrentUser(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未授权访问").setCode(401);
            }

            // 清除敏感信息
            currentUser.setPassword(null);
            return Result.success().setData(currentUser);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage()).setCode(400);
        }
    }

    // 更新用户个人资料
    @PutMapping("/user")
    public Result updateProfile(@RequestBody User profileData, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未授权访问").setCode(401);
            }

            // 确保用户名匹配
            if (!currentUser.getUsername().equals(profileData.getUsername())) {
                return Result.error("不允许修改其他用户的信息").setCode(400);
            }

            // 更新用户资料
            boolean success = userService.updateProfile(profileData);
            if (success) {
                return Result.success("用户资料更新成功");
            } else {
                return Result.error("用户资料更新失败").setCode(400);
            }
        } catch (Exception e) {
            return Result.error("更新用户资料失败: " + e.getMessage()).setCode(400);
        }
    }

    // 修改用户密码
    @PostMapping("/change-password")
    public Result changePassword(@Valid @RequestBody ChangePasswordRequest passwordRequest, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未授权访问").setCode(401);
            }

            // 验证确认密码
            if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())) {
                return Result.error("新密码与确认密码不一致").setCode(400);
            }

            // 修改密码
            boolean success = userService.changePassword(
                    currentUser.getUsername(),
                    passwordRequest.getCurrentPassword(),
                    passwordRequest.getNewPassword()
            );

            if (success) {
                return Result.success("密码修改成功");
            } else {
                return Result.error("密码修改失败").setCode(400);
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage()).setCode(400);
        } catch (Exception e) {
            return Result.error("修改密码失败: " + e.getMessage()).setCode(400);
        }
    }

    // 注销用户账号
    @DeleteMapping("/account")
    public Result deleteAccount(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未授权访问").setCode(401);
            }

            boolean success = userService.deleteAccount(currentUser.getUsername());
            if (success) {
                return Result.success("账户注销成功");
            } else {
                return Result.error("账户注销失败").setCode(400);
            }
        } catch (Exception e) {
            return Result.error("注销账户失败: " + e.getMessage()).setCode(400);
        }
    }

    // 获取安全设置
    @GetMapping("/settings/security")
    public Result getSecuritySettings(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未授权访问").setCode(401);
            }

            Map<String, Object> settings = new HashMap<>();
            settings.put("rememberLogin", currentUser.getRememberLogin() != null ? currentUser.getRememberLogin() : false);
            settings.put("autoLogin", currentUser.getAutoLogin() != null ? currentUser.getAutoLogin() : false);

            return Result.success().setData(settings);
        } catch (Exception e) {
            return Result.error("获取安全设置失败: " + e.getMessage()).setCode(400);
        }
    }

    // 更新安全设置
    @PutMapping("/settings/security")
    public Result updateSecuritySettings(@RequestBody SecuritySettings settings, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未授权访问").setCode(401);
            }

            User userToUpdate = new User();
            userToUpdate.setUsername(currentUser.getUsername());
            userToUpdate.setRememberLogin(settings.getRememberLogin());
            userToUpdate.setAutoLogin(settings.getAutoLogin());

            boolean success = userService.updateProfile(userToUpdate);
            if (success) {
                return Result.success("安全设置更新成功");
            } else {
                return Result.error("安全设置更新失败").setCode(400);
            }
        } catch (Exception e) {
            return Result.error("更新安全设置失败: " + e.getMessage()).setCode(400);
        }
    }

    // 获取隐私设置
    @GetMapping("/settings/privacy")
    public Result getPrivacySettings(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未授权访问").setCode(401);
            }

            Map<String, Object> settings = new HashMap<>();
            settings.put("showDiaries", currentUser.getShowDiaries() != null ? currentUser.getShowDiaries() : true);
            settings.put("showTestResults", currentUser.getShowTestResults() != null ? currentUser.getShowTestResults() : true);

            return Result.success().setData(settings);
        } catch (Exception e) {
            return Result.error("获取隐私设置失败: " + e.getMessage()).setCode(400);
        }
    }

    // 更新隐私设置
    @PutMapping("/settings/privacy")
    public Result updatePrivacySettings(@RequestBody PrivacySettings settings, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未授权访问").setCode(401);
            }

            User userToUpdate = new User();
            userToUpdate.setUsername(currentUser.getUsername());
            userToUpdate.setShowDiaries(settings.getShowDiaries());
            userToUpdate.setShowTestResults(settings.getShowTestResults());

            boolean success = userService.updateProfile(userToUpdate);
            if (success) {
                return Result.success("隐私设置更新成功");
            } else {
                return Result.error("隐私设置更新失败").setCode(400);
            }
        } catch (Exception e) {
            return Result.error("更新隐私设置失败: " + e.getMessage()).setCode(400);
        }
    }

    // 获取通知设置
    @GetMapping("/settings/notifications")
    public Result getNotificationSettings(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未授权访问").setCode(401);
            }

            Map<String, Object> settings = new HashMap<>();
            settings.put("community", currentUser.getCommunityNotifications() != null ? currentUser.getCommunityNotifications() : true);
            settings.put("tests", currentUser.getTestsNotifications() != null ? currentUser.getTestsNotifications() : true);

            return Result.success().setData(settings);
        } catch (Exception e) {
            return Result.error("获取通知设置失败: " + e.getMessage()).setCode(400);
        }
    }

    // 更新通知设置
    @PutMapping("/settings/notifications")
    public Result updateNotificationSettings(@RequestBody NotificationSettings settings, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未授权访问").setCode(401);
            }

            User userToUpdate = new User();
            userToUpdate.setUsername(currentUser.getUsername());
            userToUpdate.setCommunityNotifications(settings.getCommunity());
            userToUpdate.setTestsNotifications(settings.getTests());

            boolean success = userService.updateProfile(userToUpdate);
            if (success) {
                return Result.success("通知设置更新成功");
            } else {
                return Result.error("通知设置更新失败").setCode(400);
            }
        } catch (Exception e) {
            return Result.error("更新通知设置失败: " + e.getMessage()).setCode(400);
        }
    }

    // 获取账号详情信息
    @GetMapping("/account/info")
    public Result getAccountInfo(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("未授权访问").setCode(401);
            }

            Map<String, Object> accountInfo = new HashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            accountInfo.put("registrationDate", 
                currentUser.getRegistrationDate() != null ? 
                currentUser.getRegistrationDate().format(formatter) : null);
                
            accountInfo.put("lastLoginDate", 
                currentUser.getLastLoginDate() != null ? 
                currentUser.getLastLoginDate().format(formatter) : null);
                
            accountInfo.put("status", currentUser.getStatus());

            return Result.success().setData(accountInfo);
        } catch (Exception e) {
            return Result.error("获取账户信息失败: " + e.getMessage()).setCode(400);
        }
    }

    // 内部类用于密码修改请求
    public static class ChangePasswordRequest {
        @NotBlank(message = "当前密码不能为空")
        private String currentPassword;

        @NotBlank(message = "新密码不能为空")
        private String newPassword;

        @NotBlank(message = "确认密码不能为空")
        private String confirmPassword;

        // Getters and setters
        public String getCurrentPassword() {
            return currentPassword;
        }

        public void setCurrentPassword(String currentPassword) {
            this.currentPassword = currentPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }

    // 内部类用于安全设置
    public static class SecuritySettings {
        @NotNull(message = "rememberLogin不能为空")
        private Boolean rememberLogin;

        @NotNull(message = "autoLogin不能为空")
        private Boolean autoLogin;

        // Getters and setters
        public Boolean getRememberLogin() {
            return rememberLogin;
        }

        public void setRememberLogin(Boolean rememberLogin) {
            this.rememberLogin = rememberLogin;
        }

        public Boolean getAutoLogin() {
            return autoLogin;
        }

        public void setAutoLogin(Boolean autoLogin) {
            this.autoLogin = autoLogin;
        }
    }

    // 内部类用于隐私设置
    public static class PrivacySettings {
        @NotNull(message = "showDiaries不能为空")
        private Boolean showDiaries;

        @NotNull(message = "showTestResults不能为空")
        private Boolean showTestResults;

        // Getters and setters
        public Boolean getShowDiaries() {
            return showDiaries;
        }

        public void setShowDiaries(Boolean showDiaries) {
            this.showDiaries = showDiaries;
        }

        public Boolean getShowTestResults() {
            return showTestResults;
        }

        public void setShowTestResults(Boolean showTestResults) {
            this.showTestResults = showTestResults;
        }
    }

    // 内部类用于通知设置
    public static class NotificationSettings {
        @NotNull(message = "community不能为空")
        private Boolean community;

        @NotNull(message = "tests不能为空")
        private Boolean tests;

        // Getters and setters
        public Boolean getCommunity() {
            return community;
        }

        public void setCommunity(Boolean community) {
            this.community = community;
        }

        public Boolean getTests() {
            return tests;
        }

        public void setTests(Boolean tests) {
            this.tests = tests;
        }
    }
}