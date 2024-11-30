package org.example.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.dto.Topic;

import java.util.List;


@Mapper
public interface TopicMapper extends BaseMapper<Topic> {
    @Select("""
        select * from db_topic left join db_account on uid = db_account.id
        order by `time` desc limit ${start}, 10
""")
    List<Topic> getTopics(int start);

    @Select("""
    select * from db_topic left join db_account on uid = db_account.id
   where type= #{type}
   order by `time` desc limit ${start}, 10
 """)
    List<Topic> getTopicsByType(int start, int type);
}
