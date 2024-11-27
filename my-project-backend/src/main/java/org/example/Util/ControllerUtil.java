package org.example.Util;

import org.example.entity.RestBean;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
public class ControllerUtil {
    public  <T> RestBean<T> messageHander(Supplier<String> action) {
        String message = action.get();
        if (message == null) {
            return RestBean.success();
        }else {
            return RestBean.failure(400,message);
        }
    }
}
