package com.hospital.dietary.models;

import com.hospital.dietary.data.entities.ItemEntity;

public class Item {
    private ItemEntity entity;

    public Item() {
        this.entity = new ItemEntity();
    }

    public Item(ItemEntity entity) {
        this.entity = entity;
    }

    public long getItemId() { return entity.getItemId(); }
    public void setItemId(long id) { entity.setItemId(id); }

    public String getName() { return entity.getName(); }
    public void setName(String name) { entity.setName(name); }

    public String getCategory() { return entity.getCategory(); }
    public void setCategory(String category) { entity.setCategory(category); }

    public String getDescription() { return entity.getDescription(); }
    public void setDescription(String desc) { entity.setDescription(desc); }

    public boolean isAdaFriendly() { return entity.isAdaFriendly(); }
    public void setAdaFriendly(boolean ada) { entity.setAdaFriendly(ada); }

    public ItemEntity toEntity() { return entity; }
    public static Item fromEntity(ItemEntity entity) { return new Item(entity); }
}