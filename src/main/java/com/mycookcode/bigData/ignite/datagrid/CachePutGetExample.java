package com.mycookcode.bigData.ignite.datagrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

import java.util.HashMap;
import java.util.Map;

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
        try (Ignite ignite = Ignition.start("client-cache.xml")){
            try(IgniteCache<Integer,String> cache = ignite.getOrCreateCache(CACHE_NAME)){
                 putGet(cache);
                //批量获得和放入数据
                putAllGetAll(cache);
            }
        }
    }

    /**
     *往缓存中put get数据
     *
     * @param cache
     */
    private static void putGet(IgniteCache<Integer,String> cache)
    {
        System.out.println();
        System.out.println(">>> Cache put-get example started.");
        final int keyCnt = 10;

        //将key存储到缓存中
        for(int i = 0;i < keyCnt;i++)
        {
            cache.put(i,Integer.toString(i));
        }

        System.out.println(">>> Stored values in cache.");

        for (int i = 0;i < keyCnt;i++)
        {
            System.out.println("Got [key=" + i +",val="+cache.get(i)+"]");
        }
    }

    /**
     * 使用getAll()或putALL()方法批量获得或存储数据
     *
     * @param cache
     */
    private static void putAllGetAll(IgniteCache<Integer,String> cache)
    {
        System.out.println();
        System.out.println(">>> Starting putAll-getAll example.");
        final int keyCnt = 10;
        Map<Integer,String> batch = new HashMap<>();
        for (int i = 0;i < keyCnt;i++)
        {
            batch.put(i,"bulk-" + Integer.toString(i));
        }

        //批量保存数据到缓存中
        cache.putAll(batch);
        System.out.println(">>> Bulk-stored values in cache.");
        //批量获得数据
        Map<Integer,String> vals = cache.getAll(batch.keySet());
        for(Map.Entry<Integer,String> e:vals.entrySet())
        {
            System.out.println("Got entry [key=" + e.getKey() + ", val=" + e.getValue() + ']');
        }
    }
}
