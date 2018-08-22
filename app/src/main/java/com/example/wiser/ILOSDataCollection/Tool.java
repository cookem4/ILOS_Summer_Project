package com.example.wiser.ILOSDataCollection;


public class Tool {

    // @par
    public float[] matrixMultiplication9X3(float[] RR, float[] x) {
        float[] result = new float[3];
        result[0] = RR[0] * x[0] + RR[1] * x[1] + RR[2] * x[2];
        result[1] = RR[3] * x[0] + RR[4] * x[1] + RR[5] * x[2];
        result[2] = RR[6] * x[0] + RR[7] * x[1] + RR[8] * x[2];

        return result;
    }


    /**
     * Convert Latitude-Longtitude distance to meaters
     *
     * @param lat1 Starting lat
     * @param lng1 Starting lng
     * @param lat2 Ending lat
     * @param lng2 Ending lng
     * @return distance in meters between the starting and ending points
     */
    public double distance(double lat1,
                           double lng1,
                           double lat2,
                           double lng2) {
        double radius = 6378.137;
        double dlat = Math.toRadians(lat2 - lat1);
        double dlng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dlng / 2) * Math.sin(dlng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = radius * c;
        double result = d * 1000;
        return result;

    }

    /**
     * Convert the gps coordinate to local coordinate system with the designated origin point
     *
     * @param lat0 lat of origin point
     * @param lng0 lng of origin point
     * @param lat1 lat of given point
     * @param lng1 lng of given point
     * @return coordinates of local coordination
     */
    public double[] offset_coord(double lat0,
                                 double lng0,
                                 double lat1,
                                 double lng1) {
        double result[] = {0, 0};
        double x_project[] = {lat0, lng1};
        double y_project[] = {lat1, lng0};
        double x_offset = distance(lat0, lng0, x_project[0], x_project[1]);
        double y_offset = distance(lat0, lng0, y_project[0], y_project[1]);

        //if at the south that the origin flip the sign
        if (lat1 < lat0) {
            y_offset = y_offset * (-1);
        }

        //if at the west of the origin flip the sign
        if (lng1 < lng0) {
            x_offset = x_offset * (-1);
        }

        //return x offset and y offset
        result[0] = x_offset;
        result[1] = y_offset;
        return result;

    }

    /**
     * return the gps coordinate based on the offset and origin gps coordinate
     *
     * @param lat0
     * @param lng0
     * @param x_offset
     * @param y_offset
     * @return
     */
    public double[] gps_fromxy(double lat0,
                               double lng0,
                               double x_offset,
                               double y_offset) {
        double result[] = {0, 0};
        double R = 6378137.0;
        //offset in radians
        double dlat = y_offset / R;
        double dlon = x_offset / (R * Math.cos(Math.PI * lat0 / 180.0));
        //offset gps in decimal degrees
        double lat1 = lat0 + dlat * 180 / Math.PI;
        double lng1 = lng0 + dlon * 180 / Math.PI;
        result[0] = lat1;
        result[1] = lng1;
        return result;
    }

}