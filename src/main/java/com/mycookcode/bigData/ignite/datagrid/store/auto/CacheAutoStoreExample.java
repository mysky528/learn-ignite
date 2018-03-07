package com.mycookcode.bigData.ignite.datagrid.store.auto;

import com.mycookcode.bigData.ignite.model.Person;
import com.mycookcode.bigData.ignite.util.DbH2ServerStartup;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStore;
import org.apache.ignite.cache.store.jdbc.CacheJdbcPojoStoreFactory;
import org.apache.ignite.cache.store.jdbc.JdbcType;
import org.apache.ignite.cache.store.jdbc.JdbcTypeField;
import org.apache.ignite.cache.store.jdbc.dialect.H2Dialect;
import org.apache.ignite.cache.store.jdbc.dialect.MySQLDialect;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.h2.jdbcx.JdbcConnectionPool;

import java.sql.Types;

import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;

/**
 * 演示使用基本持久存储配置的缓存使用
 *
 * Created by zhaolu on 2018/3/1.
 */
public class CacheAutoStoreExample {

    private static final Long id = 25121642L;

    public static final String CACHE_NAME = CacheAutoStoreExample.class.getSimpleName();

    private static final class CacheJdbcPojoStoreExampleFactory extends CacheJdbcPojoStoreFactory<Long,Person>
    {

        @Override
        public CacheJdbcPojoStore<Long,Person> create()
        {
            setDataSource(JdbcConnectionPool.create("jdbc:h2:tcp://127.0.0.1/mem:ExampleDb", "sa", ""));
            return super.create();
        }
    }


    /**
     * 配置存储缓存
     *
     * @return
     */
    private static CacheConfiguration<Long,Person> cacheConfiguration()
    {
        CacheConfiguration<Long,Person> cfg = new CacheConfiguration<>(CACHE_NAME);

        CacheJdbcPojoStoreExampleFactory storeFactory = new CacheJdbcPojoStoreExampleFactory();
        storeFactory.setDialect(new H2Dialect());

        JdbcType jdbcType = new JdbcType();
        jdbcType.setCacheName(CACHE_NAME);
        jdbcType.setDatabaseSchema("PUBLIC");
        jdbcType.setDatabaseTable("PERSION");
        jdbcType.setKeyType("java.lang.Long");
        jdbcType.setKeyFields(new JdbcTypeField(Types.BIGINT, "ID", Long.class, "id"));
        jdbcType.setValueType("com.mycookcode.bigData.ignite.model.Person");
        jdbcType.setValueFields(
                new JdbcTypeField(Types.BIGINT, "ID", Long.class, "id"),
                new JdbcTypeField(Types.VARCHAR, "FIRST_NAME", String.class, "firstName"),
                new JdbcTypeField(Types.VARCHAR, "LAST_NAME", String.class, "lastName")
        );
        storeFactory.setTypes(jdbcType);
        cfg.setCacheStoreFactory(storeFactory);

        cfg.setAtomicityMode(TRANSACTIONAL);
        cfg.setReadThrough(true);
        cfg.setWriteThrough(true);

        return cfg;
    }

    public static void main(String args[]) throws Exception
    {
        try(Ignite ignite = Ignition.start("example-ignite.xml")){

            System.out.println();
            System.out.println(">>> Populate database with data...");
            DbH2ServerStartup.populateDatabase();
            System.out.println();
            System.out.println(">>> Cache auto store example started...");

            try(IgniteCache<Long,Person> cache = ignite.getOrCreateCache(cacheConfiguration())){
                try(Transaction tx = ignite.transactions().txStart())
                {
                    Person val = cache.get(id);
                    System.out.println(">>> Read value: " + val);

                    val = cache.getAndPut(id, new Person(id, 1L, "Isaac", "Newton", 100.10, "English physicist and mathematician"));
                    System.out.println(">>> Overwrote old value: " + val);
                    val = cache.get(id);

                    System.out.println(">>> Read value: " + val);
                    System.out.println(">>> Update salary in transaction...");
                    val.salary *= 2;
                    cache.put(id, val);
                    tx.commit();
                }
                System.out.println(">>> Read value after commit: " + cache.get(id));
                cache.clear();

                System.out.println(">>> ------------------------------------------");
                System.out.println(">>> Load data to cache from DB with custom SQL...");
                //使用sql语句在所有节点的缓存中加载数据
                cache.loadCache(null, "java.lang.Long", "select * from PERSON where id <= 3");
                System.out.println(">>> Loaded cache entries: " + cache.size());
                cache.clear();

                System.out.println(">>> Load ALL data to cache from DB...");
                cache.loadCache(null);

                System.out.println(">>> Loaded cache entries: " + cache.size());

            }finally {
                ignite.destroyCache(CACHE_NAME);
            }

        }
    }
}
