package com.mensa.zhmensa.navigation;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.navigation.NavigationFavoritesHeader;
import com.mensa.zhmensa.navigation.NavigationMenuChild;
import com.mensa.zhmensa.navigation.NavigationMenuHeader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NavigationExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<NavigationMenuHeader> listDataHeader;
    private Map<NavigationMenuHeader, List<NavigationMenuChild>> listDataChild;

    public NavigationExpandableListAdapter(Context context, List<NavigationMenuHeader> listDataHeader,
                                           Map<NavigationMenuHeader, List<NavigationMenuChild>> listChildData) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        for (List<NavigationMenuChild> children: listDataChild.values()) {
            Collections.sort(children);
        }

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

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = getChild(groupPosition, childPosition).getDisplayName();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group_child, null);
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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        NavigationMenuHeader header = getGroup(groupPosition);



        String headerTitle = header.getDisplayName();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             convertView = inflater.inflate(R.layout.list_group_header, null);

             if(header.category != null && header.category.getCategoryIconId() != null) {
                 ((TextView) convertView.findViewById(R.id.lblListHeader)).setCompoundDrawablesRelativeWithIntrinsicBounds(header.category.getCategoryIconId(),0,0,0);
             }
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
}
