package org.example.entity.vo.response;

import lombok.Data;

import java.util.Date;

@Data
public class TopicDetailVo {
    Integer id;
    String title;
    Integer type;
    String content;
    Date time;
    User user;

    @Data
    public static class User{
        Integer id;
        String username;
        String avatar;
        String desc;
        Integer gender;
        String qq;
        String wx;
        String phone;
        String email;
    }

}

