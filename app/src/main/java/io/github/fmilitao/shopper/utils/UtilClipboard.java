package io.github.fmilitao.shopper.utils;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class UtilClipboard {

    //
    // Clipboard I/O
    //

    /**
     * Fetches the content of the clipboard as a text string.
     *
     * @return The clipboard text or null if nothing is there.
     */
    static public String getClipboardString(Activity activity) {
        Object clipboardService = activity.getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboardService == null || !(clipboardService instanceof ClipboardManager)) {
            return null;
        }

        ClipboardManager clipboard = (ClipboardManager) clipboardService;
        ClipData text = clipboard.getPrimaryClip();

        if (text == null || text.getItemCount() <= 0) {
            return null;
        }

        return text.getItemAt(0).coerceToText(activity).toString();
    }

    /**
     * Sets the clipboard to the given text string, if possible.
     *
     * @return true if the text was set, false otherwise.
     */
    static public boolean setClipboardString(Activity activity, String label, String text) {
        Object clipboardService = activity.getSystemService(Context.CLIPBOARD_SERVICE);

        if (clipboardService == null || !(clipboardService instanceof ClipboardManager)) {
            return false;
        }

        ClipboardManager clipboard = (ClipboardManager) clipboardService;
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        return true;
    }

}
