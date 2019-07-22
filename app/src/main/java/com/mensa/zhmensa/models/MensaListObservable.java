package com.mensa.zhmensa.models;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Observable;

/**
 * Observable used to communicate when new Mensas are loaded from the REST API
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
public class MensaListObservable extends Observable implements Serializable {

    @NonNull
    private final List<Mensa> mensaList = new ArrayList<>();
    @NonNull
    private final List<Mensa> newItems = new ArrayList<>();

    @NonNull
    public List<Mensa> getNewItems() {
        return newItems;
    }

    public final Mensa.Weekday day;
    public final Mensa.MenuCategory mealType;

    public MensaListObservable(Mensa.Weekday day, Mensa.MenuCategory mealType) {
        this.day = day;
        this.mealType = mealType;
    }

    @SuppressWarnings("unused")
    public void clear() {
        mensaList.clear();
        newItems.clear();
    }

    public void addNewMensaList(@NonNull List<Mensa> mensas) {
        newItems.clear();
        newItems.addAll(mensas);
        mensaList.addAll(mensas);
        Collections.sort(mensaList, new Comparator<Mensa>() {
            @Override
            public int compare(@NonNull Mensa mensa, @NonNull Mensa mensa2) {
                return mensa.getDisplayName().compareTo(mensa2.getDisplayName());
            }
        });

        Collections.sort(newItems, new Comparator<Mensa>() {
            @Override
            public int compare(@NonNull Mensa mensa, @NonNull Mensa mensa2) {
                return mensa.getDisplayName().compareTo(mensa2.getDisplayName());
            }
        });
        setChanged();

        notifyObservers(mensas);
    }
    public void addNewMensa(Mensa... mensas) {
        addNewMensaList(Arrays.asList(mensas));
    }

    @SuppressWarnings("unused")
    public void pushSilently(Mensa mensa) {
        newItems.add(mensa);
        mensaList.add(mensa);
    }

    @SuppressWarnings("unused")
    void notifyAllObservers() {
        Collections.sort(mensaList, new Comparator<Mensa>() {
            @Override
            public int compare(@NonNull Mensa mensa, @NonNull Mensa mensa2) {
                return mensa.getDisplayName().compareTo(mensa2.getDisplayName());
            }
        });

        Collections.sort(newItems, new Comparator<Mensa>() {
            @Override
            public int compare(@NonNull Mensa mensa, @NonNull Mensa mensa2) {
                return mensa.getDisplayName().compareTo(mensa2.getDisplayName());
            }
        });
        setChanged();

        notifyObservers(newItems);
    }
}
