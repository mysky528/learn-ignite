package com.mycookcode.bigData.ignite.streaming;

import com.mycookcode.bigData.ignite.ExamplesUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.stream.StreamTransformer;

import java.util.List;
import java.util.Random;

/**
 * 演示将数据以流的方式添加到缓存中
 * 
 * Created by zhaolu on 2018/2/4.
 */
public class StreamTransformerExample {

    /*生成随机数*/
    private static final Random RAND = new Random();

    /*生成随机数的范围*/
    private static final int RANGE = 1000;

    private static final String CACHE_NAME = "randomNumbers";

    public static void main(String[] args)throws Exception
    {
        //做为集群的客户端
        Ignition.setClientMode(true);

        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            //没有服务节点退出
            if(!ExamplesUtils.hasServerNodes(ignite))
            {
                return;
            }

            CacheConfiguration<Integer,Long> cfg = new CacheConfiguration<>(CACHE_NAME);
            //谁索引的key和value
            cfg.setIndexedTypes(Integer.class,Long.class);
            try(IgniteCache<Integer,Long> smtCache = ignite.getOrCreateCache(cfg))
            {
                //流接收器可以以并置的方式直接在缓存该数据条目的节点上对数据流做出反应，可以在数据进入缓存之前修改数据或者在数据上添加任何的预处理逻辑
                try (IgniteDataStreamer<Integer,Long> stmr = ignite.dataStreamer(smtCache.getName())){
                        //允许所有的数据进行更新
                        stmr.allowOverwrite(true);

                        stmr.receiver(StreamTransformer.from((e,arg) -> {
                        //获得当前值
                        Long val = e.getValue();

                        e.setValue(val == null?1L:val+1);
                        return null;
                    }));

                    for(int i = 1;i<=10_000_000;i++){
                        stmr.addData(RAND.nextInt(RANGE),1L);

                        if(i % 500_000 == 0)
                        System.out.println("Number of tuples streamed into Ignite: " + i);
                    }

                }


                SqlFieldsQuery top10Qry = new SqlFieldsQuery("select _key, _val from Long order by _val desc limit 10");
                List<List<?>> top10 = smtCache.query(top10Qry).getAll();
                System.out.println("Top 10 most popular numbers:");
                ExamplesUtils.printQueryResults(top10);
            }finally {
               ignite.destroyCache(CACHE_NAME);
            }
        }
    }
}
