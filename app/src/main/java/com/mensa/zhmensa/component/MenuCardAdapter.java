package com.mensa.zhmensa.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.component.MenuViewHolder;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.Menu;

import java.util.List;


/**
 * Adapter class that holds all menu views for a Mensa.
 * Is used inside the recycler view to display different menus.
 */
public class MenuCardAdapter extends RecyclerView.Adapter<MenuViewHolder> {


    private List<Menu> menus;

    public static MenuCardAdapter forMensa(Mensa mensa) {
        // TODO maybe implement caching
        return new MenuCardAdapter(mensa);
    }

    public MenuCardAdapter(Mensa mensa) {
        menus = mensa.getMenus();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_card, parent,false);
        MenuViewHolder viewHolder = new MenuViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
       Menu menu = menus.get(position);
       MenuViewHolder.bind(holder, menu);
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }
}
