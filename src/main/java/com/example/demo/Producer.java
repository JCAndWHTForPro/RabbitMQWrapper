package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: jicheng
 * @Since: 2018年04月01日 下午9:03
 */
@Service
public class Producer {

    @Autowired
    private ISendMsg sendMsg;

    public void send(){
        sendMsg.sendMsg("jicheng");
    }
}
