package org.example.angelbacked.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment_like")
public class CommentLike {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer commentId;

    private Integer userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}