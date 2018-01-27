package com.mycookcode.bigData.ignite.datagrid;


import javax.cache.processor.EntryProcessor;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 当在缓存中执行puts和updates操作时，通常需要在网络中发送完整的状态数据，而EntryProcessor可以
 * 直接在主节点上出来数据，只需要传输增量数据而不是全量数据
 *
 * Created by zhaolu on 2018/1/27.
 */
public class CacheEntryProcessorExample {

    /*缓存名称*/
    private static final String CACHE_NAME = CacheEntryProcessorExample.class.getSimpleName();

    /*定义key的数量*/
    private static final int KEY_CNT = 20;

    /*定义需要预处理key的集合*/
    private static final Set<Integer> KEY_SET;

    /**
     * 初始化key集合，用于在本例子中进行批量操作
     */
    static {
        KEY_SET = new HashSet<>();
        for(int i = 0;i < KEY_CNT;i++)
        {
            KEY_SET.add(i);
        }
    }

    public static void main(String[] args)
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Entry processor example started.");

            try (IgniteCache<Integer,Integer> cache = ignite.getOrCreateCache(CACHE_NAME)){
                populateEntriesWithInvoke(cache);
                incrementEntriesWithInvokeAll(cache);
            }finally {
                ignite.destroyCache(CACHE_NAME);
            }
        }
    }


    /**
     * 向缓存中添加实体对象
     *
     * @param cache
     */
    private static void populateEntriesWithInvoke(IgniteCache<Integer,Integer> cache)
    {
        printCacheEntries(cache);


        System.out.println("");
        System.out.println(">> Populating the cache using EntryProcessor.");

        //按顺序添加实体对象
        for (int i = 0;i < KEY_CNT;i++)
        {
            cache.invoke(i,(entry,object) -> {
                if(entry.getValue() == null)
                {
                    entry.setValue((entry.getKey()+1) * 10);
                }
                return null;
            });
        }
    }

    /**
     * 使用缓存中存储的实体对象增加值
     *
     * @param cache
     */
    private static void incrementEntriesWithInvokeAll(IgniteCache<Integer,Integer> cache)
    {
        System.out.println("");
        System.out.println(">> Incrementing values in the cache using EntryProcessor.");

        cache.invokeAll(KEY_SET,(entry,object) -> {
            entry.setValue(entry.getValue() + 5);
            return null;
        });

        printCacheEntries(cache);
    }

    /**
     * 打印输出缓存中的实体对象
     *
     * @param cache
     */
    private static void printCacheEntries(IgniteCache<Integer,Integer> cache)
    {
        System.out.println();
        System.out.println(">>> Entries in the cache.");
        Map<Integer,Integer> entries = cache.getAll(KEY_SET);

        if(entries.isEmpty())
        {
            System.out.println("No entries in the cache.");
        }else
        {
            for (Map.Entry<Integer,Integer> entry:entries.entrySet())
            {
                System.out.println("Entry [key=" + entry.getKey() + ", value=" + entry.getValue() + ']');
            }
        }
    }
}
