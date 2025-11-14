package org.example.angelbacked.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("post_like")
public class PostLike {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer postId;

    private Integer userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}