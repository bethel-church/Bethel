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

import com.bethel.R;
import com.bethel.interfaces.CategoryCallback;

import java.util.List;


public class CategoryAdapter extends BaseAdapter {
    Context context;
    List<String> rowItems;
    CategoryCallback categoryCallback;

    public CategoryAdapter(Context context, List<String> items) {
        this.context = context;
        this.rowItems = items;
        categoryCallback= (CategoryCallback) context;
    }

        /*private view holder class*/
        private class ViewHolder {
            Button txtTitle;
        }
        int itemSelected=-1;

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.category_row_item, null);
                holder = new ViewHolder();
                holder.txtTitle = (Button) convertView.findViewById(R.id.catnametv);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

             holder.txtTitle.setText(rowItems.get(position));

            holder.txtTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemSelected=position;
                    categoryCallback.onCategoryClick(position,false);
                    notifyDataSetChanged();
                    }
            });

            if(itemSelected==position){
                holder.txtTitle.setBackgroundColor(ContextCompat.getColor(context,R.color.colorTextGreen));
            }else{
                holder.txtTitle.setBackgroundColor(Color.parseColor("#D3D3D3"));
            }

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
}
