package com.mycookcode.bigData.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteCallable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by zhaolu on 2017/12/13.
 */
public class IgniteComputeApplication {

    public static void main( String[] args ) throws Exception
    {
        try (Ignite ignite = Ignition.start("/Users/zhaolu/Code/java/apache-ignite-fabric-2.3.0-bin/config/default-config.xml")) {
            Collection<IgniteCallable<Integer>> calls = new ArrayList<>();

            // Iterate through all the words in the sentence and create Callable jobs.
            for (final String word : "Count characters using callable".split(" "))
                calls.add(word::length);

            // Execute collection of Callables on the grid.
            Collection<Integer> res = ignite.compute().call(calls);

            // Add up all the results.
            int sum = res.stream().mapToInt(Integer::intValue).sum();

            System.out.println("Total number of characters is '" + sum + "'.");
        }
    }
}
