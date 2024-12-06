package org.example.service.serviceImpl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.Util.Const;
import org.example.Util.FlowUtil;
import org.example.entity.dto.Account;
import org.example.entity.dto.AccountDetails;
import org.example.entity.dto.AccountPrivacy;
import org.example.entity.vo.request.*;
import org.example.mappers.AccountDetailsMapper;
import org.example.mappers.AccountMapper;
import org.example.mappers.AccountPrivacyMapper;
import org.example.service.AccountServer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServiceImpl extends ServiceImpl<AccountMapper, Account> implements AccountServer {

    @Resource
    RabbitTemplate Rabbittemplate;

     @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FlowUtil util;

    @Resource
    PasswordEncoder encoder;


    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Resource
    AccountPrivacyMapper accountPrivacyMapper;

    @Resource
    AccountDetailsMapper accountDetailsMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = this.findAccountByUsernameOrEmail(username);
        if (account == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return User
                .withUsername(username)
                .password(account.getPassword())
                .roles(account.getRole())
                .build();
    }




    @Override
    public Account findAccountByUsernameOrEmail(String text) {
        return this.query()
                .eq("username",text).or()
                .eq("email",text)
                .one();
    }

    @Override
    public Account findAccountById(String id) {
        return this.query().eq("id",id).one();
    }

    @Override
    public String registEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if (!this.verifyLimit(ip)) {
            return "请求过于频繁,请等会再试";}
        Random random = new Random();
        int code = random.nextInt(899999) + 100000;
        Map<String, Object> data = Map.of("type",type ,"email",email ,"code",code);
        Rabbittemplate.convertAndSend("mail",data);
        stringRedisTemplate.opsForValue().set(Const.VERIFY_EMAIL_DATA+email,String.valueOf(code),3, TimeUnit.MINUTES);
        return null;
        }

    }

    @Override
    public String registEmailAccount(EmailRegistVO emailRegistVO) {
        String email = emailRegistVO.getEmail();
        String code = this.getVerifyEmailCode(email);
        String username = emailRegistVO.getUsername();
        String password = encoder.encode(emailRegistVO.getPassword());
        if(code == null) return "请先获取验证码";
        if (!code.equals(emailRegistVO.getCode())) return "验证码输入错误,请重新输入";
        if (this.existsAccountByEmail(email)) return "此邮箱已被其他人注册";
        if (this.existsAccountByUsername(username)) return "此用户名已被其他人注册";
        Account account = new Account(null,username,password,email,"user",null,new Date());
        if (this.save(account)) {
          this.deleteVerifyEmailCode(email);
          accountPrivacyMapper.insert(new AccountPrivacy(account.getId()));
            AccountDetails details = new AccountDetails();
            details.setId(account.getId());
            accountDetailsMapper.insert(details);
            return null;
        }else {
            return "内部错误,请联系管理员";
        }

    }

    private void deleteVerifyEmailCode(String email) {
        String key = Const.VERIFY_EMAIL_DATA+email;
        stringRedisTemplate.delete(key);
    }

     private String getVerifyEmailCode(String email) {
        String key = Const.VERIFY_EMAIL_DATA+email;
        return stringRedisTemplate.opsForValue().get(key);
    }



    @Override
    public String resetConfirm(ConfirmResetVO confirmResetVO) {
        String email = confirmResetVO.getEmail();
        String code = this.getVerifyEmailCode(email);
        if(code == null) return "请先获取验证码";
        if(!code.equals(confirmResetVO.getCode())) return "验证码输入错误";
        return null;
    }

    @Override
    public String resetEmailAccountPassword(EmailResetVO emailResetVO) {
        String verify = this.resetConfirm(new ConfirmResetVO(emailResetVO.getEmail(), emailResetVO.getCode()));
        if(verify != null) return verify;
        String email = emailResetVO.getEmail();
        String password = encoder.encode(emailResetVO.getPassword());
        boolean update = this.update().eq("email",email).set("password",password).update();
        if (update) {
            this.deleteVerifyEmailCode(email);
        }
        return update ? null : "更新失败请联系管理员";
    }

    @Override
    public String modifyEmail(EmailModifyVO vo,int id) {
        String email = vo.getEmail();
        if (this.getVerifyEmailCode(email) == null) return "请先获取验证码";
        if (!this.getVerifyEmailCode(email).equals(vo.getCode())) return "验证码输入错误";
        this.deleteVerifyEmailCode(email);
        if(this.existsAccountByEmail(email)) return "此邮件已被他人注册";
        this.update().eq("id",id).set("email",email).update();
        return null;
    }

     @Override
    public String changePassword(int id, ChangePasswordVO vo) {
        String password = this.query().eq("id", id).one().getPassword();
        if(!passwordEncoder.matches(vo.getPassword(), password))
            return "原密码错误，请重新输入！";
        boolean success = this.update()
                .eq("id", id)
                .set("password", passwordEncoder.encode(vo.getNew_password()))
                .update();
        return success ? null : "未知错误，请联系管理员";
    }

    // 验证这个ip是否已经被限制 false为已经限制
    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
         return util.limitOnceCheck(key,60);
    }

    // 验证邮件是否已被注册
    private boolean existsAccountByEmail(String email) {
        return baseMapper.exists(Wrappers.<Account>query()
                .eq("email",email));
    }


    // 验证邮件是否已被注册
    private boolean existsAccountByUsername(String username) {
        return baseMapper.exists(Wrappers.<Account>query()
                .eq("username",username));
    }
}
