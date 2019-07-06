package com.mensa.zhmensa.component;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.models.Menu;
import com.mensa.zhmensa.services.Helper;

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
    public static void bind(MenuViewHolder viewHolder, final IMenu menu, final Context ctx) {
        ((TextView) viewHolder.itemView.findViewById(R.id.card_title)).setText(menu.getName());
        ((TextView) viewHolder.itemView.findViewById(R.id.price_text)).setText(menu.getPrices());
        ((TextView) viewHolder.itemView.findViewById(R.id.card_content)).setText(menu.getDescription());
        ((TextView) viewHolder.itemView.findViewById(R.id.allergene)).setText(menu.getAllergene());

        final ImageButton favBtn = (ImageButton)viewHolder.itemView.findViewById(R.id.bookmark_button);


        final ImageButton shareBtn = (ImageButton)viewHolder.itemView.findViewById(R.id.share_button);
        final LinearLayout showMoreLayout = (LinearLayout) viewHolder.itemView.findViewById(R.id.showmore_layout);
        final ImageButton showMoreBtn = (ImageButton) viewHolder.itemView.findViewById(R.id.showmore_button);

        if(Helper.firstNonNull(menu.getMeta(),"").equals("dummy")) {
            shareBtn.setVisibility(View.GONE);
            showMoreBtn.setVisibility(View.GONE);
            favBtn.setVisibility(View.GONE);
            return;
        }
        favBtn.setImageResource( menu.isFavorite() ? R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp);

        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menu.setFavorite(!menu.isFavorite());
                favBtn.setImageResource( menu.isFavorite() ? R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp);
            }
        });


        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_TEXT, menu.toString());
                ctx.startActivity(Intent.createChooser(i, "Share"));
            }
        });

        showMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hide = (showMoreLayout.getVisibility() == View.VISIBLE);
                showMoreLayout.setVisibility(hide ? View.GONE : View.VISIBLE);
                showMoreBtn.setImageResource( hide ? R.drawable.ic_keyboard_arrow_down_black_24dp : R.drawable.ic_keyboard_arrow_up_black_24dp);
            }
        });


    }
}
