package com.hospital.dietary.dao;

import com.hospital.dietary.DatabaseHelper;
import com.hospital.dietary.models.Order;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private DatabaseHelper dbHelper;

    public OrderDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<String> getAvailableDates() {
        List<String> dates = new ArrayList<>();
        dates.add("2025-01-15");
        dates.add("2025-01-14");
        dates.add("2025-01-13");
        return dates;
    }

    public List<Order> getOrdersByDate(String date) {
        return new ArrayList<>(); // Temporary
    }

    public int getOrderCountForDate(String date) {
        return 0; // Temporary
    }
}