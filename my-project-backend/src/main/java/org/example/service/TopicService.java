package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.Interact;
import org.example.entity.dto.Topic;
import org.example.entity.dto.TopicType;
import org.example.entity.vo.request.CreateTopicVo;
import org.example.entity.vo.response.TopicDetailVo;
import org.example.entity.vo.response.TopicPreviewVO;
import org.example.entity.vo.response.TopicTopVO;

import java.util.List;

public interface TopicService extends IService<Topic> {
    public List<TopicType> listTypes();
    public String createTopic(int uid, CreateTopicVo vo);
    public List<TopicPreviewVO> listTopic(int page  ,int type);
    public List<TopicTopVO> topTopic();
    public TopicDetailVo topic(int tid,int uid);
    public void interact(Interact interact , Boolean state);
    public List<TopicPreviewVO> listTopicCollection(int uid);
}
