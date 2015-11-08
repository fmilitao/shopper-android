package pt.blah.shopper;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.blah.shopper.utils.UtilAdapter;

import static pt.blah.shopper.utils.Utilities.sData;

public class ShopsListAdapter extends UtilAdapter {

    final LayoutInflater mInflater;
    final Context mContext;
    final View.OnTouchListener mTouchListener;

    public ShopsListAdapter(Context context, View.OnTouchListener listener) {
        super(context);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mTouchListener = listener;
    }

    @Override
    public int getCount() {
        return sData.getShopCount();
    }

    @Override
    public Object getItem(int position) {
        return sData.getShop(position);
    }

    @Override
    public long getItemId(int position) {
        return ((DataDB.Shop) getItem(position)).id;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if (convertView == null) {
            view = mInflater.inflate(R.layout.shop_row, parent, false);

            holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.list_name);
            holder.size = (TextView) view.findViewById(R.id.list_pending);
            holder.total = (TextView) view.findViewById(R.id.list_size);

            view.setTag(holder);

            view.setOnTouchListener(mTouchListener);
        } else {
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }

        DataDB.Shop pair = sData.getShop(position);
        holder.name.setText(pair.getName());
        holder.size.setText(format(R.string.NUMBER, pair.getPending()));
        holder.total.setText(format(R.string.NUMBER, pair.getProductCount()));
        return view;
    }

    private class ViewHolder {
        public TextView name, size, total;
    }
}