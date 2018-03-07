package com.mycookcode.bigData.ignite.datagrid.store.jdbc;

import com.mycookcode.bigData.ignite.ExamplesUtils;
import com.mycookcode.bigData.ignite.model.Person;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.store.CacheStoreSessionListener;
import org.apache.ignite.cache.store.jdbc.CacheJdbcStoreSessionListener;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.h2.jdbcx.JdbcConnectionPool;

import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import java.util.UUID;

import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;


/**
 * 演示缓存在持久还缓存下的使用
 *
 * Created by zhaolu on 2018/3/5.
 */
public class CacheJdbcStoreExample {

    /*缓存名称*/
    private static final String CACHE_NAME = CacheJdbcStoreExample.class.getSimpleName();

    /*运行的内存大小*/
    public static final int MIN_MEMORY = 1024 * 1024 * 1024;

    /*加载数据量*/
    private static final int ENTRY_COUNT = 100_000;

    private static final Long id = Math.abs(UUID.randomUUID().getLeastSignificantBits());


    public static void main(String[] args)
    {
        ExamplesUtils.checkMinMemory(MIN_MEMORY);
        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            System.out.println();
            System.out.println(">>> Cache store example started.");

            CacheConfiguration<Long, Person> cacheCfg = new CacheConfiguration<>(CACHE_NAME);

            cacheCfg.setAtomicityMode(TRANSACTIONAL);

            //配置jdbc store
            cacheCfg.setCacheStoreFactory(FactoryBuilder.factoryOf(CacheJdbcPersonStore.class));

            //配置jdbc会话监听
            cacheCfg.setCacheStoreSessionListenerFactories(new Factory<CacheStoreSessionListener>() {
                @Override
                public CacheStoreSessionListener create() {
                    CacheJdbcStoreSessionListener lsnr = new CacheJdbcStoreSessionListener();
                    lsnr.setDataSource(JdbcConnectionPool.create("jdbc:h2:tcp://127.0.0.1/mem:ExampleDb", "sa", ""));
                    return lsnr;
                }
            });


            cacheCfg.setReadThrough(true);
            cacheCfg.setWriteThrough(true);

            try(IgniteCache<Long,Person> cache = ignite.getOrCreateCache(cacheCfg))
            {
                loadCache(cache);
            }finally {

            }



        }
    }

    /**
     * 加载缓存，初始化缓存数据
     *
     * @param cache
     */
    private static void loadCache(IgniteCache<Long, Person> cache)
    {
        long start = System.currentTimeMillis();
        //从持久化存储中加载数据到所有的 缓存节点
        cache.loadCache(null, ENTRY_COUNT);

        long end = System.currentTimeMillis();
        System.out.println(">>> Loaded " + cache.size() + " keys with backups in " + (end - start) + "ms.");
    }

    /**
     * 执行持久化读写的事务
     *
     * @param cache
     */
    private static void executeTransaction(IgniteCache<Long, Person> cache)
    {
        try(Transaction tx = Ignition.ignite().transactions().txStart())
        {
            Person val = cache.get(id);
            System.out.println("Read value: " + val);
            val = cache.getAndPut(id, new Person(id, "Isaac", "Newton"));

            System.out.println("Overwrote old value: " + val);
            val = cache.get(id);
            System.out.println("Read value: " + val);
            tx.commit();
        }
        System.out.println("Read value after commit: " + cache.get(id));
        //清楚内存对象
        cache.clear(id);

        //此缓存的操作不会影线存储
        IgniteCache<Long, Person> cacheSkipStore = cache.withSkipStore();
        System.out.println("Read value skipping store (expecting null): " + cacheSkipStore.get(id));

        System.out.println("Read value with store lookup (expecting NOT null): " + cache.get(id));
        //上一次调用后，数据应该在内存中
        System.out.println("Read value skipping store (expecting NOT null): " + cacheSkipStore.get(id));

    }


}
