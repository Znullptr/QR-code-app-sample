package com.example.qrcodemanager;

import java.io.Serializable;

public class Product implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
    private double price;
    private String qrdata;
    public Product(String name,double price,String qrdata){
        this.name=name;
        this.price=price;
        this.qrdata=qrdata;
    }
    public Product(String name,double price){
        this.name=name;
        this.price=price;
    }
    public String getName(){
        return name;
    }
    public double getPrice(){
        return price;
    }
    public String getQRData() {
    	return qrdata;
    }
}
