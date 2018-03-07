package com.mycookcode.bigData.ignite.util;

import org.apache.ignite.IgniteException;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.RunScript;
import org.h2.tools.Server;

import java.io.IOException;
import java.io.StringReader;
import java.sql.SQLException;

/**
 * 启动H2数据库TCP服务器，以便从其他进程访问内存数据库中的示例
 * Created by zhaolu on 2018/3/2.
 */
public class DbH2ServerStartup {

    /*创建表的脚本*/
    private static final String CREATE_PERSON_TABLE =
            "create table if not exists PERSON(id bigint not null, first_name varchar(50), last_name varchar(50), PRIMARY KEY(id));";

    /*插入样本数据*/
    private static final String POPULATE_PERSON_TABLE =
            "delete from PERSON;\n" +
                    "insert into PERSON(id, first_name, last_name) values(1, 'Johannes', 'Kepler');\n" +
                    "insert into PERSON(id, first_name, last_name) values(2, 'Galileo', 'Galilei');\n" +
                    "insert into PERSON(id, first_name, last_name) values(3, 'Henry', 'More');\n" +
                    "insert into PERSON(id, first_name, last_name) values(4, 'Polish', 'Brethren');\n" +
                    "insert into PERSON(id, first_name, last_name) values(5, 'Robert', 'Boyle');\n" +
                    "insert into PERSON(id, first_name, last_name) values(6, 'Wilhelm', 'Leibniz');";

    /**
     * 向数据库注入样本数据
     */
    public static void populateDatabase() throws SQLException
    {
        JdbcConnectionPool dataSrc = JdbcConnectionPool.create("jdbc:h2:tcp://localhost/mem:ExampleDb", "sa", "");
        //创建Person表
        RunScript.execute(dataSrc.getConnection(), new StringReader(CREATE_PERSON_TABLE));

        //注入样本数据
        RunScript.execute(dataSrc.getConnection(), new StringReader(POPULATE_PERSON_TABLE));
    }

    /**
     * 启动H2数据库服务器
     * @param args
     * @throws IgniteException
     */
    public static void main(String[] args) throws IgniteException
    {
        try {
            Server.createTcpServer("-tcpDaemon").start();
            populateDatabase();
            JdbcConnectionPool dataSrc = JdbcConnectionPool.create("jdbc:h2:tcp://localhost/mem:ExampleDb", "sa", "");
            RunScript.execute(dataSrc.getConnection(), new StringReader(CREATE_PERSON_TABLE));
            RunScript.execute(dataSrc.getConnection(), new StringReader(POPULATE_PERSON_TABLE));
        }catch (SQLException e)
        {
            throw new IgniteException("Failed to start database TCP server", e);
        }
        try{
            do{
                System.out.println("Type 'q' and press 'Enter' to stop H2 TCP server...");
            }
            while ('q' != System.in.read());
        }catch (IOException e)
        {

        }

    }

}
