package com.mycookcode.bigData.ignite.datagrid.startschema;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * 表示可购买的产品维度
 *
 * Created by zhaolu on 2018/3/9.
 */
public class DimProduct {

    /**主键*/
    @QuerySqlField(index=true)
    private int id;

    private String name;

    @QuerySqlField
    private float price;

    private int qty;

    public DimProduct(int id, String name, float price, int qty) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.qty = qty;
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

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    @Override public String toString() {
        return "DimProduct [id=" + id +
                ", name=" + name +
                ", price=" + price +
                ", qty=" + qty + ']';
    }
}
