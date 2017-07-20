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
import io.github.fmilitao.shopper.sql.queries.SelectShopItems;
import io.github.fmilitao.shopper.utils.UtilColors;

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

        viewHolder.mItemName.setText(SelectShopItems.getName(cursor));
        viewHolder.mItemQuantity.setText(SelectShopItems.getQuantityString(cursor));

        String unit = SelectShopItems.getUnit(cursor);
        if (unit != null) {
            viewHolder.mItemUnit.setVisibility(View.VISIBLE);
            viewHolder.mItemUnit.setText(unit);
        } else {
            viewHolder.mItemUnit.setVisibility(View.GONE);
        }

        boolean isDone = SelectShopItems.isDone(cursor);

        if (isDone) {
            viewHolder.mItemName.setPaintFlags(viewHolder.mItemName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            viewHolder.mItemName.setTextColor(Color.GRAY);
            viewHolder.mItemQuantity.setTextColor(Color.GRAY);
            viewHolder.mItemUnit.setTextColor(Color.GRAY);
            viewHolder.mItemCategory.setVisibility(View.GONE);

            // setting alpha conflicts with fade out animations
            // view.setAlpha(0.5f);
            view.setBackgroundColor(Color.LTGRAY);
        } else {

            String category = SelectShopItems.getCategory(cursor);
            if (category != null) {
                Integer color = UtilColors.colorMap.get(category);

                // category has color
                if (color != null) {
                    // hides text and paints background with color
                    viewHolder.mItemCategory.setVisibility(View.GONE);
                    view.setBackgroundColor(color);
                } else {
                    // category has no picked color
                    // shows label over default background color
                    viewHolder.mItemCategory.setVisibility(View.VISIBLE);
                    viewHolder.mItemCategory.setText(category);
                    view.setBackgroundColor(Color.WHITE);
                }
            } else {
                viewHolder.mItemCategory.setVisibility(View.GONE);
                view.setBackgroundColor(Color.WHITE);
            }

            viewHolder.mItemName.setPaintFlags(viewHolder.mFlags);
            viewHolder.mItemName.setTextColor(viewHolder.mTextColor);
            viewHolder.mItemQuantity.setTextColor(viewHolder.mTextColor);
            viewHolder.mItemUnit.setTextColor(viewHolder.mTextColor);

            // setting alpha here conflicts with animation fade-out
            // view.setAlpha(1);
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

            mTextColor = mItemName.getCurrentTextColor();
            mFlags = mItemName.getPaintFlags();
        }
    }
}