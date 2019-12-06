package com.bethel.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.bethel.R;
import com.bethel.interfaces.FilterCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public class FilterCategoryAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String>catList;
    private String selectedCat,selectedarr;
    private FilterCallback mFilterCallback;

    public FilterCategoryAdapter(Context context, ArrayList<String>catList,String selectedCat) {
        this.context = context;
        this.catList = catList;
        this.selectedCat=selectedCat;
        this.selectedarr=selectedCat;
        mFilterCallback=(FilterCallback)context;
    }

    /*private view holder class*/
    private class ViewHolder {
        CheckBox checkTripName;
    }
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        LayoutInflater mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_trips, null);
            holder = new ViewHolder();
            holder.checkTripName = (CheckBox) convertView.findViewById(R.id.checkTripName);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.checkTripName.setChecked(false);

        holder.checkTripName.setText(catList.get(position));
        final List<String> items = new LinkedList<String>( Arrays.asList(selectedarr.split("\\s*,\\s*")));
       if(items.size()==1){
           if (items.get(0).equalsIgnoreCase(catList.get(position))) {
               holder.checkTripName.setChecked(true);
           }else{
               holder.checkTripName.setChecked(false);
           }
       }else {
           if (items.size() > 0) {
               for (int i = 0; i < items.size(); i++) {
                   if (items.get(i).equalsIgnoreCase(catList.get(position))) {
                       holder.checkTripName.setChecked(true);
                   } else {
                   //   holder.checkTripName.setChecked(false);
                   }
               }
           }
       }
     /*   if(catList.get(position).equalsIgnoreCase(selectedCat)){
            holder.checkTripName.setChecked(true);
        }else{
         //  holder.checkTripName.setChecked(false);
        }*/

       holder.checkTripName.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               boolean add=true;
               selectedCat=catList.get(position);
               if(selectedCat.equalsIgnoreCase("All")){
                   selectedarr=selectedCat;
               }else{
                   if(selectedarr.contains("All")){
                       selectedarr="";
                   }
                   if(items.contains(selectedCat)){
                       add=false;
                       items.remove(selectedCat);
                       selectedarr="";
                       for(int i=0;i<items.size();i++){
                           if(i==0){
                               selectedarr=items.get(i);
                           }else{
                               selectedarr=selectedarr+","+items.get(i);
                           }
                       }

                   }else {
                       if (selectedarr.length() > 0) {
                           selectedarr = selectedarr + "," + selectedCat;
                       } else {
                           selectedarr = selectedCat;
                       }
                   }
               }

               mFilterCallback.filterSelection(selectedCat,add);
               notifyDataSetChanged();
           }
       });
        return convertView;
    }

    @Override
    public int getCount() {
        return catList.size();
    }

    @Override
    public Object getItem(int position) {
        return catList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return catList.indexOf(getItem(position));
    }
}
