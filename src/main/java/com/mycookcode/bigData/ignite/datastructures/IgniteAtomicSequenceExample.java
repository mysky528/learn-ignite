package com.mycookcode.bigData.ignite.datastructures;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicSequence;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteRunnable;

/**
 * 演示分布式原子性序列
 *
 * Created by zhaolu on 2018/2/10.
 */
public class IgniteAtomicSequenceExample {

    private static final int RETRIES = 20;


    public static void main(String[] args) throws Exception
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Cache atomic sequence example started.");

            ignite.compute().broadcast(new SequenceClosure("example-sequence"));

            System.out.println();
            System.out.println("Finished atomic sequence example...");
            System.out.println("Check all nodes for output (this node is also part of the cluster).");
            System.out.println();
        }
    }


    private static class SequenceClosure implements IgniteRunnable
    {
        private final String seqName;

        SequenceClosure(String seqName) {
            this.seqName = seqName;
        }

        @Override public void run()
        {
            IgniteAtomicSequence seq = Ignition.ignite().atomicSequence(seqName, 0, true);

            //获得在这个节点上的第一个值
            long firstVal = seq.get();
            System.out.println("Sequence initial value on local node: " + firstVal);

            for (int i = 0;i < RETRIES;i++)
            {
                System.out.println("Sequence [currentValue=" + seq.get() + ", afterIncrement=" +
                        seq.incrementAndGet() + ']');
            }
            System.out.println("Sequence after incrementing [expected=" + (firstVal + RETRIES) + ", actual=" +
                    seq.get() + ']');

        }
    }
}
