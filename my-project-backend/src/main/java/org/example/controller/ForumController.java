package org.example.controller;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.example.Util.ControllerUtil;
import org.example.entity.RestBean;
import org.example.entity.vo.request.CreateTopicVo;
import org.example.entity.vo.response.*;
import org.example.service.TopicService;
import org.example.service.WeatherService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forum")
public class ForumController {
    
    @Resource
    WeatherService weatherService;

    @Resource
    TopicService topicService;

    @Resource
    ControllerUtil util;
    
    @GetMapping("/weather")
    public RestBean<WeatherVO> weather(Double longitude, Double latitude) {
        // 检查参数是否为null
        if (longitude == null || latitude == null) {
            return RestBean.failure(400, "缺少必要的经纬度参数");
        }

        WeatherVO vo = weatherService.fetchWeather(longitude, latitude);

        if (vo == null) {
            System.out.println("无法获取天气信息，longitude: " + longitude + ", latitude: " + latitude);
        }

        return vo == null ? RestBean.failure(400, "获取地理位置与天气信息失败,请联系管理员") :
                RestBean.success(vo);
    }

    @GetMapping("/types")
    public RestBean<List<TopicTypeVO>> listTypes() {
        return RestBean.success(topicService.listTypes().stream()
                .map(types -> types.asViewObject(TopicTypeVO.class)).toList());
    }

    @PostMapping("/create-topic")
    public RestBean<Void> createTopic(@RequestBody @Valid CreateTopicVo vo,
                                      @RequestAttribute("id") int id) {
        return util.messageHander(() -> topicService.createTopic(id, vo));
    }

    @GetMapping("/list-topic")
    public RestBean<List<TopicPreviewVO>> listTopic(@RequestParam  @Min(0) int page,
                                                    @RequestParam  @Min(0)  int type) {
        return RestBean.success(topicService.listTopic(page +1, type));
    }

    @GetMapping("/top-topic")
    public RestBean<List<TopicTopVO>> topTopic() {
        return RestBean.success(topicService.topTopic());
    }

    @GetMapping("/topic")
    public RestBean<TopicDetailVo> topic(@RequestParam @Min(0) int tid) {
        return RestBean.success(topicService.topic(tid));
    }
}
