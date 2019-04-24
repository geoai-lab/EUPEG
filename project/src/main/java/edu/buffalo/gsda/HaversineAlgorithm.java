package edu.buffalo.gsda;

public abstract class HaversineAlgorithm {

    /**
     * Equatorial earth radius in Kilometers (KM).
     */
    private static final int EARTH_RADIUS = 6371;

    /**
     * <p>
     * Method using the Haversine formula to calculate the great-circle distance
     * between tow points by the latitude and longitude coordinates.</p>
     *
     * @param startLati Initial latitude
     * @param startLong Initial longitude
     * @param endLati Final latitude
     * @param endLong Final longitude
     * @return The distance in Kilometers (Km)
     */
    public static double distanceInKm(double startLati, double startLong, double endLati, double endLong) {

        double diffLati = Math.toRadians(endLati - startLati);
        double diffLong = Math.toRadians(endLong - startLong);
        /**
         * At this point are possible to improve the resources' utilization by
         * assign the new results inside the existing variables, like startLati
         * and endLati. But I prefer to keep the clean code and the
         * self-explanatory name convention.
         */
        double radiusStartLati = Math.toRadians(startLati);
        double radiusEndLati = Math.toRadians(endLati);

        // A and C are the 'sides' from the spherical triangle.
        double a = Math.pow(Math.sin(diffLati / 2), 2) + Math.pow(Math.sin(diffLong / 2), 2) * Math.cos(radiusStartLati) * Math.cos(radiusEndLati);
        double c = 2 * Math.asin(Math.sqrt(a));

        return EARTH_RADIUS * c;
    }

    /**
     * <p>
     * Method using the Haversine formula to calculate the great-circle distance
     * between tow points by the latitude and longitude coordinates.</p>
     *
     * @param startLati Initial latitude
     * @param startLong Initial longitude
     * @param endLati Final latitude
     * @param endLong Final longitude
     * @return The distance in Miles (Mi)
     */
    public static double distanceInMi(double startLati, double startLong, double endLati, double endLong) {

        double distanceInKm = distanceInKm(startLati, startLong, endLati, endLong);

        // A simple conversion solve the calc.
        return distanceInKm * 0.621371;
    }

}
