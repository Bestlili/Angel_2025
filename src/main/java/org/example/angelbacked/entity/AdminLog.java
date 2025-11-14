package org.example.angelbacked.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_log")
public class AdminLog {
    @TableId(type = IdType.AUTO)
    private Integer id;

    @TableField("action_type")
    private String actionType;

    @TableField("action_detail")
    private String actionDetail;

    @TableField("target_id")
    private String targetId;

    @TableField("operator_id")
    private Integer operatorId;

    @TableField("operator_name")
    private String operatorName;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("ip_address")
    private String ipAddress;
}