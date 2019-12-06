package com.bethel.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;

import com.bethel.R;
import com.bethel.model.UserModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kuljeetsingh on 10/1/16.
 */

public class SelectUserAdapter extends RecyclerView.Adapter<SelectUserAdapter.ViewHolder> implements Filterable {

    ArrayList<UserModel.TripsEntity.UserEntity> mItems = new ArrayList<>();
    OnUserClickListener mListener;
    List<UserModel.TripsEntity.UserEntity> mStringFilterList;
    ValueFilter valueFilter;


    public SelectUserAdapter(Context mContext, ArrayList<UserModel.TripsEntity.UserEntity> mItems) {
        this.mItems = mItems;
        this.mListener = (OnUserClickListener) mContext;
        this.mStringFilterList=mItems;
    }



    public interface OnUserClickListener {
        void OnUserClicked(UserModel.TripsEntity.UserEntity userModel);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trips, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.checkTripName.setChecked(mItems.get(position).isChecked());
        holder.checkTripName.setText(Html.fromHtml(mItems.get(position).getFirst_name() + " <b>"+mItems.get(position).getMiddle_name()+" "+mItems.get(position).getLast_name()+"</b>"));
        holder.checkTripName.setOnCheckedChangeListener(null);
        holder.checkTripName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                holder.checkTripName.setChecked(b);

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
                    }catch (Exception e ){
                        e.printStackTrace();
                    }
                } else {
                    mItems.get(position).setChecked(b);
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
    public void updateList(List<UserModel.TripsEntity.UserEntity> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
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
                List<UserModel.TripsEntity.UserEntity> filterList = new ArrayList<>();
                for (int i = 0; i < mStringFilterList.size(); i++) {
                    if (((""+mStringFilterList.get(i).getFirst_name()).toUpperCase())
                            .contains(constraint.toString().toUpperCase())
                            ||((""+mStringFilterList.get(i).getLast_name()).toUpperCase())
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
}
