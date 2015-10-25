package pt.blah.shopper;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import static pt.blah.shopper.Utilities.sData;
import static pt.blah.shopper.Utilities.format;

public class ShopsListAdapter extends BaseAdapter {

    final LayoutInflater mInflater;
    final Context mContext;

    public ShopsListAdapter(Context context) {
        super();
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return sData.list.size();
    }

    @Override
    public Object getItem(int position) {
        return sData.list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder holder;
        if(convertView == null) {
            view = mInflater.inflate(R.layout.shop_row, parent, false);

            holder = new ViewHolder();
            holder.name = (TextView)view.findViewById(R.id.list_name);
            holder.size = (TextView)view.findViewById(R.id.list_size);

            view.setTag(holder);
        } else {
            view = convertView;
            holder = (ViewHolder)view.getTag();
        }

        DataDB.Shop pair = sData.list.get(position);
        holder.name.setText(pair.name);
        holder.size.setText(
                format(R.string.PENDING_ITEMS,
                    pair.getPending(),pair.products.size()) );

        return view;
    }

    private class ViewHolder {
        public TextView name, size;
    }
}