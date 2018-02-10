package com.mycookcode.bigData.ignite.datastructures;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteAtomicReference;
import org.apache.ignite.Ignition;
import org.apache.ignite.lang.IgniteRunnable;

import java.util.UUID;

/**
 * 演示使用分布式的atomic reference
 *
 * Created by zhaolu on 2018/2/10.
 */
public class IgniteAtomicReferenceExample {

    public static void main(String[] args) throws Exception
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Atomic reference example started.");

            final String refName = UUID.randomUUID().toString();
            String val = UUID.randomUUID().toString();

            IgniteAtomicReference<String> ref = ignite.atomicReference(refName, val, true);
            System.out.println("Atomic reference initial value : " + ref.get() + '.');

            IgniteRunnable c = new ReferenceClosure(refName);
            ignite.compute().run(c);

            String newVal = UUID.randomUUID().toString();
            System.out.println("Try to change value of atomic reference with wrong expected value.");
            ref.compareAndSet("WRONG EXPECTED VALUE", newVal);

            ignite.compute().run(c);

            System.out.println("Try to change value of atomic reference with correct expected value.");
            ref.compareAndSet(val, newVal);

            ignite.compute().run(c);
        }

        System.out.println();
        System.out.println("Finished atomic reference example...");
        System.out.println("Check all nodes for output (this node is also part of the cluster).");
    }

    private static class ReferenceClosure implements IgniteRunnable
    {
        private final String refName;

        ReferenceClosure(String refName) {
            this.refName = refName;
        }

        @Override public void run() {
            IgniteAtomicReference<String> ref = Ignition.ignite().atomicReference(refName, null, true);

            System.out.println("Atomic reference value is " + ref.get() + '.');
        }
    }
}
