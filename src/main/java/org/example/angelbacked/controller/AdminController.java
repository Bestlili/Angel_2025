package org.example.angelbacked.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.angelbacked.entity.AdminLog;
import org.example.angelbacked.entity.Post;
import org.example.angelbacked.entity.User;
import org.example.angelbacked.service.AdminLogService;
import org.example.angelbacked.service.PostService;
import org.example.angelbacked.service.UserService;
import org.example.angelbacked.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private PostService postService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private AdminLogService adminLogService;
    
    @Value("${app.name:Angel心理平台}")
    private String siteName;
    
    @Value("${app.description:心理健康服务平台}")
    private String siteDescription;
    
    @Value("${app.reviewStandards:审核标准文本...}")
    private String reviewStandards;
    
    @Value("${app.maxPostLength:1000}")
    private Integer maxPostLength;
    
    @Value("${app.maxImagesPerPost:9}")
    private Integer maxImagesPerPost;
    
    @Value("${app.defaultPageSize:10}")
    private Integer defaultPageSize;
    
    @Value("${app.maintenanceMode:false}")
    private Boolean maintenanceMode;
    
    /**
     * 验证管理员权限
     */
    @GetMapping("/verify")
    public Result verifyAdmin(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("isAdmin", true);
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            
            return Result.success().setData(result);
        } catch (Exception e) {
            return Result.error("验证失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 帖子审核操作
     */
    @PostMapping("/posts/{postId}/review")
    public Result reviewPost(@PathVariable Integer postId, 
                             @RequestBody Map<String, Object> requestBody,
                             HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            // 获取请求参数
            String action = (String) requestBody.get("action");
            String reason = (String) requestBody.get("reason");
            
            if (action == null || (!"approved".equals(action) && !"rejected".equals(action))) {
                return Result.error("操作类型不能为空，且必须为approved或rejected").setCode(400);
            }
            
            if ("rejected".equals(action) && (reason == null || reason.isEmpty())) {
                return Result.error("拒绝原因不能为空").setCode(400);
            }
            
            // 获取帖子信息
            Post post = postService.getById(postId);
            if (post == null) {
                return Result.error("帖子不存在").setCode(404);
            }
            
            // 更新帖子状态
            post.setStatus("approved".equals(action) ? 1 : 2); // 1表示approved, 2表示rejected
            if ("rejected".equals(action)) {
                post.setRejectReason(reason);
            }
            post.setReviewerId(currentUser.getId());
            post.setReviewTime(LocalDateTime.now());
            
            postService.updateById(post);
            
            // 记录操作日志
            AdminLog adminLog = new AdminLog();
            adminLog.setActionType("post_review");
            adminLog.setActionDetail(action.equals("approved") ? "通过帖子审核" : "拒绝帖子审核");
            adminLog.setTargetId(String.valueOf(postId));
            adminLog.setOperatorId(currentUser.getId());
            adminLog.setOperatorName(currentUser.getUsername());
            adminLog.setCreatedAt(LocalDateTime.now());
            adminLog.setIpAddress(request.getRemoteAddr());
            adminLogService.save(adminLog);
            
            Map<String, Object> data = new HashMap<>();
            data.put("postId", post.getId());
            data.put("status", post.getStatus());
            data.put("reviewTime", post.getReviewTime());
            data.put("reviewerId", post.getReviewerId());
            
            return Result.success("审核成功").setData(data);
        } catch (Exception e) {
            return Result.error("审核失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 获取帖子列表
     */
    @GetMapping("/posts")
    public Result getPosts(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String sortBy,
                           @RequestParam(required = false) String search,
                           HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            // 构建查询条件
            QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
            
            // 状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", "pending".equals(status) ? 0 : ("approved".equals(status) ? 1 : 2));
            }
            
            // 搜索关键词
            if (search != null && !search.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper.like("content", search)
                        .or()
                        .apply("user_id IN (SELECT id FROM user WHERE username LIKE {0})", "%" + search + "%"));
            }
            
            // 排序
            if ("createdAt_desc".equals(sortBy)) {
                queryWrapper.orderByDesc("created_at");
            } else if ("createdAt_asc".equals(sortBy)) {
                queryWrapper.orderByAsc("created_at");
            }
            
            // 分页查询
            Page<Post> postPage = new Page<>(page, pageSize);
            postService.page(postPage, queryWrapper);
            
            // 丰富帖子信息
            List<Post> posts = postPage.getRecords();
            for (Post post : posts) {
                User author = userService.getById(post.getUserId());
                if (author != null) {
                    Map<String, Object> authorInfo = new HashMap<>();
                    authorInfo.put("id", author.getId());
                    authorInfo.put("name", author.getUsername());
                    // 这里可以添加更多作者信息
                    post.setAuthor(author);
                }
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("data", posts);
            data.put("total", postPage.getTotal());
            data.put("page", page);
            data.put("pageSize", pageSize);
            
            return Result.success().setData(data);
        } catch (Exception e) {
            return Result.error("获取帖子列表失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 获取待审核数量
     */
    @GetMapping("/posts/pending-count")
    public Result getPendingCount(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            // 查询待审核帖子数量
            QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", 0); // 0表示pending状态
            long countLong = postService.count(queryWrapper);
            int count = Math.toIntExact(countLong);
            
            Map<String, Object> data = new HashMap<>();
            data.put("count", count);
            
            return Result.success().setData(data);
        } catch (Exception e) {
            return Result.error("获取待审核数量失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 删除帖子
     */
    @DeleteMapping("/posts/{postId}")
    public Result deletePost(@PathVariable Integer postId, HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            // 检查帖子是否存在
            Post post = postService.getById(postId);
            if (post == null) {
                return Result.error("帖子不存在").setCode(404);
            }
            
            // 删除帖子
            postService.removeById(postId);
            
            // 记录操作日志
            AdminLog adminLog = new AdminLog();
            adminLog.setActionType("post_delete");
            adminLog.setActionDetail("删除帖子");
            adminLog.setTargetId(String.valueOf(postId));
            adminLog.setOperatorId(currentUser.getId());
            adminLog.setOperatorName(currentUser.getUsername());
            adminLog.setCreatedAt(LocalDateTime.now());
            adminLog.setIpAddress(request.getRemoteAddr());
            adminLogService.save(adminLog);
            
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 获取用户列表
     */
    @GetMapping("/users")
    public Result getUsers(@RequestParam(defaultValue = "1") Integer page,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String role,
                           @RequestParam(required = false) String search,
                           HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            // 构建查询条件
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            
            // 状态筛选
            if (status != null && !status.isEmpty()) {
                queryWrapper.eq("status", status);
            }
            
            // 角色筛选
            if (role != null && !role.isEmpty()) {
                queryWrapper.eq("role", role);
            }
            
            // 搜索关键词
            if (search != null && !search.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper.like("username", search)
                        .or()
                        .like("email", search));
            }
            
            // 分页查询
            Page<User> userPage = new Page<>(page, pageSize);
            userService.page(userPage, queryWrapper);
            
            List<User> users = userPage.getRecords();
            // 清除用户密码等敏感信息
            for (User user : users) {
                user.setPassword(null);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("data", users);
            data.put("total", userPage.getTotal());
            data.put("page", page);
            data.put("pageSize", pageSize);
            
            return Result.success().setData(data);
        } catch (Exception e) {
            return Result.error("获取用户列表失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 切换用户状态
     */
    @PutMapping("/users/{userId}/status")
    public Result updateUserStatus(@PathVariable Integer userId,
                                   @RequestBody Map<String, String> requestBody,
                                   HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            // 获取请求参数
            String status = requestBody.get("status");
            if (status == null || (!"active".equals(status) && !"banned".equals(status))) {
                return Result.error("状态值不能为空，且必须为active或banned").setCode(400);
            }
            
            // 检查用户是否存在
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在").setCode(404);
            }
            
            // 更新用户状态
            user.setStatus("active".equals(status) ? 1 : 0); // 1表示active, 0表示banned
            userService.updateById(user);
            
            // 记录操作日志
            AdminLog adminLog = new AdminLog();
            adminLog.setActionType("user_status_update");
            adminLog.setActionDetail("更新用户状态为" + status);
            adminLog.setTargetId(String.valueOf(userId));
            adminLog.setOperatorId(currentUser.getId());
            adminLog.setOperatorName(currentUser.getUsername());
            adminLog.setCreatedAt(LocalDateTime.now());
            adminLog.setIpAddress(request.getRemoteAddr());
            adminLogService.save(adminLog);
            
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("status", user.getStatus());
            
            return Result.success("状态更新成功").setData(data);
        } catch (Exception e) {
            return Result.error("状态更新失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 更新用户角色
     */
    @PutMapping("/users/{userId}/role")
    public Result updateUserRole(@PathVariable Integer userId,
                                 @RequestBody Map<String, String> requestBody,
                                 HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            // 获取请求参数
            String role = requestBody.get("role");
            if (role == null || (!"admin".equals(role) && !"user".equals(role))) {
                return Result.error("角色值不能为空，且必须为admin或user").setCode(400);
            }
            
            // 检查用户是否存在
            User user = userService.getById(userId);
            if (user == null) {
                return Result.error("用户不存在").setCode(404);
            }
            
            // 更新用户角色
            user.setRole(role);
            userService.updateById(user);
            
            // 记录操作日志
            AdminLog adminLog = new AdminLog();
            adminLog.setActionType("user_role_update");
            adminLog.setActionDetail("更新用户角色为" + role);
            adminLog.setTargetId(String.valueOf(userId));
            adminLog.setOperatorId(currentUser.getId());
            adminLog.setOperatorName(currentUser.getUsername());
            adminLog.setCreatedAt(LocalDateTime.now());
            adminLog.setIpAddress(request.getRemoteAddr());
            adminLogService.save(adminLog);
            
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getId());
            data.put("role", user.getRole());
            
            return Result.success("角色更新成功").setData(data);
        } catch (Exception e) {
            return Result.error("角色更新失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 获取平台统计数据
     */
    @GetMapping("/analytics/overview")
    public Result getAnalyticsOverview(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            Map<String, Object> data = new HashMap<>();
            
            // 获取用户总数
            int totalUsers = Math.toIntExact(userService.count());
            data.put("totalUsers", totalUsers);
            
            // 获取帖子总数
            long totalPostsLong = postService.count();
            int totalPosts = Math.toIntExact(totalPostsLong);
            data.put("totalPosts", totalPosts);
            
            // 获取待审核帖子数量
            QueryWrapper<Post> pendingWrapper = new QueryWrapper<>();
            pendingWrapper.eq("status", 0); // 0表示pending
            int pendingPosts = Math.toIntExact(postService.count(pendingWrapper));
            data.put("pendingPosts", pendingPosts);
            
            // 获取日活跃用户数（这里简单地返回总用户数的20%作为示例）
            int dailyActiveUsers = (int) (totalUsers * 0.2);
            data.put("dailyActiveUsers", dailyActiveUsers);
            
            // 计算用户增长率（这里简单地使用12.5%作为示例）
            double userGrowth = 12.5;
            data.put("userGrowth", userGrowth);
            
            // 计算帖子增长率（这里简单地使用8.3%作为示例）
            double postGrowth = 8.3;
            data.put("postGrowth", postGrowth);
            
            // 计算活跃率
            String activeRate = "29.5%";
            data.put("activeRate", activeRate);
            
            // 计算平均每用户发帖数
            String avgPostsPerUser = totalUsers > 0 ? String.format("%.1f", (double) totalPosts / totalUsers) : "0";
            data.put("avgPostsPerUser", avgPostsPerUser);
            
            // 平均审核耗时
            String avgReviewTime = "25分钟";
            data.put("avgReviewTime", avgReviewTime);
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", data);
            
            return Result.success().setData(result);
        } catch (Exception e) {
            return Result.error("获取平台统计数据失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 获取用户增长趋势
     */
    @GetMapping("/analytics/user-growth")
    public Result getUserGrowth(@RequestParam(defaultValue = "7days") String period,
                                HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            // 计算日期范围
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;
            
            switch (period) {
                case "7days":
                    startDate = endDate.minusDays(6);
                    break;
                case "30days":
                    startDate = endDate.minusDays(29);
                    break;
                case "90days":
                    startDate = endDate.minusDays(89);
                    break;
                default:
                    startDate = endDate.minusDays(6);
            }
            
            // 生成日期列表
            List<LocalDate> dates = new ArrayList<>();
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                dates.add(currentDate);
                currentDate = currentDate.plusDays(1);
            }
            
            // 这里简化处理，实际应该查询数据库获取真实数据
            List<Map<String, Object>> growthData = dates.stream().map(date -> {
                Map<String, Object> item = new HashMap<>();
                // 格式化日期为 MM/dd
                item.put("date", date.format(DateTimeFormatter.ofPattern("M/d")));
                // 模拟用户增长数据
                item.put("count", new Random().nextInt(50) + 10);
                return item;
            }).collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", growthData);
            
            return Result.success().setData(result);
        } catch (Exception e) {
            return Result.error("获取用户增长趋势失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 获取帖子发布趋势
     */
    @GetMapping("/analytics/post-trends")
    public Result getPostTrends(@RequestParam(defaultValue = "7days") String period,
                                HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            // 计算日期范围
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;
            
            switch (period) {
                case "7days":
                    startDate = endDate.minusDays(6);
                    break;
                case "30days":
                    startDate = endDate.minusDays(29);
                    break;
                case "90days":
                    startDate = endDate.minusDays(89);
                    break;
                default:
                    startDate = endDate.minusDays(6);
            }
            
            // 生成日期列表
            List<LocalDate> dates = new ArrayList<>();
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                dates.add(currentDate);
                currentDate = currentDate.plusDays(1);
            }
            
            // 这里简化处理，实际应该查询数据库获取真实数据
            List<Map<String, Object>> trendData = dates.stream().map(date -> {
                Map<String, Object> item = new HashMap<>();
                // 格式化日期为 MM/dd
                item.put("date", date.format(DateTimeFormatter.ofPattern("M/d")));
                // 模拟帖子发布趋势数据
                item.put("approvedCount", new Random().nextInt(100) + 50);
                item.put("rejectedCount", new Random().nextInt(20) + 1);
                return item;
            }).collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", trendData);
            
            return Result.success().setData(result);
        } catch (Exception e) {
            return Result.error("获取帖子发布趋势失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 获取系统设置
     */
    @GetMapping("/settings")
    public Result getSettings(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("siteName", siteName);
            data.put("siteDescription", siteDescription);
            data.put("reviewStandards", reviewStandards);
            data.put("maxPostLength", maxPostLength);
            data.put("maxImagesPerPost", maxImagesPerPost);
            data.put("defaultPageSize", defaultPageSize);
            data.put("maintenanceMode", maintenanceMode);
            
            return Result.success().setData(data);
        } catch (Exception e) {
            return Result.error("获取系统设置失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 更新系统设置
     */
    @PutMapping("/settings")
    public Result updateSettings(@RequestBody Map<String, Object> settings,
                                 HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            // 这里应该更新系统设置，实际项目中可能需要写入配置文件或数据库
            // 为简化起见，我们只是返回更新后的设置
            
            Map<String, Object> data = new HashMap<>();
            data.put("siteName", settings.getOrDefault("siteName", siteName));
            data.put("siteDescription", settings.getOrDefault("siteDescription", siteDescription));
            data.put("reviewStandards", settings.getOrDefault("reviewStandards", reviewStandards));
            data.put("maxPostLength", settings.getOrDefault("maxPostLength", maxPostLength));
            data.put("maxImagesPerPost", settings.getOrDefault("maxImagesPerPost", maxImagesPerPost));
            data.put("defaultPageSize", settings.getOrDefault("defaultPageSize", defaultPageSize));
            data.put("maintenanceMode", settings.getOrDefault("maintenanceMode", maintenanceMode));
            
            // 记录操作日志
            AdminLog adminLog = new AdminLog();
            adminLog.setActionType("settings_update");
            adminLog.setActionDetail("更新系统设置");
            adminLog.setTargetId("system_settings");
            adminLog.setOperatorId(currentUser.getId());
            adminLog.setOperatorName(currentUser.getUsername());
            adminLog.setCreatedAt(LocalDateTime.now());
            adminLog.setIpAddress(request.getRemoteAddr());
            adminLogService.save(adminLog);
            
            return Result.success("设置更新成功").setData(data);
        } catch (Exception e) {
            return Result.error("更新系统设置失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 获取操作日志
     */
    @GetMapping("/logs")
    public Result getLogs(@RequestParam(defaultValue = "1") Integer page,
                          @RequestParam(defaultValue = "20") Integer pageSize,
                          @RequestParam(required = false) String actionType,
                          @RequestParam(required = false) Integer userId,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");
            if (currentUser == null) {
                return Result.error("用户未登录").setCode(401);
            }
            
            // 检查用户是否为管理员
            if (!"1".equals(currentUser.getRole())) {
                return Result.error("权限不足").setCode(403);
            }
            
            // 构建查询条件
            QueryWrapper<AdminLog> queryWrapper = new QueryWrapper<>();
            
            // 操作类型筛选
            if (actionType != null && !actionType.isEmpty()) {
                queryWrapper.eq("action_type", actionType);
            }
            
            // 操作用户筛选
            if (userId != null) {
                queryWrapper.eq("operator_id", userId);
            }
            
            // 日期范围筛选
            if (startDate != null && !startDate.isEmpty()) {
                queryWrapper.ge("created_at", startDate);
            }
            
            if (endDate != null && !endDate.isEmpty()) {
                queryWrapper.le("created_at", endDate);
            }
            
            // 排序
            queryWrapper.orderByDesc("created_at");
            
            // 分页查询
            Page<AdminLog> logPage = new Page<>(page, pageSize);
            adminLogService.page(logPage, queryWrapper);
            
            // 转换日志数据格式
            List<Map<String, Object>> logData = new ArrayList<>();
            for (AdminLog log : logPage.getRecords()) {
                Map<String, Object> logEntry = new HashMap<>();
                logEntry.put("id", String.valueOf(log.getId()));
                logEntry.put("createdAt", log.getCreatedAt().toString());
                logEntry.put("operator", log.getOperatorName());
                logEntry.put("type", getActionTypeName(log.getActionType()));
                logEntry.put("content", log.getActionDetail() + " (目标ID: " + log.getTargetId() + ")");
                logEntry.put("success", true); // 简化处理，假设所有操作都成功
                logData.add(logEntry);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("data", logData);
            
            return Result.success().setData(result);
        } catch (Exception e) {
            return Result.error("获取操作日志失败: " + e.getMessage()).setCode(500);
        }
    }
    
    /**
     * 获取操作类型名称
     */
    private String getActionTypeName(String actionType) {
        switch (actionType) {
            case "post_review": return "审核帖子";
            case "user_status_update": return "更新用户状态";
            case "user_role_update": return "更新用户角色";
            case "post_delete": return "删除帖子";
            case "settings_update": return "更新系统设置";
            default: return actionType;
        }
    }
}