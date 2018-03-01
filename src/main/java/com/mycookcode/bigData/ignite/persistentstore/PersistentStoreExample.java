package com.mycookcode.bigData.ignite.persistentstore;

import com.mycookcode.bigData.ignite.datagrid.CacheQueryExample;
import com.mycookcode.bigData.ignite.model.Organization;
import java.util.List;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheWriteSynchronizationMode;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;

/**
 * Created by zhaolu on 2018/2/13.
 */
public class PersistentStoreExample {

    private static final String ORG_CACHE = CacheQueryExample.class.getSimpleName() + "Organizations";

    private static final boolean UPDATE = true;


    public static void main(String[] args) throws Exception
    {
        Ignition.setClientMode(true);
        try(Ignite ignite = Ignition.start("example-persistent-store.xml"))
        {
            //激活集群。如果启用持久化存储，在存储磁盘子集时，需要等待加入集群
            ignite.active(true);
            CacheConfiguration<Long, Organization> cacheCfg = new CacheConfiguration<>(ORG_CACHE);

            cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
            cacheCfg.setBackups(1);
            cacheCfg.setWriteSynchronizationMode(CacheWriteSynchronizationMode.FULL_SYNC);
            cacheCfg.setIndexedTypes(Long.class, Organization.class);

            IgniteCache<Long, Organization> cache = ignite.getOrCreateCache(cacheCfg);

            if (UPDATE)
            {
                System.out.println("Populating the cache...");
                try (IgniteDataStreamer<Long, Organization> streamer = ignite.dataStreamer(ORG_CACHE))
                {
                    streamer.allowOverwrite(true);
                    for (long i = 0; i < 100_000; i++) {
                        streamer.addData(i, new Organization(i, "organization-" + i));

                        if (i > 0 && i % 10_000 == 0)
                            System.out.println("Done: " + i);
                    }
                }
            }
            QueryCursor<List<?>> cur = cache.query(
                    new SqlFieldsQuery("select id, name from Organization where name like ?")
                            .setArgs("organization-54321"));

            System.out.println("SQL Result: " + cur.getAll());

            Organization org = cache.get(54321l);
            System.out.println("GET Result: " + org);

        }


    }

}
