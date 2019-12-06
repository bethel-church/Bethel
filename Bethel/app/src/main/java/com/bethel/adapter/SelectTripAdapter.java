package com.bethel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;

import com.bethel.R;
import com.bethel.model.TripModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by krishan on 28-09-2016.
 */

public class SelectTripAdapter extends RecyclerView.Adapter<SelectTripAdapter.ViewHolder> implements Filterable {

    ArrayList<TripModel.TripsEntity> mItems = new ArrayList<>();
    OnTripClickListener mListener;
    List<TripModel.TripsEntity> mStringFilterList;
    ValueFilter valueFilter;

    public SelectTripAdapter(Context mContext, ArrayList<TripModel.TripsEntity> mItems) {
        this.mItems = mItems;
        this.mStringFilterList=mItems;
        this.mListener = (OnTripClickListener) mContext;
    }




    public interface OnTripClickListener {
        void OnTripClicked(TripModel.TripsEntity tripsEntity);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trips, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.checkTripName.setChecked(mItems.get(position).getTrip().isChecked());
        holder.checkTripName.setText(mItems.get(position).getTrip().getName());

        //in some cases, it will prevent unwanted situations
        holder.checkTripName.setOnCheckedChangeListener(null);

        //if true, your checkbox will be selected, else unselected
        holder.checkTripName.setChecked(   mItems.get(position).getTrip().isChecked());



        holder.checkTripName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                holder.checkTripName.setChecked(b);
                if (b) {
                    for (int i = 0; i < mItems.size(); i++) {
                        if (position == i) {
                            mItems.get(i).getTrip().setChecked(b);
                        } else
                            mItems.get(i).getTrip().setChecked(false);

                    }
                    if (mListener != null) {
                        mListener.OnTripClicked(mItems.get(position));

                        for(int i=0;i<mStringFilterList.size();i++){
                            if(mStringFilterList.get(i).getTrip().getId().equalsIgnoreCase(mItems.get(position).getTrip().getId())){
                                mStringFilterList.get(i).getTrip().setChecked(true);
                            }else{
                                mStringFilterList.get(i).getTrip().setChecked(false);
                            }
                        }
                    }
                    try {
                        notifyDataSetChanged();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    mItems.get(position).getTrip().setChecked(b);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    /**
     * update the items of list
     * & notify the adapter about change
     *
     * @param items updated items
     */
    public void updateList(List<TripModel.TripsEntity> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.checkTripName)
        CheckBox checkTripName;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


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
                List<TripModel.TripsEntity> filterList = new ArrayList<>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if (((""+mStringFilterList.get(i).getTrip().getName()).toUpperCase())
                            .contains(constraint.toString().toUpperCase())) {

                        TripModel.TripsEntity attendee = new TripModel.TripsEntity();
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
                mItems = (ArrayList<TripModel.TripsEntity>) results.values;
                notifyDataSetChanged();
            }
        }

    }
}
