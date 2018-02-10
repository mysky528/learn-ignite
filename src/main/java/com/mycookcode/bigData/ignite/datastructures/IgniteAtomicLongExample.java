package com.mycookcode.bigData.ignite.datastructures;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicLong;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteCallable;

import java.util.UUID;

/**
 * 演示简单的使用原子long型的操作
 *
 * Created by zhaolu on 2018/2/10.
 */
public class IgniteAtomicLongExample {

    private static final int RETRIES = 20;

    public static void main(String[] args) throws Exception
    {
        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            System.out.println();
            System.out.println(">>> Atomic long example started.");

            String atomicName = UUID.randomUUID().toString();

            //初始化atomic long
            final IgniteAtomicLong atomicLong = ignite.atomicLong(atomicName, 0, true);

            System.out.println();
            System.out.println("Atomic long initial value : " + atomicLong.get() + '.');

            //在所有的节点上对这个long型数据增加1
            ignite.compute().broadcast(new IgniteCallable<Object>() {
                @Override public Object call()
                {
                    for (int i = 0; i < RETRIES; i++)
                        System.out.println("AtomicLong value has been incremented: " + atomicLong.incrementAndGet());

                    return null;
                }
            });

            System.out.println();
            System.out.println("Atomic long value after successful CAS: " + atomicLong.get());
        }
    }
}
