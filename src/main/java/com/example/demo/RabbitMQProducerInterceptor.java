package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.remoting.support.RemoteInvocation;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Objects;

public class RabbitMQProducerInterceptor implements InvocationHandler {



    private Logger logger = LoggerFactory.getLogger(getClass());


    private String queueName;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object sendObj;
        Class<?>[] parameterTypes = method.getParameterTypes();
        String methodName = method.getName();
        boolean isSendOneJson = Objects.nonNull(args) && args.length == 1 && (args[0] instanceof String);
        if (isSendOneJson) {
            sendObj = args[0];
            logger.info("发送单一json字符串消息：{}", (String) sendObj);
        } else {
            sendObj = new RemoteInvocation(methodName, parameterTypes, args);
            logger.info("发送封装消息体：{}", JSONSerializeUtil.jsonSerializerNoType(sendObj));
        }


        logger.info("发送异步消息到[{}]，方法名为[{}]", queueName, method.getName());
        //异步方式使用，同时要告知服务端不要发送响应
        amqpTemplate.convertAndSend(queueName, sendObj);
        return null;

    }


    public AmqpTemplate getAmqpTemplate() {
        return amqpTemplate;
    }

    public void setAmqpTemplate(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

}
