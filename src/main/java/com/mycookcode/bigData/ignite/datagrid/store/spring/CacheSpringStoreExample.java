package com.mycookcode.bigData.ignite.datagrid.store.spring;

import com.mycookcode.bigData.ignite.ExamplesUtils;
import com.mycookcode.bigData.ignite.model.Person;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.store.CacheStoreSessionListener;
import org.apache.ignite.cache.store.spring.CacheSpringStoreSessionListener;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;

import javax.cache.configuration.Factory;
import javax.cache.configuration.FactoryBuilder;
import java.util.UUID;

import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;

/**
 * Created by zhaolu on 2018/3/9.
 */
public class CacheSpringStoreExample {

    /*缓存名称*/
    private static final String CACHE_NAME = CacheSpringStoreExample.class.getSimpleName();

    /*内存大小*/
    public static final int MIN_MEMORY = 1024 * 1024 * 1024;

    /*加载数据记录数量*/
    private static final int ENTRY_COUNT = 100_000;

    /*在整个示例中使用的全局ID*/
    private static final Long id = Math.abs(UUID.randomUUID().getLeastSignificantBits());

    public static void main(String[] args)
    {
        ExamplesUtils.checkMinMemory(MIN_MEMORY);
        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            System.out.println();
            System.out.println(">>> Cache store example started.");

            CacheConfiguration<Long, Person> cacheCfg = new CacheConfiguration<>(CACHE_NAME);

            //设置原子事务
            cacheCfg.setAtomicityMode(TRANSACTIONAL);
            //配置使用spring store
            cacheCfg.setCacheStoreFactory(FactoryBuilder.factoryOf(CacheSpringPersonStore.class));

            //配置使用spring seesion监听
            cacheCfg.setCacheStoreSessionListenerFactories(new Factory<CacheStoreSessionListener>() {
                @Override
                public CacheStoreSessionListener create() {
                    CacheSpringStoreSessionListener lsnr = new CacheSpringStoreSessionListener();
                    lsnr.setDataSource(CacheSpringPersonStore.DATA_SRC);
                    return lsnr;
                }
            });

            cacheCfg.setReadThrough(true);
            cacheCfg.setWriteThrough(true);

            try(IgniteCache<Long,Person> cache = ignite.getOrCreateCache(cacheCfg))
            {
                //从持久存储中加载初始缓存。这是一个分布式的操作将调用cachestore。LoadCache（…）拓扑中所有节点的方法
                loadCache(cache);
                executeTransaction(cache);
            }finally {
                ignite.destroyCache(CACHE_NAME);
            }
            
        }
    }

    /**
     * 初始化缓存
     *
     * @param cache
     */
    private static void loadCache(IgniteCache<Long, Person> cache)
    {
        long start = System.currentTimeMillis();

        //从所有缓存节点上的持久化存储开始加载数据到缓存中
        cache.loadCache(null,ENTRY_COUNT);

        long end = System.currentTimeMillis();
        System.out.println(">>> Loaded " + cache.size() + " keys with backups in " + (end - start) + "ms.");
    }

    /**
     * 通过事务执行读写存储
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
    }
}
