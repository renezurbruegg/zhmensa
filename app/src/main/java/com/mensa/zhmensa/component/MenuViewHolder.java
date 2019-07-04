package com.mensa.zhmensa.component;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.IMenu;
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
    public static void bind(MenuViewHolder viewHolder, final IMenu menu) {
        ((TextView) viewHolder.itemView.findViewById(R.id.card_title)).setText(menu.getName());
        ((TextView) viewHolder.itemView.findViewById(R.id.price_text)).setText(menu.getPrices());
        ((TextView) viewHolder.itemView.findViewById(R.id.card_content)).setText(menu.getDescription());

        final ImageButton favBtn = (ImageButton)viewHolder.itemView.findViewById(R.id.bookmark_button);
        favBtn.setImageResource( menu.isFavorite() ? R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp);

        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.setFavorite(!menu.isFavorite());
                favBtn.setImageResource( menu.isFavorite() ? R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp);
            }
        });
    }
}
