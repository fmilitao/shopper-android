package io.github.fmilitao.shopper.utils;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

/**
 * Implements a basic listener of touch and click events.
 * Only supports 'swipe' movement and 'single' or 'long' clicks.
 * Code based off tutorial: https://www.youtube.com/watch?v=YCHNAi9kJI4
 *
 * Note: we must launch a timer event for the 'long' click since we did not
 * find a better way to trigger an event after a continuous touch state.
 */
public class TouchAndClickListener implements View.OnTouchListener {

    private static final int SWIPE_DURATION = 250;

    final int SWIPE_SLOP;
    final int LONG_TIMEOUT;

    final ListView mListView;

    boolean mSwiping;
    boolean mItemPressed;
    float mDownX;
    long mDownTime;

    ClickListener mOnClick;
    LongClickListener mOnLongClick;
    SwipeOutListener mOnSwipeOut;

    public TouchAndClickListener(ViewConfiguration cf, ListView listView) {
        SWIPE_SLOP = cf.getScaledTouchSlop();
        LONG_TIMEOUT = ViewConfiguration.getLongPressTimeout();
        mSwiping = false;
        mItemPressed = false;
        mListView = listView;
    }

    public void setOnClick(ClickListener onClick){
        mOnClick = onClick;
    }

    public void setOnLongClick(LongClickListener onLongClick){
        mOnLongClick = onLongClick;
    }

    public void setOnSwipeOut(SwipeOutListener onSwipeOut){
        mOnSwipeOut = onSwipeOut;
    }

    /*
        Using 'onTouch' breaks onItemClick and onItemLongClick events. To simulate the
        long click event we launch a timer (using 'handler') that will trigger the on long
        click event, unless stopped. Stopping occurs when we switch to the swipe state or when
        we are sure it is just a simple (short) click.
        'longClick' is the old handler to be removed.
     */

    final Handler handler = new Handler();
    Runnable longClick = null;


    @Override
    public boolean onTouch(final View v, MotionEvent event) {

//        android.util.Log.v("DEBUG", "EVENT: " + event.toString());

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                if (mItemPressed) {
                    // Multi-item swipes not handled
                    return false;
                }
                mItemPressed = true;
                mDownTime = System.currentTimeMillis();
                mDownX = event.getX();

                // start time out for long click
//                android.util.Log.v("DEBUG","ADDED TIMEOUT - DOWN");
                longClick = new Runnable() {
                    @Override
                    public void run() {
                        mOnLongClick.onLongClick(mListView, v);
                    }
                };
                handler.postDelayed(longClick, LONG_TIMEOUT);
                break;

            case MotionEvent.ACTION_CANCEL:
                v.setAlpha(1);
                v.setTranslationX(0);
                mItemPressed = false;
//                android.util.Log.v("DEBUG", "REMOVED TIMEOUT - CANCELED");
                handler.removeCallbacks(longClick); // abort long click
                break;

            case MotionEvent.ACTION_MOVE: {
                float x = event.getX() + v.getTranslationX();
                float deltaXAbs = Math.abs(x - mDownX);
                if (!mSwiping) {
                    if (deltaXAbs > SWIPE_SLOP) {
                        mSwiping = true;
                        mListView.requestDisallowInterceptTouchEvent(true);
//                        android.util.Log.v("DEBUG", "REMOVED TIMEOUT - MOVED");
                        handler.removeCallbacks(longClick); // abort long click
                    }
                }
                if (mSwiping) {
                    v.setTranslationX((x - mDownX));
                    v.setAlpha(1 - deltaXAbs / v.getWidth());
                }
                break;
            }

            case MotionEvent.ACTION_UP: {
                // User let go - figure out whether to animate the view out, or back into place
                if (mSwiping) {
                    float x = event.getX() + v.getTranslationX();
                    float deltaX = x - mDownX;
                    float deltaXAbs = Math.abs(deltaX);
                    float fractionCovered;
                    float endX;
                    float endAlpha;
                    final boolean remove;

                    if (deltaXAbs > v.getWidth() / 4) {
                        // Greater than a quarter of the width - animate it out
                        fractionCovered = deltaXAbs / v.getWidth();
                        endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
                        endAlpha = 0;
                        remove = true;
                    } else {
                        // Not far enough - animate it back
                        fractionCovered = 1 - (deltaXAbs / v.getWidth());
                        endX = 0;
                        endAlpha = 1;
                        remove = false;
                    }

                    // Animate position and alpha of swiped item
                    // NOTE: This is a simplified version of swipe behavior, for the
                    // purposes of this demo about animation. A real version should use
                    // velocity (via the VelocityTracker class) to send the item off or
                    // back at an appropriate speed.
                    long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
                    mListView.setEnabled(false);
                    v.animate().setDuration(duration).
                            alpha(endAlpha).translationX(endX).
                            withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    // Restore animated values
                                    v.setAlpha(1);
                                    v.setTranslationX(0);

                                    // re-enable state
                                    // note the original tutorial code only does this after the
                                    // removal animation of 'onSwipeOut' ends
                                    mSwiping = false;
                                    mListView.setEnabled(true);

                                    if (remove) {
                                        mOnSwipeOut.onSwipeOut(mListView,v);
                                    }
                                }
                            });
                } else {
                    long diff = System.currentTimeMillis() - mDownTime;
                    if (diff < LONG_TIMEOUT) {
//                        android.util.Log.v("DEBUG","REMOVED TIMEOUT - CLICK");
                        handler.removeCallbacks(longClick);
                        mOnClick.onClick(mListView, v);
                    }
                }

                mItemPressed = false;
                break;
            }

            default:
                return false;
        }
        return true;
    }


    //
    // Listener Interfaces
    //

    public interface ClickListener {
        void onClick(ListView listView, View view);
    }

    public interface LongClickListener {
        void onLongClick(ListView listView, View view);
    }

    public interface SwipeOutListener {
        void onSwipeOut(ListView listView, View view);
    }

}