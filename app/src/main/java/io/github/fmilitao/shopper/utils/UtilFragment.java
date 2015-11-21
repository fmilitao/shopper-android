package io.github.fmilitao.shopper.utils;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import io.github.fmilitao.shopper.R;

/**
 * Extensions to 'Fragment' to include some convenient methods for formatting string resources
 * and showing pop-up toasts on the top of the screen.
 */
public class UtilFragment extends Fragment {

    private static final int REQUEST_FILE_SAVE = 42;
    private static final int REQUEST_FILE_LOAD = 24;

    public String format(int id, Object... args) {
        return String.format(getActivity().getResources().getString(id), args);
    }

    public void popUp(String notification) {
        Toast t = Toast.makeText(getActivity().getApplicationContext(),
                notification,
                Toast.LENGTH_SHORT);
        t.setGravity(Gravity.TOP, 0, 0);
        t.show();
    }

    //
    // Save Dialog
    //

    private void dialog(String path, final boolean isLoad) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.file_dialog, null);
        final EditText txt = (EditText) root.findViewById(R.id.file_path);

        if (path != null) {
            txt.setText(path);
        }

        builder.setView(root)
                .setTitle(isLoad ? R.string.LOAD_DIALOG : R.string.SAVE_DIALOG)
                .setPositiveButton(isLoad ? R.string.LOAD : R.string.SAVE, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (isLoad)
                            load(txt.getText().toString());
                        else
                            save(txt.getText().toString());
                    }
                })
                .setNeutralButton(R.string.PICK_FILE, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("file/*");
                        startActivityForResult(Intent.createChooser(intent, getActivity().getString(R.string.SELECT_FILE)),
                                isLoad ? REQUEST_FILE_LOAD : REQUEST_FILE_SAVE);

                    }
                })
                .setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // intentionally empty
                    }
                })
                .create()
                .show();
    }

    protected void saveDialog(String path) {
        dialog(path, false);
    }

    protected void loadDialog(String path) {
        dialog(path, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_FILE_SAVE && data != null) {
            saveDialog(data.getData().getPath());
        }
        if (requestCode == REQUEST_FILE_LOAD && data != null) {
            loadDialog(data.getData().getPath());
        }

    }

    protected void save(String file){
        // does nothing
    }

    protected void load(String file){
        // does nothing
    }

    //
    // convenient class to enable an item when a dialog is first shown
    //

    protected static final class EnableOnShow implements DialogInterface.OnShowListener{

        MenuItem mItem;

        public EnableOnShow(MenuItem item){
            mItem = item;
        }

        @Override
        public void onShow(DialogInterface dialog) {
            // after dialog is showing, restore button to enabled
            mItem.setEnabled(true);
        }
    }

}
