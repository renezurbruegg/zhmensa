package com.mensa.zhmensa.component;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.Menu;

public class MenuViewHolder extends RecyclerView.ViewHolder {

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public static void bind(MenuViewHolder viewHolder, Menu menu) {
        ((TextView) viewHolder.itemView.findViewById(R.id.card_title)).setText(menu.getName());
        ((TextView) viewHolder.itemView.findViewById(R.id.card_content)).setText(menu.getDescription());

    }
}
