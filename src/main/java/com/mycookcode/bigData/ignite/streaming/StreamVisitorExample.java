package com.mycookcode.bigData.ignite.streaming;

import com.mycookcode.bigData.ignite.ExamplesUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.cache.query.annotations.QuerySqlField;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.stream.StreamVisitor;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * StreamVisitor演示的例子，他会访问流中的每个键值组
 * 但是访问器不会更新缓存
 * Created by zhaolu on 2018/2/4.
 */
public class StreamVisitorExample {

    private static final Random RAND = new Random();

    private static final String[] INSTRUMENTS = {"IBM", "GOOG", "MSFT", "GE", "EBAY", "YHOO", "ORCL", "CSCO", "AMZN", "RHT"};

    private static final double[] INITIAL_PRICES = {194.9, 893.49, 34.21, 23.24, 57.93, 45.03, 44.41, 28.44, 378.49, 69.50};

    public static void main(String[] args) throws Exception
    {
        Ignition.setClientMode(true);
        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            if(!ExamplesUtils.hasServerNodes(ignite))
            {
                return;
            }
            CacheConfiguration<String, Double> mktDataCfg = new CacheConfiguration<>("marketTicks");

            CacheConfiguration<String, Instrument> instCfg = new CacheConfiguration<>("instCache");

            instCfg.setIndexedTypes(String.class,Instrument.class);

            try(IgniteCache<String,Double> mktCache = ignite.getOrCreateCache(mktDataCfg);
                IgniteCache<String,Instrument> instCache = ignite.getOrCreateCache(instCfg)
            ){
                try (IgniteDataStreamer<String,Double> mktStmr = ignite.dataStreamer(mktCache.getName())) {
                    mktStmr.receiver(StreamVisitor.from((cache, e) -> {
                        String symbol = e.getKey();
                        Double tick = e.getValue();

                        Instrument inst = instCache.get(symbol);

                        if (inst == null)
                            inst = new Instrument(symbol);

                        inst.update(tick);
                        instCache.put(symbol, inst);


                    }));

                    //产生1千万条的数据
                    for (int i = 1; i <= 10_000_000; i++) {
                        int idx = RAND.nextInt(INSTRUMENTS.length);

                        double price = round2(INITIAL_PRICES[idx] + RAND.nextGaussian());

                        mktStmr.addData(INSTRUMENTS[idx], price);

                        if (i % 500_000 == 0)
                            System.out.println("Number of tuples streamed into Ignite: " + i);
                    }
                }

                    SqlFieldsQuery top3qry = new SqlFieldsQuery(
                            "select symbol, (latest - open) from Instrument order by (latest - open) desc limit 3");

                    List<List<?>> top3 = instCache.query(top3qry).getAll();

                    System.out.println("Top performing financial instruments: ");

                    ExamplesUtils.printQueryResults(top3);

            }finally {
                ignite.destroyCache(mktDataCfg.getName());
                ignite.destroyCache(instCfg.getName());
            }

        }

    }


    private static double round2(double val) {
        return Math.floor(100 * val + 0.5) / 100;
    }

    /**
     * 财经工具类
     */
    public static class Instrument implements Serializable {
        /** Instrument symbol. */
        @QuerySqlField(index = true)
        private final String symbol;

        /** Open price. */
        @QuerySqlField(index = true)
        private double open;

        /** Close price. */
        @QuerySqlField(index = true)
        private double latest;

        /**
         * @param symbol Symbol.
         */
        public Instrument(String symbol) {
            this.symbol = symbol;
        }

        /**
         * Updates this instrument based on the latest market tick price.
         *
         * @param price Latest price.
         */
        public void update(double price) {
            if (open == 0)
                open = price;

            this.latest = price;
        }
    }
}
