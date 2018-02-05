package com.mycookcode.bigData.ignite.streaming.wordcount;

import com.mycookcode.bigData.ignite.ExamplesUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.affinity.AffinityUuid;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;

import java.util.List;

/**
 * Created by zhaolu on 2018/2/5.
 */
public class QueryWords {


    public static void main(String[] args)throws Exception
    {
        Ignition.setClientMode(true);
        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            if(!ExamplesUtils.hasServerNodes(ignite))
            {
                return;
            }

            CacheConfiguration<AffinityUuid, String> cfg = CacheConfig.wordCache();

            try(IgniteCache<AffinityUuid,String> stmCache = ignite.getOrCreateCache(cfg)){
                //查询前10
                SqlFieldsQuery top10Qry = new SqlFieldsQuery(
                        "select _val, count(_val) as cnt from String group by _val order by cnt desc limit 10",
                        true /*collocated*/
                );
                //计算最大值、平均值、最小值
                SqlFieldsQuery statsQry = new SqlFieldsQuery(
                        "select avg(cnt), min(cnt), max(cnt) from (select count(_val) as cnt from String group by _val)");

                while(true)
                {
                    List<List<?>> top10 = stmCache.query(top10Qry).getAll();
                    List<List<?>> stats = stmCache.query(statsQry).getAll();

                    List<?> row = stats.get(0);

                    if (row.get(0) != null)
                        System.out.printf("Query results [avg=%d, min=%d, max=%d]%n",
                                row.get(0), row.get(1), row.get(2));

                    // Print top 10 words.
                    ExamplesUtils.printQueryResults(top10);
                    Thread.sleep(5000);
                }
            }finally {
                ignite.destroyCache(cfg.getName());
            }
        }
    }
}
