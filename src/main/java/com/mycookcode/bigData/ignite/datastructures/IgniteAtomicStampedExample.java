package com.mycookcode.bigData.ignite.datastructures;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicStamped;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteRunnable;

import java.util.UUID;

/**
 * 演示分布式atomic stamped例子
 * Created by zhaolu on 2018/2/10.
 */
public class IgniteAtomicStampedExample {

    public static void main(String[] args) throws Exception
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Atomic stamped example started.");

            String stampedName = UUID.randomUUID().toString();

            String val = UUID.randomUUID().toString();

            String stamp = UUID.randomUUID().toString();

            IgniteAtomicStamped<String, String> stamped = ignite.atomicStamped(stampedName, val, stamp, true);
            System.out.println("Atomic stamped initial [value=" + stamped.value() + ", stamp=" + stamped.stamp() + ']');

            IgniteRunnable c = new StampedUpdateClosure(stampedName);

            ignite.compute().broadcast(c);

            String newVal = UUID.randomUUID().toString();
            String newStamp = UUID.randomUUID().toString();
            System.out.println("Try to change value and stamp of atomic stamped with wrong expected value and stamp.");
            stamped.compareAndSet("WRONG EXPECTED VALUE", newVal, "WRONG EXPECTED STAMP", newStamp);

            ignite.compute().run(c);
            System.out.println("Try to change value and stamp of atomic stamped with correct value and stamp.");
            stamped.compareAndSet(val, newVal, stamp, newStamp);
            ignite.compute().run(c);
        }

        System.out.println();
        System.out.println("Finished atomic stamped example...");
        System.out.println("Check all nodes for output (this node is also part of the cluster).");
    }

    private static class StampedUpdateClosure implements IgniteRunnable
    {
        private final String stampedName;

        StampedUpdateClosure(String stampedName) {
            this.stampedName = stampedName;
        }


        @Override public void run() {
            IgniteAtomicStamped<String, String> stamped = Ignition.ignite().
                    atomicStamped(stampedName, null, null, true);

            System.out.println("Atomic stamped [value=" + stamped.value() + ", stamp=" + stamped.stamp() + ']');
        }
    }
}
