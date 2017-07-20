package io.github.fmilitao.shopper.shops;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.fmilitao.shopper.R;
import io.github.fmilitao.shopper.sql.queries.JoinShopsItems;

public class ShopsAdapter extends CursorAdapter {

    private static class ViewHolder {
        public final TextView mShopName;
        public final TextView mNotDoneItems;
        public final TextView mAllItems;

        public ViewHolder(View view) {
            mShopName = (TextView) view.findViewById(R.id.list_name);
            mNotDoneItems = (TextView) view.findViewById(R.id.list_pending);
            mAllItems = (TextView) view.findViewById(R.id.list_size);
        }
    }

    final View.OnTouchListener mTouchListener;

    public ShopsAdapter(Context context, Cursor c, int flags, View.OnTouchListener listener) {
        super(context, c, flags);

        mTouchListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.shop_list_row, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        view.setOnTouchListener(mTouchListener);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.mShopName.setText(JoinShopsItems.getName(cursor));
        viewHolder.mAllItems.setText(JoinShopsItems.getItemCountString(cursor));
        viewHolder.mNotDoneItems.setText(JoinShopsItems.getNotDoneItemCountString(cursor));
    }

}