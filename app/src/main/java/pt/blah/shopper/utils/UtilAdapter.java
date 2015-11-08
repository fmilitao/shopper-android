package pt.blah.shopper.utils;

import android.content.Context;
import android.widget.BaseAdapter;

/**
 * Extends 'BaseAdapter' for a more convenient 'format' method
 */
abstract public class UtilAdapter extends BaseAdapter{

    protected Context mContext;

    public UtilAdapter(Context context) {
        super();
        mContext = context;
    }

    protected String format(int id, Object... args){
        return String.format(mContext.getResources().getString(id),args);
    }

}
