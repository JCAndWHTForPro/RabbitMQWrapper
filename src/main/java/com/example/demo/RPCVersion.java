package com.example.demo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来标注RPC接口的版本，采用a.b.c的形式，类似软件版本号。
 * <ul>
 * <li>a - 大版本，认为协议不兼容。</li>
 * <li>b - 小版本，原则上兼容。</li>
 * <li>c - 修正版，必须兼容。</li>
 * </ul>
 * 这些会在服务两端进行校验
* @author fanghj
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RPCVersion {

    String value();
}
