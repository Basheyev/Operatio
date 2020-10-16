package com.axiom.operatio.model.market;

public class Order {

    public static final int INVALID = 0;
    public static final int BUY = 1;
    public static final int SELL = 2;

    public int type;
    public int quantity;
    public long price;

}
