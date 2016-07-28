package com.ibm.ecm.sample.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibm.ecm.api.coresdk.model.IBMECMContentItem;
import com.ibm.ecm.sample.R;
import com.ibm.ecm.sample.activities.SampleBrowseActivity;
import com.ibm.ecm.sample.activities.SampleSearchActivity;

import java.util.ArrayList;

/**
 Important: This sample project is not intended to be a tutorial on how to design or create an Android application.
 It is only for the purpose of showing how to use the IBM Content Navigator SDK.
 For example, it does not contain robust error handling, recovery, or ideal design structure.
 */
public class ContentItemArrayAdapter extends ArrayAdapter<IBMECMContentItem> {

    public ContentItemArrayAdapter(Activity activity) {
        super(activity, R.layout.listview_item, new ArrayList<IBMECMContentItem>());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity)getContext()).getLayoutInflater();
            view = inflater.inflate(R.layout.listview_item, null);
            final ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) view.findViewById(R.id.itemlabel);
            viewHolder.image = (ImageView) view.findViewById(R.id.itemicon);

            view.setTag(viewHolder);
        } else {
            view = convertView;
        }

        if ((position+1) == getCount()) {
            //a quick way to detect more pages (not ideal to do it here)
            //loads additional pages if there are more items
            Context context = getContext();
            if (context instanceof SampleBrowseActivity){
                ((SampleBrowseActivity)context).loadNextPage();
            }
            else if (context instanceof SampleSearchActivity){
                ((SampleSearchActivity)context).loadNextPage();
            }
        }

        ViewHolder holder = (ViewHolder) view.getTag();
        IBMECMContentItem item = getItem(position);
        holder.text.setText(item.getName());
        if(item.isFolder()) {
            holder.image.setImageResource(R.drawable.folder_icon);
        }
        else {
            holder.image.setImageResource(R.drawable.file_icon);
        }
        return view;
    }

    private static class ViewHolder {
        ImageView image;
        TextView text;
    }
}
