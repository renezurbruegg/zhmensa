package com.mensa.zhmensa.navigation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.models.MensaListObservable;
import com.mensa.zhmensa.services.MensaManager;

import java.util.Collections;
import java.util.List;

public class NavigationFavoritesHeader extends NavigationMenuHeader{


    public NavigationFavoritesHeader() {
        super(new MensaCategory(MensaManager.activityContext.getString(R.string.favorites_title), 0) {
            @NonNull
            @Override
            public List<MensaListObservable> loadMensasFromAPI() {
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
    public String getDisplayName() {
        return MensaManager.activityContext.getString(R.string.favorites_title);
    }

}
