package org.example.entity.vo.request;

import lombok.Data;

@Data
public class AddCommentVO {
    Integer id;
    int tid;
    String content;
    int quote;
}
