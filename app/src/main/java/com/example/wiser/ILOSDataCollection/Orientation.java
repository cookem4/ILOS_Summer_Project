package com.example.wiser.ILOSDataCollection;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public abstract class Orientation implements OrientationInterface,
        SensorEventListener {

    protected static final float EPSILON = 0.1f;
    // private static final float NS2S = 1.0f / 10000.0f;
    // Nano-second to second conversion
    protected static final float NS2S = 1.0f / 1000000000.0f;
    protected boolean meanFilterSmoothingEnabled = true;
    protected boolean isOrientationValidAccelMag = false;
    protected float dT = 0;
    protected float meanFilterTimeConstant = 0.2f;
    // angular speeds from gyro
    protected float[] vGyroscope = new float[3];
    // magnetic field vector
    protected float[] vMagnetic = new float[3];
    protected boolean isFirstMag = true;
    // Low pass filter parameter
    protected float ALAPHA = 0.75f;
    // accelerometer vector
    protected float[] vAcceleration = new float[3];
    protected boolean isFirstAcc = true;
    // accelerometer and magnetometer based rotation matrix
    protected float[] rmOrientationAccelMag = new float[9];
    protected float[] vOrientationAccelMag = new float[3];
    protected long timeStampGyroscope = 0;
    protected long timeStampGyroscopeOld = 0;
    protected Context context;
    // We need the SensorManager to register for Sensor Events.
    protected SensorManager sensorManager;
    private String TAG = "JENNY_J";

    private boolean calibratedGyroscopeEnabled = true;
    private MeanFilterSmoothing meanFilterAcceleration;
    private MeanFilterSmoothing meanFilterMagnetic;
    private MeanFilterSmoothing meanFilterGyroscope;
    private StepListener mStepDataListener;

    public Orientation(Context context, StepListener dataListener) {
        this.context = context;
        this.sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        initFilters();
        mStepDataListener = dataListener;
    }

    public void reset(){

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                // Get a local copy of the raw magnetic values from the device
                // sensor.
                if (isFirstAcc) {
                    this.vAcceleration[0] = event.values[0];
                    this.vAcceleration[1] = event.values[1];
                    this.vAcceleration[2] = event.values[2];
                    isFirstAcc = false;
                } else {
                    this.vAcceleration[0] = event.values[0] * (1 - ALAPHA) + ALAPHA * this.vAcceleration[0];
                    this.vAcceleration[1] = event.values[1] * (1 - ALAPHA) + ALAPHA * this.vAcceleration[1];
                    this.vAcceleration[2] = event.values[2] * (1 - ALAPHA) + ALAPHA * this.vAcceleration[2];
                }

                // old way to copy the event values
                // System.arraycopy(event.values, 0, this.vAcceleration, 0,
                //		this.vGyroscope.length);

                if (meanFilterSmoothingEnabled) {
                    this.vAcceleration = meanFilterAcceleration
                            .addSamples(this.vAcceleration);
                }

                // We fuse the orientation of the magnetic and acceleration sensor
                // based on acceleration sensor updates. It could be done when the
                // magnetic sensor updates or when they both have updated if you
                // want to spend the resources to make the checks.
                calculateOrientationAccelMag();

            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                // Get a local copy of the raw magnetic values from the device
                // sensor.
                // System.arraycopy(event.values, 0, this.vMagnetic, 0,
                //		this.vGyroscope.length);
                if (isFirstMag) {
                    this.vMagnetic[0] = event.values[0];
                    this.vMagnetic[1] = event.values[1];
                    this.vMagnetic[2] = event.values[2];
                } else {
                    this.vMagnetic[0] = event.values[0] * (1 - ALAPHA) + ALAPHA * this.vMagnetic[0];
                    this.vMagnetic[1] = event.values[1] * (1 - ALAPHA) + ALAPHA * this.vMagnetic[1];
                    this.vMagnetic[2] = event.values[2] * (1 - ALAPHA) + ALAPHA * this.vMagnetic[2];
                }


                if (meanFilterSmoothingEnabled) {
                    this.vMagnetic = meanFilterMagnetic.addSamples(this.vMagnetic);
                }
            }

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float threshold = 0.0000001f;
                System.arraycopy(event.values, 0, this.vGyroscope, 0,
                        this.vGyroscope.length);
                if (meanFilterSmoothingEnabled) {
                    this.vGyroscope = meanFilterGyroscope
                            .addSamples(this.vGyroscope);
                }
                if (Math.abs(this.vGyroscope[0]) < threshold)
                    this.vGyroscope[0] = 0f;
                if (Math.abs(this.vGyroscope[1]) < threshold)
                    this.vGyroscope[1] = 0f;
                if (Math.abs(this.vGyroscope[2]) < threshold)
                    this.vGyroscope[2] = 0f;
                timeStampGyroscope = event.timestamp;

                onGyroscopeChanged();
            }

            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
                System.arraycopy(event.values, 0, this.vGyroscope, 0,
                        this.vGyroscope.length);

                if (meanFilterSmoothingEnabled) {
                    this.vGyroscope = meanFilterGyroscope
                            .addSamples(this.vGyroscope);
                }

                timeStampGyroscope = event.timestamp;

                onGyroscopeChanged();
            }

            updateHeadingRM();
        }
    }

    public void onPause() {
        sensorManager.unregisterListener(this);
    }

    public void onResume() {
        calibratedGyroscopeEnabled = true;
        meanFilterSmoothingEnabled = false;
        meanFilterTimeConstant = 0.2f;

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);

        if (calibratedGyroscopeEnabled) {
            sensorManager.registerListener(this,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    SensorManager.SENSOR_DELAY_FASTEST);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                sensorManager.registerListener(this, sensorManager
                                .getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED),
                        SensorManager.SENSOR_DELAY_FASTEST);
            }
        }
    }

    protected void calculateOrientationAccelMag() {
        // To get the orientation vector from the acceleration and magnetic
        // sensors, we let Android do the heavy lifting. This call will
        // automatically compensate for the tilt of the compass and fail if the
        // magnitude of the acceleration is not close to 9.82m/sec^2. You could
        // perform these steps yourself, but in my opinion, this is the best way
        // to do it.
        if (SensorManager.getRotationMatrix(rmOrientationAccelMag, null,
                vAcceleration, vMagnetic)) {
            SensorManager.getOrientation(rmOrientationAccelMag,
                    vOrientationAccelMag);

            isOrientationValidAccelMag = true;
        }
    }

    protected abstract void onGyroscopeChanged();

    public abstract void updateHeadingRM();

    /**
     * Initialize the mean filters.
     */
    private void initFilters() {
        meanFilterAcceleration = new MeanFilterSmoothing();
        meanFilterAcceleration.setTimeConstant(meanFilterTimeConstant);

        meanFilterMagnetic = new MeanFilterSmoothing();
        meanFilterMagnetic.setTimeConstant(meanFilterTimeConstant);

        meanFilterGyroscope = new MeanFilterSmoothing();
        meanFilterGyroscope.setTimeConstant(meanFilterTimeConstant);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
