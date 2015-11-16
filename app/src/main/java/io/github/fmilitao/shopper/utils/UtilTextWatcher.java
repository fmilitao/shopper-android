package io.github.fmilitao.shopper.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.text.Editable;
import android.text.TextWatcher;

public class UtilTextWatcher implements TextWatcher{

    private AlertDialog mDialog;

    public UtilTextWatcher(AlertDialog d){
        mDialog = d;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(s.toString().trim().length() > 0);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
