package com.mycookcode.bigData.ignite.computegrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

/**
 * 所有的call(...)和run(...)方法都可以在集群或者集群组内既可以执行单独的作业也可以执行作业的集合
 *
 * Created by zhaolu on 2018/2/10.
 */
public class ComputeRunnableExample {

    public static void main(String[] args) throws IgniteException
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println("Compute runnable example started.");

            IgniteCompute compute = ignite.compute();

            for(final String word: "Print words using runnable".split(" "))
            {
                compute.run(() -> {
                    System.out.println();
                    System.out.println(">>> Printing '" + word + "' on this node from ignite job.");
                });
            }

            System.out.println();
            System.out.println(">>> Finished printing words using runnable execution.");
            System.out.println(">>> Check all nodes for output (this node is also part of the cluster).");
        }
    }
}
