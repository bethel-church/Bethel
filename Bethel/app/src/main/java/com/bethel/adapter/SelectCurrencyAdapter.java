package com.bethel.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.bethel.R;
import com.bethel.model.CurrenciesListModel;
import com.bethel.model.CurrencyModel1;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by kuljeetsingh on 10/2/16.
 */

public class SelectCurrencyAdapter extends RecyclerView.Adapter<SelectCurrencyAdapter.ViewHolder> {

    ArrayList<CurrencyModel1> mItems = new ArrayList<>();
Context context ;
    public SelectCurrencyAdapter(Context mContext, ArrayList<CurrencyModel1> mItems) {
        context = mContext;
        this.mItems = mItems;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trips, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.checkTripName.setOnCheckedChangeListener(null);
        holder.checkTripName.setChecked(mItems.get(position).isChecked());
         holder.checkTripName.setText(mItems.get(position).getName());
        holder.checkTripName.setTag(position);
        holder.checkTripName.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                int count = checkCheckedCurrenciesLength();
                CheckBox checkBox = (CheckBox) compoundButton;
                int checkedposition = (int) checkBox.getTag();
                if (mItems.get(checkedposition).isChecked()) {
                    holder.checkTripName.setChecked(false);
                    mItems.get(checkedposition).setChecked(false);
                    notifyDataSetChanged();
                } else {
                    if (count <= 5) {
                        if (checkedposition == position) {
                            holder.checkTripName.setChecked(b);
                            mItems.get(checkedposition).setChecked(b);
                        }
                    } else {
                        holder.checkTripName.setChecked(false);
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Five already Selected!");
                        builder.setMessage("A maximum of 5 currencies can be selected for a trip!")
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do things
                                        dialog.dismiss();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                }
            }
        });
    }

    private int checkCheckedCurrenciesLength()
    {
        int selectedCurrencyCount = 1;
        for(int count=0;count<mItems.size();count++)
        {
            if(mItems.get(count).isChecked())
            {
                selectedCurrencyCount++;
            }
        }
       return selectedCurrencyCount;
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
    public void updateList(List<CurrencyModel1> items) {
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
}
