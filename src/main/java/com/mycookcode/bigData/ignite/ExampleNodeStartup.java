package com.mycookcode.bigData.ignite;

import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

/**
 * 节点启动工具类
 *
 * Created by zhaolu on 2018/2/4.
 */
public class ExampleNodeStartup {

    public static void main(String[] args) throws IgniteException {
        Ignition.start("example-ignite.xml");
    }
}
