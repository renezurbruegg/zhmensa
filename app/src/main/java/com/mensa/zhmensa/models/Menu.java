package com.mensa.zhmensa.models;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;

/**
 * The menu object
 */
public class Menu implements IMenu{

    @Nullable
    private String name;
    @Nullable
    private String description;
    @Nullable
    private String prices;
    @Nullable
    private String allergene;
    @Nullable
    private String meta;
    @NonNull
    private String id;



    public Menu(@Nullable String id,@Nullable String name,@Nullable String description,@Nullable String prices, @Nullable String allergene,@Nullable String meta) {
        this.name = name;
        this.description = description;
        this.prices = prices;
        this.meta = meta;
        setId(id);
        setAllergene(allergene);
    }

    public Menu(String id, String name, String description, String prices, String allergene) {
        this(id, name, description, prices, allergene, null);
    }

    public void setId(@Nullable String id) {
        if(id == null)
            this.id = System.currentTimeMillis() +"";
        else
            this.id = id;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public String getPrices() {
        return prices;
    }

    @Override
    @NonNull
    public String getId() {
        return id;
    }

    void setPrices(@Nullable String prices) {
        this.prices = prices;
    }

    @Nullable
    public String getAllergene() {
        return "Allergene: " + allergene;
    }

    @Override
    public boolean isFavorite() {
       return MensaManager.isFavorite(getId());
    }

    public void setAllergene(@Nullable String allergene) {
        this.allergene = allergene;
        if(this.allergene != null) {
            this.allergene = this.allergene.replaceAll(" *Allergene *:? *","");
        }
    }

    @Nullable
    public String getMeta() {
        return meta;
    }

    public void setMeta(@Nullable String meta) {
        this.meta = meta;
    }


    public String getSharableString() {
       return getName() +
                "\n" +
                getPrices() +
                "\n" +
                getDescription();
    }

    @Override
    public boolean hasAllergene() {
        return (allergene != null && !allergene.isEmpty() && !allergene.equals("null"));
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(@Nullable IMenu otherMenu) {
        if(otherMenu == null)
            return 1;

        return Helper.firstNonNull(getName(), "").toLowerCase().compareTo(Helper.firstNonNull(otherMenu.getName(),"").toLowerCase());
    }

    @NonNull
    private String getAsComparableString() {
        return id + /*name + */  description;
    }

    @Override
    public boolean equals(Object obj) {
        if( obj instanceof  Menu) {
            return getAsComparableString().equals(((Menu) obj).getAsComparableString());
        } else  if (obj instanceof IMenu) {
            return Helper.firstNonNull(getId(), "").equals(((IMenu) obj).getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Helper.firstNonNull(getId(),"").hashCode();
    }
}
