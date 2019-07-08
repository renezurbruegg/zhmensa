package com.mensa.zhmensa.models;

import android.widget.Adapter;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.mensa.zhmensa.component.MenuViewHolder;

public class ComparableSortedListAdapterCallback<T extends  Comparable> extends SortedList.Callback<T> {

    private final RecyclerView.Adapter adapter;
    public ComparableSortedListAdapterCallback(RecyclerView.Adapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public int compare(Comparable o1, Comparable o2) {
        return o1.compareTo(o2);
    }

    @Override
    public void onChanged(int position, int count) {
        adapter.notifyItemRangeChanged(position, count);
    }

    @Override
    public boolean areContentsTheSame(Comparable oldItem, Comparable newItem) {
        return oldItem.equals(newItem);
    }

    @Override
    public boolean areItemsTheSame(Comparable item1, Comparable item2) {
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
