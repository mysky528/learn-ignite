package com.mycookcode.bigData.ignite.datagrid;

import com.mycookcode.bigData.ignite.model.Organization;
import com.mycookcode.bigData.ignite.model.Person;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.affinity.AffinityKey;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.TextQuery;
import org.apache.ignite.lang.IgniteBiPredicate;

import javax.cache.Cache;

/**
 * 缓存查询
 *
 * Created by zhaolu on 2018/1/29.
 */
public class CacheQueryExample {

    private static final String ORG_CACHE = CacheQueryExample.class.getSimpleName() + "Organizations";


    private static final String PERSON_CACHE = CacheQueryExample.class.getSimpleName() + "Persons";


    /**
     * 扫描查询可以通过客户定义的谓词以分布式的形式进行缓存查询
     */
    private static void scanQuery()
    {
        IgniteCache<BinaryObject,BinaryObject> cache = Ignition.ignite().cache(PERSON_CACHE).withKeepBinary();

        ScanQuery<BinaryObject,BinaryObject> scan = new ScanQuery<>(
                new IgniteBiPredicate<BinaryObject, BinaryObject>() {
                    @Override
                    public boolean apply(BinaryObject key, BinaryObject person) {

                        return person.<Double>field("salary") <= 1000;
                    }
                }
        );

        print("People with salaries between 0 and 1000 (queried with SCAN query): ", cache.query(scan).getAll());
    }


    /**
     * 文本查询
     */
    private static void textQuery()
    {
        IgniteCache<Long, Person> cache = Ignition.ignite().cache(PERSON_CACHE);

        //  Query for all people with "Master Degree" in their resumes.
        QueryCursor<Cache.Entry<Long, Person>> masters =
                cache.query(new TextQuery<Long, Person>(Person.class, "Master"));

        // Query for all people with "Bachelor Degree" in their resumes.
        QueryCursor<Cache.Entry<Long, Person>> bachelors =
                cache.query(new TextQuery<Long, Person>(Person.class, "Bachelor"));

        print("Following people have 'Master Degree' in their resumes: ", masters.getAll());
        print("Following people have 'Bachelor Degree' in their resumes: ", bachelors.getAll());
    }


    /**
     * 产生初始化数据
     */
    private static void initialize()
    {
        IgniteCache<Long, Organization> orgCache = Ignition.ignite().cache(ORG_CACHE);
        //运行前先清空缓存
        orgCache.clear();
        // Organizations.
        Organization org1 = new Organization("ApacheIgnite");
        Organization org2 = new Organization("Other");

        orgCache.put(org1.id(), org1);
        orgCache.put(org2.id(), org2);


        IgniteCache<AffinityKey<Long>, Person> colPersonCache = Ignition.ignite().cache(PERSON_CACHE);

        colPersonCache.clear();
        Person p1 = new Person(org1, "John", "Doe", 2000, "John Doe has Master Degree.");
        Person p2 = new Person(org1, "Jane", "Doe", 1000, "Jane Doe has Bachelor Degree.");
        Person p3 = new Person(org2, "John", "Smith", 1000, "John Smith has Bachelor Degree.");
        Person p4 = new Person(org2, "Jane", "Smith", 2000, "Jane Smith has Master Degree.");

        colPersonCache.put(p1.key(), p1);
        colPersonCache.put(p2.key(), p2);
        colPersonCache.put(p3.key(), p3);
        colPersonCache.put(p4.key(), p4);

    }

    private static void print(String msg,Iterable<?> col)
    {
        print(msg);
        print(col);
    }

    private static void print(String msg)
    {
        System.out.println();
        System.out.println(">>> " + msg);
    }

    private static void print(Iterable<?> col) {
        for (Object next : col)
            System.out.println(">>>     " + next);
    }
}

