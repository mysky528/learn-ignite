package com.mycookcode.bigData.ignite.computegrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

import java.util.Collection;

/**
 * 演示在集群中广播计算.所有的broadcast(...)方法会将一个给定的作业广播到所有的集群节点或者集群组
 *
 * Created by zhaolu on 2018/2/10.
 */
public class ComputeBroadcastExample {


    public static void main(String[] args)throws IgniteException
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Compute broadcast example started.");

            hello(ignite);
            gatherSystemInfo(ignite);
        }
    }

    /**
     * 在所有的集群节点中打印hello信息
     * @param ignite
     */
    private static void hello(Ignite ignite)
    {
        ignite.compute().broadcast(()->{ System.out.println();
                    System.out.println(">>> Hello Node! :)");}
        );

        System.out.println();
        System.out.println(">>> Check all nodes for hello message output.");
    }

    /**
     * 获得所有节点的信息并打印输出
     *
     * @param ignite
     * @throws IgniteException
     */
    private static void gatherSystemInfo(Ignite ignite) throws IgniteException
    {
        Collection<String> res = ignite.compute().broadcast(()->{
            System.out.println();
            System.out.println("Executing task on node: " + ignite.cluster().localNode().id());
            return "Node ID: " + ignite.cluster().localNode().id() + "\n" +
                    "OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " +
                    System.getProperty("os.arch") + "\n" +
                    "User: " + System.getProperty("user.name") + "\n" +
                    "JRE: " + System.getProperty("java.runtime.name") + " " +
                    System.getProperty("java.runtime.version");
        });

        System.out.println();
        System.out.println("Nodes system information:");
        System.out.println();

        res.forEach(r -> {
            System.out.println(r);
            System.out.println();
        });

    }
}
