package io.github.fmilitao.shopper.items;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import io.github.fmilitao.shopper.R;

import static io.github.fmilitao.shopper.sql.DBContract.SelectShopItemsQuery.INDEX_ID;
import static io.github.fmilitao.shopper.sql.DBContract.SelectShopItemsQuery.INDEX_IS_DONE;
import static io.github.fmilitao.shopper.sql.DBContract.SelectShopItemsQuery.INDEX_NAME;
import static io.github.fmilitao.shopper.sql.DBContract.SelectShopItemsQuery.INDEX_QUANTITY;

// FIXME-FEATURE does not display units nor category
public class ItemsMoveAdapter extends CursorAdapter {

    final Long[] set;

    public ItemsMoveAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        set = new Long[c.getCount()];
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_move_row, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final int position = cursor.getPosition();

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.mItemName.setText(cursor.getString(INDEX_NAME));
        viewHolder.mItemQuantity.setText(cursor.getString(INDEX_QUANTITY));

        final long itemId = cursor.getLong(INDEX_ID);
        final boolean isDone = cursor.getInt(INDEX_IS_DONE) != 0;

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

        viewHolder.mItemName.setOnClickListener(null);
        viewHolder.mItemName.setChecked(set[position] != null);
        viewHolder.mItemName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                set[position] = cb.isChecked() ? itemId : null;
            }
        });
    }

    public Long[] getSelectedItemIds(){
        return set;
    }

    private static class ViewHolder {
        public final CheckBox mItemName;
        public final TextView mItemQuantity;
        public final int mTextColor, mFlags;

        public ViewHolder(View view) {
            mItemName = (CheckBox) view.findViewById(R.id.item_name);
            mItemQuantity = (TextView) view.findViewById(R.id.item_quantity);

            mTextColor  = mItemName.getCurrentTextColor();
            mFlags = mItemName.getPaintFlags();
        }
    }
}