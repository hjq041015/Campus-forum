package org.example.entity.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class EmailRegistVO {
    @Email
    String email;

    @Length(max = 6,min = 6)
    String code;

    @Length(min = 6,max = 20)
    String password;

    @Pattern(regexp = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$")
    @Length(min = 1, max = 10)
    String username;
}
