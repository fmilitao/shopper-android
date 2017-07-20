package io.github.fmilitao.shopper.utils;


import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

// recall that UtilColors position 0 is the dummy/no-color
public class ColorAdapter extends ArrayAdapter<String> {

    private static boolean hasDefaults = false;
    private static int defaultText;
    private static int defaultBackground;

    public ColorAdapter(Activity activity, int resource) {
        super(activity, resource, UtilColors.colorNames);
    }

    private void init(TextView v) {
        if (!hasDefaults) {
            defaultBackground = v.getDrawingCacheBackgroundColor();
            defaultText = v.getCurrentTextColor();
            hasDefaults = true;
        }
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView v = (TextView) super.getView(position, convertView, parent);
        init(v);
        if (position != 0) {
            v.setBackgroundColor(UtilColors.colorValues[position]);
        } else {
            if (hasDefaults) {
                v.setBackgroundColor(defaultBackground);
                v.setTextColor(defaultText);
            }
        }
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView v = (TextView) super.getDropDownView(position, convertView, parent);
        init(v);

        if (position != 0) {
            int color = UtilColors.colorValues[position];
            v.setBackgroundColor(color);
            // inverts color to try to be readable
            v.setTextColor(Color.rgb(255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color)));
        } else {
            if (hasDefaults) {
                v.setBackgroundColor(defaultBackground);
                v.setTextColor(defaultText);
            }
        }
        return v;
    }
}
