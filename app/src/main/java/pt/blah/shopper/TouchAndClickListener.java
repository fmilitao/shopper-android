package pt.blah.shopper;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ListView;

public class TouchAndClickListener implements View.OnTouchListener {

    private static final int SWIPE_DURATION = 250;

    //based on https://www.youtube.com/watch?v=YCHNAi9kJI4
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


    TouchAndClickListener(ViewConfiguration cf, ListView listView) {
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

    @Override
    public boolean onTouch(final View v, MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mItemPressed) {
                    // Multi-item swipes not handled
                    return false;
                }
                mItemPressed = true;
                mDownTime = System.currentTimeMillis();
                mDownX = event.getX();
                break;
            case MotionEvent.ACTION_CANCEL:
                v.setAlpha(1);
                v.setTranslationX(0);
                mItemPressed = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX() + v.getTranslationX();
                float deltaXAbs = Math.abs(x - mDownX);
                if (!mSwiping) {
                    if (deltaXAbs > SWIPE_SLOP) {
                        mSwiping = true;
                        mListView.requestDisallowInterceptTouchEvent(true);
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
                        mOnClick.onClick(mListView, v);
                    } else {
                        mOnLongClick.onLongClick(mListView,v);
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