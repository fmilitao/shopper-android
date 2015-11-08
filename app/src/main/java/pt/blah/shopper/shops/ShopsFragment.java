package pt.blah.shopper.shops;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import java.util.Stack;

import pt.blah.shopper.DataDB;
import pt.blah.shopper.R;
import pt.blah.shopper.items.ItemsActivity;
import pt.blah.shopper.utils.ListAnimations;
import pt.blah.shopper.utils.ShakeSensor;
import pt.blah.shopper.utils.TouchAndClickListener;
import pt.blah.shopper.utils.UtilFragment;
import pt.blah.shopper.utils.Utilities;

import static pt.blah.shopper.utils.Utilities.sData;

public class ShopsFragment extends UtilFragment implements ShakeSensor.ShakeListener,
        TouchAndClickListener.ClickListener, TouchAndClickListener.LongClickListener, TouchAndClickListener.SwipeOutListener{

    ShopsListAdapter mAdapter;
    ListView mListView;
    ShakeSensor mShakeSensor;
    Stack<DataDB.Shop> undo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        undo = new Stack<>();
        mShakeSensor = new ShakeSensor(this);
        mShakeSensor.onCreate(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        // this may be unnecessary, but we just force the notification to recompute
        // each list's done and total counts
        mAdapter.notifyDataSetChanged();

        mShakeSensor.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        sData.save();
        mShakeSensor.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.shop_menu, menu);
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

            final List<DataDB.Product> list = new LinkedList<>();

            box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (box.isChecked()) {
                        List<DataDB.Product> tmp = Utilities.parseProductList(Utilities.getClipboardString(getActivity()));
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
                    String name = text.getText().toString();
                    if (name.length() > 0) {
                        final DataDB.Shop shop = sData.newShop(name,list);

                        animateAdd(new Runnable() {
                            @Override
                            public void run() {
                                Utilities.sData.addShop(shop);
                                mAdapter.notifyDataSetChanged();
                            }
                        }, shop.id);

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

        TouchAndClickListener t = new TouchAndClickListener(ViewConfiguration.get(this.getContext()),mListView);
        mAdapter = new ShopsListAdapter(getActivity(), t);
        t.setOnClick(this);
        t.setOnLongClick(this);
        t.setOnSwipeOut(this);

        mListView.setAdapter(mAdapter);

        return rootView;
    }

    private void animateAdd(Runnable action, int added){
        ListAnimations.animateAdd(mAdapter, mListView, action, added);
    }

    @Override
    public void onShake() {
        if (undo.isEmpty()) {
            popUp(getString(R.string.SHAKE_FAIL));
            return;
        }

        final DataDB.Shop shop = undo.pop();

        animateAdd(new Runnable() {
            @Override
            public void run() {
                Utilities.sData.addShop(shop);
                mAdapter.notifyDataSetChanged();
            }
        }, shop.id);

        popUp(format(R.string.SHAKE_UNDO, shop.getName()));
    }

    @Override
    public void onClick(ListView listView, View view) {
        int position = mListView.getPositionForView(view);
        Intent intent = new Intent(getActivity(), ItemsActivity.class);
        // links to position in the DB
        intent.putExtra(Utilities.INTENT_TAG, position);
        startActivity(intent);

        // for animation
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public void onLongClick(ListView listView, View view) {
        final int position = mListView.getPositionForView(view);
        final DataDB.Shop shop = Utilities.sData.getShop(position);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        @SuppressLint("InflateParams")
        final View root = inflater.inflate(R.layout.shop_edit_dialog, null);

        final EditText text = (EditText) root.findViewById(R.id.dialog_shop_name);

        builder.setTitle(R.string.EDIT_LIST).setView(root);

        builder.setPositiveButton(R.string.UPDATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String old = shop.getName();
                shop.rename(text.getText().toString());
                mAdapter.notifyDataSetChanged();

                popUp(format(R.string.LIST_RENAMED, old, shop.getName()));
            }
        });

        builder.setNeutralButton(R.string.COPY_TO_CLIPBOARD, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = shop.getName();
                String text = Utilities.stringifyProductList(shop.forEachProduct());

                Utilities.setClipboardString(getActivity(), name, text);
                popUp(format(R.string.ITEMS_COPIED, name, shop.getProductCount()));
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // aborted, nothing to do
            }
        });

        text.setText(shop.getName());
        text.setSelection(text.getText().length());

        builder.create().show();
    }

    @Override
    public void onSwipeOut(ListView listView, View view) {
        final int position = listView.getPositionForView(view);
        animateAdd(new Runnable() {
            @Override
            public void run() {
                undo.push(sData.deleteShop(position));
                mAdapter.notifyDataSetChanged();
            }
        }, -1);
    }
}
