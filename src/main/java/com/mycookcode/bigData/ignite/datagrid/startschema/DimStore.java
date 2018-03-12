package com.mycookcode.bigData.ignite.datagrid.startschema;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * 商店维度
 *
 * Created by zhaolu on 2018/3/9.
 */
public class DimStore {

    @QuerySqlField(index = true)
    private int id;

    @QuerySqlField
    private String name;

    private String zip;

    private String addr;


    public DimStore(int id, String name, String zip, String addr) {
        this.id = id;
        this.name = name;
        this.zip = zip;
        this.addr = addr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    @Override public String toString() {
        return "DimStore [id=" + id +
                ", name=" + name +
                ", zip=" + zip +
                ", addr=" + addr + ']';
    }
}
