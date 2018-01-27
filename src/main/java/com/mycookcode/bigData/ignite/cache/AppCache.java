package com.mycookcode.bigData.ignite.cache;

import org.apache.ignite.Ignite;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

/**
 * Ignite 数据网格 基于内存分布式建值存储
 *
 * Created by zhaolu on 2018/1/22.
 */
public class AppCache {

    public static void main( String[] args )
    {
        Ignite ignite = Ignition.start("/Users/zhaolu/Code/java/apache-ignite-fabric-2.3.0-bin/config/example-cache.xml");
        //创建分布式缓存

        IgniteCache<Integer,String> cache = ignite.getOrCreateCache("myCache2");

        //清除缓存
        cache.clear();
        //存储key到缓存中，值将会缓存到不同的节点
        for(int i = 0;i < 10;i++)
        {

            cache.put(i,Integer.toString(i));
        }

        for(int i = 0;i < 10;i++)
        {
            System.out.println("Got [key=" + i + ", val=" + cache.get(i) + ']');
        }

        //cache.destroy();

    }
}
