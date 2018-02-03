package com.mycookcode.bigData.ignite.datagrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.ContinuousQuery;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteAsyncCallback;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.resources.IgniteInstanceResource;

import javax.cache.Cache;
import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryUpdatedListener;

/**
 * 这个类演示的是异步持续查询api
 */
public class CacheContinuousAsyncQueryExample {

    private static final String CACHE_NAME = CacheContinuousAsyncQueryExample.class.getSimpleName();


    public static void main(String[] args)throws Exception
    {
        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            System.out.println();
            System.out.println(">>> Cache continuous query example started.");

            try (IgniteCache<Integer, String> cache = ignite.getOrCreateCache(CACHE_NAME)) {
                int keyCnt = 20;

                for (int i = 0; i < keyCnt; i++)
                    cache.put(i, Integer.toString(i));

                ContinuousQuery<Integer, String> qry = new ContinuousQuery<>();

                qry.setInitialQuery(new ScanQuery<>(new IgniteBiPredicate<Integer, String>() {
                    @Override public boolean apply(Integer key, String val) {
                        return key > 10;
                    }
                }));

                qry.setLocalListener(new CacheEntryUpdatedListener<Integer, String>() {
                    @Override public void onUpdated(Iterable<CacheEntryEvent<? extends Integer, ? extends String>> evts) {
                        for (CacheEntryEvent<? extends Integer, ? extends String> e : evts)
                            System.out.println("Updated entry [key=" + e.getKey() + ", val=" + e.getValue() + ']');
                    }
                });

                qry.setRemoteFilterFactory(new Factory<CacheEntryEventFilter<Integer, String>>() {
                    @Override public CacheEntryEventFilter<Integer, String> create() {
                        return new CacheEntryFilter();
                    }
                });

                try (QueryCursor<Cache.Entry<Integer, String>> cur = cache.query(qry)) {

                    for (Cache.Entry<Integer, String> e : cur)
                        System.out.println("Queried existing entry [key=" + e.getKey() + ", val=" + e.getValue() + ']');


                    for (int i = 0; i < keyCnt; i++)
                        cache.put(i, Integer.toString(i));


                    Thread.sleep(2000);
                }


                for (int i = 0; i < 10; i++)
                    System.out.println("Entry updated from filter [key=" + i + ", val=" + cache.get(i) + ']');

        }finally {
                ignite.destroyCache(CACHE_NAME);
            }
        }
    }

    /**
     * 对缓存数据进行过滤，返回key大于10的数据
     */
    @IgniteAsyncCallback
    private static class CacheEntryFilter implements CacheEntryEventFilter<Integer,String>
    {

        @IgniteInstanceResource
        private Ignite ignite;

        @Override
        public boolean evaluate(CacheEntryEvent<? extends Integer, ? extends String> e)
        {
            if (e.getKey() < 10 && String.valueOf(e.getKey()).equals(e.getValue()))
                ignite.cache(CACHE_NAME).put(e.getKey(), e.getValue() + "_less_than_10");
            return e.getKey() > 10;
        }
    }
}
