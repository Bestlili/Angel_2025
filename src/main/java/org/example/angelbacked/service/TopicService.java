package org.example.angelbacked.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.angelbacked.entity.Topic;
import org.example.angelbacked.util.Result;

public interface TopicService extends IService<Topic> {
    
    Result getHotTopics();
}