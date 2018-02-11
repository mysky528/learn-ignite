package com.mycookcode.bigData.ignite.servicegrid;

import com.mycookcode.bigData.ignite.ExamplesUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteServices;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.ServiceResource;

import java.util.Collection;

/**
 *
 * 分布式服务例子
 * 可以从集群内的任意节点访问已部署的服务实例
 * 如果一个服务部署在某个节点，那么本地部署的实例会被返回
 * 否则，如果服务不是本地的，那么会创建服务的一个远程代理
 *
 * Created by zhaolu on 2018/2/11.
 */
public class ServicesExample {


    public static void main(String[] args) throws Exception
    {
        Ignition.setClientMode(true);

        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            if (!ExamplesUtils.hasServerNodes(ignite))
                return;
            //只在服务端部署服务
            IgniteServices svcs = ignite.services(ignite.cluster().forServers());

            svcs.deployClusterSingleton("myClusterSingletonService", new SimpleMapServiceImpl());

            svcs.deployNodeSingleton("myNodeSingletonService", new SimpleMapServiceImpl());

            svcs.deployMultiple("myMultiService", new SimpleMapServiceImpl(),2,0);

            serviceProxyExample(ignite);

            serviceInjectionExample(ignite);
        }
    }

    /**
     * 使用服务代理远程调用部署的服务
     * 代理既可以是粘性的也可以是非粘性的
     * 如果代理是粘性的，Ignite会总是访问同一个集群节点的服务
     * 如果代理是非粘性的，那么Ignite会在服务部署的所有集群节点内对远程服务代理的调用进行负载平衡
     *
     * @param ignite
     * @throws Exception
     */
    private static void serviceProxyExample(Ignite ignite) throws Exception
    {
        System.out.println(">>>");
        System.out.println(">>> Starting service proxy example.");
        System.out.println(">>>");

        SimpleMapService mapSvc = ignite.services().serviceProxy("myClusterSingletonService",
                SimpleMapService.class,
                true);

        int cnt = 10;

        for (int i = 0; i < cnt; i++)
            mapSvc.put(i, Integer.toString(i));

        //从远程节点获取map大小
        int mapSize = mapSvc.size();

        System.out.println("Map service size: " + mapSize);

        if (mapSize != cnt)
            throw new Exception("Invalid map size [expected=" + cnt + ", actual=" + mapSize + ']');


    }


    /**
     * 注册代理服务到分布式闭包
     * @param ignite
     * @throws Exception
     */
    private static void serviceInjectionExample(Ignite ignite)throws Exception
    {
        System.out.println(">>>");
        System.out.println(">>> Starting service injection example.");
        System.out.println(">>>");


        SimpleMapService mapSvc = ignite.services().serviceProxy("myClusterSingletonService",
                SimpleMapService.class,
                true);

        int cnt = 10;
        //每个服务调用都会经过一个代理到远程集群单实例

        for (int i = 0; i < cnt; i++)
            mapSvc.put(i, Integer.toString(i));

        final Collection<Integer> mapSizes = ignite.compute().broadcast(new SimpleClosure());

        System.out.println("Closure execution result: " + mapSizes);

        for (int mapSize : mapSizes)
            if (mapSize != cnt)
                throw new Exception("Invalid map size [expected=" + cnt + ", actual=" + mapSize + ']');
    }


    private static class SimpleClosure implements IgniteCallable<Integer>
    {
        //注入服务代理
        @ServiceResource(serviceName = "myClusterSingletonService", proxyInterface = SimpleMapService.class)
        private transient SimpleMapService mapSvc;


        @Override
        public Integer call() throws Exception
        {
            int mapSize = mapSvc.size();
            System.out.println("Executing closure [mapSize=" + mapSize + ']');
            return mapSize;
        }
    }
}
