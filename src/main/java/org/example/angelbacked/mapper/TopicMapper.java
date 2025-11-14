package org.example.angelbacked.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.example.angelbacked.entity.Topic;

import java.util.List;

public interface TopicMapper extends BaseMapper<Topic> {
    
    @Select("SELECT * FROM topic WHERE is_hot = true ORDER BY participant_count DESC LIMIT 10")
    List<Topic> selectHotTopics();
}