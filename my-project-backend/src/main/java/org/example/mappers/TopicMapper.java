package org.example.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.dto.Topic;

import java.util.List;


@Mapper
public interface TopicMapper extends BaseMapper<Topic> {

}