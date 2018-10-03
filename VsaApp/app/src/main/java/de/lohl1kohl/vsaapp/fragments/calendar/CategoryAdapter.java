package de.lohl1kohl.vsaapp.fragments.calendar;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import de.lohl1kohl.vsaapp.R;
import de.lohl1kohl.vsaapp.holders.DatesHolder;
import de.lohl1kohl.vsaapp.holders.SpHolder;

public class CategoryAdapter extends ArrayAdapter<Category> {
    private boolean addEditEntry;
    private LayoutInflater mInflater;
    private Context mActivity;
    public List<Category> data;
    private DatesFragment parent;

    public CategoryAdapter(DatesFragment parent, Context c, int textViewResourceId, List<Category> data, boolean addEditEntry)
    {
        super(c, textViewResourceId);

        this.parent = parent;
        this.addEditEntry = addEditEntry;
        this.data = data;
        mActivity = c;
        mInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public int getCount(){
        return data.size() + (addEditEntry ? 1 : 0);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        View v;

        // Normal category...
        if (position != data.size() || !addEditEntry) {
            v = mInflater.inflate(R.layout.dates_category, null);
            View color = v.findViewById(R.id.category_color);
            TextView name = v.findViewById(R.id.category_name);
            color.setBackgroundColor(Color.rgb(data.get(position).color.r, data.get(position).color.g, data.get(position).color.b));
            name.setText(data.get(position).name);
        }
        // Set categories option...
        else {
            v = mInflater.inflate(R.layout.dates_category, null);
            View color = v.findViewById(R.id.category_color);
            TextView name = v.findViewById(R.id.category_name);
            color.setBackgroundColor(Color.rgb(91, 198, 56));
            name.setText(mActivity.getString(R.string.edit_categories));
            name.setTypeface(name.getTypeface(), Typeface.ITALIC);

            v.setOnClickListener(view -> {
                this.parent.editCategories(this);
            });
        }

        return v;
    }
}
