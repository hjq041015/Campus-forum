package org.example.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.dto.TopicComment;

@Mapper
public interface TopicCommentMapper extends BaseMapper<TopicComment> {
}
