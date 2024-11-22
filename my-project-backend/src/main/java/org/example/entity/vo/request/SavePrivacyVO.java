package org.example.entity.vo.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SavePrivacyVO {
    @Pattern(regexp = "(phone|qq|wx|email|gender)")
    String type;
    boolean status;
}
