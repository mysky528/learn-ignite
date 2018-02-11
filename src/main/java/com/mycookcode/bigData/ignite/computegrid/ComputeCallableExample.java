package com.mycookcode.bigData.ignite.computegrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteCallable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 演示在集群上执行任务。所有的call(...)和run(...)方法都可以在集群或者集群组内既可以执行单独的作业也可以执行作业的集合。
 *
 * Created by zhaolu on 2018/2/10.
 */
public class ComputeCallableExample {

    public static void main(String[] args) throws IgniteException{
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Compute callable example started.");

            Collection<IgniteCallable<Integer>> calls = new ArrayList<>();

            for (String word : "Count characters using callable".split(" "))
            {
                calls.add(() -> {
                    System.out.println();
                    System.out.println(">>> Printing '" + word + "' on this node from ignite job.");

                    return word.length();
                });
            }

            Collection<Integer> res = ignite.compute().call(calls);
            int sum = res.stream().mapToInt(i -> i).sum();
            System.out.println();
            System.out.println(">>> Total number of characters in the phrase is '" + sum + "'.");
            System.out.println(">>> Check all nodes for output (this node is also part of the cluster).");
        }
    }
}
