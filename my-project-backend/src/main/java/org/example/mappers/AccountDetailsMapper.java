package org.example.mappers;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.entity.dto.AccountDetails;
@Mapper
public interface AccountDetailsMapper extends BaseMapper<AccountDetails> {
}
