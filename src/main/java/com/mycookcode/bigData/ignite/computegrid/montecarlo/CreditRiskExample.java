package com.mycookcode.bigData.ignite.computegrid.montecarlo;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.lang.IgniteReducer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

/**
 * 分布式风险计算
 *
 * Created by zhaolu on 2018/2/11.
 */
public class CreditRiskExample {


    public static void main(String[] args) throws IgniteException {
        try (Ignite ignite = Ignition.start("example-ignite.xml")) {

            System.out.println();
            System.out.println("Credit risk example started.");

            Credit[] portfolio = new Credit[5000];

            Random rnd = new Random();

            //生成数据
            for (int i = 0; i < portfolio.length; i++) {
                portfolio[i] = new Credit(
                        50000 * rnd.nextDouble(), // Credit amount.
                        rnd.nextInt(1000), // Credit term in days.
                        rnd.nextDouble() / 10, // APR.
                        rnd.nextDouble() / 20 + 0.02 // EDF.
                );
            }

            int horizon = 365;
            int iter = 10000;
            double percentile = 0.95;

            long start = System.currentTimeMillis();

            double crdRisk = ignite.compute().call(jobs(ignite.cluster().nodes().size(), portfolio, horizon, iter, percentile),
                    new IgniteReducer<Double, Double>() {

                        private double sum;

                        private int cnt;

                        @Override
                        public synchronized boolean collect(@Nullable Double e) {
                            sum += e;
                            cnt++;
                            return true;
                        }

                        @Override
                        public synchronized Double reduce() {
                            return sum / cnt;
                        }
                    });

            System.out.println();
            System.out.println("Credit risk [crdRisk=" + crdRisk + ", duration=" +
                    (System.currentTimeMillis() - start) + "ms]");
        }
    }

    /**
     * 创建闭包风险计算任务
     *
     * @param clusterSize
     * @param portfolio
     * @param horizon
     * @param iter
     * @param percentile
     * @return
     */
    private static Collection<IgniteCallable<Double>> jobs(int clusterSize, final Credit[] portfolio,
                                                           final int horizon, int iter, final double percentile) {
        //每个节点完成的迭代次数
        int iterPerNode = Math.round(iter / (float) clusterSize);

        //唯一一个节点迭代的次数
        int lastNodeIter = iter - (clusterSize - 1) * iterPerNode;

        Collection<IgniteCallable<Double>> clos = new ArrayList<>(clusterSize);

        for (int i = 0; i < clusterSize; i++) {
            final int nodeIter = i == clusterSize - 1 ? lastNodeIter : iterPerNode;

            clos.add(new IgniteCallable<Double>() {
                @Override
                public Double call() {
                    return new CreditRiskManager().calculateCreditRiskMonteCarlo(
                            portfolio, horizon, nodeIter, percentile);
                }
            });
        }
        return clos;
    }
}