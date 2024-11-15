package org.example.server;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.Account;
import org.example.entity.vo.request.EmailRegistVO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public interface AccountServer extends IService<Account> , UserDetailsService {
    Account findAccountByUsernameOrEmail(String text);
    String registEmailVerifyCode(String type, String email, String ip);
    String registEmailAccount(EmailRegistVO emailRegistVO);
}
