package org.example.service.serviceImpl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.example.Util.CacheUtils;
import org.example.Util.Const;
import org.example.Util.FlowUtil;
import org.example.entity.dto.Topic;
import org.example.entity.dto.TopicType;
import org.example.entity.vo.request.CreateTopicVo;
import org.example.entity.vo.response.TopicPreviewVO;
import org.example.entity.vo.response.TopicTopVO;
import org.example.mappers.TopicMapper;
import org.example.mappers.TopicTypeMapper;
import org.example.service.TopicService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService  {

    @Resource
    TopicTypeMapper topicTypeMapper;

    @Resource
    FlowUtil flowUtil;

    @Resource
    CacheUtils cacheUtils;

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
       if (!this.textLimitCheck(vo.getContent()))
           return "内容过多超过限制,无法发表";
       if (!types.contains(vo.getType()))
           return "发表的种类不合法";
       String key = Const.FORUM_CREATE_COUNTER + uid;
       if (!flowUtil.limitPeriodCounterCheck(key,5,3600))
           return "发送论坛过于频繁,请稍后再试";
        Topic topic = new Topic();
        BeanUtils.copyProperties(vo,topic);
        topic.setContent(vo.getContent().toJSONString());
        topic.setUid(uid);
        topic.setTime(new Date());
        if (this.save(topic)) {
            cacheUtils.deleteCachePattern(Const.FORUM_TOPIC_PREVIEW_CACHE + "*");
            return null;
        }else {
            return "内部错误,请联系管理员";
        }
    }

    @Override
    public List<TopicPreviewVO> listTopic(int page, int type) {
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE + page + type;
        List<TopicPreviewVO>  list = cacheUtils.takeListFromCache(key,TopicPreviewVO.class);
        if (list != null) return list;
        List<Topic> topics;
        if (type == 0) {
            topics = baseMapper.getTopics(page *10);
        }else {
            topics = baseMapper.getTopicsByType(page * 10, type );
        }
        if (topics.isEmpty()) return null;
        list = topics.stream().map(this::resolveToPreviewVo).toList();
        cacheUtils.saveListToCache(key,list,60);
        return list;
    }

    @Override
    public List<TopicTopVO> topTopic() {
        List<Topic> topics = baseMapper.selectList(Wrappers.<Topic>query()
                .select("id","title","time")
                .eq("top",1));
        return topics.stream().map(topic -> {
            TopicTopVO vo = new TopicTopVO();
            BeanUtils.copyProperties(topic,vo);
            return vo;
        }).toList();
    }


    // 此方法将传入的Topic对象转换成TopicPreviewVo对象
    private TopicPreviewVO resolveToPreviewVo(Topic topic){
        TopicPreviewVO vo = new TopicPreviewVO();
        BeanUtils.copyProperties(topic,vo);
        List<String> images = new ArrayList<>();
        StringBuilder previewText = new StringBuilder();
        JSONArray ops = JSONObject.parseObject(topic.getContent()).getJSONArray("ops");
        for (Object op: ops) {
            Object insert = JSONObject.from(op).get("insert");
            if (insert instanceof String text) {
                if (previewText.length() >= 300) continue;
                previewText.append(text);
            }
            if (insert instanceof Map<?,?> map) {
                Optional.ofNullable(map.get("image")).ifPresent(obj -> {
                    images.add(obj.toString());
                });
            }
        }
        vo.setText(previewText.length() > 300 ? previewText.substring(0,300) : previewText.toString() );
        vo.setImage(images);
        return vo;
    }

    private boolean textLimitCheck(JSONObject object) {
        if (object == null) return false;
        long length = 0;
        for (Object ops : object.getJSONArray("ops")) {
            length += JSONObject.from(ops).getString("insert").length();
            if (length > 20000) return false;
        }
        return true;
    }


}



