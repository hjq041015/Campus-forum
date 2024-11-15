package org.example.server.serverImpl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.Util.Const;
import org.example.Util.FlowUtil;
import org.example.entity.dto.Account;
import org.example.entity.vo.request.EmailRegistVO;
import org.example.mappers.AccountMapper;
import org.example.server.AccountServer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AccountServerImpl extends ServiceImpl<AccountMapper, Account> implements AccountServer {

    @Resource
    RabbitTemplate Rabbittemplate;

     @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    FlowUtil util;

    @Resource
    PasswordEncoder encoder;

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
    public String registEmailVerifyCode(String type, String email, String ip) {
        synchronized (ip.intern()) {
            if (!this.verifyLimit(ip)) {
            return "请求过于频繁,请等会再试";}
        Random random = new Random();
        int code = random.nextInt(899999) + 100000;
        Map<String, Object> data = Map.of("type",type ,"email",email ,"code",code);
        Rabbittemplate.convertAndSend("mail",data);
        stringRedisTemplate.opsForValue().set(Const.VERIFY_EMAIL_DATA,String.valueOf(code),3, TimeUnit.MINUTES);
        return null;
        }

    }

    @Override
    public String registEmailAccount(EmailRegistVO emailRegistVO) {
        String email = emailRegistVO.getEmail();
        String code = stringRedisTemplate.opsForValue().get(Const.VERIFY_EMAIL_DATA);
        String username = emailRegistVO.getUsername();
        String password = encoder.encode(emailRegistVO.getPassword());
        if(code == null) return "请先获取验证码";
        if (!code.equals(emailRegistVO.getCode())) return "验证码输入错误,请重新输入";
        if (this.existsAccountByEmail(email)) return "此邮箱已被其他人注册";
        if (this.existsAccountByUsername(username)) return "此用户名已被其他人注册";
        Account account = new Account(null,username,password,email,"user",new Date());
        if (this.save(account)) {
            stringRedisTemplate.delete(Const.VERIFY_EMAIL_DATA);
            return null;
        }else {
            return "内部错误,请联系管理员";
        }

    }

    // 验证这个ip是否已经发起过请求
    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
         return util.limitOnceCLick(key,60);
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
