package com.mycookcode.bigData.ignite;

import org.apache.ignite.Ignite;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterGroup;


import java.util.Arrays;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
       Ignite ignite = Ignition.start("/Users/zhaolu/Code/java/apache-ignite-fabric-2.3.0-bin/config/example-cache.xml");
        //创建分布式缓存
       // IgniteCache<?,?> cache = ignite.createCache("myCache");

        //在客户端创建作业
        //ClusterGroup clientGroup = ignite.cluster().forClients();
        IgniteCompute compute = ignite.compute();
        compute.broadcast(() -> System.out.println("Hello Server"));
    }
}
