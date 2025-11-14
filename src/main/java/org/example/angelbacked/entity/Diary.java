package org.example.angelbacked.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@TableName("diary")
public class Diary {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String content;

    private Integer moodId;

    private String moodName;

    private String moodIcon;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    private Boolean isDraft = false;

    private String tags;

    @TableField("date")
    private LocalDate date;

}