    package com.mycookcode.bigData.ignite.datagrid;

    import org.apache.ignite.Ignite;
    import org.apache.ignite.IgniteCache;
    import org.apache.ignite.IgniteException;
    import org.apache.ignite.Ignition;
    import org.apache.ignite.events.CacheEvent;
    import org.apache.ignite.lang.IgniteBiPredicate;
    import org.apache.ignite.lang.IgnitePredicate;

    import java.util.UUID;

    import static org.apache.ignite.events.EventType.EVT_CACHE_OBJECT_PUT;
    import static org.apache.ignite.events.EventType.EVT_CACHE_OBJECT_READ;
    import static org.apache.ignite.events.EventType.EVT_CACHE_OBJECT_REMOVED;

    /**
     * 消息API的演示
     *
     * Created by zhaolu on 2018/2/4.
     */
    public class CacheEventsExample {

        private static final String CACHE_NAME = CacheEventsExample.class.getSimpleName();

        public static void main(String[] args)throws IgniteException, InterruptedException
        {
            try(Ignite ignite = Ignition.start("example-ignite.xml"))
            {
                System.out.println();
                System.out.println(">>> Cache events example started.");

                try(IgniteCache<Integer, String> cache = ignite.getOrCreateCache(CACHE_NAME))
                {
                    //每个事件通知都调用这个可选的本地回调
                    IgniteBiPredicate<UUID,CacheEvent> locLsnr = new IgniteBiPredicate<UUID, CacheEvent>() {
                        @Override
                        public boolean apply(UUID uuid, CacheEvent evt) {
                            System.out.println("Received event [evt=" + evt.name() + ", key=" + evt.key() +
                                    ", oldVal=" + evt.oldValue() + ", newVal=" + evt.newValue());
                            return true;
                        }
                    };

                    //远程监听器 并且实在数据的主节点上
                    IgnitePredicate<CacheEvent> rmtLsnr = new IgnitePredicate<CacheEvent>() {
                        @Override
                        public boolean apply(CacheEvent evt) {
                            System.out.println("Cache event [name=" + evt.name() + ", key=" + evt.key() + ']');

                            int key = evt.key();

                            return key >= 10 && ignite.affinity(CACHE_NAME).isPrimary(ignite.cluster().localNode(), key);
                        }
                    };

                    //添加事件类型
                    ignite.events(ignite.cluster().forCacheNodes(CACHE_NAME)).remoteListen(locLsnr, rmtLsnr,
                            EVT_CACHE_OBJECT_PUT, EVT_CACHE_OBJECT_READ, EVT_CACHE_OBJECT_REMOVED);

                    for (int i = 0; i < 20; i++)
                        cache.put(i, Integer.toString(i));

                    Thread.sleep(2000);
                }finally {
                    ignite.destroyCache(CACHE_NAME);
                }
            }
        }
    }

