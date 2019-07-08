package com.mensa.zhmensa.navigation;

import androidx.annotation.Nullable;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.models.MensaListObservable;

import java.util.Collections;
import java.util.List;

public class NavigationFavoritesHeader extends NavigationMenuHeader{


    public NavigationFavoritesHeader() {
        super(new MensaCategory("Favorites") {
            @Override
            public List<MensaListObservable> loadMensasFromAPI() {
                return Collections.emptyList();
            }

            @Nullable
            @Override
            public Integer getCategoryIconId() {
                return R.drawable.ic_favorite_black_24dp;
            }
        }, false);
    }

    public String getDisplayName() {
        return "Favorites";
    }

}
