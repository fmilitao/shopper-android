package pt.blah.shopper.shops;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.blah.shopper.R;

import static pt.blah.shopper.sql.DBContract.JoinShopItemQuery.INDEX_ALL_ITEMS_COUNT;
import static pt.blah.shopper.sql.DBContract.JoinShopItemQuery.INDEX_NAME;
import static pt.blah.shopper.sql.DBContract.JoinShopItemQuery.INDEX_NOT_DONE_ITEMS_COUNT;

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

        viewHolder.mShopName.setText(cursor.getString(INDEX_NAME));
        viewHolder.mAllItems.setText(cursor.getString(INDEX_ALL_ITEMS_COUNT));
        viewHolder.mNotDoneItems.setText(cursor.getString(INDEX_NOT_DONE_ITEMS_COUNT));
    }

}