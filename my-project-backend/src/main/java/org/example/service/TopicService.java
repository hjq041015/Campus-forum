package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.Topic;
import org.example.entity.dto.TopicType;
import org.example.entity.vo.request.CreateTopicVo;

import java.util.List;

public interface TopicService extends IService<Topic> {
    public List<TopicType> listTypes();
    public String createTopic(int uid, CreateTopicVo vo);
}
