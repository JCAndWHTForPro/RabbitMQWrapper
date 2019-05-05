package com.example.demo;

import com.rabbitmq.client.*;
import com.rabbitmq.client.impl.ChannelN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.TimeoutException;

/**
 * @Author: jicheng
 * @Since: 2018年04月01日 下午9:03
 */
@Service
public class Producer {

    @Autowired
    private ISendMsg sendMsg;

    public void send() {
        sendMsg.sendMsg("jicheng");
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("127.0.0.1");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("root");
        connectionFactory.setPassword("root123");
        connectionFactory.setVirtualHost("/root");
        // 创建连接
        Connection connection = connectionFactory.newConnection();
        // 创建信道
        Channel channel = connection.createChannel();
        String msg = "21123123";
        channel.basicPublish("ExchargeName", "RoutingKey",
                MessageProperties.PERSISTENT_TEXT_PLAIN, msg.getBytes());

        channel.basicQos(64);
        channel.basicConsume("queueName", false, "tag", new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
                String routingKey = envelope.getRoutingKey();
                String contentType = properties.getContentType();
                long deliveryTag = envelope.getDeliveryTag();
                String msg = new String(body, "UTF-8");
                // 这里进行消息处理
                channel.basicAck(deliveryTag, false);
            }
        });

        GetResponse response = channel.basicGet("QueueName", false);
        System.out.println(response.getBody());
        channel.basicAck(response.getEnvelope().getDeliveryTag(), false);

        connection.addShutdownListener(new ShutdownListener() {
            @Override
            public void shutdownCompleted(ShutdownSignalException cause) {
                // 业务逻辑
                Method reason = cause.getReason();
            }
        });

        channel.basicPublish("exchangeName", "routingKey",
                true, MessageProperties.PERSISTENT_TEXT_PLAIN, "test".getBytes());
        channel.addReturnListener(new ReturnListener() {

            @Override
            public void handleReturn(int replyCode, String replyText,
                                     String exchange, String routingKey,
                                     AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body);
                System.out.println("返回的结果是：" + msg);
            }
        });

        Map<String, Object> arguments = new HashMap<>();
        // x-message-ttl通过这个参数进行设置
        arguments.put("x-message-ttl", 6000);
        channel.queueDeclare("queueName", true, false, false, arguments);

        AMQP.BasicProperties.Builder builder = new AMQP.BasicProperties.Builder();
        builder.deliveryMode(2);// 持久化消息
        builder.expiration("60000");//设置TTL=60000ms
        AMQP.BasicProperties properties = builder.build();
        channel.basicPublish("exchangeName", "routingKey", properties, "123".getBytes());


        channel.exchangeDeclare("exchange.dlx", "direct",
                true, false, false, null);
        channel.exchangeDeclare("exchange.normal", "fanout",
                true, false, false, null);
        Map<String, Object> argument = new HashMap<>();
        // 设置DLX
        argument.put("x-dead-letter-exchange", "exchange.dlx");
        // 设置DLK，就是消息变成死信之后的路由键
        argument.put("x-dead-letter-routing-key", "routingkey");
        // 设置队列的过期时间
        argument.put("x-message-ttl", 10000);


        channel.queueDeclare("queue.normal", false, false, false, argument);
        channel.queueBind("queue.normal", "exchange.normal", "");
        channel.queueDeclare("queue.dlx", true, false, false, null);
        channel.queueBind("queue.dlx", "exchange.dlx", "routingkey");
        channel.basicPublish("exchange.normal", "rk",
                MessageProperties.PERSISTENT_TEXT_PLAIN, "dlx".getBytes());

        try {
            channel.txSelect();
            channel.basicPublish("exchange.normal", "rk",
                    MessageProperties.PERSISTENT_TEXT_PLAIN, "dlx".getBytes());
            channel.txCommit();
        } catch (IOException e) {
            e.printStackTrace();
            channel.txRollback();
        }


        channel.basicQos(5);





        // 关闭资源
        channel.close();
        connection.close();
    }
}
