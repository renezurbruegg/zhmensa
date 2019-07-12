package com.mensa.zhmensa.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.ComparableSortedListAdapterCallback;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.services.MensaManager;

import java.util.List;


/**
 * Adapter class that holds all menu views for a Mensa.
 * Is used inside the recycler view to display different menus.
 */
class MenuCardAdapter extends RecyclerView.Adapter<MenuViewHolder> {


    private Context context;
    private final SortedList<IMenu> menus;

    public MenuCardAdapter(@Nullable List<IMenu> menus, String mensaId) {
        this.menus = new SortedList<>(IMenu.class, new ComparableSortedListAdapterCallback(this));

        if (menus != null) {
            this.menus.addAll(menus);
        }

        if (this.menus.size() == 0) {
            this.menus.addAll(MensaManager.getPlaceholderForEmptyMenu(mensaId));
        }
    }

    public void setItems(@NonNull List<IMenu> items ){
        this.menus.clear();
        this.menus.addAll(items);
    }


    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_card, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        IMenu menu = menus.get(position);
        MenuViewHolder.bind(holder, menu, context);
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }


    public SortedList<IMenu> getItems() {
        return menus;
    }
}
