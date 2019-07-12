package com.mensa.zhmensa.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

/**
 * Implementation of simple callback to keep menus inside recyclerview in order.
 * @param <T> Comparable type that is stored in the list and should be ordered
 */
public class ComparableSortedListAdapterCallback<T extends  Comparable> extends SortedList.Callback<T> {

    private final RecyclerView.Adapter adapter;

    public ComparableSortedListAdapterCallback(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int compare(@Nullable Comparable o1, @Nullable Comparable o2) {
        if(o1 == null || o2 == null)
            return -1;

        return o1.compareTo(o2);
    }

    @Override
    public void onChanged(int position, int count) {
        adapter.notifyItemRangeChanged(position, count);
    }

    @Override
    public boolean areContentsTheSame(@NonNull Comparable oldItem, Comparable newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areItemsTheSame(@NonNull Comparable item1, Comparable item2) {
        return item1.equals(item2);
    }

    @Override
    public void onInserted(int position, int count) {
        adapter.notifyItemRangeInserted(position, count);
    }

    @Override
    public void onRemoved(int position, int count) {
        adapter.notifyItemRangeRemoved(position, count);
    }

    @Override
    public void onMoved(int fromPosition, int toPosition) {
        adapter.notifyItemMoved(fromPosition, toPosition);
    }
}
