package org.example.angelbacked.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.angelbacked.entity.Topic;
import org.example.angelbacked.mapper.TopicMapper;
import org.example.angelbacked.service.TopicService;
import org.example.angelbacked.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService {
    
    @Autowired
    private TopicMapper topicMapper;
    
    @Override
    public Result getHotTopics() {
        List<Topic> topics = topicMapper.selectHotTopics();
        
        Map<String, Object> result = new HashMap<>();
        result.put("topics", topics);
        
        return Result.success().setData(result);
    }
}