package io.github.fmilitao.shopper.items;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.github.fmilitao.shopper.R;

import static io.github.fmilitao.shopper.sql.DBContract.SelectItemQuery.INDEX_CATEGORY;
import static io.github.fmilitao.shopper.sql.DBContract.SelectItemQuery.INDEX_IS_DONE;
import static io.github.fmilitao.shopper.sql.DBContract.SelectItemQuery.INDEX_NAME;
import static io.github.fmilitao.shopper.sql.DBContract.SelectItemQuery.INDEX_QUANTITY;
import static io.github.fmilitao.shopper.sql.DBContract.SelectItemQuery.INDEX_UNIT;

public class ItemsAdapter extends CursorAdapter {

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

        String unit = cursor.getString(INDEX_UNIT);
        if( unit != null  ){
            viewHolder.mItemUnit.setVisibility(View.VISIBLE);
            viewHolder.mItemUnit.setText(unit);
        }else{
            viewHolder.mItemUnit.setVisibility(View.GONE);
        }

        boolean isDone = cursor.getInt(INDEX_IS_DONE) != 0;

        String category = cursor.getString(INDEX_CATEGORY);
        if( !isDone && category != null  ){
            viewHolder.mItemCategory.setVisibility(View.VISIBLE);
            viewHolder.mItemCategory.setText(category);
        }else{
            viewHolder.mItemCategory.setVisibility(View.GONE);
        }

        if( isDone ){
            viewHolder.mItemName.setPaintFlags(viewHolder.mItemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            viewHolder.mItemName.setTextColor(Color.GRAY);
            viewHolder.mItemQuantity.setTextColor(Color.GRAY);
            viewHolder.mItemUnit.setTextColor(Color.GRAY);

            view.setAlpha(0.5f);
            view.setBackgroundColor(Color.LTGRAY);
        }else{
            viewHolder.mItemName.setPaintFlags(viewHolder.mFlags);

            viewHolder.mItemName.setTextColor(viewHolder.mTextColor);
            viewHolder.mItemQuantity.setTextColor(viewHolder.mTextColor);
            viewHolder.mItemUnit.setTextColor(viewHolder.mTextColor);

            view.setAlpha(1);
            view.setBackgroundColor(Color.WHITE);
        }
    }

    private static class ViewHolder {
        public final TextView mItemName, mItemQuantity, mItemUnit, mItemCategory;
        public final int mTextColor, mFlags;

        public ViewHolder(View view) {
            mItemName = (TextView) view.findViewById(R.id.item_name);
            mItemQuantity = (TextView) view.findViewById(R.id.item_quantity);
            mItemUnit = (TextView) view.findViewById(R.id.item_unit);
            mItemCategory = (TextView) view.findViewById(R.id.item_category);

            mTextColor  = mItemName.getCurrentTextColor();
            mFlags = mItemName.getPaintFlags();
        }
    }
}