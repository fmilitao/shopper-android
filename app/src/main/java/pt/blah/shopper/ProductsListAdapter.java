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

import static pt.blah.shopper.Utilities.format;
import static pt.blah.shopper.Utilities.sData;

public class ProductsListAdapter extends BaseAdapter {

    final int pos;
    final DataDB.Shop shop;
    final LayoutInflater mInflater;
    final View.OnTouchListener mTouchListener;

    public ProductsListAdapter(Context context, int position, View.OnTouchListener listener) {
        mInflater = LayoutInflater.from(context);
        pos = position;
        shop = sData.getShop(position);
        mTouchListener = listener;
    }

    @Override
    public int getCount() {
        return shop.getProductCount();
    }

    @Override
    public Object getItem(int position) {
        return shop.getProduct(position);
    }

    @Override
    public long getItemId(int position) {
        return ((DataDB.Product)getItem(position)).id;
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

            view.setOnTouchListener(mTouchListener);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        DataDB.Product product = shop.getProduct(position);
        holder.name.setText(product.getName());
        holder.quantity.setText(format(R.string.NUMBER, product.getQuantity()));

        if( product.isDone() ){
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