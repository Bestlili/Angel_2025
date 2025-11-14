package org.example.angelbacked.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post")
public class Post {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private Integer likes = 0;

    private Integer commentsCount = 0;

    private String images; // 存储图片URL数组的JSON字符串

    private String tags; // 存储标签数组的JSON字符串

    // 管理员功能扩展字段
    @TableField("status")
    private Integer status; // pending/approved/rejected

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("reviewer_id")
    private Integer reviewerId;

    @TableField("review_time")
    private LocalDateTime reviewTime;

    // 不存储到数据库的字段，用于传输当前用户是否点赞/收藏
    @TableField(exist = false)
    private Boolean isLiked = false;

    @TableField(exist = false)
    private Boolean isSaved = false;

    // 关联的作者信息
    @TableField(exist = false)
    private User author;
    
    // 添加username字段，便于前端直接访问
    @TableField(exist = false)
    private String username;
}