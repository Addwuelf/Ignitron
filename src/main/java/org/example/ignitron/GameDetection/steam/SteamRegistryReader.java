package org.example.ignitron.GameDetection.steam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SteamRegistryReader {
    public static Path getSteamPath() {
        try {
            Process process = Runtime.getRuntime().exec(
                    "reg query HKCU\\Software\\Valve\\Steam /v SteamPath"
            );

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("SteamPath")) {

                    // Split on 2+ spaces (registry column separator)
                    String[] parts = line.trim().split("\\s{2,}");

                    // parts[0] = "SteamPath"
                    // parts[1] = "REG_SZ"
                    // parts[2] = "C:\\Program Files (x86)\\Steam"

                    if (parts.length >= 3) {
                        return Paths.get(parts[2]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}
