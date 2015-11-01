package pt.blah.shopper;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import static pt.blah.shopper.Utilities.sData;

public class ProductsMoveAdapter extends BaseAdapter {

    final int pos;
    final DataDB.Shop shop;
    final LayoutInflater mInflater;
    final boolean[] set;

    public ProductsMoveAdapter(Context context, int position) {
        mInflater = LayoutInflater.from(context);
        pos = position;
        shop = sData.list.get(position);
        set = new boolean[shop.products.size()];
    }

    @Override
    public int getCount() {
        return shop.products.size();
    }

    @Override
    public Object getItem(int position) {
        return shop.products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.move_row, parent, false);

            holder = new ViewHolder();
            holder.name = (CheckBox)view.findViewById(R.id.product_name);
            holder.quantity = (TextView)view.findViewById(R.id.product_quantity);

            holder.color = holder.name.getCurrentTextColor();
            holder.flags = holder.name.getPaintFlags();

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        holder.name.setOnClickListener(null);
        holder.name.setChecked(set[position]);
        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                set[position] = cb.isChecked();
            }
        });

        DataDB.Product product = shop.products.get(position);
        holder.name.setText(product.name);
        holder.quantity.setText( Utilities.format(R.string.NUMBER, product.quantity) );


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

    public boolean[] getSelected(){
        return set;
    }

    private class ViewHolder {
        public CheckBox name;
        public TextView quantity;
        public int color;
        public int flags;
    }
}