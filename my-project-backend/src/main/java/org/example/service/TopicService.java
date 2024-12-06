package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.Interact;
import org.example.entity.dto.Topic;
import org.example.entity.dto.TopicType;
import org.example.entity.vo.request.AddCommentVO;
import org.example.entity.vo.request.CreateTopicVo;
import org.example.entity.vo.request.TopicUpdateVO;
import org.example.entity.vo.response.CommentVO;
import org.example.entity.vo.response.TopicDetailVo;
import org.example.entity.vo.response.TopicPreviewVO;
import org.example.entity.vo.response.TopicTopVO;

import java.util.List;

public interface TopicService extends IService<Topic> {
    List<TopicType> listTypes();
    String createTopic(int uid, CreateTopicVo vo);
    List<TopicPreviewVO> listTopic(int page  ,int type);
    List<TopicTopVO> topTopic();
    TopicDetailVo topic(int tid,int uid);
    void interact(Interact interact , Boolean state);
    List<TopicPreviewVO> listTopicCollection(int uid);
    String updateTopic(int uid, TopicUpdateVO vo);
    String addComment(int uid, AddCommentVO vo);
    List<CommentVO> comments(int tid, int pageNumber);
    void deleteComment(int id, int uid);
}
