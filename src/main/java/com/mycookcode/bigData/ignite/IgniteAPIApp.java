package com.mycookcode.bigData.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.FieldsQueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;

import java.util.Iterator;
import java.util.List;

/**
 * Created by zhaolu on 2017/12/12.
 */
public class IgniteAPIApp {

    public static void main( String[] args )
    {
        Ignite ignite = Ignition.start() ;

        IgniteCache<Long,City> cityCache = ignite.cache("SQL_PUBLIC_CITY");

        SqlFieldsQuery query = new SqlFieldsQuery("SELECT p.name, c.name " +
                " FROM Person p, City c WHERE p.city_id = c.id");

        FieldsQueryCursor<List<?>> cursor = cityCache.query(query);

        Iterator<List<?>> iterator = cursor.iterator();
        while (iterator.hasNext())
        {
            List<?> row = iterator.next();
            System.out.println(row.get(0) + ", " + row.get(1));
        }

    }

    public  class City
    {
        private Long id;

        private String name;

        public void setId(Long id)
        {
            this.id = id;
        }

        public Long getId()
        {
            return this.id;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }

    }
}
