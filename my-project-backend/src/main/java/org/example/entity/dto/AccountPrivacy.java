package org.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.example.entity.BaseData;


@Data
@TableName("db_account_privacy")
public class AccountPrivacy implements BaseData {
 @TableId(type = IdType.AUTO)
    final Integer id;
    boolean phone = true;
    boolean qq = true;
    boolean wx = true;
    boolean email = true;
    boolean gender = true;

}
