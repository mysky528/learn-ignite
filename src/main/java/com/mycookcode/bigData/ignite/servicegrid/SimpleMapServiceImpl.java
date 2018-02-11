package com.mycookcode.bigData.ignite.servicegrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;

/**
 * Created by zhaolu on 2018/2/11.
 */
public class SimpleMapServiceImpl<K,V> implements Service,SimpleMapService<K,V>{

    private static final long serialVersionUID = 0L;

    @IgniteInstanceResource
    private Ignite ignite;

    private IgniteCache<K, V> cache;

    /** {@inheritDoc} */
    @Override public void put(K key, V val) {
        cache.put(key, val);
    }

    /** {@inheritDoc} */
    @Override public V get(K key) {
        return cache.get(key);
    }

    /** {@inheritDoc} */
    @Override public void clear() {
        cache.clear();
    }

    /** {@inheritDoc} */
    @Override public int size() {
        return cache.size();
    }

    /** {@inheritDoc} */
    @Override public void cancel(ServiceContext ctx) {
        ignite.destroyCache(ctx.name());

        System.out.println("Service was cancelled: " + ctx.name());
    }

    /** {@inheritDoc} */
    @Override public void init(ServiceContext ctx) throws Exception {
        // Create a new cache for every service deployment.
        // Note that we use service name as cache name, which allows
        // for each service deployment to use its own isolated cache.
        cache = ignite.getOrCreateCache(new CacheConfiguration<K, V>(ctx.name()));

        System.out.println("Service was initialized: " + ctx.name());
    }

    @Override public void execute(ServiceContext ctx) throws Exception {
        System.out.println("Executing distributed service: " + ctx.name());
    }

}
