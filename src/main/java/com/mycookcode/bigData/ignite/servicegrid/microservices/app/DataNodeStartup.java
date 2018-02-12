package com.mycookcode.bigData.ignite.servicegrid.microservices.app;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

/**
 * 数据节点启动类
 *
 * Created by zhaolu on 2018/2/12.
 */
public class DataNodeStartup {

    public static void main(String[] args) throws IgniteException {
        Ignite ignite = Ignition.start("microservices/data-node-config.xml");
    }
}
