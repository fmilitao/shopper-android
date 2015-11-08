package pt.blah.shopper.utils;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

/**
 * Sensor to listen to device 'shakes'.
 * Code based off:
 * http://stackoverflow.com/questions/2317428/android-i-want-to-shake-it
 * http://stackoverflow.com/questions/5271448/how-to-detect-shake-event-with-android
 */
public class ShakeSensor implements SensorEventListener {

    static final int SHAKE_THRESHOLD = 5;
    static final int MIN_INTERVAL = 500;

    SensorManager mSensorManager;

    float mAccel; // acceleration apart from gravity
    float mAccelCurrent; // current acceleration including gravity
    float mAccelLast; // last acceleration including gravity
    long lastUpdate = 0;

    ShakeListener mOnShake;

    public ShakeSensor(@NonNull ShakeListener onShakeAction) {
        mOnShake = onShakeAction;
    }

    public void onSensorChanged(SensorEvent se) {

        long curTime = System.currentTimeMillis();
        if ((curTime - lastUpdate) > MIN_INTERVAL) {
            lastUpdate = curTime;

            float x = se.values[0];
            float y = se.values[1];
            float z = se.values[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt((double) (x * x + y * y + z * z));
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta; // perform low-cut filter

            if (mAccel > SHAKE_THRESHOLD) {
                mOnShake.onShake();
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // intentionally empty
    }

    //
    // Attach the following as appropriate
    //

    public void onCreate(Activity a) {
        mSensorManager = (SensorManager) a.getSystemService(Context.SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;
    }

    public void onPause() {
        mSensorManager.unregisterListener(this);
    }

    public void onResume() {
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    //
    // Listener Interface
    //

    public interface ShakeListener {
        void onShake();
    }

}
