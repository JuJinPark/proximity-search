package com.example.proximity_search;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class SeedSqlGenerator {

    private static final double LAT_MIN = 37.45;
    private static final double LAT_MAX = 37.7;
    private static final double LNG_MIN = 126.8;
    private static final double LNG_MAX = 127.1;

    private static final int BATCH_SIZE = 1000;

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java SeedSqlGenerator <rowCount>");
            return;
        }

        int rowCount = Integer.parseInt(args[0]);
        File dir = new File("seed-data");
        if (!dir.exists()) dir.mkdir();

        String fileName = "seed-data/seed_" + rowCount + ".sql";
        System.out.println("🔧 Generating " + rowCount + " rows → " + fileName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            generateInserts(writer, rowCount);
            System.out.println("✅ Done. File saved at: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateInserts(PrintWriter writer, int rowCount) {
        Random random = new Random();
        int written = 0;

        while (written < rowCount) {
            int currentBatch = Math.min(BATCH_SIZE, rowCount - written);
            writer.print("INSERT INTO items (lat, lng) VALUES\n");

            for (int i = 0; i < currentBatch; i++) {
                double lat = round(randomBetween(random, LAT_MIN, LAT_MAX), 6);
                double lng = round(randomBetween(random, LNG_MIN, LNG_MAX), 6);

                writer.print("(" + lat + ", " + lng + ")");
                writer.print(i < currentBatch - 1 ? ",\n" : ";\n");
            }

            written += currentBatch;
        }
    }

    private static double randomBetween(Random random, double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    private static double round(double value, int precision) {
        double scale = Math.pow(10, precision);
        return Math.round(value * scale) / scale;
    }
}