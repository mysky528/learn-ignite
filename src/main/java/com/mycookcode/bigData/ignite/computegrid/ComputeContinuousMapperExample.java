package com.mycookcode.bigData.ignite.computegrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.compute.*;
import org.apache.ignite.resources.TaskContinuousMapperResource;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示持续计算例子
 *
 * Created by zhaolu on 2018/2/11.
 */
public class ComputeContinuousMapperExample {


    public static void main(String[] args) throws IgniteException
    {
        System.out.println();
        System.out.println(">>> Compute continuous mapper example started.");

        try(Ignite ignite = Ignition.start("example-ignite.xml")){
            int phraseLen = ignite.compute().execute(ContinuousMapperTask.class, "Hello Continuous Mapper");
            System.out.println();
            System.out.println(">>> Total number of characters in the phrase is '" + phraseLen + "'.");
        }
    }

    @ComputeTaskNoResultCache
    private static class ContinuousMapperTask extends ComputeTaskAdapter<String, Integer>
    {
        @TaskContinuousMapperResource
        private ComputeTaskContinuousMapper mapper;


        private final Queue<String> words = new ConcurrentLinkedQueue<>();

        private final AtomicInteger totalChrCnt = new AtomicInteger(0);

        @Override public Map<? extends ComputeJob, ClusterNode> map(List<ClusterNode> nodes, String phrase)
        {
            if (phrase == null || phrase.isEmpty())
                throw new IgniteException("Phrase is empty.");

            //填充数据
            Collections.addAll(words, phrase.split(" "));


            return null;
        }

        @Override public ComputeJobResultPolicy result(ComputeJobResult res, List<ComputeJobResult> rcvd)
        {
            // 如果出险错误切换到另外一个节点
            if (res.getException() != null)
                return super.result(res, rcvd);

            totalChrCnt.addAndGet(res.<Integer>getData());
            sendWord();

            return ComputeJobResultPolicy.WAIT;
        }

        @Override public Integer reduce(List<ComputeJobResult> results) {
            return totalChrCnt.get();
        }

        private void sendWord()
        {
            //从队列中获得第一个单词
            String word = words.poll();

            if(word != null)
            {
                //Map 下一个单词
                mapper.send(new ComputeJobAdapter(word)
                {
                    @Override public Object execute()
                    {
                        String word = argument(0);
                        System.out.println();
                        System.out.println(">>> Printing '" + word + "' from ignite job at time: " + new Date());
                        int cnt = word.length();
                        try {
                            Thread.sleep(1000);
                        }
                        catch (InterruptedException ignored) {
                            // No-op.
                        }
                        return cnt;
                    }
                });
            }
        }
    }
}
