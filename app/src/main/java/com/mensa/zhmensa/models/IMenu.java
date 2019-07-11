package com.mensa.zhmensa.models;

/**
 * Interface that wraps every meal into an object.
 */
public interface IMenu extends Comparable<IMenu> {

    String getName();

    String getDescription();

    String getPrices();

    String getId();

    String getAllergene();

    boolean isFavorite();

    String getMeta();

    void setName(String name);

    String getSharableString();

    boolean hasAllergene();
}
