package com.mensa.zhmensa.navigation;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.categories.MensaCategory;
import com.mensa.zhmensa.models.MensaListObservable;

import java.util.Collections;
import java.util.List;

public class NavigationFavoritesHeader extends NavigationMenuHeader{


    public NavigationFavoritesHeader(Context ctx) {
        super(new MensaCategory(ctx.getString(R.string.favorites_title), 0) {
            @NonNull
            @Override
            public List<MensaListObservable> loadMensasFromAPI(String code) {
                return Collections.emptyList();
            }

            @Nullable
            @Override
            public Integer getCategoryIconId() {
                return R.drawable.ic_favorite_black_24dp;
            }
        }, false, -1);
    }

    @NonNull
    public String getDisplayName(Context ctx) {
        return ctx.getString(R.string.favorites_title);
    }

}
