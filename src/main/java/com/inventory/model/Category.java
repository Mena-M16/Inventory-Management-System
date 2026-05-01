package com.inventory.model;

import java.sql.Timestamp;

/**
 * Model class representing a product category.
 */
public class Category {
    private int id;
    private String name;
    private String description;
    private boolean isActive;
    private Timestamp createdAt;

    public Category() {}

    public Category(int id, String name, String description, boolean isActive, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return name; }
}
