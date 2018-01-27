package com.mycookcode.bigData.ignite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by zhaolu on 2017/12/12.
 */
public class IgniteJDBCApp {

    public static void main( String[] args ) throws Exception
    {

        //注册JDBC驱动
        Class.forName("org.apache.ignite.IgniteJdbcThinDriver");
        //打开JDBC连接
        Connection conn = DriverManager.getConnection("jdbc:ignite:thin://127.0.0.1/");
        //获取数据
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT p.name, c.name " +
                    " FROM Person p, City c " +
                    " WHERE p.city_id = c.id");
            while (rs.next())
            {
                System.out.println(rs.getString(1)+","+rs.getString(2));
            }
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            conn.close();
        }
    }
}
