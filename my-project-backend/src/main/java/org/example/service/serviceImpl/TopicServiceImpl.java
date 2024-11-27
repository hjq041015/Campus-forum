package org.example.service.serviceImpl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.example.Util.Const;
import org.example.Util.FlowUtil;
import org.example.entity.dto.Topic;
import org.example.entity.dto.TopicType;
import org.example.entity.vo.request.CreateTopicVo;
import org.example.mappers.TopicMapper;
import org.example.mappers.TopicTypeMapper;
import org.example.service.TopicService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService  {

    @Resource
    TopicTypeMapper topicTypeMapper;

    @Resource
    FlowUtil util;

    private static Set<Integer> types = null;

    // 初始化时自动执行,将所有Type的id放在一起用于检测Type是否合法
    @PostConstruct
    private void initTypes() {
        types = this.listTypes()
                .stream()
                .map(TopicType::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public List<TopicType> listTypes() {
        return topicTypeMapper.selectList(null);
    }

    @Override
    public String createTopic(int uid, CreateTopicVo vo) {
       if (!this.textLimitCheck(vo.getContent(),20000))
           return "内容过多超过限制,无法发表";
       if (!types.contains(vo.getType()))
           return "发表的种类不合法";
       String key = Const.FORUM_CREATE_COUNTER + uid;
       if (!util.limitPeriodCounterCheck(key,5,3600))
           return "发送论坛过于频繁,请稍后再试";
       Topic topic = new Topic();
        BeanUtils.copyProperties(vo,topic);
        topic.setContent(vo.getContent().toJSONString());
        topic.setUid(uid);
        topic.setTime(new Date());
        if (this.save(topic)) {
            return null;
        }else {
            return "内部错误,请联系管理员";
        }
    }


    private boolean textLimitCheck(JSONObject object, int max) {
        if (object == null) return false;
        long length = 0;
        for (Object ops : object.getJSONArray("ops")) {
            length += JSONObject.from(ops).getString("insert").length();
            if (length > max) return false;
        }
        return true;
    }


}



