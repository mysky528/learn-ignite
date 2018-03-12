package com.mycookcode.bigData.ignite.datagrid.startschema;

import org.apache.ignite.cache.query.annotations.QuerySqlField;

/**
 * 采购记录事实表
 *
 * Created by zhaolu on 2018/3/9.
 */
public class FactPurchase {

    @QuerySqlField(index = true)
    private  int id;

    @QuerySqlField
    private int storeId;

    @QuerySqlField
    private int productId;

    @QuerySqlField
    private float purchasePrice;

    public FactPurchase(int id, int productId, int storeId, float purchasePrice) {
        this.id = id;
        this.productId = productId;
        this.storeId = storeId;
        this.purchasePrice = purchasePrice;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public float getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(float purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    @Override public String toString() {
        return "FactPurchase [id=" + id +
                ", productId=" + productId +
                ", storeId=" + storeId +
                ", purchasePrice=" + purchasePrice + ']';
    }
}
