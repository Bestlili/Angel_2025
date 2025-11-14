package org.example.angelbacked.config;

import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 将原来的 config.addAllowedOrigin("*") 替换为具体的源或使用 allowedOriginPatterns
        config.addAllowedOriginPattern("*"); // 使用 allowedOriginPatterns 允许所有域名（更安全的方式）
        config.addAllowedHeader("*"); // 允许所有请求头
        config.addAllowedMethod("*");  // 允许所有HTTP方法
        config.setAllowCredentials(true); // 允许携带Cookie

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 对所有接口生效
        return new CorsFilter(source);
    }
}