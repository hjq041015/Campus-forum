package org.example.Util;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class FlowUtil {
    @Resource
    StringRedisTemplate template;
    // 当overclock的值为true是表示超过限制频率,则返回false
    private static final LimitAction defaultAction = overclock -> !overclock;

    /**
     * 针对于单次频率限制，请求成功后，在冷却时间内不得再次进行请求，如3秒内不能再次发起请求
     * @param key 储存在Redis中用于计数的名称
     * @param blockTime 限制时间
     * @return 是否通过限流检查
     */
    public boolean limitOnceCheck(String key , int blockTime) {
        return this.internalCheck(key,1,blockTime,defaultAction);
    }

     /**
     * 针对于单次频率限制，请求成功后，在冷却时间内不得再次进行请求
     * 如3秒内不能再次发起请求，如果不听劝阻继续发起请求，将限制更长时间
     * @param key 键
     * @param frequency 请求频率
     * @param baseTime 基础限制时间
     * @param upgradeTime 升级限制时间
     * @return 是否通过限流检查
     */
     public boolean limitOnceUpdateCheck(String key, int frequency, int baseTime, int upgradeTime) {
         return this.internalCheck(key,frequency,baseTime,overclock -> {
             if (overclock) {
                 template.opsForValue().set(key,"1",upgradeTime,TimeUnit.SECONDS);
             }
              return false;
         });
     }

      /**
     * 针对于在时间段内多次请求限制，如3秒内限制请求20次，超出频率则封禁一段时间
     * @param counterKey 计数键
     * @param blockKey 封禁键
     * @param blockTime 封禁时间
     * @param frequency 请求频率
     * @param period 计数周期
     * @return 是否通过限流检查
     */
      public boolean limitPeriodCheck(String counterKey, String blockKey, int blockTime, int frequency, int period) {
        return this.internalCheck(counterKey,frequency,period,overclock -> {
            if (overclock) {
                template.opsForValue().set(blockKey,"", blockTime,TimeUnit.SECONDS);
            }
            return !overclock;
        });
      }



      /**
     * 针对于在时间段内多次请求限制，如3秒内20次请求
     * @param counterKey 计数键
     * @param frequency 请求频率
     * @param period 计数周期
     * @return 是否通过限流检查
     */
      public boolean limitPeriodCounterCheck(String counterKey, int frequency, int period) {
          return this.internalCheck(counterKey,frequency,period,defaultAction);
      }



    /**
     * 内部使用请求限制主要逻辑
     * @param key        储存在Redis中用于计数的名称
     * @param frequency  请求的频率
     * @param period     限制的时间(在多少分钟内只能请求几次)
     * @param action     自定义的限制行为
     * @return           是否通过限流检查
     */
    private boolean internalCheck(String key, int frequency, int period, LimitAction action) {
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            Long value = Optional.ofNullable(template.opsForValue().increment(key)).orElse(0L);
            return action.run(value > frequency);
        }else {
            template.opsForValue().set("key","1",period,TimeUnit.SECONDS);
            return true;
        }
    }


    /**
     * 内部使用,提供可自定义的限制行为
     */
    private interface LimitAction {
        boolean run(boolean overclock);
    }
}
