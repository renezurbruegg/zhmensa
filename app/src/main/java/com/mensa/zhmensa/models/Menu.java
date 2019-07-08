package com.mensa.zhmensa.models;


import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;

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
    private String id;



    public Menu(String id, String name, String description, String prices, String allergene, String meta) {
        this.name = name;
        this.description = description;
        this.prices = prices;
        this.allergene = allergene;
        this.meta = meta;
        this.id = id;
    }

    public Menu(String id, String name, String description, String prices, String allergene) {
        this(id, name, description, prices, allergene, null);
    }

    public void setId(String id) {
        this.id = id;
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

    @Override
    public String getId() {
        return id;
    }

    public void setPrices(String prices) {
        this.prices = prices;
    }

    public String getAllergene() {
        return "Allergene: " + allergene;
    }

    @Override
    public boolean isFavorite() {
       return MensaManager.isFavorite(getId());
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append("\n");
        sb.append(getPrices());
        sb.append("\n");
        sb.append(getDescription());
        return sb.toString();
    }

    @Override
    public int compareTo(IMenu otherMenu) {
        return Helper.firstNonNull(getName(), "").compareTo(Helper.firstNonNull(otherMenu.getName(),""));
    }
}
