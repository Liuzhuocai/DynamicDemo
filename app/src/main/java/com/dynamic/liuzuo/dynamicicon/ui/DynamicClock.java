package com.dynamic.liuzuo.dynamicicon.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.widget.TextView;

import com.dynamic.liuzuo.dynamicicon.R;
import com.dynamic.liuzuo.dynamicicon.provider.IDynamic;

/**
 * Created by liuzuo on 17-12-5.
 */

public class DynamicClock implements IDynamic {
    public DynamicClock() {
    }

    private Drawable mHourHand;
    private Drawable mMinuteHand;
    private Drawable mSecondHand;
    private Drawable mDial;

    private final String TAG = "DeskClockDynamic";
    PaintFlagsDrawFilter mPaintFlagsDrawFilter = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
    private Context mContext;
    private Time mCalendar;
    private boolean mIsRegister;
    private boolean mChanged = true;
    private int mIconSize;
    private static boolean hasSecondHand = true;
    private final int REFRESH_SECOND_INTERVAL = 1000;

    private final Handler mHandler = new Handler();

    private float mMinutes;
    private float mHour;
    private float mSeconds;

    private TextView mView;

    @Override
    public boolean init(Context context, TextView bubbleTextView) {
        final Resources r = context.getResources();
        mContext = context;

        mCalendar = new Time();

        mView = bubbleTextView;
        if (mView != null) {
            mIconSize = 168;
        }
        mDial = r.getDrawable( R.drawable.dym_clock_dial);
        mHourHand = r.getDrawable(R.drawable.dym_clock_hand_hour);
        mMinuteHand = r.getDrawable( R.drawable.dym_clock_hand_minute);
        mSecondHand = r.getDrawable( R.drawable.dym_clock_hand_second);

        updateDynamicIcon(true);
        if (hasSecondHand) {
            secondHandRun();
        }
        return true;
    }


    @Override
    public void removeDynamicReceiver() {
        if (hasSecondHand) {
            mHandler.removeCallbacks(mRunnable);
        }

    }

    @Override
    public boolean updateDynamicIcon(boolean register) {
        onTimeChanged(true);
        if (hasSecondHand) {
            secondHandRun();
        }
        if (register) {
            registerReceiver();
        }

        return true;
    }

    private void onTimeChanged(boolean force) {
        mCalendar.setToNow();
        float hour = mCalendar.hour + mCalendar.minute / 60.0f;
        float minute = mCalendar.minute;
        float second = mCalendar.second;
        if (force||(mSeconds != second || mMinutes != minute || mHour != hour)) {
            mSeconds = second;
            mMinutes = minute;
            mHour = hour;
            updateClockIcon();
        }
        updateContentDescription(mCalendar);
    }

    private void updateContentDescription(Time time) {
        final int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR;
        String contentDescription = DateUtils.formatDateTime(mContext,
                time.toMillis(false), flags);
        mView.setContentDescription(contentDescription);
    }

    private void updateClockIcon() {
        if(mDial==null||mIconSize == 0)
            return;
        Bitmap clock = Bitmap.createBitmap(mIconSize, mIconSize, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(clock);
        canvas.setDrawFilter(mPaintFlagsDrawFilter);
        boolean changed = mChanged;
/*        if (changed) {
            mChanged = false;
        }*/
        int availableWidth = mIconSize;
        int availableHeight = mIconSize;

        int x = availableWidth / 2;
        int y = availableHeight / 2;

        final Drawable dial = mDial;
        int w = dial.getIntrinsicWidth();
        int h = dial.getIntrinsicHeight();

        boolean scaled = false;

        if (availableWidth < w || availableHeight < h) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) w,
                    (float) availableHeight / (float) h);
            canvas.save();

            canvas.scale(scale, scale, x, y);
        }

        if (changed) {
            dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        dial.draw(canvas);

        canvas.save();
        canvas.rotate(mHour / 12.0f * 360.0f, x, y);
        final Drawable hourHand = mHourHand;
        if (changed) {
            w = hourHand.getIntrinsicWidth();
            h = hourHand.getIntrinsicHeight();
            hourHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        hourHand.draw(canvas);
        canvas.restore();

        canvas.save();
        canvas.rotate(mMinutes / 60.0f * 360.0f, x, y);

        final Drawable minuteHand = mMinuteHand;
        if (changed) {
            w = minuteHand.getIntrinsicWidth();
            h = minuteHand.getIntrinsicHeight();
            minuteHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
        }
        minuteHand.draw(canvas);

        if (hasSecondHand) {
            canvas.restore();

            canvas.save();

            canvas.rotate(mSeconds / 60.0f * 360.0f, x, y);
            final Drawable secondHand = mSecondHand;
            w = secondHand.getIntrinsicWidth();
            h = secondHand.getIntrinsicHeight();
            secondHand.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
            secondHand.draw(canvas);
        }
        canvas.restore();
        if (mView != null) {
            FastBitmapDrawable drawable = new FastBitmapDrawable(clock);
            if (mIconSize != -1) {
                drawable.setBounds(0, 0, mIconSize, mIconSize);
            }
            mView.  setCompoundDrawables(null, drawable, null, null);
        }
        }


    @Override
    public void clearDynamicIcon() {
        unregisterReceiver();
        removeDynamicReceiver();
    }

    private synchronized void registerReceiver() {
        if (!mIsRegister) {
            IntentFilter filter = new IntentFilter();
            if(!hasSecondHand) {
                filter.addAction(Intent.ACTION_TIME_TICK);
            }
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            if (mContext != null) {
                mContext.registerReceiver(mIntentReceiver, filter);
            }
            mIsRegister = true;
        }
    }

    private synchronized void unregisterReceiver() {
        try {
            if (mIsRegister) {
                mContext.unregisterReceiver(mIntentReceiver);
                mIsRegister = false;
            }
        } catch (Exception e) {
        }
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "deskClock onReceive");
                updateDynamicIcon(false);
        }
    };
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            onTimeChanged(false);
            mHandler.postDelayed(mRunnable, REFRESH_SECOND_INTERVAL);
        }
    };

    private void secondHandRun() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.post(mRunnable);
    }
}
