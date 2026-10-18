/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.rising.clarifae;

import android.content.Context;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

public class ClarifaeController {
    private static final String TAG = "ClarifaeController";

    public static boolean restartAudio(Context context) {
        try {
            if (context != null) {
                AudioManager am = context.getSystemService(AudioManager.class);
                if (am != null) {
                    am.setParameters("clarifae_reload=1");
                    am.setParameters("restart_audio=1");
                }
            }
            try {
                SystemProperties.set("ctl.restart", "audioserver");
            } catch (Exception ignored) {}

            try {
                Runtime.getRuntime().exec(new String[]{"killall", "-9", "audioserver"}).waitFor();
            } catch (Exception ignored) {}

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error restarting audio: " + e.getMessage(), e);
            return false;
        }
    }

    public static boolean isLib64Installed() {
        return new File("/vendor/lib64/soundfx/libclarifae.so").exists() ||
                new File("/system/lib64/soundfx/libclarifae.so").exists() ||
                new File("/data/adb/modules/clarifae/system/lib64/soundfx/libclarifae.so").exists();
    }

    public static boolean isLib32Installed() {
        return new File("/vendor/lib/soundfx/libclarifae.so").exists() ||
                new File("/system/lib/soundfx/libclarifae.so").exists() ||
                new File("/data/adb/modules/clarifae/system/lib/soundfx/libclarifae.so").exists();
    }

    public static boolean isXmlConfigured() {
        File[] candidates = {
                new File("/vendor/etc/audio_effects.xml"),
                new File("/system/etc/audio_effects.xml"),
                new File("/odm/etc/audio_effects.xml")
        };
        for (File f : candidates) {
            if (f.exists() && fileContains(f, "clarifae")) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAudioserverRunning() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"pidof", "audioserver"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = reader.readLine();
            return line != null && !line.trim().isEmpty();
        } catch (Exception e) {
            return true;
        }
    }

    public static boolean isEffectLoaded() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"dumpsys", "media.audio_flinger"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains("clarifae")) {
                    return true;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    public static boolean isModelDtlnReady() {
        File m1 = new File("/data/adb/clarifae/models/dtln/model_1.tflite");
        File m2 = new File("/data/adb/clarifae/models/dtln/model_2.tflite");
        return m1.exists() && m2.exists();
    }

    public static boolean isModelDfReady() {
        File dfDir = new File("/data/adb/clarifae/models/df");
        return dfDir.exists() && dfDir.isDirectory() && dfDir.list() != null && dfDir.list().length > 0;
    }

    public static String readLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Clarifae Log File ===\n");

        File[] logFiles = {
                new File("/data/misc/clarifae/clarifae.log"),
                new File("/data/adb/clarifae/clarifae.log"),
        };
        boolean foundLog = false;
        for (File f : logFiles) {
            if (f.exists() && f.canRead()) {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    foundLog = true;
                    break;
                } catch (Exception ignored) {}
            }
        }
        if (!foundLog) {
            sb.append("(No clarifae.log file found)\n\n");
        }

        sb.append("\n=== logcat -d -s Clarifae ===\n");
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"logcat", "-d", "-s", "Clarifae:V", "ClarifaeConfig:V", "ClarifaeController:V"});
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < 80) {
                sb.append(line).append("\n");
                count++;
            }
            if (count == 0) {
                sb.append("(No active Clarifae logcat messages found)\n");
            }
        } catch (Exception e) {
            sb.append("Error executing logcat: ").append(e.getMessage()).append("\n");
        }

        return sb.toString();
    }

    private static boolean fileContains(File f, String query) {
        try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(query)) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
