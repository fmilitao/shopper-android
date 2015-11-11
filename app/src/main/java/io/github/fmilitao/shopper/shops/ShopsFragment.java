package io.github.fmilitao.shopper.shops;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import io.github.fmilitao.shopper.R;
import io.github.fmilitao.shopper.items.ItemsActivity;
import io.github.fmilitao.shopper.sql.DBContract;
import io.github.fmilitao.shopper.sql.DatabaseMiddleman;
import io.github.fmilitao.shopper.utils.ListAnimations;
import io.github.fmilitao.shopper.utils.ShakeSensor;
import io.github.fmilitao.shopper.utils.TouchAndClickListener;
import io.github.fmilitao.shopper.utils.UtilFragment;
import io.github.fmilitao.shopper.utils.Utilities;


public class ShopsFragment extends UtilFragment implements ShakeSensor.ShakeListener,
        TouchAndClickListener.ClickListener, TouchAndClickListener.LongClickListener, TouchAndClickListener.SwipeOutListener{

    ListView mListView;
    ShakeSensor mShakeSensor;

    DatabaseMiddleman mDb;
    CursorAdapter mAdapter;

    Stack<Pair<String,Long>> undo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        undo = null;
        mShakeSensor = new ShakeSensor(this);
        mShakeSensor.onCreate(getActivity());

        mDb = new DatabaseMiddleman(getContext());
    }

    @Override
    public void onResume() {
        super.onResume();

        // this may be unnecessary, but we just force the notification to recompute
        // each list's done and total counts
        mDb.open();
        // since items table may have changed while we paused.
        mAdapter.changeCursor(mDb.fetchAllShops());
        mAdapter.notifyDataSetChanged();

        mShakeSensor.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        mDb.close();
        mShakeSensor.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.shops_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.import_list) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final LayoutInflater inflater = getActivity().getLayoutInflater();

            @SuppressLint("InflateParams")
            final View root = inflater.inflate(R.layout.shop_create_dialog, null);

            final CheckBox box = (CheckBox) root.findViewById(R.id.dialog_shop_clipboard);
            final EditText text = (EditText) root.findViewById(R.id.dialog_shop_name);

            final List<Pair<String,Integer>> list = new LinkedList<>();

            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (box.isChecked()) {
                        List<Pair<String,Integer>> tmp = Utilities.parseProductList(Utilities.getClipboardString(getActivity()));
                        if (tmp != null) {
                            list.addAll(tmp);
                            box.setText(format(R.string.INCLUDE_ITEMS, tmp.size()));
                        }
                    } else {
                        list.clear();
                        box.setText(R.string.INCLUDE_CLIPBOARD);
                    }
                }
            });

            builder.setTitle(R.string.NEW_LIST).setView(root);
            builder.setPositiveButton(R.string.CREATE, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final String name = text.getText().toString();
                    if (name.length() > 0) {

                        animateAdd(new ListAnimations.Runner() {
                            @Override
                            public void run(Set<Long> set) {
                                long newShop = mDb.createShop(name,list);
                                if( newShop > 0 ) {
                                    set.add(newShop);
                                    mAdapter.changeCursor(mDb.fetchAllShops());
                                    mAdapter.notifyDataSetChanged();
                                }
                            }
                        });

                        popUp(format(R.string.LIST_ADDED, name));
                    }

                }
            });

            builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // aborted, nothing to do
                }
            });

            AlertDialog dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.shop_list_fragment, container, false);
        mListView = (ListView) rootView.findViewById(R.id.shops_list);

        mDb.open();

        if( undo == null ) {
            // should only run once in the lifetime of this Fragment
            mDb.gcShops();
            undo = new Stack<>();

            // cleans tables and inserts some example values, for testing purposes
//            mDb.deleteAll();
//            mDb.insertSomeValues();
        }


        TouchAndClickListener t = new TouchAndClickListener(ViewConfiguration.get(getContext()),mListView);
        t.setOnClick(this);
        t.setOnLongClick(this);
        t.setOnSwipeOut(this);

        mAdapter = new ShopsAdapter(getActivity(), mDb.fetchAllShops(),0,t);

        mListView.setAdapter(mAdapter);

        return rootView;
    }

    private void animateAdd(ListAnimations.Runner action){
        ListAnimations.animateAdd(mAdapter, mListView, action);
    }

    @Override
    public void onShake() {
        if (undo.isEmpty()) {
            popUp(getString(R.string.SHAKE_FAIL));
            return;
        }

        final Pair<String,Long> u = undo.pop();
        final long shopId = u.second;
        final String shopName = u.first;

        animateAdd(new ListAnimations.Runner() {
            @Override
            public void run(Set<Long> set) {
                if( mDb.updateShopDeleted(shopId, false) ) {
                    set.add(shopId);
                    mAdapter.changeCursor(mDb.fetchAllShops());
                    mAdapter.notifyDataSetChanged();
                }
            }
        });

        popUp(format(R.string.SHAKE_UNDO,shopName));
    }

    @Override
    public void onClick(ListView listView, View view) {
        int position = mListView.getPositionForView(view);
        Cursor c = (Cursor) mListView.getItemAtPosition(position);
        long shopId = c.getLong(DBContract.JoinShopItemQuery.INDEX_ID);
        String shopName = c.getString(DBContract.JoinShopItemQuery.INDEX_NAME);

        Intent intent = new Intent(getActivity(), ItemsActivity.class);
        // links to position in the DB
        intent.putExtra(ItemsActivity.INTENT_SHOP_ID_LONG, shopId);
        intent.putExtra(ItemsActivity.INTENT_SHOP_NAME_STRING, shopName);
        startActivity(intent);

        // for animation
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void onLongClick(ListView listView, View view) {
        final int position = listView.getPositionForView(view);
        final Cursor cursor = (Cursor) listView.getItemAtPosition(position);

        final String oldName = cursor.getString(DBContract.JoinShopItemQuery.INDEX_NAME);
        final long shopId = cursor.getLong(DBContract.JoinShopItemQuery.INDEX_ID);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.shop_edit_dialog, null);

        final EditText text = (EditText) root.findViewById(R.id.dialog_shop_name);

        builder.setTitle(R.string.EDIT_LIST).setView(root);

        builder.setPositiveButton(R.string.UPDATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = text.getText().toString();
                if( mDb.renameShop(shopId,newName) ) {
                    mAdapter.changeCursor(mDb.fetchAllShops());
                    mAdapter.notifyDataSetChanged();

                    popUp(format(R.string.LIST_RENAMED, oldName, newName));
                }else{
                    popUp(format(R.string.LIST_RENAME_FAILED, oldName));
                }
            }
        });

        builder.setNeutralButton(R.string.COPY_TO_CLIPBOARD, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String text = mDb.stringifyItemList(shopId);
                Utilities.setClipboardString(getActivity(), oldName, text);
                popUp(format(R.string.ITEMS_COPIED, oldName));
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // aborted, nothing to do
            }
        });

        text.setText(oldName);
        text.setSelection(text.getText().length());

        builder.create().show();
    }

    @Override
    public void onSwipeOut(ListView listView, View view) {
        final int position = listView.getPositionForView(view);
        final Cursor cursor = (Cursor) listView.getItemAtPosition(position);
        final long shopId = cursor.getLong(DBContract.JoinShopItemQuery.INDEX_ID);
        final String shopName = cursor.getString(DBContract.JoinShopItemQuery.INDEX_NAME);

        animateAdd(new ListAnimations.Runner() {
            @Override
            public void run(Set<Long> set) {
                if( mDb.updateShopDeleted(shopId,true) ) {
                    undo.push(new Pair<>(shopName,shopId));
                    mAdapter.changeCursor(mDb.fetchAllShops());
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    //
    // TODO: proper way to save/load!
    // TODO: also on the items? (i.e. save all lists, and save single list?)
    //

    protected void saveDialog(String path){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View root = inflater.inflate(R.layout.file_dialog, null);
        EditText txt = (EditText) root.findViewById(R.id.file_path);

        if( path != null ){
            txt.setText(path);
        }
        // Inflate and set the layout for the file_dialog
        // Pass null as the parent view because its going in the file_dialog layout
        builder.setView(root)
                .setTitle("Testing")
                        // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                    }
                })
                .setNeutralButton("File Chooser", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("file/*");
                        startActivityForResult(Intent.createChooser(intent, "Select File"), 1);

                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });
        builder.create().show();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == 1 && data != null ) {
            Uri file = data.getData();
            Log.w("FILE: ", file.getPath());
            saveDialog(file.getPath());
        }

    }
}
