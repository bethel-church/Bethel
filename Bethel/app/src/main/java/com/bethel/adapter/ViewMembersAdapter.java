package com.bethel.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.bethel.R;
import com.bethel.interfaces.CategoryCallback;
import com.bethel.model.UserDetailsModel;
import com.bethel.model.UserModel;
import com.bethel.ui.ViewMembersTrips;
import com.bethel.utils.SharedPreferencesHandler;

import java.util.ArrayList;
import java.util.List;


public class ViewMembersAdapter extends BaseAdapter implements Filterable {
    Context context;
    List<UserDetailsModel> rowItems;
    ArrayList<UserDetailsModel> mItems=new ArrayList<>();
    List<UserDetailsModel> mStringFilterList;
     ValueFilter valueFilter;
    public ViewMembersAdapter(Context context, List<UserDetailsModel> items) {
        this.context = context;
        this.rowItems = items;
        this.mStringFilterList=rowItems;
    }

    /*private view holder class*/
    private class ViewHolder {
        TextView checkTripName;
    }
    int itemSelected=-1;

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        LayoutInflater mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_trip_members, null);
            holder = new ViewHolder();
            holder.checkTripName = (TextView) convertView.findViewById(R.id.checkTripName);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final String name=rowItems.get(position).getFirst_name()+" "+
                rowItems.get(position).getMiddle_name()+" "+
                rowItems.get(position).getLast_name();
        double budget=Double.valueOf(rowItems.get(position).getTotal_spent());
        budget=Math.round(budget * 100.0) / 100.0;
        String storedName= SharedPreferencesHandler.getStringValues(context, "First_Name")+" "+
                SharedPreferencesHandler.getStringValues(context, "Middle_Name")+" "+
                SharedPreferencesHandler.getStringValues(context, "Last_Name");
       /* if(storedName.equalsIgnoreCase(name)){
            holder.checkTripName.setText("Me "+ "($"+budget+")");
        }else{*/
            holder.checkTripName.setText(name+" ($"+budget+")");
//        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context,name,Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(context, ViewMembersTrips.class);
                intent.putExtra("memberId",rowItems.get(position).getId());
                intent.putExtra("firstname",rowItems.get(position).getFirst_name());
                intent.putExtra("middlename",rowItems.get(position).getMiddle_name());
                intent.putExtra("lastname",rowItems.get(position).getLast_name());
                intent.putExtra("name",name);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    @Override
    public int getCount() {
        return rowItems.size();
    }

    @Override
    public Object getItem(int position) {
        return rowItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rowItems.indexOf(getItem(position));
    }


    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                List<UserDetailsModel> filterList = new ArrayList<>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if (((""+mStringFilterList.get(i).getFirst_name()).toUpperCase())
                            .contains(constraint.toString().toUpperCase())
                            ||((""+mStringFilterList.get(i).getLast_name()).toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {

                        UserDetailsModel attendee = new UserDetailsModel();
                        attendee=mStringFilterList.get(i);
                        filterList.add(attendee);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mStringFilterList.size();
                results.values = mStringFilterList;
            }
            return results;

        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            if(results.values!=null) {
                rowItems = (ArrayList<UserDetailsModel>) results.values;
                notifyDataSetChanged();
            }
        }

    }
}
