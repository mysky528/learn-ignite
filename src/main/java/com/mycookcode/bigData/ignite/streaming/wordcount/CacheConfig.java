package com.mycookcode.bigData.ignite.streaming.wordcount;

import org.apache.ignite.cache.affinity.AffinityUuid;
import org.apache.ignite.configuration.CacheConfiguration;

import javax.cache.configuration.FactoryBuilder;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by zhaolu on 2018/2/5.
 */
public class CacheConfig {

    public static CacheConfiguration<AffinityUuid,String> wordCache()
    {
        CacheConfiguration<AffinityUuid, String> cfg = new CacheConfiguration<>("words");
        cfg.setIndexedTypes(AffinityUuid.class,String.class);
        cfg.setExpiryPolicyFactory(FactoryBuilder.factoryOf(new CreatedExpiryPolicy(new Duration(SECONDS, 1))));
        return cfg;
    }
}
