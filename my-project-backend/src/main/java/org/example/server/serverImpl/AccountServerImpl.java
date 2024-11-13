package org.example.server.serverImpl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.example.Util.Const;
import org.example.Util.FlowUtil;
import org.example.entity.dto.Account;
import org.example.mappers.AccountMapper;
import org.example.server.AccountServer;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

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


    private boolean verifyLimit(String ip){
        String key = Const.VERIFY_EMAIL_LIMIT + ip;
         return util.limitOnceCLick(key,60);
    }
}
