package org.example.entity.vo.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
public class ConfirmResetVO {

    String email;

    @Length(max = 6,min = 6)
    String code;

}
