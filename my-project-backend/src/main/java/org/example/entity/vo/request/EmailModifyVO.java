package org.example.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmailModifyVO {
    @Email
    String email;

    @Length(max = 6,min = 6)
    String code;
}
