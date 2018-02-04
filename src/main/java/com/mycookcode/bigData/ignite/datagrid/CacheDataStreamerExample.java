package com.mycookcode.bigData.ignite.datagrid;

import com.mycookcode.bigData.ignite.ExamplesUtils;
import org.apache.ignite.*;

/**
 * 演示使用缓存数据流API例子
 *
 * Created by zhaolu on 2018/2/4.
 */
public class CacheDataStreamerExample {

    private static final String CACHE_NAME = CacheDataStreamerExample.class.getSimpleName();

    /*加载到缓存中的数据记录数*/
    private static final int ENTRY_COUNT = 500000;

    /*运行时内存的大小*/
    public static final int MIN_MEMORY = 512 * 1024 * 1024;

    public static void main(String[] args) throws IgniteException
    {
        ExamplesUtils.checkMinMemory(MIN_MEMORY);
        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            System.out.println();
            System.out.println(">>> Cache data streamer example started.");


            try(IgniteCache<Integer,String> cache = ignite.getOrCreateCache(CACHE_NAME))
            {
                long start = System.currentTimeMillis();
                try(IgniteDataStreamer<Integer,String> stmr = ignite.dataStreamer(CACHE_NAME))
                {
                    stmr.perNodeBufferSize(1024);
                    stmr.perNodeParallelOperations(8);
                    for (int i = 0; i < ENTRY_COUNT; i++) {
                        stmr.addData(i, Integer.toString(i));

                        // Print out progress while loading cache.
                        if (i > 0 && i % 10000 == 0)
                            System.out.println("Loaded " + i + " keys.");
                    }

                }
                long end = System.currentTimeMillis();

                System.out.println(">>> Loaded " + ENTRY_COUNT + " keys in " + (end - start) + "ms.");
            }finally {
                ignite.destroyCache(CACHE_NAME);
            }
        }
    }
}
