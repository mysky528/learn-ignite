package com.mycookcode.bigData.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;

/**
 * Created by zhaolu on 2017/12/13.
 */
public class IgniteDataGridApp {

    public static void main( String[] args )
    {
        try (Ignite ignite = Ignition.start()){
            IgniteCache<Integer,String> cache = ignite.getOrCreateCache("appCache");

            for(int i = 0;i < 10;i++)
            {
                cache.put(i,Integer.toString(i));
            }

            for (int i = 0; i < 10; i++)
                System.out.println("Got [key=" + i + ", val=" + cache.get(i) + ']');

            String oldVal = cache.getAndPutIfAbsent(11, "11");
            boolean success = cache.putIfAbsent(22, "22");
            oldVal = cache.getAndReplace(11,"10");
        }
    }

}
