package com.bethel.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bethel.R;
import com.bethel.model.CurrencyModel1;
import com.bethel.model.SelectedCurrencyModel;

import java.util.List;

/**
 * Created by siddharth.brahmi on 10/7/2016.
 */

public class SelectedCurrencyAdapter extends BaseAdapter {
    private Context mContext;
    private List<CurrencyModel1> mSelectedCurrencyModelList;
    private LayoutInflater mInflater;
    private onItemDeletedListener monItemDeletedListener;
    public interface  onItemDeletedListener{
         void deletedItemPosition(int position);
    }
    public SelectedCurrencyAdapter(Context context, onItemDeletedListener onItemDeletedListener,List<CurrencyModel1> mSelectedCurrencyModelList)
    {
      this.mSelectedCurrencyModelList=mSelectedCurrencyModelList;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        monItemDeletedListener = onItemDeletedListener;
    }



    @Override
    public int getCount() {
        return mSelectedCurrencyModelList.size();
    }

    @Override
    public Object getItem(int i) {
        return mSelectedCurrencyModelList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mSelectedCurrencyModelList.get(i).hashCode();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null)
        {
            convertView = mInflater.inflate(R.layout.activity_leader_settings_selected_currencies, parent,false);
            viewHolder = new ViewHolder();
            viewHolder.mCurrencyname = (TextView) convertView.findViewById(R.id.activity_leader_setting_selected_currency);
            viewHolder.mDeleteCurrency = (ImageView)convertView.findViewById(R.id.activity_leader_setting_selected_currency_delete);

            convertView.setTag(viewHolder);
        }else{
            viewHolder =(ViewHolder) convertView.getTag();
        }
       viewHolder.mDeleteCurrency.setTag(position);

        viewHolder.mDeleteCurrency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = (int)((ImageView)v).getTag();
                monItemDeletedListener.deletedItemPosition(position);
            }
        });

        Drawable d;
        if(mSelectedCurrencyModelList.get(position).isDisabled())
        {
            d = viewHolder.mCurrencyname.getBackground();
            PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
            d.setColorFilter(Color.parseColor("#DBDBDB"), mMode);
            viewHolder.mDeleteCurrency.setImageDrawable(d);
            viewHolder.mCurrencyname.setTextColor(Color.parseColor("#9A9A9A"));
            viewHolder.mDeleteCurrency.setVisibility(View.GONE);
        }else{
            d = viewHolder.mDeleteCurrency.getDrawable();
            PorterDuff.Mode mMode = PorterDuff.Mode.SRC_ATOP;
            d.setColorFilter(Color.parseColor("#DADADA"), mMode);
            viewHolder.mDeleteCurrency.setImageDrawable(d);
            viewHolder.mDeleteCurrency.setVisibility(View.VISIBLE);
            viewHolder.mCurrencyname.setBackground(ContextCompat.getDrawable(mContext,R.drawable.grey_rounded));
        }
        viewHolder.mCurrencyname.setText(mSelectedCurrencyModelList.get(position).getCode() +"\n"+mSelectedCurrencyModelList.get(position).getName());
        return convertView;
    }

    private class ViewHolder
    {
        TextView mCurrencyname;
        ImageView mDeleteCurrency;
    }
}
