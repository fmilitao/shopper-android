package io.github.fmilitao.shopper.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;

public class UtilTextWatcher implements TextWatcher{

    private AlertDialog mDialog;
    private boolean mHasNeutral;

    public UtilTextWatcher(AlertDialog d){
        this(d,false);
    }

    public UtilTextWatcher(AlertDialog d, boolean hasNeutral){
        mDialog = d;
        mHasNeutral = hasNeutral;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        boolean enable = s.toString().trim().length() > 0;
        mDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(enable);
        if( mHasNeutral ){
            mDialog.getButton(Dialog.BUTTON_NEUTRAL).setEnabled(enable);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
