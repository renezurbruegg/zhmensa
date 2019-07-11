package com.mensa.zhmensa.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.ComparableSortedListAdapterCallback;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Menu;

import java.util.List;


/**
 * Adapter class that holds all menu views for a Mensa.
 * Is used inside the recycler view to display different menus.
 */
public class MenuCardAdapter extends RecyclerView.Adapter<MenuViewHolder> {


    private Context context;
    private SortedList<IMenu> menus;

    public MenuCardAdapter(List<IMenu> menus) {
        this.menus = new SortedList<IMenu>(IMenu.class, new ComparableSortedListAdapterCallback(this));

        if (menus != null) {
            this.menus.addAll(menus);
        }

        if (this.menus.size() == 0) {
            this.menus.add(new Menu("", "", "Momentan kein Menü verfügbar", "", "", "dummy"));
        }
    }

    public void setItems(List<IMenu> items ){
        this.menus.clear();
        this.menus.addAll(items);
    }


    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_card, parent, false);
        MenuViewHolder viewHolder = new MenuViewHolder(view);
        return viewHolder;
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
