package org.example.angelbacked.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private String role;

    private String content;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime timestamp;

    private Integer sequence;

    private String metadata;
}