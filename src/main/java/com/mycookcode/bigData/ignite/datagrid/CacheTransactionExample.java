package com.mycookcode.bigData.ignite.datagrid;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.transactions.Transaction;

import java.io.Serializable;

import static org.apache.ignite.transactions.TransactionConcurrency.PESSIMISTIC;
import static org.apache.ignite.transactions.TransactionIsolation.REPEATABLE_READ;

/**
 * 本类是缓存相关实务操作的演示
 */
public class CacheTransactionExample {

    private static final String CACHE_NAME = CacheTransactionExample.class.getSimpleName();



    public static void main(String[] args) throws IgniteException
    {
        try (Ignite ignite = Ignition.start("example-ignite.xml")){
            System.out.println();
            System.out.println(">>> Cache transaction example started.");

            CacheConfiguration<Integer, Account> cfg = new CacheConfiguration<>(CACHE_NAME);
            cfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);

            try (IgniteCache<Integer, Account> cache = ignite.getOrCreateCache(cfg)) {
                // Initialize.
                cache.put(1, new Account(1, 100));
                cache.put(2, new Account(1, 200));

                System.out.println();
                System.out.println(">>> Accounts before deposit: ");
                System.out.println(">>> " + cache.get(1));
                System.out.println(">>> " + cache.get(2));

                // Make transactional deposits.
                deposit(cache, 1, 100);
                deposit(cache, 2, 200);

                System.out.println();
                System.out.println(">>> Accounts after transfer: ");
                System.out.println(">>> " + cache.get(1));
                System.out.println(">>> " + cache.get(2));

                System.out.println(">>> Cache transaction example finished.");
            }finally {
                ignite.destroyCache(CACHE_NAME);
            }
        }


    }

    /**
     * 存入指定的账户中
     *
     * @param cache
     * @param acctId
     * @param amount
     */
    private static void deposit(IgniteCache<Integer, Account> cache, int acctId, double amount)
    {
        try (Transaction tx = Ignition.ignite().transactions().txStart(PESSIMISTIC,REPEATABLE_READ)){
            Account acct= cache.get(acctId);
            assert acct != null;
            acct.update(amount);
            cache.put(acctId, acct);
            tx.commit();
        }
        System.out.println();
        System.out.println(">>> Transferred amount: $" + amount);
    }

    /**
     * Account.
     */
    private static class Account implements Serializable {
        /** Account ID. */
        private int id;

        /** Account balance. */
        private double balance;

        /**
         * @param id Account ID.
         * @param balance Balance.
         */
        Account(int id, double balance) {
            this.id = id;
            this.balance = balance;
        }

        /**
         * Change balance by specified amount.
         *
         * @param amount Amount to add to balance (may be negative).
         */
        void update(double amount) {
            balance += amount;
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            return "Account [id=" + id + ", balance=$" + balance + ']';
        }
    }
}
