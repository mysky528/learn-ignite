package com.mycookcode.bigData.ignite.datagrid.store.jdbc;

import com.mycookcode.bigData.ignite.model.Person;
import org.apache.ignite.cache.store.CacheStoreAdapter;
import org.apache.ignite.cache.store.CacheStoreSession;
import org.apache.ignite.lang.IgniteBiInClosure;
import org.apache.ignite.resources.CacheStoreSessionResource;

import javax.cache.Cache;
import javax.cache.integration.CacheLoaderException;
import javax.cache.integration.CacheWriterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 使用JDBC和缓存事务实现CacheStore的例子
 *
 * Created by zhaolu on 2018/3/5.
 */
public class CacheJdbcPersonStore extends CacheStoreAdapter<Long,Person> {

    @CacheStoreSessionResource
    private CacheStoreSession ses;


    @Override
    public Person load(Long key)
    {
        System.out.println(">>> Store load [key=" + key + ']');
        Connection conn = ses.attachment();
        try(PreparedStatement st = conn.prepareStatement("select * from PERSON where id = ?"))
        {
            st.setString(1, key.toString());
            ResultSet rs = st.executeQuery();
            return rs.next() ? new Person(rs.getLong(1), rs.getString(2), rs.getString(3)) : null;
        }catch (SQLException e)
        {
            throw new CacheLoaderException("Failed to load object [key=" + key + ']', e);
        }

    }

    @Override
    public void write(Cache.Entry<? extends Long, ? extends Person> entry)
    {
        Long key = entry.getKey();
        Person val = entry.getValue();

        System.out.println(">>> Store write [key=" + key + ", val=" + val + ']');

        try{
            Connection conn = ses.attachment();
            int updated;

            try (PreparedStatement st = conn.prepareStatement(
                    "update PERSON set first_name = ?, last_name = ? where id = ?")) {
                st.setString(1, val.firstName);
                st.setString(2, val.lastName);
                st.setLong(3, val.id);

                updated = st.executeUpdate();
            }

            if(updated == 0)
            {
                try(PreparedStatement st = conn.prepareStatement(
                        "insert into PERSON (id, first_name, last_name) values (?, ?, ?)")){
                    st.setLong(1, val.id);
                    st.setString(2, val.firstName);
                    st.setString(3, val.lastName);

                    st.executeUpdate();
                }
            }
        }catch (SQLException e){
            throw new CacheWriterException("Failed to write object [key=" + key + ", val=" + val + ']', e);
        }
    }


    @Override
    public void delete(Object key)
    {
        System.out.println(">>> Store delete [key=" + key + ']');
        Connection conn = ses.attachment();

        try (PreparedStatement st = conn.prepareStatement("delete from PERSON where id=?")) {
            st.setLong(1, (Long)key);

            st.executeUpdate();
        }

        catch (SQLException e)
        {
            throw new CacheWriterException("Failed to delete object [key=" + key + ']', e);
        }
    }


    @Override
    public void loadCache(IgniteBiInClosure<Long, Person> clo, Object... args)
    {
        if (args == null || args.length == 0 || args[0] == null)
            throw new CacheLoaderException("Expected entry count parameter is not provided.");

        final int entryCnt = (Integer)args[0];

        Connection conn = ses.attachment();

        try (PreparedStatement stmt = conn.prepareStatement("select * from PERSON limit ?"))
        {
            stmt.setInt(1, entryCnt);
            ResultSet rs = stmt.executeQuery();
            int cnt = 0;
            while(rs.next())
            {
                Person person = new Person(rs.getLong(1), rs.getString(2), rs.getString(3));
                clo.apply(person.id, person);
                cnt++;
            }
            System.out.println(">>> Loaded " + cnt + " values into cache.");
        }catch (SQLException e)
        {
            throw new CacheLoaderException("Failed to load values from cache store.", e);
        }
    }
}
