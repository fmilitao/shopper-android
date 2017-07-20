package io.github.fmilitao.shopper.utils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class UtilEditorActionListener implements EditText.OnEditorActionListener {

    private AlertDialog mDialog;

    public UtilEditorActionListener(AlertDialog d) {
        mDialog = d;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        // ENTER keyboard event or IME_ACTION_DONE
        if ((event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
                || (actionId == EditorInfo.IME_ACTION_DONE)) {
            Button ok = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            if (ok.isEnabled()) {
                ok.performClick();
                return true;
            }
        }
        return false;
    }
}
