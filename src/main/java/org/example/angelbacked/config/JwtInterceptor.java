package org.example.angelbacked.config;

import org.example.angelbacked.entity.User;
import org.example.angelbacked.service.UserService;
import org.example.angelbacked.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的token
        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        System.out.println("Received request to: " + request.getRequestURI());
        System.out.println("Authorization header: " + requestTokenHeader);
        System.out.println("Request method: " + request.getMethod());

        // JWT Token的格式为 "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            System.out.println("Extracted token: " + jwtToken);
            if (!jwtToken.isEmpty()) {
                try {
                    username = jwtUtil.getUsernameFromToken(jwtToken);
                    System.out.println("从token中提取用户名: " + username);
                } catch (Exception e) {
                    System.out.println("无法获取token中的用户名: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        // 验证token
        if (username != null && !username.isEmpty()) {
            // 查询用户信息并存储在请求属性中
            User user = userService.getByUsername(username);
            if (user != null) {
                System.out.println("找到用户: " + user.getUsername());
                request.setAttribute("currentUser", user);
                return true;
            } else {
                System.out.println("未找到用户: " + username);
            }
        } else {
            System.out.println("用户名为空，token可能无效");
        }

        // 检查是否是排除的路径
        String requestURI = request.getRequestURI();
        // 对于/api/community/posts的GET请求，允许公开访问
        if ((requestURI.equals("/api/community/posts") && "GET".equalsIgnoreCase(request.getMethod())) ||
            requestURI.startsWith("/api/auth/login") || 
            requestURI.startsWith("/api/auth/register") || 
            requestURI.startsWith("/api/auth/logout") ||
            requestURI.startsWith("/api/community/users/recommended") ||
            requestURI.startsWith("/api/community/topics/hot")) {
            System.out.println("请求路径在排除列表中，允许访问");
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("{\"code\": 1, \"message\": \"用户未登录\"}");
        return false;
    }
}