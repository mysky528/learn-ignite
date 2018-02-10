package com.mycookcode.bigData.ignite.datastructures;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCountDownLatch;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteRunnable;

import java.util.UUID;

/**
 * 演示倒计数锁存器
 *
 * Created by zhaolu on 2018/2/10.
 */
public class IgniteCountDownLatchExample {

    private static final int INITIAL_COUNT = 10;

    public static void main(String[] args)
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Cache atomic countdown latch example started.");

            final String latchName = UUID.randomUUID().toString();

            IgniteCountDownLatch latch = ignite.countDownLatch(latchName, INITIAL_COUNT, false, true);

            System.out.println("Latch initial value: " + latch.count());
            for (int i = 0; i < INITIAL_COUNT; i++)
                ignite.compute().run(new LatchClosure(latchName));
            latch.await();
            System.out.println("All latch closures have completed.");
        }
    }

    private static class   LatchClosure implements IgniteRunnable
    {
        private final String latchName;

        LatchClosure(String latchName) {
            this.latchName = latchName;
        }

        @Override public void run()
        {
            IgniteCountDownLatch latch = Ignition.ignite().countDownLatch(latchName, 1, false, true);

            int newCnt = latch.countDown();

            System.out.println("Counted down [newCnt=" + newCnt + ", nodeId=" + Ignition.ignite().cluster().localNode().id() + ']');
        }
    }


}
