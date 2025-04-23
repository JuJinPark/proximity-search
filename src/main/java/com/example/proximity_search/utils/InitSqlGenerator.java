package com.example.proximity_search.utils;

import java.io.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

public class InitSqlGenerator {

    // Seoul region coordinates
    private static final double LAT_MIN = 37.45;
    private static final double LAT_MAX = 37.7;
    private static final double LNG_MIN = 126.8;
    private static final double LNG_MAX = 127.1;

    private static final int BATCH_SIZE = 2000;
    private static final String OUTPUT_DIR = "mysql-init";
    private static final String OUTPUT_FILE = OUTPUT_DIR + "/init.sql";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java InitSqlGenerator <rowCount>");
            System.err.println("Example: java InitSqlGenerator 1000");
            return;
        }

        int rowCount = Integer.parseInt(args[0]);
        
        createOutputDirectory(OUTPUT_DIR);
        generateSqlFile(rowCount, OUTPUT_FILE);
        compressFile(OUTPUT_FILE);
    }

    private static void createOutputDirectory(String outputDir) {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private static void compressFile(String filePath) {
        String gzipPath = filePath + ".gz";
        System.out.println("🗜️ Compressing the SQL file...");

        try (FileInputStream fis = new FileInputStream(filePath);
             FileOutputStream fos = new FileOutputStream(gzipPath);
             GZIPOutputStream gzos = new GZIPOutputStream(fos);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            byte[] buffer = new byte[1024];
            int len;
            while ((len = bis.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }
            
            // Delete the original file after successful compression
            if (new File(filePath).delete()) {
                System.out.println("✨ Done! Output saved to: " + gzipPath);
            } else {
                System.err.println("Warning: Could not delete original file: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Error compressing file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void generateSqlFile(int rowCount, String outputFile) {
        System.out.println("📝 Generating " + rowCount + " rows → " + outputFile);

        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFile))) {
            writeSchema(writer);
            generateInsertStatements(writer, rowCount);
        } catch (IOException e) {
            System.err.println("Error generating SQL file: " + e.getMessage());
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
        writer.println("  lng DOUBLE NOT NULL,");
        writer.println("  description VARCHAR(255) NOT NULL");
        writer.println(");\n");

        // Table with composite index
        writer.println("-- Composite index on (lat, lng)");
        writer.println("CREATE TABLE items_composite (");
        writer.println("  id INT AUTO_INCREMENT PRIMARY KEY,");
        writer.println("  lat DOUBLE NOT NULL,");
        writer.println("  lng DOUBLE NOT NULL,");
        writer.println("  description VARCHAR(255) NOT NULL,");
        writer.println("  INDEX idx_lat_lng (lat, lng)");
        writer.println(");\n");

        // Table with spatial index
        writer.println("-- Spatial index on POINT with SRID 4326");
        writer.println("CREATE TABLE items_spatial (");
        writer.println("  id INT AUTO_INCREMENT PRIMARY KEY,");
        writer.println("  location POINT NOT NULL SRID 4326,");
        writer.println("  description VARCHAR(255) NOT NULL,");
        writer.println("  SPATIAL INDEX(location)");
        writer.println(");\n");
    }

    private static void generateInsertStatements(PrintWriter writer, int rowCount) {
        Random random = new Random();
        int written = 0;
        int currentId = 1;

        while (written < rowCount) {
            int currentBatch = Math.min(BATCH_SIZE, rowCount - written);
            
            StringBuilder defaultInsert = new StringBuilder("INSERT INTO items_default (lat, lng, description) VALUES\n");
            StringBuilder compositeInsert = new StringBuilder("INSERT INTO items_composite (lat, lng, description) VALUES\n");
            StringBuilder spatialInsert = new StringBuilder("INSERT INTO items_spatial (location, description) VALUES\n");

            for (int i = 0; i < currentBatch; i++) {
                double lat = generateRandomLatitude(random);
                double lng = generateRandomLongitude(random);
                String description = "Location " + currentId;

                String latlng = String.format("(%f, %f, '%s')", lat, lng, description);
                String spatialPoint = String.format("(ST_PointFromText('POINT(%f %f)', 4326), '%s')", lat, lng, description);

                boolean isLastRow = i == currentBatch - 1;
                String separator = isLastRow ? ";\n" : ",\n";

                defaultInsert.append(latlng).append(separator);
                compositeInsert.append(latlng).append(separator);
                spatialInsert.append(spatialPoint).append(separator);

                currentId++;
            }

            writer.println(defaultInsert);
            writer.println(compositeInsert);
            writer.println(spatialInsert);

            written += currentBatch;
        }
    }

    private static double generateRandomLatitude(Random random) {
        return round(LAT_MIN + (LAT_MAX - LAT_MIN) * random.nextDouble());
    }

    private static double generateRandomLongitude(Random random) {
        return round(LNG_MIN + (LNG_MAX - LNG_MIN) * random.nextDouble());
    }

    private static double round(double value) {
        double scale = Math.pow(10, 6);
        return Math.round(value * scale) / scale;
    }
}