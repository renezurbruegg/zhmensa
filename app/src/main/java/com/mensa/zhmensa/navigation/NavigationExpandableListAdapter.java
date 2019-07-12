package com.mensa.zhmensa.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.SortedList;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.ComparableSortedListAdapterCallback;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.models.MensaCategory;
import com.mensa.zhmensa.services.MensaManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NavigationExpandableListAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final List<NavigationMenuHeader> listDataHeader = new ArrayList<>(); // new SortedList<NavigationMenuHeader>(new ComparableSortedListAdapterCallback<NavigationFavoritesHeader>(this));
    private final Map<NavigationMenuHeader, List<NavigationMenuChild>> listDataChild = new TreeMap<>();

    private final List<MensaCategory> storedCategories = new ArrayList<>();
    private final List<Mensa> storedMensas = new ArrayList<>();

    public NavigationExpandableListAdapter(Context context /*, List<NavigationMenuHeader> listDataHeader,
                                           Map<NavigationMenuHeader, List<NavigationMenuChild>> listChildData*/) {
        this.context = context;
        //listDataHeader= new SortedList<NavigationMenuHeader>(new ComparableSortedListAdapterCallback<NavigationFavoritesHeader>(this));
        setFavorite();

       // this.listDataChild = listChildData;

     /*   for (List<NavigationMenuChild> children: listDataChild.values()) {
            if(children.size() > 1)
                Collections.sort(children);
        }*/

    }


    private void setFavorite() {

        NavigationFavoritesHeader fav = new NavigationFavoritesHeader();
        Mensa favMensa = MensaManager.getFavoritesMensa();
        NavigationMenuChild favMensaChild = new NavigationMenuChild(favMensa);

        listDataHeader.add(fav);
        listDataChild.put(fav, Collections.<NavigationMenuChild>singletonList(favMensaChild));

        storedCategories.add(favMensa.getCategory());
        storedMensas.add(favMensa);
    }


    public void addAll(List<Mensa> mensas) {
        for(Mensa mensa: mensas)
            addMensa(mensa);

        super.notifyDataSetChanged();
    }


    public boolean containsMensa(Mensa m) {
        return storedMensas.contains(m);
    }


    public void addCategory(MensaCategory category) {
        storedCategories.add(category);
        NavigationMenuHeader header = new NavigationMenuHeader(category, true, category.getPosition());
        listDataHeader.add(header);
        listDataChild.put(header, new ArrayList<NavigationMenuChild>());
        Collections.sort(listDataHeader);
        notifyDataSetChanged();
    }

    public void addMensa(Mensa mensa) {
        if(mensa == null || storedMensas.contains(mensa) ||MensaManager.getFavoritesMensa().equals(mensa))
            return;

        if(!storedCategories.contains(mensa.getCategory())) {
            addCategory(mensa.getCategory());
        }
        storedMensas.add(mensa);

        NavigationMenuChild child = new NavigationMenuChild(mensa);

        for(NavigationMenuHeader header : listDataHeader) {
            if(header.category.equals(mensa.getCategory())) {
                Log.d("ListAdapter.addMensa", "found header for nmensa: " + mensa.getDisplayName());
                List<NavigationMenuChild> children = listDataChild.get(header);
                children.add(child);
                Collections.sort(children);
            }
        }

        //List<NavigationMenuChild> storedMensas =  listDataChild.get(mensa.getCategory());


    }


    @Override
    public NavigationMenuChild getChild(int groupPosition, int childPosititon) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                .get(childPosititon);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Nullable
    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, @Nullable View convertView, ViewGroup parent) {

        final String childText = getChild(groupPosition, childPosition).getDisplayName();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group_child, parent, false);
        }

        TextView txtListChild = convertView
                .findViewById(R.id.lblListItem);

        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        if (this.listDataChild.get(this.listDataHeader.get(groupPosition)) == null )
            return 0;
        if (!this.listDataHeader.get(groupPosition).hasChildren())
            return 0;
        else
            return this.listDataChild.get(this.listDataHeader.get(groupPosition))
                    .size();
    }

    @Override
    public NavigationMenuHeader getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();

    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Nullable
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             @Nullable View convertView, ViewGroup parent) {

        NavigationMenuHeader header = getGroup(groupPosition);



        String headerTitle = header.getDisplayName();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

             convertView = inflater.inflate(R.layout.list_group_header, parent, false);
        }

        if(header.category != null && header.category.getCategoryIconId() != null) {
            ((TextView) convertView.findViewById(R.id.lblListHeader)).setCompoundDrawablesRelativeWithIntrinsicBounds(header.category.getCategoryIconId(),0,0,0);
        }

        TextView lblListHeader = convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Nullable
    public int getPositionForMensaId(String mensaId) {
        int pos = 0;
        for(List<NavigationMenuChild> children: listDataChild.values()) {
            for (NavigationMenuChild child: children) {
                if(mensaId.equals(child.mensa.getUniqueId())){
                    Log.d("expandListAdapter", "getMensaIdForPos: pos " + pos);
                    return pos;
                }
                pos ++;
            }
        }
        return -1;
    }
    @Nullable
    public String getIdForPosition(int position) {
        for(List<NavigationMenuChild> child: listDataChild.values()) {
            if(child.size() <= position) {
                position -= child.size();
            } else {
                return child.get(position).mensa.getUniqueId();
            }
        }
        return null;
    }

    public int getAllChildrenCount() {
        return storedMensas.size();
    }

    public void clearCache() {
        listDataChild.clear();
        listDataHeader.clear();
        storedMensas.clear();
        storedCategories.clear();
        setFavorite();
    }
}
