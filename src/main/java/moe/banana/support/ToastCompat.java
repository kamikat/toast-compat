package moe.banana.support;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.Queue;

public class ToastCompat {

    public static final int LENGTH_SHORT = 0;
    public static final int LENGTH_LONG = 1;

    @IntDef({LENGTH_SHORT, LENGTH_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {}

    static final int MSG_ENQUEUE_TOAST = 0x01;
    static final int MSG_CANCEL_TOAST = 0x02;
    static final int MSG_NEXT_TOAST = 0x03;

    static final Handler mHandler = new Handler(Looper.getMainLooper()) {

        Queue<ToastCompat> mTQ = new LinkedList<>();
        ToastCompat mCurrentToast;

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ENQUEUE_TOAST:
                    mTQ.add(((ToastCompat) msg.obj));
                    if (mCurrentToast == null) {
                        sendEmptyMessage(MSG_NEXT_TOAST);
                    }
                    break;
                case MSG_CANCEL_TOAST:
                    mTQ.remove(((ToastCompat) msg.obj));
                    if (mCurrentToast == msg.obj) {
                        removeMessages(MSG_NEXT_TOAST);
                        sendEmptyMessage(MSG_NEXT_TOAST);
                    }
                    break;
                case MSG_NEXT_TOAST:
                    if (mCurrentToast != null) {
                        mCurrentToast.mTN.handleHide();
                    }
                    mCurrentToast = mTQ.poll();
                    if (mCurrentToast != null) {
                        mCurrentToast.mTN.handleShow();
                        sendEmptyMessageDelayed(MSG_NEXT_TOAST, mCurrentToast.mDuration == LENGTH_LONG ? 3500 : 2000);
                    }
                    break;
            }
        }
    };

    final Context mContext;
    final TN mTN;
    int mDuration;
    View mNextView;

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     */
    public ToastCompat(Context context) {
        mContext = context;
        mTN = new TN();
        mTN.mY = context.getResources().getDimensionPixelSize(Resources.getSystem().getIdentifier("toast_y_offset", "dimen", "android"));
        mTN.mGravity = context.getResources().getInteger(Resources.getSystem().getIdentifier("config_toastDefaultGravity", "integer", "android"));
    }

    /**
     * Show the view for the specified duration.
     */
    public void show() {
        if (mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }
        TN tn = mTN;
        tn.mNextView = mNextView;
        Message.obtain(mHandler, MSG_ENQUEUE_TOAST, this).sendToTarget();
    }

    /**
     * Close the view if it's showing, or don't show it if it isn't showing yet.
     * You do not normally have to call this.  Normally view will disappear on its own
     * after the appropriate duration.
     */
    public void cancel() {
        Message.obtain(mHandler, MSG_CANCEL_TOAST, this).sendToTarget();
    }

    /**
     * Set the view to show.
     * @see #getView
     */
    public void setView(View view) {
        mNextView = view;
    }

    /**
     * Return the view.
     * @see #setView
     */
    public View getView() {
        return mNextView;
    }

    /**
     * Set how long to show the view for.
     * @see #LENGTH_SHORT
     * @see #LENGTH_LONG
     */
    public void setDuration(@Duration int duration) {
        mDuration = duration;
    }

    /**
     * Return the duration.
     * @see #setDuration
     */
    @Duration
    public int getDuration() {
        return mDuration;
    }

    /**
     * Set the margins of the view.
     *
     * @param horizontalMargin The horizontal margin, in percentage of the
     *        container width, between the container's edges and the
     *        notification
     * @param verticalMargin The vertical margin, in percentage of the
     *        container height, between the container's edges and the
     *        notification
     */
    public void setMargin(float horizontalMargin, float verticalMargin) {
        mTN.mHorizontalMargin = horizontalMargin;
        mTN.mVerticalMargin = verticalMargin;
    }

    /**
     * Return the horizontal margin.
     */
    public float getHorizontalMargin() {
        return mTN.mHorizontalMargin;
    }

    /**
     * Return the vertical margin.
     */
    public float getVerticalMargin() {
        return mTN.mVerticalMargin;
    }

    /**
     * Set the location at which the notification should appear on the screen.
     * @see Gravity
     * @see #getGravity
     */
    public void setGravity(int gravity, int xOffset, int yOffset) {
        mTN.mGravity = gravity;
        mTN.mX = xOffset;
        mTN.mY = yOffset;
    }

    /**
     * Get the location at which the notification should appear on the screen.
     * @see Gravity
     * @see #getGravity
     */
    public int getGravity() {
        return mTN.mGravity;
    }

    /**
     * Return the X offset in pixels to apply to the gravity's location.
     */
    public int getXOffset() {
        return mTN.mX;
    }

    /**
     * Return the Y offset in pixels to apply to the gravity's location.
     */
    public int getYOffset() {
        return mTN.mY;
    }

    /**
     * Gets the LayoutParams for the Toast window.
     * @hide
     */
    public WindowManager.LayoutParams getWindowParams() {
        return mTN.mParams;
    }

    /**
     * Make a standard toast that just contains a text view.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param text     The text to show.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or
     *                 {@link #LENGTH_LONG}
     *
     */
    public static ToastCompat makeText(Context context, CharSequence text, @Duration int duration) {
        ToastCompat result = new ToastCompat(context);

        LayoutInflater inflate = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflate.inflate(Resources.getSystem().getIdentifier("transient_notification", "layout", "android"), null);
        TextView tv = (TextView)v.findViewById(Resources.getSystem().getIdentifier("message", "id", "android"));
        tv.setText(text);

        result.mNextView = v;
        result.mDuration = duration;

        return result;
    }

    /**
     * Make a standard toast that just contains a text view with the text from a resource.
     *
     * @param context  The context to use.  Usually your {@link android.app.Application}
     *                 or {@link android.app.Activity} object.
     * @param resId    The resource id of the string resource to use.  Can be formatted text.
     * @param duration How long to display the message.  Either {@link #LENGTH_SHORT} or
     *                 {@link #LENGTH_LONG}
     *
     * @throws Resources.NotFoundException if the resource can't be found.
     */
    public static ToastCompat makeText(Context context, @StringRes int resId, @Duration int duration) throws Resources.NotFoundException {
        return makeText(context, context.getString(resId), duration);
    }


    private static class TN {
        private final WindowManager.LayoutParams mParams = new WindowManager.LayoutParams();

        int mGravity;
        int mX, mY;
        float mHorizontalMargin;
        float mVerticalMargin;


        View mView;
        View mNextView;

        WindowManager mWM;

        TN() {
            // XXX This should be changed to use a Dialog, with a Theme.Toast
            // defined that sets up the layout params appropriately.
            final WindowManager.LayoutParams params = mParams;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.format = PixelFormat.TRANSLUCENT;
            params.windowAnimations = R.style.ToastCompat_Animation;
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
            params.setTitle("Toast");
            params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }

        public void handleShow() {
            if (mView != mNextView) {
                // remove the old view if necessary
                handleHide();
                mView = mNextView;
                Context context = mView.getContext().getApplicationContext();
                String packageName = mView.getContext().getPackageName();
                if (context == null) {
                    context = mView.getContext();
                }
                mWM = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
                // We can resolve the Gravity here by using the Locale for getting
                // the layout direction
                final int gravity = GravityCompat.getAbsoluteGravity(mGravity, ViewCompat.getLayoutDirection(mView));
                mParams.gravity = gravity;
                if ((gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.FILL_HORIZONTAL) {
                    mParams.horizontalWeight = 1.0f;
                }
                if ((gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.FILL_VERTICAL) {
                    mParams.verticalWeight = 1.0f;
                }
                mParams.x = mX;
                mParams.y = mY;
                mParams.verticalMargin = mVerticalMargin;
                mParams.horizontalMargin = mHorizontalMargin;
                mParams.packageName = packageName;
                if (mView.getParent() != null) {
                    mWM.removeView(mView);
                }
                mWM.addView(mView, mParams);
                trySendAccessibilityEvent();
            }
        }

        private void trySendAccessibilityEvent() {
            AccessibilityManager accessibilityManager =
                    (AccessibilityManager) mView.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (!accessibilityManager.isEnabled()) {
                return;
            }
            // treat toasts as notifications since they are used to
            // announce a transient piece of information to the user
            AccessibilityEvent event = AccessibilityEvent.obtain(
                    AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED);
            event.setClassName(getClass().getName());
            event.setPackageName(mView.getContext().getPackageName());
            mView.dispatchPopulateAccessibilityEvent(event);
            accessibilityManager.sendAccessibilityEvent(event);
        }

        public void handleHide() {
            if (mView != null) {
                // note: checking parent() just to make sure the view has
                // been added...  i have seen cases where we get here when
                // the view isn't yet added, so let's try not to crash.
                if (mView.getParent() != null) {
                    mWM.removeView(mView);
                }

                mView = null;
            }
        }
    }
}
