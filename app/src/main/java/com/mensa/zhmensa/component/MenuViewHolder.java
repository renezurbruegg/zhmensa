package com.mensa.zhmensa.component;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.Menu;

/**
 * Simple implementation for a Mensa Menu view.
 * This class is used in the MenuCardAdapter that calls the bind function, to load the values of a menu into a card.
 */
public class MenuViewHolder extends RecyclerView.ViewHolder {

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /**
     * Binds the menu to a view.
     * @param viewHolder to view to bind to menu to
     * @param menu to menu
     */
    public static void bind(MenuViewHolder viewHolder, Menu menu) {
        ((TextView) viewHolder.itemView.findViewById(R.id.card_title)).setText(menu.getName());
        ((TextView) viewHolder.itemView.findViewById(R.id.card_content)).setText(menu.getDescription());

    }
}
