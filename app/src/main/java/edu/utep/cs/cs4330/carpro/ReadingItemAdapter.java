package edu.utep.cs.cs4330.carpro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by marcolopez on 5/3/17.
 */

public class ReadingItemAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<ReadingItem> mDataSource;

    public ReadingItemAdapter(Context context, ArrayList<ReadingItem> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //1
    @Override
    public int getCount() {
        return mDataSource.size();
    }

    //2
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    //3
    @Override
    public long getItemId(int position) {
        return position;
    }

    //4
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ReadingItem thisItem = (ReadingItem) getItem(position);

        // Get view for row item
        View rowView = mInflater.inflate(R.layout.list_item_reading, parent, false);

        // Get title element
        TextView titleTextView =
                (TextView) rowView.findViewById(edu.utep.cs.cs4330.carpro.R.id.recipe_list_title);

        // Get subtitle element
        TextView subtitleTextView =
                (TextView) rowView.findViewById(edu.utep.cs.cs4330.carpro.R.id.recipe_list_subtitle);

        // Get detail element
        TextView detailTextView =
                (TextView) rowView.findViewById(edu.utep.cs.cs4330.carpro.R.id.recipe_list_detail);


        titleTextView.setText(thisItem.title);
        subtitleTextView.setText(thisItem.description);
        detailTextView.setText(thisItem.details);

        return rowView;
    }
}
