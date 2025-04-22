package com.example.proximitysearch.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class InitSqlGenerator {

    // Seoul region coordinates
    private static final double LAT_MIN = 37.45;
    private static final double LAT_MAX = 37.7;
    private static final double LNG_MIN = 126.8;
    private static final double LNG_MAX = 127.1;

    private static final int BATCH_SIZE = 1000;
    private static final String OUTPUT_DIR = "mysql-init";
    private static final String OUTPUT_FILE = OUTPUT_DIR + "/init.sql";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java InitSqlGenerator <rowCount>");
            return;
        }

        int rowCount = Integer.parseInt(args[0]);
        createOutputDirectory();
        generateSqlFile(rowCount);
    }

    private static void createOutputDirectory() {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    private static void generateSqlFile(int rowCount) {
        System.out.println("🔧 Generating " + rowCount + " rows → " + OUTPUT_FILE);

        try (PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_FILE))) {
            writeSchema(writer);
            generateInsertStatements(writer, rowCount);
            System.out.println("✅ Done. File saved at: " + OUTPUT_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeSchema(PrintWriter writer) {
        // Drop existing tables
        writer.println("DROP TABLE IF EXISTS items_default;");
        writer.println("DROP TABLE IF EXISTS items_composite;");
        writer.println("DROP TABLE IF EXISTS items_spatial;");

        // Default table with no indexes
        writer.println("\n-- Default table with no indexes");
        writer.println("CREATE TABLE items_default (");
        writer.println("  id INT AUTO_INCREMENT PRIMARY KEY,");
        writer.println("  lat DOUBLE NOT NULL,");
        writer.println("  lng DOUBLE NOT NULL");
        writer.println(");\n");

        // Table with composite index
        writer.println("-- Composite index on (lat, lng)");
        writer.println("CREATE TABLE items_composite (");
        writer.println("  id INT AUTO_INCREMENT PRIMARY KEY,");
        writer.println("  lat DOUBLE NOT NULL,");
        writer.println("  lng DOUBLE NOT NULL,");
        writer.println("  INDEX idx_lat_lng (lat, lng)");
        writer.println(");\n");

        // Table with spatial index
        writer.println("-- Spatial index on POINT with SRID 4326");
        writer.println("CREATE TABLE items_spatial (");
        writer.println("  id INT AUTO_INCREMENT PRIMARY KEY,");
        writer.println("  location POINT NOT NULL SRID 4326,");
        writer.println("  SPATIAL INDEX(location)");
        writer.println(");\n");
    }

    private static void generateInsertStatements(PrintWriter writer, int rowCount) {
        Random random = new Random();
        int written = 0;

        while (written < rowCount) {
            int currentBatch = Math.min(BATCH_SIZE, rowCount - written);
            
            StringBuilder defaultInsert = new StringBuilder("INSERT INTO items_default (lat, lng) VALUES\n");
            StringBuilder compositeInsert = new StringBuilder("INSERT INTO items_composite (lat, lng) VALUES\n");
            StringBuilder spatialInsert = new StringBuilder("INSERT INTO items_spatial (location) VALUES\n");

            for (int i = 0; i < currentBatch; i++) {
                double lat = generateRandomLatitude(random);
                double lng = generateRandomLongitude(random);

                String latlng = "(" + lat + ", " + lng + ")";
                String spatialPoint = "(ST_PointFromText('POINT(" + lat + " " + lng + ")', 4326))";

                boolean isLastRow = i == currentBatch - 1;
                String separator = isLastRow ? ";\n" : ",\n";

                defaultInsert.append(latlng).append(separator);
                compositeInsert.append(latlng).append(separator);
                spatialInsert.append(spatialPoint).append(separator);
            }

            writer.println(defaultInsert);
            writer.println(compositeInsert);
            writer.println(spatialInsert);

            written += currentBatch;
        }
    }

    private static double generateRandomLatitude(Random random) {
        return round(LAT_MIN + (LAT_MAX - LAT_MIN) * random.nextDouble(), 6);
    }

    private static double generateRandomLongitude(Random random) {
        return round(LNG_MIN + (LNG_MAX - LNG_MIN) * random.nextDouble(), 6);
    }

    private static double round(double value, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(value * scale) / scale;
    }
}