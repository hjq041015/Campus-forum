package org.example.server;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.Account;
import org.example.entity.vo.request.ConfirmResetVO;
import org.example.entity.vo.request.EmailRegistVO;
import org.example.entity.vo.request.EmailResetVO;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public interface AccountServer extends IService<Account> , UserDetailsService {
    Account findAccountByUsernameOrEmail(String text);
    Account findAccountById(String id);
    String registEmailVerifyCode(String type, String email, String ip);
    String registEmailAccount(EmailRegistVO emailRegistVO);
    String resetConfirm(ConfirmResetVO confirmResetVO);
    String resetEmailAccountPassword(EmailResetVO emailResetVO);
}
