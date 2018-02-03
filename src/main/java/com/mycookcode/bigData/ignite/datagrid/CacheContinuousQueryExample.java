package com.mycookcode.bigData.ignite.datagrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheEntryProcessor;
import org.apache.ignite.cache.query.ContinuousQuery;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.lang.IgniteBiPredicate;

import javax.cache.Cache;
import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;

/**
 * 这个类演示持续查询API的使用
 */
public class CacheContinuousQueryExample {

    private static final String CACHE_NAME = CacheContinuousQueryExample.class.getName();


    public static void main(String[] args)throws Exception
    {
        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            System.out.println();
            System.out.println(">>> Cache continuous query example started.");
            try (IgniteCache<Integer,String> cache = ignite.getOrCreateCache(CACHE_NAME))
            {
                int keyCnt = 20;
                //初始化查询的数据
                for(int i = 0;i < keyCnt;i++)
                {
                    cache.put(i,Integer.toString(i));
                }
                //创建一个持续的查询
                ContinuousQuery<Integer,String> qry = new ContinuousQuery<>();
                //初始化持续查询：当要执行持续查询时，在将持续查询注册在集群中以及开始接收更新之前，可以有选择地指定一个初始化查询
                qry.setInitialQuery(new ScanQuery<>(
                        new IgniteBiPredicate<Integer, String>() {
                            @Override
                            public boolean apply(Integer key, String val) {
                                return key > 10;
                            }
                        }
                ));

                //设置本地监听器：当接收到更新通知时，在本地调用回调
                qry.setLocalListener(new CacheEntryUpdatedListener<Integer, String>() {
                    @Override
                    public void onUpdated(Iterable<CacheEntryEvent<? extends Integer, ? extends String>> evts) throws CacheEntryListenerException {
                        for(CacheEntryEvent<? extends Integer,? extends String> e:evts)
                        {
                            System.out.println("Updated entry [key=" + e.getKey() + ", val=" + e.getValue() + ']');
                        }
                    }
                });

                //设置远程过滤器:这个过滤器在给定键对应的主和备节点上执行，然后评估更新是否需要作为一个事件传播给该查询的本地监听器。
                //如果过滤器返回true，那么本地监听器就会收到通知，否则事件会被忽略。产生更新的特定主和备节点，会在主/备节点以及应用端执行的本地监听器之间，减少不必要的网络流量
                qry.setRemoteFilterFactory(new Factory<CacheEntryEventFilter<Integer, String>>() {
                    @Override
                    public CacheEntryEventFilter<Integer, String> create() {
                        return new CacheEntryEventFilter<Integer, String>() {
                            @Override public boolean evaluate(CacheEntryEvent<? extends Integer, ? extends String> e) {
                                return e.getKey() > 10;
                            }
                        };
                    }
                });

                //执行查询
                try(QueryCursor<Cache.Entry<Integer,String>> cur = cache.query(qry))
                {
                    for (Cache.Entry<Integer, String> e : cur)
                        System.out.println("Queried existing entry [key=" + e.getKey() + ", val=" + e.getValue() + ']');

                    //添加新数据
                    for (int i = keyCnt; i < keyCnt + 10; i++)
                        cache.put(i, Integer.toString(i));

                    Thread.sleep(2000);
                }

            } finally {
                ignite.destroyCache(CACHE_NAME);
            }
        }
    }
}
