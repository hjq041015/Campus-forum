package org.example.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.dto.ImageStore;

@Mapper
public interface ImageStoreMapper extends BaseMapper<ImageStore> {
}
