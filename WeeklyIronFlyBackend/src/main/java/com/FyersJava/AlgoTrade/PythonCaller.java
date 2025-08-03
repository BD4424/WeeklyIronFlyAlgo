package com.FyersJava.AlgoTrade;



import com.FyersJava.AlgoTrade.utility.AccessToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonCaller {

    public static void main(String[] args) {
        try {
            // Command to run Python script
            ProcessBuilder pb = new ProcessBuilder("python", "AccessToken.py");

            // Set working directory if needed
            // pb.directory(new File("/path/to/your/python/script"));

            Process process = pb.start();

            // Read the output (value of x)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Received from Python: " + output.toString());

                // You can convert it if needed
                String x = output.toString();
                // Use x as needed
            } else {
                System.err.println("Python script failed with exit code " + exitCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String accessToken(){
        try {
            // Command to run Python script
            ProcessBuilder pb = new ProcessBuilder("python", "AccessToken.py");

            // Set working directory if needed
            // pb.directory(new File("/path/to/your/python/script"));

            Process process = pb.start();

            // Read the output (value of x)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Received from Python: " + output.toString());

                AccessToken.saveAccessToken(output.toString());
                // You can convert it if needed
                return output.toString();
                // Use x as needed
            } else {
                System.err.println("Python script failed with exit code " + exitCode);
                throw new RuntimeException("Python script failed with exit code " + exitCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Failed";
    }


}
