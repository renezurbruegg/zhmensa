package com.mensa.zhmensa.models;


/**
 * The menu object
 */
public class Menu implements IMenu{

    private String name;
    private String description;
    private String prices;
    private String allergene;
    private String meta;
    private boolean favorite;


    public Menu(String name, String description, String prices, String allergene, String meta) {
        this.name = name;
        this.description = description;
        this.prices = prices;
        this.allergene = allergene;
        this.meta = meta;
    }

    public Menu(String name, String description, String prices, String allergene) {
        this(name, description, prices, allergene, null);
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

    public String getPrices() {
        return prices;
    }

    public void setPrices(String prices) {
        this.prices = prices;
    }

    public String getAllergene() {
        return allergene;
    }

    @Override
    public boolean isFavorite() {
        return favorite;
    }

    @Override
    public void setFavorite(boolean isFavorite) {
        this.favorite = isFavorite;
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
