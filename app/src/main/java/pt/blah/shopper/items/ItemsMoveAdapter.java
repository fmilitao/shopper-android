package pt.blah.shopper.items;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import pt.blah.shopper.DataDB;
import pt.blah.shopper.R;
import pt.blah.shopper.utils.UtilAdapter;

import static pt.blah.shopper.utils.Utilities.sData;

public class ItemsMoveAdapter extends UtilAdapter {

    final int pos;
    final DataDB.Shop shop;
    final LayoutInflater mInflater;
    final boolean[] set;

    public ItemsMoveAdapter(Context context, int position) {
        super(context);
        mInflater = LayoutInflater.from(context);
        pos = position;
        shop = sData.getShop(position);
        set = new boolean[shop.getProductCount()];
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
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.item_move_row, parent, false);

            holder = new ViewHolder();
            holder.name = (CheckBox)view.findViewById(R.id.item_name);
            holder.quantity = (TextView)view.findViewById(R.id.item_quantity);

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

        DataDB.Product product = shop.getProduct(position);
        holder.name.setText(product.getName());
        holder.quantity.setText( format(R.string.NUMBER, product.getQuantity()) );


        if( product.isDone() ){
            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.name.setTextColor(Color.GRAY);

            holder.quantity.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.quantity.setTextColor(Color.GRAY);

            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.GRAY_BACK));
        }else{
            holder.name.setPaintFlags(holder.flags);
            holder.name.setTextColor(holder.color);

            holder.quantity.setPaintFlags(holder.flags);
            holder.quantity.setTextColor(holder.color);

            view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.NORMAL_BACK));
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