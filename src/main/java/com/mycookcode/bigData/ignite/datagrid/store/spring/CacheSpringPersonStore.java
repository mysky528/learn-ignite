package com.mycookcode.bigData.ignite.datagrid.store.spring;

import com.mycookcode.bigData.ignite.model.Person;
import org.apache.ignite.cache.store.CacheStoreAdapter;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.h2.jdbcx.JdbcConnectionPool;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import javax.cache.integration.CacheLoaderException;
import javax.sql.DataSource;


import javax.cache.Cache;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * cache store spring jdbc例子
 *
 * Created by zhaolu on 2018/3/9.
 */
public class CacheSpringPersonStore extends CacheStoreAdapter<Long,Person> {

    /*数据源*/
    public static final DataSource DATA_SRC =
            JdbcConnectionPool.create("jdbc:h2:tcp://localhost/mem:ExampleDb", "sa", "");


    /*Spring jdbc template*/
    private JdbcTemplate jdbcTemplate;

    public CacheSpringPersonStore()
    {
        jdbcTemplate = new JdbcTemplate(DATA_SRC);
    }

    @Override
    public Person load(Long key)
    {
        System.out.println(">>> Store load [key=" + key + ']');
        try
        {
            return jdbcTemplate.queryForObject("select * from PERSON where id = ?", new RowMapper<Person>() {
                @Override
                public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new Person(rs.getLong(1), rs.getString(2), rs.getString(3));
                }
            },key);
        }catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    @Override
    public void write(Cache.Entry<? extends Long, ? extends Person> entry)
    {
        Long key = entry.getKey();
        Person val = entry.getValue();
        System.out.println(">>> Store write [key=" + key + ", val=" + val + ']');
        int updated = jdbcTemplate.update("update PERSON set first_name = ?, last_name = ? where id = ?",
                val.firstName, val.lastName, val.id);
        if (updated == 0) {
            jdbcTemplate.update("insert into PERSON (id, first_name, last_name) values (?, ?, ?)",
                    val.id, val.firstName, val.lastName);
        }

    }


    @Override
    public void delete(Object key)
    {
        System.out.println(">>> Store delete [key=" + key + ']');

        jdbcTemplate.update("delete from PERSON where id = ?", key);
    }

    @Override
    public void loadCache(final IgniteBiInClosure<Long, Person> clo, Object... args)
    {
        if(args == null || args.length == 0 || args[0] == null)
        {
            throw new CacheLoaderException("Expected entry count parameter is not provided.");
        }
        int entryCnt = (Integer)args[0];

        final AtomicInteger cnt = new AtomicInteger();

        jdbcTemplate.query("select * from PERSON limit ?", new RowCallbackHandler() {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                Person person = new Person(rs.getLong(1), rs.getString(2), rs.getString(3));
                clo.apply(person.id, person);
                cnt.incrementAndGet();
            }
        }, entryCnt);
        System.out.println(">>> Loaded " + cnt + " values into cache.");
    }

}
