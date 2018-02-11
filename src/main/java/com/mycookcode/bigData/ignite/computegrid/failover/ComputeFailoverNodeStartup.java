package com.mycookcode.bigData.ignite.computegrid.failover;

import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.checkpoint.sharedfs.SharedFsCheckpointSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by zhaolu on 2018/2/11.
 */
public class ComputeFailoverNodeStartup {

    public static void main(String[] args) throws IgniteException {
        Ignition.start(configuration());
    }


    /**
     * 创建Ignite缓存检查点配置
     *
     * @return
     * @throws IgniteException
     */
    public static IgniteConfiguration configuration() throws IgniteException
    {
        IgniteConfiguration cfg = new IgniteConfiguration();
        cfg.setLocalHost("127.0.0.1");
        cfg.setPeerClassLoadingEnabled(true);

        //配置共享检查点
        SharedFsCheckpointSpi checkpointSpi = new SharedFsCheckpointSpi();
        checkpointSpi.setDirectoryPaths(Collections.singletonList("/Users/zhaolu/data/igniteCheckpoint"));
        cfg.setCheckpointSpi(checkpointSpi);

        TcpDiscoverySpi discoSpi = new TcpDiscoverySpi();

        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();

        ipFinder.setAddresses(Arrays.asList("127.0.0.1:47500..47509"));

        discoSpi.setIpFinder(ipFinder);

        cfg.setDiscoverySpi(discoSpi);
        return cfg;
    }
}
