package com.blog.common.constant;

public final class RabbitMqConstants {

    private RabbitMqConstants() {
    }

    public static final String EXCHANGE_USER_EXP = "blog.user.exp.exchange";

    public static final String ROUTING_KEY_EXP_GRANT = "exp.grant";
    public static final String ROUTING_KEY_EXP_CHANGED = "exp.changed";

    public static final String QUEUE_EXP_GRANT = "blog.user.exp.grant.queue";
    public static final String QUEUE_EXP_NOTIFY = "blog.user.exp.notify.queue";
    public static final String QUEUE_EXP_RANK = "blog.user.exp.rank.queue";
}
