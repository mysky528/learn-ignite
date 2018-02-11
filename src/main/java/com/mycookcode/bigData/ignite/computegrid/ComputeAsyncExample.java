package com.mycookcode.bigData.ignite.computegrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteFuture;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 异步调用计算网格
 *
 * Created by zhaolu on 2018/2/10.
 */
public class ComputeAsyncExample {

    public static void main(String[] args) throws IgniteException
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println("Compute asynchronous example started.");

            IgniteCompute compute = ignite.compute().withAsync();
            Collection<IgniteFuture<?>> futs = new ArrayList<>();

            for (final String word : "Print words using runnable".split(" "))
            {
                compute.run(() -> {
                    System.out.println();
                    System.out.println(">>> Printing '" + word + "' on this node from ignite job.");
                });

                futs.add(compute.future());
            }

            futs.forEach(IgniteFuture::get);
            System.out.println();
            System.out.println(">>> Finished printing words using runnable execution.");
            System.out.println(">>> Check all nodes for output (this node is also part of the cluster).");
        }
    }
}
