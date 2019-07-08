package com.mensa.zhmensa.models;

public interface IMenu extends Comparable<IMenu> {

    public String getName();

    public String getDescription();

    public String getPrices();

    public String getId();

    public String getAllergene();

    public boolean isFavorite();

    public void setFavorite(boolean isFavorite);

    String getMeta();
}
