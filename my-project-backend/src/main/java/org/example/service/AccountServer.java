package org.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.entity.dto.Account;
import org.example.entity.vo.request.*;
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
    String modifyEmail(EmailModifyVO vo,int id);
    String changePassword(int id, ChangePasswordVO vo);
}
