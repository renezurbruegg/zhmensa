package com.mensa.zhmensa.models.menu;

import android.content.Context;

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

    boolean isVegi();

    @Nullable
    String getAllergene(Context ctx);

    boolean isFavorite();

    @Nullable
    String getMeta();

    void setName(String name);

    @Nullable
    String getSharableString();

    boolean hasAllergene();

}
