package com.mensa.zhmensa.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Menu;
import com.mensa.zhmensa.services.Helper;

import java.util.ArrayList;
import java.util.List;


/**
 * Adapter class that holds all menu views for a Mensa.
 * Is used inside the recycler view to display different menus.
 */
public class MenuCardAdapter extends RecyclerView.Adapter<MenuViewHolder> {


    private Context context;
    private List<IMenu> menus;

    public MenuCardAdapter(List<IMenu> menus) {
        this.menus = new ArrayList<>();

        if(menus != null) {
            this.menus.addAll(menus);
        }

        if(this.menus.isEmpty()) {
            this.menus.add(new Menu("","","Momentan kein Menü verfügbar","", "dummy"));
        }
    }


    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_card, parent,false);
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
}
