package org.example.angelbacked.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("topic")
public class Topic {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private Integer participantCount = 0;

    private Boolean isHot = false;
}