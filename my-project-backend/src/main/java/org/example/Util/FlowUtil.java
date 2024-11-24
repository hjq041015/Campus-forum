package org.example.Util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class FlowUtil {
    @Resource
    StringRedisTemplate template;

    public boolean limitOnceCLick(String key , int blockTime) {
        if(Boolean.TRUE.equals(template.hasKey(key))) {
            return false;
        }else {
            template.opsForValue().set(key,"",blockTime, TimeUnit.SECONDS);
            return true;
        }
    }
}
