package com.mycookcode.bigData.ignite.datastructures;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteSet;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.CollectionConfiguration;
import org.apache.ignite.lang.IgniteRunnable;


import java.util.UUID;

import static org.apache.ignite.cache.CacheAtomicityMode.TRANSACTIONAL;
import static org.apache.ignite.cache.CacheMode.PARTITIONED;

/**
 * 分布式集合的例子
 *
 * Created by zhaolu on 2018/2/9.
 */
public class IgniteSetExample {

    private static IgniteSet<String> set;

    public static void main(String[] args) throws Exception
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Ignite set example started.");

            String setName = UUID.randomUUID().toString();

            set = initializeSet(ignite, setName);
            writeToSet(ignite);
            clearAndRemoveSet();
        }
        System.out.println("Ignite set example finished.");
    }

    /**
     * 初始化集合
     *
     * @param ignite
     * @param setName
     * @return
     */
    private static IgniteSet<String> initializeSet(Ignite ignite,String setName)
    {
        CollectionConfiguration setCfg = new CollectionConfiguration();
        setCfg.setAtomicityMode(TRANSACTIONAL);
        setCfg.setCacheMode(PARTITIONED);

        //初始化一个新的集合
        IgniteSet<String> set = ignite.set(setName,setCfg);
        for(int i = 0;i < 10;i++)
        {
            set.add(Integer.toString(i));
        }

        System.out.println("Set size after initializing:" + set.size());
        return set;
    }

    /**
     * 写数据到集合中
     *
     * @param ignite
     */
    private static void writeToSet(Ignite ignite)
    {
       final String setName = set.name();
        //将数据写入到每个节点
        ignite.compute().broadcast(new SetClosure(setName));
        System.out.println("Set size after writing [expected=" + (10 + ignite.cluster().nodes().size() * 5) +
                ", actual=" + set.size() + ']');

        System.out.println("Iterate over set.");

        for(String item:set)
        {
            System.out.println("Set item: " + item);
        }

        if (!set.contains("0"))
            throw new RuntimeException("Set should contain '0' among its elements.");


        if (set.add("0"))
            throw new RuntimeException("Set should not allow duplicates.");

        if (!set.remove("0"))
            throw new RuntimeException("Set should correctly remove elements.");


        if (set.contains("0"))
            throw new RuntimeException("Set should not contain '0' among its elements.");

        if (!set.add("0"))
            throw new RuntimeException("Set should correctly add new elements.");
    }


    /**
     * 清空删除集合
     */
    private static void clearAndRemoveSet()
    {
        System.out.println("Set size before clearing: " + set.size());
        set.clear();
        System.out.println("Set size after clearing: " + set.size());

        set.close();
        System.out.println("Set was removed: " + set.removed());

        try {
            set.contains("1");
        }
        catch (IllegalStateException expected) {
            System.out.println("Expected exception - " + expected.getMessage());
        }
    }


    private static class SetClosure implements IgniteRunnable
    {

        private final String setName;

        /**
         * @param setName Set name.
         */
        SetClosure(String setName) {
            this.setName = setName;
        }

        @Override public void run()
        {
            IgniteSet<String> set = Ignition.ignite().set(setName, null);
            UUID locId = Ignition.ignite().cluster().localNode().id();
            for (int i = 0; i < 5; i++) {
                String item = locId + "_" + Integer.toString(i);

                set.add(item);

                System.out.println("Set item has been added: " + item);
            }
        }

    }
}
