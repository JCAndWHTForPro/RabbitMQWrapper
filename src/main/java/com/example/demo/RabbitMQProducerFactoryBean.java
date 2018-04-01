package com.example.demo;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.lang.reflect.Proxy;

/**
 * 建立RPC代理Bean
 *
 * @author fanghj
 */
public class RabbitMQProducerFactoryBean<T> extends RabbitMQProducerInterceptor implements FactoryBean<T> {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Class<?> serviceInterface;

    @Autowired
    private ConnectionFactory rabbitConnectionFactory;

    @Value("${mq.queue.durable}")
    private String durable;

    @Value("${mq.queue.exclusive}")
    private String exclusive;

    @Value("${mq.queue.autoDelete}")
    private String autoDelete;

    @SuppressWarnings("unchecked")
    @Override
    public T getObject() throws Exception {

        //初始化
        if (getQueueName() != null) {
            logger.info("指定的目标列队名[{}]，覆盖接口定义。", getQueueName());
        } else {
            RPCQueueName name = serviceInterface.getAnnotation(RPCQueueName.class);
            if (name == null)
                throw new IllegalArgumentException("接口" + serviceInterface.getCanonicalName() + "没有指定@RPCQueueName");
            setQueueName(name.value());
        }
        //创建队列
        declareQueue();
        logger.info("建立MQ客户端代理接口[{}]，目标队列[{}]。", serviceInterface.getCanonicalName(), getQueueName());

        return (T) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[]{serviceInterface}, this);
    }

    private void declareQueue() {
        Connection connection = rabbitConnectionFactory.createConnection();
        Channel channel = connection.createChannel(true);
        try {
            channel.queueDeclare(getQueueName(), Boolean.valueOf(durable), Boolean.valueOf(exclusive)
                    , Boolean.valueOf(autoDelete), null);
            logger.info("注册队列成功！");
        } catch (IOException e) {
            logger.warn("队列注册失败", e);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return serviceInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    @Required
    public void setServiceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }


    public ConnectionFactory getRabbitConnectionFactory() {
        return rabbitConnectionFactory;
    }

    public void setRabbitConnectionFactory(ConnectionFactory rabbitConnectionFactory) {
        this.rabbitConnectionFactory = rabbitConnectionFactory;
    }

}
