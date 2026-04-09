package com.blog.config;

import com.blog.common.constant.RabbitMqConstants;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange userExpExchange() {
        return new TopicExchange(RabbitMqConstants.EXCHANGE_USER_EXP, true, false);
    }

    @Bean
    public Queue expGrantQueue() {
        return new Queue(RabbitMqConstants.QUEUE_EXP_GRANT, true);
    }

    @Bean
    public Queue expNotifyQueue() {
        return new Queue(RabbitMqConstants.QUEUE_EXP_NOTIFY, true);
    }

    @Bean
    public Queue expRankQueue() {
        return new Queue(RabbitMqConstants.QUEUE_EXP_RANK, true);
    }

    @Bean
    public Binding expGrantBinding(@Qualifier("expGrantQueue") Queue expGrantQueue, TopicExchange userExpExchange) {
        return BindingBuilder.bind(expGrantQueue).to(userExpExchange).with(RabbitMqConstants.ROUTING_KEY_EXP_GRANT);
    }

    @Bean
    public Binding expNotifyBinding(@Qualifier("expNotifyQueue") Queue expNotifyQueue, TopicExchange userExpExchange) {
        return BindingBuilder.bind(expNotifyQueue).to(userExpExchange).with(RabbitMqConstants.ROUTING_KEY_EXP_CHANGED);
    }

    @Bean
    public Binding expRankBinding(@Qualifier("expRankQueue") Queue expRankQueue, TopicExchange userExpExchange) {
        return BindingBuilder.bind(expRankQueue).to(userExpExchange).with(RabbitMqConstants.ROUTING_KEY_EXP_CHANGED);
    }
}
