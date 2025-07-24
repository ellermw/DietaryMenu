package com.hospital.dietary.dao;

import com.hospital.dietary.DatabaseHelper;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private DatabaseHelper dbHelper;

    public CategoryDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        categories.add("Breakfast Main");
        categories.add("Hot Cereal");
        categories.add("Cold Cereal");
        categories.add("Bread");
        categories.add("Juice");
        categories.add("Hot Beverage");
        categories.add("Lunch Protein");
        categories.add("Starch");
        categories.add("Vegetable");
        categories.add("Dessert");
        categories.add("Dinner Protein");
        return categories;
    }

    public boolean addCategory(String categoryName) {
        return true; // Temporary
    }

    public boolean deleteCategory(String categoryName) {
        return true; // Temporary
    }

    public boolean updateCategory(String oldName, String newName) {
        return true; // Temporary
    }

    public boolean categoryExists(String categoryName) {
        return getAllCategories().contains(categoryName);
    }

    public List<CategoryInfo> getCategoriesWithCounts() {
        List<CategoryInfo> result = new ArrayList<>();
        for (String cat : getAllCategories()) {
            result.add(new CategoryInfo(cat, 10)); // Dummy count
        }
        return result;
    }

    // Inner class for category info
    public static class CategoryInfo {
        private String name;
        private int itemCount;

        public CategoryInfo(String name, int itemCount) {
            this.name = name;
            this.itemCount = itemCount;
        }

        public String getName() { return name; }
        public int getItemCount() { return itemCount; }

        @Override
        public String toString() {
            return name + " (" + itemCount + " items)";
        }
    }
}