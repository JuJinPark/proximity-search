package com.example.proximity_search.benchmark;
import com.example.proximity_search.model.Item;
import com.example.proximity_search.service.ProximitySearchService;
import com.example.proximity_search.utils.GeoUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProximitySearchBenchmark {
    private final ProximitySearchService proximitySearchService;

    // lat,lng order
    private static final double[][] TEST_COORDINATES = {
        {37.5665, 126.9780},  // (City Center)
    };
    private static final double RADIUS = 100;

    public ProximitySearchBenchmark(ProximitySearchService proximitySearchService) {
        this.proximitySearchService = proximitySearchService;
    }

    public void runBenchmark() {
        System.out.println("\n=== Starting Proximity Search Benchmark ===\n");

        for (double[] cords : TEST_COORDINATES) {
            benchmarkLocation(cords[0], cords[1], RADIUS);
            benchmarkLocationWithCompositeIndex(cords[0], cords[1], RADIUS);
        }

        System.out.println("\n=== Benchmark Complete ===\n");
    }

    private void benchmarkLocation(double lat, double lng, double radius) {
        runBenchmark("Default (No Index)", lat, lng, radius, 
            () -> proximitySearchService.getItemsFromDBWithoutIndex(lat, lng, radius));
    }

    private void benchmarkLocationWithCompositeIndex(double lat, double lng, double radius) {
        runBenchmark("Composite Index", lat, lng, radius, 
            () -> proximitySearchService.getItemsFromDBWithCompositeIndex(lat, lng, radius));
    }
    
    private void runBenchmark(String benchmarkName, double lat, double lng, double radius, 
                             BenchmarkOperation operation) {
        System.out.printf("\nBenchmarking %s at location: (%.4f, %.4f)\n", benchmarkName, lat, lng);
        System.out.println("Starting benchmark runs...");
        double totalTime = 0;
        int runs = 5;
        
        for (int i = 0; i < runs; i++) {
            long start = System.nanoTime();
            List<Item> items = operation.execute();
            long end = System.nanoTime();
            verifyResult(items, lat, lng, radius);
            double duration = (end - start) / 1_000_000.0;
            totalTime += duration;
            System.out.printf("Run %d: Found %d items in %.2f ms\n", i + 1, items.size(), duration);
        }

        System.out.printf("Average time: %.2f ms\n", totalTime / runs);
    }
    
    @FunctionalInterface
    private interface BenchmarkOperation {
        List<Item> execute();
    }

    // verify the result by using Haversine formula
    private void verifyResult(List<Item> items, double lat, double lng, double radius) {
        boolean allItemsWithinRadius = items.stream()
                .allMatch(item -> GeoUtils.isWithinRadius(lat, lng, item.lat(), item.lng(), radius));
        assert allItemsWithinRadius : "Some items are wrong";
    }
} 