package com.bethel.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bethel.R;
import com.bethel.interfaces.CategoryCallback;
import com.bethel.model.CurrencyModel1;

import java.util.List;


public class CurrencyAdapter extends BaseAdapter {
    Context context;
    List<CurrencyModel1> mSelectedCurrencyModelList;
    CategoryCallback categoryCallback;

    public CurrencyAdapter(Context context, List<CurrencyModel1> mSelectedCurrencyModelList) {
        this.context = context;
        this.mSelectedCurrencyModelList = mSelectedCurrencyModelList;
        categoryCallback= (CategoryCallback) context;
    }

    /*private view holder class*/
    private class ViewHolder {
        TextView txtTitle,tvCode;
        FrameLayout flLabelbg;
    }
    int itemSelected=-1,previousselected=-1;
    boolean sameSelection;
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        LayoutInflater mInflater = (LayoutInflater)
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.currency_row_item, null);
            holder = new ViewHolder();
            holder.txtTitle = (TextView) convertView.findViewById(R.id.catnametv);
            holder.tvCode = (TextView) convertView.findViewById(R.id.countrycodetv);
            holder.flLabelbg=(FrameLayout)convertView.findViewById(R.id.baclabefl) ;
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtTitle.setText(mSelectedCurrencyModelList.get(position).getCode());
        holder.tvCode.setText(mSelectedCurrencyModelList.get(position).getName());
        holder.tvCode.setVisibility(View.VISIBLE);
        holder.flLabelbg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        //        if(previousselected!=-1){
          /*          if(previousselected==position){
                        sameSelection=true;
                        notifyDataSetChanged();
                    }else{
                        itemSelected=position;
                        categoryCallback.onCategoryClick(mSelectedCurrencyModelList.get(position).getName());
                        notifyDataSetChanged();
                    }
                }else{*/
                    itemSelected=position;
                    categoryCallback.onCategoryClick(position,true);

                for(int i=0;i<mSelectedCurrencyModelList.size();i++) {

                    if(i==position) {
                        if (mSelectedCurrencyModelList.get(i).isChecked()) {
                            mSelectedCurrencyModelList.get(i).setChecked(false);
                        } else {
                            mSelectedCurrencyModelList.get(i).setChecked(true);
                        }
                    }else {
                        mSelectedCurrencyModelList.get(i).setChecked(false);

                    }

                }
                notifyDataSetChanged();
//                }
            }
        });
    /*    if(sameSelection){
            holder.txtTitle.setBackgroundColor(Color.parseColor("#D3D3D3"));
            sameSelection=false;
        }else {*/
       /*     if (itemSelected == position) {
                previousselected = position;
                holder.flLabelbg.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTextGreen));
                mSelectedCurrencyModelList.get(position).setChecked(true);
            } else {
                mSelectedCurrencyModelList.get(position).setChecked(false);
                holder.flLabelbg.setBackgroundColor(Color.parseColor("#D3D3D3"));
            }*/

        if(mSelectedCurrencyModelList.get(position).isChecked()){
                holder.flLabelbg.setBackgroundColor(ContextCompat.getColor(context, R.color.colorTextGreen));
            } else {
                holder.flLabelbg.setBackgroundColor(Color.parseColor("#D3D3D3"));
        }
      //  }
        return convertView;
    }

    @Override
    public int getCount() {
        return mSelectedCurrencyModelList.size();
    }

    @Override
    public Object getItem(int position) {
        return mSelectedCurrencyModelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mSelectedCurrencyModelList.indexOf(getItem(position));
    }
}
