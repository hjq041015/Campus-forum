package org.example.service.serviceImpl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.example.Util.CacheUtils;
import org.example.Util.Const;
import org.example.Util.FlowUtil;
import org.example.entity.dto.*;
import org.example.entity.vo.request.AddCommentVO;
import org.example.entity.vo.request.CreateTopicVo;
import org.example.entity.vo.request.TopicUpdateVO;
import org.example.entity.vo.response.CommentVO;
import org.example.entity.vo.response.TopicDetailVo;
import org.example.entity.vo.response.TopicPreviewVO;
import org.example.entity.vo.response.TopicTopVO;
import org.example.mappers.*;
import org.example.service.NotificationService;
import org.example.service.TopicService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@Service
public class TopicServiceImpl extends ServiceImpl<TopicMapper, Topic> implements TopicService  {

    @Resource
    TopicTypeMapper topicTypeMapper;

    @Resource

    FlowUtil flowUtil;

    @Resource
    CacheUtils cacheUtils;

    @Resource
    AccountMapper accountMapper;

    @Resource
    AccountDetailsMapper accountDetailsMapper;

    @Resource
    AccountPrivacyMapper accountPrivacyMapper;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    TopicCommentMapper topicCommentMapper;

    @Resource
    NotificationService notificationService;
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
    public List<TopicPreviewVO> listTopic(int pageNumber, int type) {
        String key = Const.FORUM_TOPIC_PREVIEW_CACHE + pageNumber + type;
        List<TopicPreviewVO>  list = cacheUtils.takeListFromCache(key,TopicPreviewVO.class);
        if (list != null) return list;
        Page<Topic> page = Page.of(pageNumber,10);
        if (type == 0) {
            baseMapper.selectPage(page,Wrappers.<Topic>query().orderByDesc("time"));
        }else {
            baseMapper.selectPage(page,Wrappers.<Topic>query().eq("type",type).orderByDesc("time"));
        }
        List<Topic> topics = page.getRecords();
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

    @Override
    public TopicDetailVo topic(int tid,int uid) {
        TopicDetailVo vo = new TopicDetailVo();
        Topic topic = baseMapper.selectById(tid);
        BeanUtils.copyProperties(topic,vo);
        TopicDetailVo.Interact interact = new TopicDetailVo.Interact(
                hasInteract(uid,tid,"like"),
                hasInteract(uid,tid,"collect")
        );
        vo.setInteract(interact);
        TopicDetailVo.User user = new TopicDetailVo.User();
        vo.setUser(this.FillUserDetailByPrivacy(user,topic.getUid()));
        vo.setComments(topicCommentMapper.selectCount(Wrappers.<TopicComment>query().eq("tid",tid)));
        return vo;
    }

    private boolean hasInteract(int uid,int tid,String type) {
        String key = tid + ":" + uid;
        if (stringRedisTemplate.opsForHash().hasKey(type,key)){
            return Boolean.parseBoolean(stringRedisTemplate.opsForHash().entries(type).get(key).toString());
        }
        // 大于0说明数据库中有数据
        return baseMapper.userInteractCount(tid,type,uid) > 0;
    }
     @Override
        public List<TopicPreviewVO> listTopicCollection(int uid) {
             return baseMapper.collectionTopic(uid)
                    .stream()
                    .map( topic -> {
                        TopicPreviewVO vo = new TopicPreviewVO();
                        BeanUtils.copyProperties(topic,vo);
                        return vo;
                    }
            ).toList();
        }

    @Override
    public String updateTopic(int uid, TopicUpdateVO vo) {
        if(!textLimitCheck(vo.getContent(),20000))
            return "文章内容太多，发文失败！";
        if(!types.contains(vo.getType()))
            return "文章类型非法！";
        baseMapper.update(null, Wrappers.<Topic>update()
                .eq("uid", uid)
                .eq("id", vo.getId())
                .set("title", vo.getTitle())
                .set("content", vo.getContent().toString())
                .set("type", vo.getType())
        );
        return null;
    }

    @Override
    public String addComment(int uid, AddCommentVO vo) {
        String key = Const.FORUM_TOPIC_COMMENT;
        if (!this.textLimitCheck(JSONObject.parseObject(vo.getContent()),2000))
           return "评论过多超过限制,无法发表";
        if(!flowUtil.limitPeriodCounterCheck(key,5,3600))
            return "评论过于频繁,无法发表";
        TopicComment comment = new TopicComment();
        BeanUtils.copyProperties(vo,comment);
        comment.setUid(uid);
        comment.setTime(new Date());
        topicCommentMapper.insert(comment);
        Topic topic = baseMapper.selectById(vo.getTid());
        Account account = accountMapper.selectById(uid);
        if (vo.getQuote() > 0) {
            // 被回复的那个评论
            TopicComment com = topicCommentMapper.selectById(vo.getQuote());
            if (!Objects.equals(com.getUid(), account.getId())) {
                notificationService.addNotification(com.getUid(),"您有新的帖子评论回复",
                        account.getUsername()+" 回复了你发表的评论，快去看看吧！",
                        "success","/index/topic-detail/"+com.getTid());
            }
        }else if (!Objects.equals(topic.getUid(), account.getId())) {
            notificationService.addNotification(topic.getUid(),"您有新的帖子评论回复",
                        account.getUsername()+" 回复了你发表的帖子，快去看看吧！",
                        "success","/index/topic-detail/"+topic.getId());
        }
        return null;
    }

    @Override
    public List<CommentVO> comments(int tid, int pageNumber) {
        Page<TopicComment> page = Page.of(pageNumber,10);
        topicCommentMapper.selectPage(page,Wrappers.<TopicComment>query().eq("tid",tid));
        return page.getRecords().stream().map(dto -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(dto,vo);
            if (dto.getQuote() > 0) {
                TopicComment comment = topicCommentMapper.selectOne(Wrappers.<TopicComment>query().eq("id", dto.getQuote()).orderByAsc("time"));
                if (comment != null) {
                    JSONObject object = JSONObject.parseObject(comment.getContent());
                    StringBuilder builder = new StringBuilder();
                    this.shortContent(object.getJSONArray("ops"),builder,ignore ->{});
                    vo.setQuote(builder.toString());
                }else {
                    vo.setQuote("此评论已被删除");
                }
            }
            CommentVO.User user = new CommentVO.User();
            this.FillUserDetailByPrivacy(user,dto.getUid());
            vo.setUser(user);
            return vo;
        }).toList();
    }


    @Override
    public void deleteComment(int id, int uid) {
        topicCommentMapper.delete(Wrappers.<TopicComment>query().eq("id",id).eq("uid",uid));
    }

    @Override
    public void interact(Interact interact, Boolean state) {
        String type = interact.getType();
        synchronized (type.intern()) {
             stringRedisTemplate.opsForHash().put(type,interact.toKey(),Boolean.toString(state));
             saveInteractSchedule(type);
        }

    }

    // 确保在一段时间后（3秒）只执行一次保存操作,避免数据库冲突
    private final Map<String ,Boolean> state = new HashMap<>();
    ScheduledExecutorService service = Executors.newScheduledThreadPool(2);
    private void saveInteractSchedule(String type) {
        if(!state.getOrDefault(type, false)) {
            state.put(type, true);
            service.schedule(() -> {
                saveInteract(type);
                state.put(type, false);
            }, 3, TimeUnit.SECONDS);
        }
    }

    // 根据Type以及state的不同,完成点赞取消点赞,收藏取消收藏功能
    private void saveInteract(String type) {
        synchronized (type.intern()) {
        List<Interact> check = new LinkedList<>();
        List<Interact> uncheck = new LinkedList<>();
        stringRedisTemplate.opsForHash().entries(type).forEach( (k, v) -> {
          if( Boolean.parseBoolean(v.toString()) )  {
              check.add(Interact.parseInteract(k.toString(),type));
          }else {
              uncheck.add(Interact.parseInteract(k.toString(),type));
          }
        });
        if (!check.isEmpty())
            baseMapper.addInteract(check,type);
        if (!uncheck.isEmpty())
            baseMapper.deleteInteract(uncheck,type);
        stringRedisTemplate.delete(type);
        }
    }


    private <T> T FillUserDetailByPrivacy(T targert, int uid) {
        Account account = accountMapper.selectById(uid);
        AccountDetails details = accountDetailsMapper.selectById(uid);
        AccountPrivacy privacy = accountPrivacyMapper.selectById(uid);
        String[] ignore = privacy.hiddenFields();
        BeanUtils.copyProperties(account,targert,ignore);
        BeanUtils.copyProperties(details,targert,ignore);
        return  targert;
    }



    // 此方法将传入的Topic对象转换成TopicPreviewVo对象
    private TopicPreviewVO resolveToPreviewVo(Topic topic){
        TopicPreviewVO vo = new TopicPreviewVO();
        BeanUtils.copyProperties(accountMapper.selectById(topic.getUid()),vo);
        BeanUtils.copyProperties(topic,vo);
        vo.setLike(baseMapper.interactCount(topic.getId(),"like"));
        vo.setCollect(baseMapper.interactCount(topic.getId(),"collect"));
        List<String> images = new ArrayList<>();
        StringBuilder previewText = new StringBuilder();
        JSONArray ops = JSONObject.parseObject(topic.getContent()).getJSONArray("ops");
        this.shortContent(ops,previewText, obj-> images.add(obj.toString()));
        vo.setText(previewText.length() > 300 ? previewText.substring(0,300) : previewText.toString() );
        vo.setImage(images);
        return vo;
    }

    private void shortContent(JSONArray ops, StringBuilder previewText, Consumer<Object>ImageHander){
        for (Object op: ops) {
            Object insert = JSONObject.from(op).get("insert");
            if (insert instanceof String text) {
                if (previewText.length() >= 300) continue;
                previewText.append(text);
            }
            if (insert instanceof Map<?,?> map) {
                Optional.ofNullable(map.get("image")).ifPresent(ImageHander);
            }
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



