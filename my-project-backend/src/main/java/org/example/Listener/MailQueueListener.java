package org.example.Listener;

import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "mail")
public class MailQueueListener {

    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    String username;


    @RabbitHandler
    public void sendMailMessage(Map<String, Object> data) {
        String email = (String) data.get("email");
        int code = (Integer) data.get("code");
        String type = (String) data.get("type");
        SimpleMailMessage message = switch (type) {
            case "register" -> createMessage("欢迎注册我们的网站",
                    "您的邮件验证码为:"+code+ "有效时间为三分钟,为了保障您的安全,请勿向他人谢露验证码信息",email);

            case "reset" -> createMessage("你的密码重置邮件","您好,您正在进行重置密码操作,验证码:"+code+"有效时间为三分钟,如非本人操作请忽略",email);

            case "modify" -> createMessage("你的邮件重置邮件","您好,您正在进行重置密码操作,验证码:" + code+ "有效时间为三分钟,如非本人操作请忽略",email);

            default -> null;

        };
        if (message == null) return;
        sender.send(message);
    }


    private SimpleMailMessage createMessage(String title, String context, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject(title);
        message.setText(context);
        message.setTo(email);
        message.setFrom(username);
        return message;
    }
}
