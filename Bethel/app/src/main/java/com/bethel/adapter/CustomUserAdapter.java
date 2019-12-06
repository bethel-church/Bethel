package com.bethel.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bethel.R;
import com.bethel.model.TripModel;
import com.bethel.model.UserModel;

import java.util.ArrayList;
import java.util.List;

public class CustomUserAdapter extends BaseAdapter implements Filterable{

    private   LayoutInflater inflater=null;

    ArrayList<UserModel.TripsEntity.UserEntity> mItems = new ArrayList<>();
    CustomUserAdapter.OnUserClickListener mListener;
    List<UserModel.TripsEntity.UserEntity> mStringFilterList;
    ValueFilter valueFilter;

    public CustomUserAdapter(Context mContext, ArrayList<UserModel.TripsEntity.UserEntity> mItems) {
        this.mItems = mItems;
        this.mStringFilterList=mItems;
        this.mListener = (CustomUserAdapter.OnUserClickListener) mContext;
        inflater = ( LayoutInflater )mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    public interface OnUserClickListener {
        void OnUserClicked(UserModel.TripsEntity.UserEntity userModel);
    }





    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mItems == null ? 0 : mItems.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewHolder holder = null;

        if (convertView == null) {
        convertView = inflater.inflate(R.layout.item_trips, null);
            holder = new ViewHolder();
            holder.chBoxUcast=(CheckBox)convertView.findViewById(R.id.checkTripName);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.chBoxUcast.setText(mItems.get(position).getFirst_name()+ " "+ mItems.get(position).getMiddle_name()+" "+mItems.get(position).getLast_name());

        holder.chBoxUcast.setOnCheckedChangeListener(null);


        //if true, your checkbox will be selected, else unselected
        holder.chBoxUcast.setChecked(   mItems.get(position).isChecked());


        final ViewHolder finalHolder = holder;
        holder.chBoxUcast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                finalHolder.chBoxUcast.setChecked(b);
                if (b) {
                    for (int i = 0; i < mItems.size(); i++) {
                        if (position == i) {
                            mItems.get(i).setChecked(b);
                        } else
                            mItems.get(i).setChecked(false);

                    }
                    if (mListener != null) {
                        mListener.OnUserClicked(mItems.get(position));

                        for(int i=0;i<mStringFilterList.size();i++){
                            if(mStringFilterList.get(i).getId().equalsIgnoreCase(mItems.get(position).getId())){
                                mStringFilterList.get(i).setChecked(true);
                            }else{
                                mStringFilterList.get(i).setChecked(false);
                            }
                        }
                    }
                    try {
                        notifyDataSetChanged();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    mItems.get(position).setChecked(b);
                }
            }
        });


        return convertView;
    }

    public class ViewHolder {
        CheckBox chBoxUcast;
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
                List<UserModel.TripsEntity.UserEntity> filterList = new ArrayList<>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if (((""+mStringFilterList.get(i).getFirst_name()).toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {

                        UserModel.TripsEntity.UserEntity attendee = new UserModel.TripsEntity.UserEntity();
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
                mItems = (ArrayList<UserModel.TripsEntity.UserEntity>) results.values;
                notifyDataSetChanged();
            }
        }

    }

    /**
     * update the items of list
     * & notify the adapter about change
     *
     * @param items updated items
     */
    public void updateList(List<UserModel.TripsEntity.UserEntity> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

}
