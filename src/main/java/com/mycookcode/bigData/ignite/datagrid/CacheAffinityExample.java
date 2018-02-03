package com.mycookcode.bigData.ignite.datagrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class CacheAffinityExample {

    private static final String CACHE_NAME = CacheAffinityExample.class.getSimpleName();

    private static final int KEY_CNT = 20;

    public static void main(String[] args) throws IgniteException
    {
        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            System.out.println();
            System.out.println(">>> Cache affinity example started.");
            CacheConfiguration<Integer,String> cfg = new CacheConfiguration<>();

            cfg.setCacheMode(CacheMode.PARTITIONED);
            cfg.setName(CACHE_NAME);

            try (IgniteCache<Integer, String> cache = ignite.getOrCreateCache(cfg))
            {
                for (int i = 0; i < KEY_CNT; i++)
                    cache.put(i, Integer.toString(i));

                visitUsingAffinityRun();
                visitUsingMapKeysToNodes();
            }finally {
                ignite.destroyCache(CACHE_NAME);
            }

        }
    }



    private static void visitUsingAffinityRun()
    {
        Ignite ignite = Ignition.ignite();

        final IgniteCache<Integer, String> cache = ignite.cache(CACHE_NAME);

        for (int i = 0; i < KEY_CNT; i++) {
            int key = i;
            ignite.compute().affinityRun(CACHE_NAME,key,() -> System.out.println("Co-located using affinityRun [key= " + key + ", value=" + cache.localPeek(key) + ']'));
        }
    }

    private static void visitUsingMapKeysToNodes()
    {
        final Ignite ignite = Ignition.ignite();

        Collection<Integer> keys = new ArrayList<>(KEY_CNT);

        for (int i = 0; i < KEY_CNT; i++)
            keys.add(i);

        Map<ClusterNode, Collection<Integer>> mappings = ignite.<Integer>affinity(CACHE_NAME).mapKeysToNodes(keys);

        for(Map.Entry<ClusterNode, Collection<Integer>> mapping:mappings.entrySet())
        {
            ClusterNode node = mapping.getKey();
            final Collection<Integer> mappedKeys = mapping.getValue();
            if(node != null)
            {
                ignite.compute(ignite.cluster().forNode(node)).run(()->{
                    IgniteCache<Integer, String> cache = ignite.cache(CACHE_NAME);
                    for (Integer key : mappedKeys)
                        System.out.println("Co-located using mapKeysToNodes [key= " + key +
                                ", value=" + cache.localPeek(key) + ']');
                });
            }
        }
    }
}
