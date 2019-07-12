package com.mensa.zhmensa.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Interface that wraps every meal into an object.
 */
public interface IMenu extends Comparable<IMenu> {

    @Nullable
    String getName();

    @Nullable
    String getDescription();

    @Nullable
    String getPrices();

    @NonNull
    String getId();

    @Nullable
    String getAllergene();

    boolean isFavorite();

    @Nullable
    String getMeta();

    void setName(String name);

    @Nullable
    String getSharableString();

    boolean hasAllergene();
}
