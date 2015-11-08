package pt.blah.shopper.utils;

import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class ListAnimations {

    private static final int MOVE_SPEED = 250;

    //
    // fades and moves row to the right
    //

    public static void animateDelete(
            final ListAdapter mAdapter,
            final ListView listView,
            final Runnable andThen,
            final int... deletedItems
    ) {

        boolean foundFirst = false;

        if( deletedItems != null ){
            int firstVisiblePosition = listView.getFirstVisiblePosition();

            for (int i = 0; i < listView.getChildCount(); ++i) {
                final View child = listView.getChildAt(i);
                final int position = firstVisiblePosition + i;
                final long itemId = mAdapter.getItemId(position);

                for (int deletedItem : deletedItems) {
                    if (deletedItem == itemId) {
                        // found deleted item!
                        ViewPropertyAnimator anim = child.animate().setDuration(MOVE_SPEED)
                                .alpha(0)
                                .translationX(child.getWidth());

                        if (!foundFirst) {
                            anim.withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    child.setAlpha(1);
                                    child.setTranslationX(0);
                                    // now do the 'andThen' action
                                    if (andThen != null)
                                        andThen.run();
                                }
                            });
                            foundFirst = true;
                        } else {
                            anim.withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    child.setAlpha(1);
                                    child.setTranslationX(0);
                                }
                            });
                        }
                    }
                }
            }
        }

        if( !foundFirst && andThen != null ){
            andThen.run();
        }
    }

    //
    // re-arranges old items and pushes new row in from the left
    //

    public static void animateAdd(
            final ListAdapter mAdapter,
            final ListView listview,
            final Runnable action,
            final int added
    ) {

        final Map<Long,Integer> map = new HashMap<>();

        // save old top positions *before* the list changes
        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            int position = firstVisiblePosition + i;
            long itemId = mAdapter.getItemId(position);

            // stores old top of a view on a Map
            map.put(itemId, child.getTop());

        }

        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);

                int firstVisiblePosition = listview.getFirstVisiblePosition();

                for (int i = 0; i < listview.getChildCount(); ++i) {
                    View child = listview.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = mAdapter.getItemId(position);

                    if( itemId == added ){
                        // the new product
                        child.setTranslationX(listview.getWidth());
                        child.animate().setDuration(MOVE_SPEED*2).translationX(0);
                        continue;
                    }

                    // is null on non existing views (i.e. outside screen)
                    Integer oldTop = map.get(itemId);
                    int newTop = child.getTop(); // i.e. the new position!

                    // already in correct position
                    if (oldTop != null && oldTop == newTop)
                        continue;

                    // child not previously present
                    if (oldTop == null) {
                        int childHeight = child.getHeight() + listview.getDividerHeight();
                        oldTop = newTop + (i > 0 ? childHeight : -childHeight);
                    }

                    int delta = oldTop - newTop;
                    child.setTranslationY(delta);
                    child.animate().setDuration(MOVE_SPEED).translationY(0);
                }
                map.clear(); // kinda of pointless with this code, but nevermind.
                return true;
            }
        });

        // changes only become visible after the action ir run.
        if( action != null ) {
            action.run();
        }

    }


    //
    // TODO TESTING
    //

    public static void animateAdd(
            final ListAdapter mAdapter,
            final ListView listview,
            final Runner action
    ) {

        final Map<Long,Integer> map = new HashMap<>();

        // save old top positions *before* the list changes
        int firstVisiblePosition = listview.getFirstVisiblePosition();
        for (int i = 0; i < listview.getChildCount(); ++i) {
            View child = listview.getChildAt(i);
            int position = firstVisiblePosition + i;
            long itemId = mAdapter.getItemId(position);

            // stores old top of a view on a Map
            map.put(itemId, child.getTop());

        }

        final Set<Long> set = new TreeSet<>();
        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);

                int firstVisiblePosition = listview.getFirstVisiblePosition();

                for (int i = 0; i < listview.getChildCount(); ++i) {
                    View child = listview.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    long itemId = mAdapter.getItemId(position);

                    if( set.contains(itemId) ){
                        // the new product
                        child.setTranslationX(listview.getWidth());
                        child.animate().setDuration(MOVE_SPEED*2).translationX(0);
                        continue;
                    }

                    // is null on non existing views (i.e. outside screen)
                    Integer oldTop = map.get(itemId);
                    int newTop = child.getTop(); // i.e. the new position!

                    // already in correct position
                    if (oldTop != null && oldTop == newTop)
                        continue;

                    // child not previously present
                    if (oldTop == null) {
                        int childHeight = child.getHeight() + listview.getDividerHeight();
                        oldTop = newTop + (i > 0 ? childHeight : -childHeight);
                    }

                    int delta = oldTop - newTop;
                    child.setTranslationY(delta);
                    child.animate().setDuration(MOVE_SPEED).translationY(0);
                }
                map.clear(); // kinda of pointless with this code, but nevermind.
                return true;
            }
        });

        // changes only become visible after the action ir run.
        if( action != null ) {
            action.run(set);
        }

    }

    public interface Runner {
        void run(Set<Long> added);
    }

}
