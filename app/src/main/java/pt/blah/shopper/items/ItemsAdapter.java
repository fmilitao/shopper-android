package pt.blah.shopper.items;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import pt.blah.shopper.R;

import static pt.blah.shopper.sql.DBContract.SelectItemQuery.INDEX_IS_DONE;
import static pt.blah.shopper.sql.DBContract.SelectItemQuery.INDEX_NAME;
import static pt.blah.shopper.sql.DBContract.SelectItemQuery.INDEX_QUANTITY;

public class ItemsAdapter extends CursorAdapter {

    private static class ViewHolder {
        public final TextView mItemName;
        public final TextView mItemQuantity;
        public final int mTextColor, mFlags;

        public ViewHolder(View view) {
            mItemName = (TextView) view.findViewById(R.id.item_name);
            mItemQuantity = (TextView) view.findViewById(R.id.item_quantity);

            mTextColor  = mItemName.getCurrentTextColor();
            mFlags = mItemName.getPaintFlags();
        }
    }

    final View.OnTouchListener mTouchListener;

    public ItemsAdapter(Context context, Cursor c, int flags, View.OnTouchListener listener) {
        super(context, c, flags);

        mTouchListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_row, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        view.setOnTouchListener(mTouchListener);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.mItemName.setText(cursor.getString(INDEX_NAME));
        viewHolder.mItemQuantity.setText(cursor.getString(INDEX_QUANTITY));

        boolean isDone = cursor.getInt(INDEX_IS_DONE) != 0;

        if( isDone ){
            viewHolder.mItemName.setPaintFlags(viewHolder.mItemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.mItemName.setTextColor(Color.GRAY);

            viewHolder.mItemQuantity.setPaintFlags(viewHolder.mItemQuantity.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            viewHolder.mItemQuantity.setTextColor(Color.GRAY);

            view.setBackgroundColor(Color.LTGRAY);
        }else{
            viewHolder.mItemName.setPaintFlags(viewHolder.mFlags);
            viewHolder.mItemName.setTextColor(viewHolder.mTextColor);

            viewHolder.mItemQuantity.setPaintFlags(viewHolder.mFlags);
            viewHolder.mItemQuantity.setTextColor(viewHolder.mTextColor);

            view.setBackgroundColor(Color.WHITE);
        }
    }

}