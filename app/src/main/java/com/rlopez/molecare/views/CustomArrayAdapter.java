/*
 * @author   Raúl López
 * @version  1.0
 * @year     2020
 */

package com.rlopez.molecare.views;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.rlopez.molecare.R;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter<RowItem> {

    private Context context;

    public CustomArrayAdapter(@NonNull Context context, int resource, @NonNull List<RowItem> items) {
        super(context, resource, items);
        this.context = context;
    }

    // Private view holder class. Contains item view
    private static class ViewHolder {
        TextView nameView;
        ImageView imageView;
    }

    // Get view with corresponding name and image
    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        ViewHolder holder;
        RowItem item = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        // If view doesn't exist, create a new one. If it exists, use it
        if (view == null) {
            assert inflater != null;
            view = inflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.nameView = view.findViewById(R.id.itemName);
            holder.imageView = view.findViewById(R.id.itemImage);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        assert item != null;
        holder.nameView.setText(item.getName());
        holder.imageView.setImageBitmap(item.getImage());

        return view;
    }
}
