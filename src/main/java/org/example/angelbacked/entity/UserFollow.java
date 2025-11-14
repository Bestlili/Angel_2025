package org.example.angelbacked.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_follow")
public class UserFollow {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer followerId; // 关注者ID

    private Integer followingId; // 被关注者ID

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}