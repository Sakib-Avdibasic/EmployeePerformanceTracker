package com.sakibavdibasicipia.example.config;

import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class DatabaseConfig {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    private DatabaseConfig() {}

    public static MongoDatabase getDatabase() {
        if (database == null) {
            synchronized (DatabaseConfig.class) {
                if (mongoClient == null) {
                    try {
                        mongoClient = MongoClients.create("mongodb+srv://sakib:SAnmq9737GRstFJD@clusteripia.vmjnk.mongodb.net/");
                        database = mongoClient.getDatabase("payroll_management");
                    } catch (MongoException e) {
                        System.err.println("Error connecting to MongoDB: " + e.getMessage());
                        throw new RuntimeException("MongoDB connection failed", e);
                    }
                }
            }
        }
        return database;
    }

    public static void close() {
        if (mongoClient != null) {
            try {
                mongoClient.close();
                mongoClient = null;
                System.out.println("MongoDB connection closed.");
            } catch (MongoException e) {
                System.err.println("Error closing MongoDB connection: " + e.getMessage());
            }
        }
    }
}
