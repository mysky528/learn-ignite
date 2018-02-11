package com.mycookcode.bigData.ignite.computegrid.montecarlo;

import java.util.Arrays;
import java.util.Random;

/**
 * 计算风险信用因子
 *
 * Created by zhaolu on 2018/2/11.
 */
public class CreditRiskManager {

    private static Random rndGen = new Random();


    /**
     * 计算给定信贷组合的信贷风险
     * @param portfolio 信用组合
     * @param horizon 预测期
     * @param num 抽样数量
     * @param percentile
     * @return
     */
    public double calculateCreditRiskMonteCarlo(Credit[] portfolio, int horizon, int num, double percentile)
    {
        System.out.println(">>> Calculating credit risk for portfolio [size=" + portfolio.length + ", horizon=" +
                horizon + ", percentile=" + percentile + ", iterations=" + num + "] <<<");

        long start = System.currentTimeMillis();

        double[] losses = calculateLosses(portfolio, horizon, num);

        Arrays.sort(losses);

        double[] lossProbs = new double[losses.length];

        for (int i = 0; i < losses.length; i++)
            if (i == 0)
                // First time it's just a probability of first value.
                lossProbs[i] = getLossProbability(losses, 0);
            else if (losses[i] != losses[i - 1])
                // Probability of this loss plus previous one.
                lossProbs[i] = getLossProbability(losses, i) + lossProbs[i - 1];
            else
                // The same loss the same probability.
                lossProbs[i] = lossProbs[i - 1];

        // Count percentile.
        double crdRisk = 0;

        for (int i = 0; i < lossProbs.length; i++)
            if (lossProbs[i] > percentile) {
                crdRisk = losses[i - 1];

                break;
            }
        System.out.println(">>> Finished calculating portfolio risk [risk=" + crdRisk +
                ", time=" + (System.currentTimeMillis() - start) + "ms]");


        return crdRisk;
    }

    /**
     * 使用蒙特卡洛模拟计算给定信贷组合的损失
     * @param portfolio 投资组合信用组合
     * @param horizon 预测期
     * @param num 模特卡诺迭代数量
     * @return
     */
    private double[] calculateLosses(Credit[] portfolio, int horizon, int num)
    {
        double[] losses = new double[num];
        for (int i = 0; i < num; i++)
        {
            for (Credit crd : portfolio)
            {
                int remDays = Math.min(crd.getRemainingTerm(), horizon);
                if (rndGen.nextDouble() >= 1 - crd.getDefaultProbability(remDays))
                {
                    // (1 + 'r' * min(H, W) / 365) * S.
                    // Where W is a horizon, H is a remaining crediting term, 'r' is an annual credit rate,
                    // S is a remaining credit amount.
                    losses[i] += (1 + crd.getAnnualRate() * Math.min(horizon, crd.getRemainingTerm()) / 365)
                            * crd.getRemainingAmount();
                }else
                {
                    // - 'r' * min(H,W) / 365 * S
                    // Where W is a horizon, H is a remaining crediting term, 'r' is a annual credit rate,
                    // S is a remaining credit amount.
                    losses[i] -= crd.getAnnualRate() * Math.min(horizon, crd.getRemainingTerm()) / 365 *
                            crd.getRemainingAmount();
                }
            }
        }
        return losses;
    }

    /**
     * 计算损失概率
     *
     * @param losses 损失集合
     * @param i 集合中某个损失元素的索引
     * @return 损失概率
     */
    private double getLossProbability(double[] losses, int i)
    {
        double cnt = 0;
        double loss = losses[i];

        for(double tmp:losses)
        {
            if(tmp == loss)
            {
                cnt++;
            }
        }
        return cnt++ / losses.length;
    }
}
