package com.example.proximity_search.utils;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Rectangle;

public class GeoUtils {
    // Get the default spatial context
    private static final SpatialContext CONTEXT = SpatialContext.GEO;
    /**
     * Calculate distance between two points in meters using Haversine formula.
     */
    public static double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lng2 - lng1);
 
        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
 
        // apply formulae
        double a = Math.pow(Math.sin(dLat / 2), 2) + 
                   Math.pow(Math.sin(dLon / 2), 2) * 
                   Math.cos(lat1) * 
                   Math.cos(lat2);
        double earthRadiusKm = 6371;  // Earth's radius in kilometers
        double c = 2 * Math.asin(Math.sqrt(a));
        
        // Return distance in meters
        return earthRadiusKm * c * 1000;
    }
    
    /**
     * Check if a point is within a specified radius.
     */
    public static boolean isWithinRadius(double lat1, double lng1, double lat2, double lng2, double radius) {
        return calculateDistance(lat1, lng1, lat2, lng2) <= radius;
    }
    
    /**
     * Calculate a bounding box around a point with a radius in meters.
     * Returns [minLat, maxLat, minLng, maxLng].
     */
    public static double[] calculateBoundingBox(double latitude, double longitude, double radius) {
        double radiusKm = radius / 1000.0;
        double radiusDegrees = radiusKm * DistanceUtils.KM_TO_DEG;
        
        Rectangle bbox = CONTEXT.getShapeFactory().circle(longitude, latitude, radiusDegrees).getBoundingBox();
        
        return new double[] {
            bbox.getMinY(),  // minLat
            bbox.getMaxY(),  // maxLat
            bbox.getMinX(),  // minLng
            bbox.getMaxX()   // maxLng
        };
    }
} 