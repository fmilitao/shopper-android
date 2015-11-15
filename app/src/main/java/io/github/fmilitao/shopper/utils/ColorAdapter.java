package io.github.fmilitao.shopper.utils;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Map;
import java.util.TreeMap;

import io.github.fmilitao.shopper.R;

public class ColorAdapter extends ArrayAdapter<String>{

    static public final Map<String,Integer> colorMap = new TreeMap<>();

    static final String DEFAULT = "<Show Text>";

    static String[] colorNames;
    static int[] colorValues;

    static boolean hasDefaults = false;
    static int defaultText;
    static int defaultBackground;

    static public int getColorPosition(int color){
        if( colorValues == null )
            return -1;
        for(int i=0; i<colorValues.length; ++i ){
            if( colorValues[i] == color )
                return i;
        }
        return -1;
    }

    static public int getColorAt(int position){
        if( colorValues == null )
            return -1;
        return colorValues[position];
    }

    static private String[] init(Context context){
        // loads all color names and color values
        if( colorNames == null ){
            // initializes colorNames including default value
            String[] tmp = context.getResources().getStringArray(R.array.colorNames);
            colorNames = new String[tmp.length+1];
            colorNames[0] = DEFAULT;
            System.arraycopy(tmp,0,colorNames,1,tmp.length);

            // initializes colorValues, position 0 is 0 for no special reason.
            colorValues = new int[tmp.length+1];
            colorValues[0] = 0;
            TypedArray ta = context.getResources().obtainTypedArray(R.array.colors);
            for(int i = 0; i < tmp.length; ++i){
                colorValues[i+1] = ContextCompat.getColor(context, ta.getResourceId(i, 0));
            }
            ta.recycle();
        }
        return colorNames;
    }

    //
    //
    //

    public ColorAdapter(Context context, int resource) {
        super(context, resource, init(context));
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
            v.setBackgroundColor(colorValues[position]);
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
            int color = colorValues[position];
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
