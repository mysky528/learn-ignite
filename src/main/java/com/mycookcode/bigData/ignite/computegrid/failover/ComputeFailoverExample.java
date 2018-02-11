package com.mycookcode.bigData.ignite.computegrid.failover;

import com.mycookcode.bigData.ignite.ExamplesUtils;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.compute.ComputeJobFailoverException;
import org.apache.ignite.compute.ComputeTaskSession;
import org.apache.ignite.compute.ComputeTaskSessionFullSupport;
import org.apache.ignite.lang.IgniteBiTuple;
import org.apache.ignite.lang.IgniteClosure;
import org.apache.ignite.resources.LoggerResource;
import org.apache.ignite.resources.TaskSessionResource;

import java.util.Arrays;
import java.util.List;

/**
 * 演示故障自动转移的例子
 *
 * Created by zhaolu on 2018/2/11.
 */
public class ComputeFailoverExample {


    public static void main(String[] args) throws IgniteException {
        try (Ignite ignite = Ignition.start(ComputeFailoverNodeStartup.configuration())) {
            if (!ExamplesUtils.checkMinTopologySize(ignite.cluster(), 2))
                return;

            System.out.println();
            System.out.println("Compute failover example started.");

            // Number of letters.
            int charCnt = ignite.compute().apply(new CheckPointJob(), "Stage1 Stage2");

            System.out.println();
            System.out.println(">>> Finished executing fail-over example with checkpoints.");
            System.out.println(">>> Total number of characters in the phrase is '" + charCnt + "'.");
            System.out.println(">>> You should see exception stack trace from failed job on some node.");
            System.out.println(">>> Failed job will be failed over to another node.");
        }
    }

    @ComputeTaskSessionFullSupport
    private static final class CheckPointJob implements IgniteClosure<String, Integer>
    {
        /*注入分布式任务会话*/
        @TaskSessionResource
        private ComputeTaskSession jobSes;

        @LoggerResource
        private IgniteLogger log;

        private IgniteBiTuple<Integer, Integer> state;

        private String phrase;

        /**
         * 使用key来设置检查点，使用抛出异常模拟节点失败
         * @param phrase
         * @return
         */
        @Override public Integer apply(String phrase)
        {
            System.out.println();
            System.out.println(">>> Executing fail-over example job.");
            this.phrase = phrase;

            List<String> words = Arrays.asList(phrase.split(" "));
            final String cpKey = checkpointKey();

            IgniteBiTuple<Integer, Integer> state = jobSes.loadCheckpoint(cpKey);
            int idx = 0;
            int sum = 0;
            if (state != null) {
                this.state = state;

               //获得最后出来的单词索引和长度
                idx = state.get1();
                sum = state.get2();
            }
            for(int i = idx; i < words.size(); i++)
            {
                sum += words.get(i).length();
                this.state = new IgniteBiTuple<>(i + 1, sum);

                //保存任务检查点，如果任务成功完成会自动移除
                jobSes.saveCheckpoint(cpKey, this.state);

                //模拟节点计算失败
                if (i == 0) {
                    System.out.println();
                    System.out.println(">>> Job will be failed over to another node.");

                    throw new ComputeJobFailoverException("Expected example job exception.");
                }
            }



            return sum;
        }

        private String checkpointKey() {
            return getClass().getName() + '-' + phrase;
        }

    }
}
