package org.example.controller;

import jakarta.annotation.Resource;
import org.example.entity.RestBean;
import org.example.entity.vo.response.WeatherVO;
import org.example.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/forum")
public class ForumController {
    
    @Resource
    WeatherService service;
    
    @GetMapping("/weather")
    public RestBean<WeatherVO> weather(Double longitude, Double latitude) {
        // 检查参数是否为null
        if (longitude == null || latitude == null) {
            return RestBean.failure(400, "缺少必要的经纬度参数");
        }

        WeatherVO vo = service.fetchWeather(longitude, latitude);

        if (vo == null) {
            System.out.println("无法获取天气信息，longitude: " + longitude + ", latitude: " + latitude);
        }

        return vo == null ? RestBean.failure(400, "获取地理位置与天气信息失败,请联系管理员") :
                RestBean.success(vo);
    }


}
