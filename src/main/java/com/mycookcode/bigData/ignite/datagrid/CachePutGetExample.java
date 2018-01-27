package com.mycookcode.bigData.ignite.datagrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

/**
 * 这个类展示了缓存最基本的操作，比如put或get数据
 *
 * Created by zhaolu on 2018/1/23.
 */
public class CachePutGetExample {

    /*定义缓存的名称*/
    private static final String CACHE_NAME = CachePutGetExample.class.getName();

    /**
     * 类的执行方法
     *
     * @param args
     */
    public static void main(String[] args)
    {
        try (Ignite ignite = Ignition.start("/Users/zhaolu/Code/java/apache-ignite-fabric-2.3.0-bin/config/example-cache.xml")){

        }
    }

    /**
     *单独的往缓存中put get数据
     *
     * @param cache
     */
    private static void putGet(IgniteCache<Integer,String> cache)
    {
        System.out.println();
        System.out.println(">>> Cache put-get example started.");
        final int keyCnt = 10;

    }
}
