package com.example.wiser.ILOSDataCollection;


import android.content.Context;


public class AccMagOrientation extends Orientation {


    private float heading;
    private double headingOffset;
    public AccMagOrientation(Context context, StepListener dataListener) {
        super(context, dataListener);
    }

    @Override
    public void reset() {

    }

    @Override
    protected void onGyroscopeChanged() {

    }

    @Override
    public void updateHeadingRM() {
        float[] RM = getRM();
        float[] raw_magnetic = vMagnetic;
        float[] magneticEarch = new Tool().matrixMultiplication9X3(RM, vMagnetic);
        // Log.i("Earth", ""+magneticEarch[0] + "    " + magneticEarch[1] + "      " + magneticEarch[2]);
        heading = (float) Math.toDegrees(getOrientation()[0]); // orientation
        heading = ((heading) + 360) % 360;

        /*        this.setMag_heading(heading);
        this.setRM(RM);
        setMagneticEarch(magneticEarch);
        this.setMagneticPhone(raw_magnetic);*/
    }

    @Override
    public float[] getOrientation() {
        if (isOrientationValidAccelMag) {
            return vOrientationAccelMag;
        } else {
            return new float[3];
        }
    }

    @Override
    public float[] getRM() {
        return rmOrientationAccelMag;
    }


    @Override
    public void setFilterCoefficient(float filterCoefficient) {

    }
    public float getHeading() {
        return heading;
    }
    public void setHeadingOffset(double offset){
        headingOffset = offset;
    }
    public double getHeadingOffset() {
        return headingOffset;
    }

}

