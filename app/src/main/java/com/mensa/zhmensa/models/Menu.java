package com.mensa.zhmensa.models;

public class Menu {
    private String name;
    private String description;
    private double price;
    private String allergene;
    private String meta;

    public Menu(String name, String description, double price, String allergene, String meta) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.allergene = allergene;
        this.meta = meta;
    }

    public Menu(String name, String description, double price, String allergene) {
        this(name, description,price,allergene, null);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getAllergene() {
        return allergene;
    }

    public void setAllergene(String allergene) {
        this.allergene = allergene;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }
}
