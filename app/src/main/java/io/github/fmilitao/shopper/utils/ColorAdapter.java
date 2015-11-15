package io.github.fmilitao.shopper.utils;


import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ColorAdapter extends ArrayAdapter<String>{

    static boolean hasDefaults = false;
    static int defaultText;
    static int defaultBackground;

    public ColorAdapter(Activity activity, int resource) {
        super(activity, resource, UtilColors.colorNames);
    }

    protected void init(TextView v){
        if( !hasDefaults ){
            defaultBackground = v.getDrawingCacheBackgroundColor();
            defaultText = v.getCurrentTextColor();
            hasDefaults = true;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView v = (TextView) super.getView(position, convertView, parent);
        init(v);
        if( position != 0 ) {
            v.setBackgroundColor(UtilColors.colorValues[position]);
        }else{
            if( hasDefaults ){
                v.setBackgroundColor(defaultBackground);
                v.setTextColor(defaultText);
            }
        }
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView v = (TextView) super.getDropDownView(position, convertView, parent);
        init(v);

        if( position != 0 ) {
            int color = UtilColors.colorValues[position];
            v.setBackgroundColor(color);
            // inverts color to try to be readable
            v.setTextColor(Color.rgb(255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color)));
        }else{
            if( hasDefaults ){
                v.setBackgroundColor(defaultBackground);
                v.setTextColor(defaultText);
            }
        }
        return v;
    }
}
