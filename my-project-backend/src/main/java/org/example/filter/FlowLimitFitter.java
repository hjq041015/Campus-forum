package org.example.filter;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.Util.Const;
import org.example.entity.RestBean;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Order(Const.FLOW_ORDER)
public class FlowLimitFitter extends HttpFilter {

    @Resource
    StringRedisTemplate template;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
       if (this.tryCount(request.getRemoteAddr())) {
           chain.doFilter(request,response);
       }else {
           this.writeBlockMessage(response);
       }
    }


    private boolean tryCount(String ip){
        synchronized (ip.intern()) {
            if (Boolean.TRUE.equals(template.hasKey(Const.FLOW_LIMIT_BLOCK + ip))) {
            return false;
        }
        return this.limitPeriodCheck(ip);
        }

    }

    // 对接收的ip地址进行请求计数
    private boolean limitPeriodCheck(String ip) {
    String countKey = Const.FLOW_LIMIT_COUNT + ip;

    // 此ip已经记过数了.每次请求进来计数+1
    if (Boolean.TRUE.equals(template.hasKey(countKey))) {
        long increment = Optional.ofNullable(template.opsForValue().increment(countKey)).orElse(0L);

        // 当计数大于10时,将此ip放入封禁名单,封禁30分钟
        if (increment > 30) {
            template.opsForValue().set(Const.FLOW_LIMIT_BLOCK + ip, "", 30, TimeUnit.SECONDS);
            return false;
        }
    } else {
        // 此ip从未记过数,将此ip放入redis中进行计数,初始值设置为0
        template.opsForValue().set(countKey, "0", 3, TimeUnit.MINUTES);
    }
    return true;
}



    // 当ip已经被封禁的时候调用
    private void writeBlockMessage(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(RestBean.forbidden("请求频繁,请稍微再试").asJsonString());
    }
}
