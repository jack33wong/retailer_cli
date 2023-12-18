package com.generic.retailer.dao;

import com.generic.retailer.model.Order;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final List<Order> data = new ArrayList();

    public void add(Order order) {
        data.add(order);
    }

    public Order find(Integer index) {
        return data.get(index);
    }

    public List<Order> findAll() {
        return data;
    }

}
