package org.example.entity.vo.request;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmailResetVO {
    String email;

    @Length(min = 6,max = 6)
    String code;

    @Length(min = 6,max = 20)
    String password;
}
