package com.mensa.zhmensa.models;

public interface IMenu {

    public String getName();

    public String getDescription();

    public String getPrices();

    public String getAllergene();

    public boolean isFavorite();

    public void setFavorite(boolean isFavorite);

    String getMeta();
}
