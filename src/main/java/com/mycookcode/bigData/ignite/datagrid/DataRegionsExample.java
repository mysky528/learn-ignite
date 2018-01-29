package com.mycookcode.bigData.ignite.datagrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;

/**
 * 本例子演示如何调整ignite内存的特定设置
 *
 * Created by zhaolu on 2018/1/28.
 */
public class DataRegionsExample {


    /*在example-data-regions.xml配置文件定义默认内存的数据区域*/
    public static final String REGION_DEFAULT = "Default_Regio";

    /*数据区域的名称，该区域启用40M的内存区域*/
    public static final String REGION_40MB_EVICTION = "40MB_Region_Eviction";

    /*创建映射到内存映射文件的内存区*/
    public static final String REGION_30MB_MEMORY_MAPPED_FILE = "30MB_Region_Swapping";


    public static void main(String[] args)
    {
        try (Ignite ignite = Ignition.start("example-date-regions.xml")){
            System.out.println();
            System.out.println(">>> Data regions example started.");

            CacheConfiguration<Integer, Integer> firstCacheCfg = new CacheConfiguration<>("firstCache");
            firstCacheCfg.setDataRegionName(REGION_40MB_EVICTION);
            //内存模式：分区模式、复制模式、本地模式
            firstCacheCfg.setCacheMode(CacheMode.PARTITIONED);
            firstCacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

            CacheConfiguration<Integer, Integer> secondCacheCfg = new CacheConfiguration<>("secondCache");
            secondCacheCfg.setDataRegionName(REGION_40MB_EVICTION);
            secondCacheCfg.setCacheMode(CacheMode.REPLICATED); //复制模式
            secondCacheCfg.setAtomicityMode(CacheAtomicityMode.ATOMIC);

        }
    }




}
