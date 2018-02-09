package com.mycookcode.bigData.ignite.streaming.wordcount.socket;

import com.mycookcode.bigData.ignite.ExamplesUtils;
import com.mycookcode.bigData.ignite.streaming.wordcount.CacheConfig;
import org.apache.ignite.*;
import org.apache.ignite.cache.affinity.AffinityUuid;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteBiTuple;
import org.apache.ignite.stream.StreamSingleTupleExtractor;
import org.apache.ignite.stream.socket.SocketMessageConverter;
import org.apache.ignite.stream.socket.SocketStreamer;
import org.apache.ignite.IgniteException;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.Map;

/**
 * 演示在组件外部将数据以流的方式加载到ignite cache中(Ignite客户端接受外部数据并保存到缓存中)
 * Created by zhaolu on 2018/2/9.
 */
public class WordsSocketStreamerServer {

    private static final int PORT = 5555;

    private static final byte[] DELIM = new byte[] {0};

    public static void main(String[] args) throws Exception
    {
        Ignition.setClientMode(true);
        CacheConfiguration<AffinityUuid, String> cfg = CacheConfig.wordCache();

        try(Ignite ignite = Ignition.start("example-ignite.xml")){
                if(!ExamplesUtils.hasServerNodes(ignite))
                    return;

            //cfg配数据流的窗口期
            try(IgniteCache<AffinityUuid,String> stmCache = ignite.getOrCreateCache(cfg))
            {
                IgniteDataStreamer<AffinityUuid, String> stmr = ignite.dataStreamer(stmCache.getName());
                InetAddress addr = InetAddress.getLocalHost();
                //配置socket的数据流
                SocketStreamer<String,AffinityUuid,String> sockStmr = new SocketStreamer<>();
                sockStmr.setAddr(addr);
                sockStmr.setPort(PORT);
                sockStmr.setDelimiter(DELIM);
                sockStmr.setIgnite(ignite);
                sockStmr.setStreamer(stmr);
                sockStmr.setConverter(
                        new SocketMessageConverter<String>() {
                            @Override
                            public String convert(byte[] bytes) {

                                return null;
                            }
                        }
                );

                sockStmr.setSingleTupleExtractor(new StreamSingleTupleExtractor<String, AffinityUuid, String>() {
                    @Override public Map.Entry<AffinityUuid, String> extract(String word) {
                        return new IgniteBiTuple<>(new AffinityUuid(word), word);
                    }
                });

                sockStmr.start();
            } catch (IgniteException e) {
                System.err.println("Streaming server didn't start due to an error: ");

                e.printStackTrace();
            }
            finally {
                ignite.destroyCache(cfg.getName());
            }

        }
    }

}
