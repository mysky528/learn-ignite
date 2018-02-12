package com.mycookcode.bigData.ignite.servicegrid.microservices.cachestore;

import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.store.CacheStoreAdapter;

import javax.cache.Cache;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhaolu on 2018/2/11.
 */
public class SimpleCacheStore extends CacheStoreAdapter<Long,BinaryObject> {

    /*使用ConcurrentHashMap代替实际的存储*/
    private Map<Long, BinaryObject> storeImpl = new ConcurrentHashMap<Long, BinaryObject>();


    public BinaryObject load(Long key) throws CacheLoaderException
    {
        System.out.println(" >>> Getting Value From Cache Store: " + key);
        return storeImpl.get(key);
    }


    public void write(Cache.Entry<? extends Long, ? extends BinaryObject> entry) throws CacheWriterException
    {
        System.out.println(" >>> Writing Value To Cache Store: " + entry);
        storeImpl.put(entry.getKey(), entry.getValue());
    }

    public void delete(Object key) throws CacheWriterException {
        System.out.println(" >>> Removing Key From Cache Store: " + key);
        storeImpl.remove(key);
    }
}
