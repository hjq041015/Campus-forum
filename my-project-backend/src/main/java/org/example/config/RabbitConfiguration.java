package org.example.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
// 创建消息队列
public class RabbitConfiguration {
    @Bean("queueEmail")
    public Queue queueEmail() {
        return QueueBuilder.durable("mail").build();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
}
}
