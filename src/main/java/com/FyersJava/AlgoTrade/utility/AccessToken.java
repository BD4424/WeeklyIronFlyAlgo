package com.FyersJava.AlgoTrade.utility;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AccessToken {

    public static void saveAccessToken(String token) {
        try (FileWriter writer = new FileWriter("access_token_file.txt")) {
            writer.write(token);
            System.out.println("Access token saved successfully.");
        } catch (IOException e) {
            System.err.println("Failed to save access token: " + e.getMessage());
        }
    }

    public static String readAccessToken() {
        StringBuilder token = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("access_token_file.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                token.append(line);
            }
            return token.toString();
        } catch (IOException e) {
            System.err.println("Failed to read access token: " + e.getMessage());
            return null;
        }
    }
}
