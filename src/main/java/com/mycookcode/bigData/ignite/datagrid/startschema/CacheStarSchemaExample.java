package com.mycookcode.bigData.ignite.datagrid.startschema;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlQuery;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.cache.Cache;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;



/**
 * 演示星型模型缓存查询
 *
 * Created by zhaolu on 2018/3/9.
 */
public class CacheStarSchemaExample {

    /**分区缓存 事实表*/
    private static final String FACT_CACHE_NAME = CacheStarSchemaExample.class.getSimpleName() + "Fact";

    /**复制缓存 商店维度*/
    private static final String DIM_STORE_CACHE_NAME = CacheStarSchemaExample.class.getSimpleName() + "DimStore";

    private static final String DIM_PROD_CACHE_NAME = CacheStarSchemaExample.class.getSimpleName() + "DimProd";

    /** ID generator. */
    private static int idGen;

    /**商店维度*/
    private static Map<Integer, DimStore> dataStore = new HashMap<>();

    /**产品维度*/
    private static Map<Integer, DimProduct> dataProduct = new HashMap<>();


    public static void main(String[] args)
    {
        try(Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Cache star schema example started.");

            CacheConfiguration<Integer, FactPurchase> factCacheCfg = new CacheConfiguration<>(FACT_CACHE_NAME);
            factCacheCfg.setCacheMode(CacheMode.PARTITIONED);
            factCacheCfg.setIndexedTypes(Integer.class, FactPurchase.class);

            CacheConfiguration<Integer, DimStore> dimStoreCacheCfg = new CacheConfiguration<>(DIM_STORE_CACHE_NAME);
            dimStoreCacheCfg.setCacheMode(CacheMode.REPLICATED);
            dimStoreCacheCfg.setIndexedTypes(Integer.class, DimStore.class);

            CacheConfiguration<Integer, DimProduct> dimProdCacheCfg = new CacheConfiguration<>(DIM_PROD_CACHE_NAME);
            dimProdCacheCfg.setCacheMode(CacheMode.REPLICATED);
            dimProdCacheCfg.setIndexedTypes(Integer.class, DimProduct.class);

            try(IgniteCache<Integer, FactPurchase> factCache = ignite.getOrCreateCache(factCacheCfg);
                IgniteCache<Integer, DimStore> dimStoreCache = ignite.getOrCreateCache(dimStoreCacheCfg);
                IgniteCache<Integer, DimProduct> dimProdCache = ignite.getOrCreateCache(dimProdCacheCfg))
            {
                populateDimensions(dimStoreCache, dimProdCache);
                populateFacts(factCache);
                queryStorePurchases();
                queryProductPurchases();
            }finally {
                ignite.destroyCache(FACT_CACHE_NAME);
                ignite.destroyCache(DIM_STORE_CACHE_NAME);
                ignite.destroyCache(DIM_PROD_CACHE_NAME);
            }

        }
    }

    /**
     * 初始化维度数据
     * @param dimStoreCache
     * @param dimProdCache
     * @throws IgniteException
     */
    private static void populateDimensions(Cache<Integer, DimStore> dimStoreCache,
                                           Cache<Integer, DimProduct> dimProdCache) throws IgniteException
    {
        DimStore store1 = new DimStore(idGen++, "Store1", "12345", "321 Chilly Dr, NY");
        DimStore store2 = new DimStore(idGen++, "Store2", "54321", "123 Windy Dr, San Francisco");

        //注入商店数据
        dimStoreCache.put(store1.getId(), store1);
        dimStoreCache.put(store2.getId(), store2);

        dataStore.put(store1.getId(), store1);
        dataStore.put(store2.getId(), store2);

        //注入产品数据

        for (int i = 0; i < 20; i++) {
            int id = idGen++;

            DimProduct product = new DimProduct(id, "Product" + i, i + 1, (i + 1) * 10);

            dimProdCache.put(id, product);

            dataProduct.put(id, product);
        }

    }

    /**
     * 初始化事实表数据
     *
     * @param factCache
     * @throws IgniteException
     */
    private static void populateFacts(Cache<Integer, FactPurchase> factCache) throws IgniteException
    {
        for (int i = 0; i < 100; i++) {
            int id = idGen++;

            DimStore store = rand(dataStore.values());
            DimProduct prod = rand(dataProduct.values());

            factCache.put(id, new FactPurchase(id, prod.getId(), store.getId(), (i + 1)));
        }
    }

    /**
     * 查询指定商店的所有采购。此方法查询使用跨缓存join
     */
    private static void queryStorePurchases()
    {
        IgniteCache<Integer, FactPurchase> factCache = Ignition.ignite().cache(FACT_CACHE_NAME);

        QueryCursor<Cache.Entry<Integer,FactPurchase>> storePurchases = factCache.query(new SqlQuery(FactPurchase.class, "from \"" + DIM_STORE_CACHE_NAME + "\".DimStore, \"" + FACT_CACHE_NAME + "\".FactPurchase "
                + "where DimStore.id=FactPurchase.storeId and DimStore.name=?").setArgs("Store1"));

        printQueryResults("All purchases made at store1:", storePurchases.getAll());
    }

    /**
     * 查询在特定商店为3种特定产品购买的所有商品
     */
    private static void queryProductPurchases()
    {
        IgniteCache<Integer, FactPurchase> factCache = Ignition.ignite().cache(FACT_CACHE_NAME);
        QueryCursor<Cache.Entry<Integer, FactPurchase>> storePurchases = factCache.query(new SqlQuery(
                FactPurchase.class,
                "from \"" + DIM_STORE_CACHE_NAME + "\".DimStore, \"" + FACT_CACHE_NAME + "\".FactPurchase "
                        + "where DimStore.id=FactPurchase.storeId and DimStore.name=?").setArgs("Store1"));

        printQueryResults("All purchases made at store1:", storePurchases.getAll());
    }

    /**
     * 控制台打印结果
     *
     * @param msg
     * @param res
     * @param <V>
     */
    private static <V> void printQueryResults(String msg, Iterable<Cache.Entry<Integer, V>> res) {
        System.out.println(msg);

        for (Cache.Entry<?, ?> e : res)
            System.out.println("    " + e.getValue().toString());
    }


    /**
     * 从集合中获得随机值
     *
     * @param c
     * @param <T>
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    private static <T> T rand(Collection<? extends T> c) {
        if (c == null)
            throw new IllegalArgumentException();

        int n = ThreadLocalRandom.current().nextInt(c.size());

        int i = 0;

        for (T t : c) {
            if (i++ == n)
                return t;
        }

        throw new ConcurrentModificationException();
    }
}
