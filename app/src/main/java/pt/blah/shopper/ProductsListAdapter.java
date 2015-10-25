package pt.blah.shopper;


import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;

import static pt.blah.shopper.Utilities.sData;

public class ProductsListAdapter extends BaseAdapter {

    final int pos;
    final DataDB.Shop shop;
    final LayoutInflater mInflater;

    HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

    public ProductsListAdapter(Context context, int position) {
        mInflater = LayoutInflater.from(context);
        pos = position;
        shop = sData.list.get(position);

        for (int i = 0; i < shop.products.size(); ++i) {
            mIdMap.put(shop.products.get(i).name, i);
        }
    }

    @Override
    public int getCount() {
        return shop.products.size();
    }

    @Override
    public Object getItem(int position) {
        return shop.products.get(position);
    }

//    @Override
//    public long getItemId(int position) {
//        return position;
//    }

    @Override
    public long getItemId(int position) {
        String item = ((DataDB.Product)getItem(position)).name;
        return mIdMap.get(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.product_row, parent, false);

            holder = new ViewHolder();
            holder.name = (TextView)view.findViewById(R.id.product_name);
            holder.quantity = (TextView)view.findViewById(R.id.product_quantity);

            holder.color = holder.name.getCurrentTextColor();
            holder.flags = holder.name.getPaintFlags();

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        DataDB.Product product = shop.products.get(position);
        holder.name.setText(product.name);
        holder.quantity.setText(Integer.toString(product.quantity));

        if( product.done ){
            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.name.setTextColor(Color.GRAY);

            holder.quantity.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.quantity.setTextColor(Color.GRAY);

            view.setBackgroundColor(ContextCompat.getColor(Utilities.context, R.color.GRAY_BACK));
        }else{
            holder.name.setPaintFlags(holder.flags);
            holder.name.setTextColor(holder.color);

            holder.quantity.setPaintFlags(holder.flags);
            holder.quantity.setTextColor(holder.color);

            view.setBackgroundColor(ContextCompat.getColor(Utilities.context, R.color.NORMAL_BACK));
        }

        return view;
    }

    private class ViewHolder {
        public TextView quantity, name;
        public int color;
        public int flags;
    }
}