package pt.blah.shopper;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;

import static pt.blah.shopper.Utilities.sData;
import static pt.blah.shopper.Utilities.format;

public class ShopsFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    ShopsListAdapter mAdapter;
    ListView mListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        Utilities.addListener(this, mAdapter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Note: listener cannot be removed or it will miss events.
        //Utilities.removeListener(this);
        Utilities.save();
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
                            DataDB.sort(list);
                            box.setText(String.format(getString(R.string.INCLUDE_ITEMS), tmp.size()));
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
                        final DataDB.Shop shop = sData.newShop(name);
                        shop.products = list;

                        animateAdd(new Runnable() {
                            @Override
                            public void run() {
                                Utilities.sData.list.add(shop);
                                Utilities.notifyListeners();
                            }
                        }, shop.id);

                        Utilities.popUp(getActivity(), format(R.string.LIST_ADDED, name));
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

        mAdapter = new ShopsListAdapter(getActivity());

        View rootView = inflater.inflate(R.layout.shop_fragment, container, false);
        mListView = (ListView) rootView.findViewById(R.id.shops_list);

        mListView.setAdapter(mAdapter);

        mListView.setClickable(true);
        mListView.setOnItemClickListener(this);

        mListView.setLongClickable(true);
        mListView.setOnItemLongClickListener(this);

        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), ProductsActivity.class);
        // links to position in the DB
        intent.putExtra(Utilities.INTENT_TAG, position);
        startActivity(intent);

        // for animation
        getActivity().overridePendingTransition(R.anim.pull_in_right, R.anim.push_out_left);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

        final DataDB.Shop shop = Utilities.sData.list.get(position);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View root = inflater.inflate(R.layout.shop_edit_dialog, null);

        final EditText text = (EditText) root.findViewById(R.id.dialog_shop_name);
        final Button delete = (Button) root.findViewById(R.id.dialog_delete_shop);

        builder.setTitle(R.string.EDIT_LIST).setView(root);

        builder.setPositiveButton(R.string.UPDATE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String old = shop.name;
                shop.name = text.getText().toString();
                Utilities.notifyListeners();

                Utilities.popUp(getActivity(), format(R.string.LIST_RENAMED, old, shop.name));
            }
        });

        builder.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // aborted, nothing to do
            }
        });

        text.setText(shop.name);
        text.setSelection(text.getText().length());

        final Dialog dialog = builder.create();

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DataDB.Shop s = sData.list.get(position);
                dialog.dismiss();

                animateDelete(new Runnable() {
                    @Override
                    public void run() {
                        sData.list.remove(position);
                        Utilities.notifyListeners();
                    }
                }, s.id);

                Utilities.popUp(getActivity(), format(R.string.LIST_DELETED, shop.name));
            }
        });

        dialog.show();
        return true;
    }


    private void animateAdd(Runnable action, int added){
        ListAnimations.animateAdd(mAdapter, mListView, action, added);
    }

    private void animateDelete(Runnable andThen, int delete){
        ListAnimations.animateDelete(mAdapter, mListView, andThen, delete);
    }
}
