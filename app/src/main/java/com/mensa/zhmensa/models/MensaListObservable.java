package com.mensa.zhmensa.models;

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
public class MensaListObservable extends Observable implements Serializable {

    private List<Mensa> mensaList = new ArrayList<>();
    private List<Mensa> newItems = new ArrayList<>();

    public List<Mensa> getNewItems() {
        return newItems;
    }

    public List<Mensa> getAllItems() {
        return mensaList;
    }

    public final Mensa.Weekday day;
    public final Mensa.MenuCategory mealType;

    public MensaListObservable(Mensa.Weekday day, Mensa.MenuCategory mealType) {
        this.day = day;
        this.mealType = mealType;
    }


    public void clear() {
        mensaList.clear();
        newItems.clear();
    }

    public void addNewMensaList(List<Mensa> mensas) {
        newItems.clear();
        newItems.addAll(mensas);
        mensaList.addAll(mensas);
        Collections.sort(mensaList, new Comparator<Mensa>() {
            @Override
            public int compare(Mensa mensa, Mensa mensa2) {
                return mensa.getDisplayName().compareTo(mensa2.getDisplayName());
            }
        });

        Collections.sort(newItems, new Comparator<Mensa>() {
            @Override
            public int compare(Mensa mensa, Mensa mensa2) {
                return mensa.getDisplayName().compareTo(mensa2.getDisplayName());
            }
        });
        setChanged();

        notifyObservers(mensas);
    }
    public void addNewMensa(Mensa... mensas) {
        addNewMensaList(Arrays.asList(mensas));
    }

    public void pushSilently(Mensa mensa) {
        newItems.add(mensa);
        mensaList.add(mensa);
    }

    public void notifyAllObservers() {
        Collections.sort(mensaList, new Comparator<Mensa>() {
            @Override
            public int compare(Mensa mensa, Mensa mensa2) {
                return mensa.getDisplayName().compareTo(mensa2.getDisplayName());
            }
        });

        Collections.sort(newItems, new Comparator<Mensa>() {
            @Override
            public int compare(Mensa mensa, Mensa mensa2) {
                return mensa.getDisplayName().compareTo(mensa2.getDisplayName());
            }
        });
        setChanged();

        notifyObservers(newItems);
    }
}
