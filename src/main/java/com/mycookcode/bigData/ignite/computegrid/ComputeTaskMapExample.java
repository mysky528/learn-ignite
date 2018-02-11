package com.mycookcode.bigData.ignite.computegrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.ComputeJob;
import org.apache.ignite.compute.ComputeJobAdapter;
import org.apache.ignite.compute.ComputeJobResult;
import org.apache.ignite.compute.ComputeTaskAdapter;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**ComputeTask定义了要在集群内执行的作业以及这些作业到节点的映射，他还定义了如何处理作业的返回值(Reduce)。
 * 所有的IgniteCompute.execute(...)方法都会在集群上执行给定的任务，应用只需要实现ComputeTask接口的map(...)和reduce(...)方法即可
 *
 * 只有当需要对作业到节点的映射做细粒度控制或者对故障转移进行定制的时候，才使用ComputeTask。
 * 对于所有其他的场景，都需要使用分布式闭包中介绍的集群内闭包执行来实现
 *
 * Created by zhaolu on 2018/2/10.
 */
public class ComputeTaskMapExample {


    public static void main(String[] args) throws IgniteException
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println("Compute task map example started.");

            //在集群上执行任务
            int cnt = ignite.compute().execute(MapExampleCharacterCountTask.class,"Hello Ignite Enabled World!");
            System.out.println();
            System.out.println(">>> Total number of characters in the phrase is '" + cnt + "'.");
            System.out.println(">>> Check all nodes for output (this node is also part of the cluster).");
        }
    }

    /**
     * 统计单词任务
     * ComputeTaskAdapter定义了一个默认的result(...)方法实现，
     * 他在当一个作业抛出异常时返回一个FAILOVER策略，否则会返回一个WAIT策略，
     * 这样会等待所有的作业完成，并且有结果
     */
    private static class MapExampleCharacterCountTask extends ComputeTaskAdapter<String,Integer>
    {

        @Override public Map<? extends ComputeJob,ClusterNode> map(List<ClusterNode> nodes, String arg)
        {
            Map<ComputeJob, ClusterNode> map = new HashMap<>();
            Iterator<ClusterNode> it = nodes.iterator();
            for (final String word : arg.split(" "))
            {
                if (!it.hasNext())
                    it = nodes.iterator();
                ClusterNode node = it.next();

                map.put(new ComputeJobAdapter() {
                    @Nullable @Override public Object execute() {
                        System.out.println();
                        System.out.println(">>> Printing '" + word + "' on this node from ignite job.");

                        // Return number of letters in the word.
                        return word.length();
                    }
                }, node);
            }
            return map;
        }

        @Nullable
        @Override public Integer reduce(List<ComputeJobResult> results)
        {
            int sum = 0;
            for(ComputeJobResult res : results)
            {
                sum += res.<Integer>getData();
            }
            return sum;
        }
    }
}
