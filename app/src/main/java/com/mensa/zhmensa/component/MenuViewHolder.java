package com.mensa.zhmensa.component;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.FavoriteMenu;
import com.mensa.zhmensa.models.IMenu;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;

/**
 * Simple implementation for a Mensa Menu view.
 * This class is used in the MenuCardAdapter that calls the bind function, to load the values of a menu into a card.
 */
class MenuViewHolder extends RecyclerView.ViewHolder {

    public static final String DUMMY = "dummy";

    MenuViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /**
     * Binds the menu to a view.
     * @param viewHolder to view to bind to menu to
     * @param menu to menu
     * @param mensaId
     */
    static void bind(MenuViewHolder viewHolder, final IMenu menu, @NonNull final Context ctx, final String mensaId) {
        ((TextView) viewHolder.itemView.findViewById(R.id.card_title)).setText(menu.getName());
        ((TextView) viewHolder.itemView.findViewById(R.id.price_text)).setText(menu.getPrices());
        ((TextView) viewHolder.itemView.findViewById(R.id.card_content)).setText(menu.getDescription());
        ((TextView) viewHolder.itemView.findViewById(R.id.allergene)).setText(menu.getAllergene(ctx));

        final ImageButton favBtn = viewHolder.itemView.findViewById(R.id.bookmark_button);


        final ImageButton shareBtn = viewHolder.itemView.findViewById(R.id.share_button);
        final LinearLayout showMoreLayout = viewHolder.itemView.findViewById(R.id.showmore_layout);
        final ImageButton showMoreBtn = viewHolder.itemView.findViewById(R.id.showmore_button);
        final ImageButton hideMenuBtn = viewHolder.itemView.findViewById(R.id.hide_button);


        if(Helper.firstNonNull(menu.getMeta(),"").equals(DUMMY)) {
            ((TextView) viewHolder.itemView.findViewById(R.id.price_text)).setGravity(View.TEXT_ALIGNMENT_CENTER);
            shareBtn.setVisibility(View.INVISIBLE);
            showMoreBtn.setVisibility(View.INVISIBLE);
            favBtn.setVisibility(View.INVISIBLE);
            hideMenuBtn.setVisibility(View.INVISIBLE);

            return;
        } else {
            ((TextView) viewHolder.itemView.findViewById(R.id.price_text)).setGravity(View.TEXT_ALIGNMENT_TEXT_START);
            shareBtn.setVisibility(View.VISIBLE);
            showMoreBtn.setVisibility(View.VISIBLE);
            favBtn.setVisibility(View.VISIBLE);
            hideMenuBtn.setVisibility( (menu instanceof FavoriteMenu) ? View.INVISIBLE : View.VISIBLE);
        }


        if(!menu.hasAllergene()) {
            showMoreBtn.setVisibility(View.INVISIBLE);
        }


        favBtn.setImageResource( menu.isFavorite() ? R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp);

        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MensaManager.toggleMenuFav(menu);
                //menu.setFavorite(!menu.isFavorite());
                favBtn.setImageResource( menu.isFavorite() ? R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp);
            }
        });


        hideMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MensaManager.hideMenu(menu, mensaId);
                Snackbar.make(view, String.format(ctx.getString(R.string.menu_deleted) , menu.getName()), Snackbar.LENGTH_LONG)
                        .setAction(ctx.getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MensaManager.showMenu(menu, mensaId);
                            }
                        })
                        .show();
            }
        });

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_TEXT, menu.getSharableString());
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
