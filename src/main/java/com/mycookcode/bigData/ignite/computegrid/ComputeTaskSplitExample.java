package com.mycookcode.bigData.ignite.computegrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobAdapter;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeTaskSplitAdapter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 演示任务分割适配器例子
 * ComputeTaskSplitAdapter继承了ComputeTaskAdapter,他增加了将作业自动分配给节点的功能
 * 它隐藏了map(...)方法然后增加了一个新的split(...)方法，使得开发者只需要提供一个待执行的作业集合
 * Created by zhaolu on 2018/2/11.
 */
public class ComputeTaskSplitExample {


    public static void main(String[] args) throws IgniteException
    {
        try(Ignite ignite = Ignition.start("example-ignite.xml"))
        {
            System.out.println();
            System.out.println("Compute task split example started.");

            int cnt = ignite.compute().execute(SplitExampleCharacterCountTask.class, "Hello Ignite Enabled World!");

            System.out.println();
            System.out.println(">>> Total number of characters in the phrase is '" + cnt + "'.");
            System.out.println(">>> Check all nodes for output (this node is also part of the cluster).");

        }
    }


    /**
     * 单词统计任务
     */
    private static class SplitExampleCharacterCountTask extends ComputeTaskSplitAdapter<String, Integer>
    {
        @Override protected Collection<? extends ComputeJob> split(int clusterSize, String arg)
        {
            Collection<ComputeJob> jobs = new LinkedList<>();
            for (final String word:arg.split(" "))
            {
                jobs.add(new ComputeJobAdapter() {
                    @Nullable @Override public Object execute() {
                        System.out.println();
                        System.out.println(">>> Printing '" + word + "' on this node from ignite job.");

                        // Return number of letters in the word.
                        return word.length();
                    }
                });
            }
            return jobs;
        }

        @Nullable
        @Override public Integer reduce(List<ComputeJobResult> results) {
            int sum = 0;

            for (ComputeJobResult res : results)
                sum += res.<Integer>getData();

            return sum;
        }
    }



}
