package org.example.entity.vo.response;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

@Data
public class WeatherVO {
    // 所在城市
    JSONObject location;
    // 当前天气情况
    JSONObject now;
    // 每小时天气情况
    JSONArray hourly;
}
