package org.example.entity.vo.response;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class TopicPreviewVO {
    int id;
    int type;
    String title;
    String text;
    Date time;
    Integer uid;
    List<String> image;
    String username;
    String avatar;
}
