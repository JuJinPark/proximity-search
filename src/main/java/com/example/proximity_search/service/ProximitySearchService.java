package com.example.proximity_search.service;

import com.example.proximity_search.utils.GeoUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

import com.example.proximity_search.model.Item;

@Service
public class ProximitySearchService {   
    private final JdbcTemplate jdbcTemplate;

    public ProximitySearchService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Item> getItemsFromDBWithoutIndex(double latitude, double longitude, double radius ) {
        String query = "SELECT * FROM items_default WHERE ST_Distance_Sphere(POINT(lng, lat), POINT(?, ?)) <= ? order by id asc";
        List<Item> result = jdbcTemplate.query(query,
                (rs, rowNum) -> new Item(rs.getLong("id"), rs.getDouble("lat"), rs.getDouble("lng"), rs.getString("description")),
                longitude, latitude, radius);
        System.out.println("Filtered items: " + result.stream().map((item -> item.id()+"")).collect(Collectors.joining(",")));
        return result;
    }

    public List<Item> getItemsFromDBWithCompositeIndex(double latitude, double longitude, double radius) {
        double[] boundingBox = GeoUtils.calculateBoundingBox(latitude, longitude, radius);
        double minLat = boundingBox[0];
        double maxLat = boundingBox[1];
        double minLng = boundingBox[2];
        double maxLng = boundingBox[3];
        
        // Use the composite index by filtering with the bounding box first
        String query = "SELECT * FROM items_composite WHERE " +
                "lat BETWEEN ? AND ? AND lng BETWEEN ? AND ? order by id asc";
        
        List<Item> items = jdbcTemplate.query(query,
                (rs, rowNum) -> new Item(rs.getLong("id"), rs.getDouble("lat"), rs.getDouble("lng"), rs.getString("description")),
                minLat, maxLat, minLng, maxLng);
        System.out.println("minLat: " + minLat + " maxLat: " + maxLat + " minLng: " + minLng + " maxLng: " + maxLng);
        // Filter out items outside the radius using Haversine formula
        List<Item> filteredItems = items.stream()
                .filter(item -> GeoUtils.isWithinRadius(latitude, longitude, item.lat(), item.lng(), radius))
                .toList();
        
        System.out.println("Filtered items: " + filteredItems.stream().map((item -> item.id()+"")).collect(Collectors.joining(",")));
        return filteredItems;
    }
}
