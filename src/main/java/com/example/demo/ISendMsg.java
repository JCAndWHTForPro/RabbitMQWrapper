package com.example.demo;

/**
 * @Author: jicheng
 * @Since: 2018年03月25日 下午10:16
 */


@RPCQueueName("test.demo.ISendMsg")
public interface ISendMsg {

    void sendMsg(String msg);
}
