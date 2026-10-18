/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.rising.clarifae;

import android.os.SystemProperties;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ClarifaeConfig {
    private static final String TAG = "ClarifaeConfig";

    public static final String PRIMARY_CONF_PATH = "/data/misc/clarifae/config.conf";
    public static final String VENDOR_CONF_PATH = "/data/vendor/clarifae/config.conf";
    public static final String ADB_CONF_PATH = "/data/adb/clarifae/config.conf";
    public static final String VENDOR_ETC_CONF_PATH = "/vendor/etc/clarifae/config.conf";

    public static final String PROP_PREFIX_VENDOR = "persist.vendor.clarifae.";
    public static final String PROP_PREFIX_SYS = "persist.sys.clarifae.";
    public static final String PROP_PREFIX_LEGACY = "persist.clarifae.";

    public boolean enabled = true;
    public boolean in_enabled = true;
    public boolean out_enabled = false;
    public int mode = 1;
    public int strength = 70;
    public int out_strength = 60;
    public int atten_db = 24;
    public int vad = 50;
    public boolean highpass = true;
    public boolean autogain = false;
    public int loglevel = 1;
    public String targets = "mic,voice_comm,camcorder,voice_rec";
    public String out_targets = "music,voice_call,system";

    public static ClarifaeConfig load() {
        ClarifaeConfig cfg = new ClarifaeConfig();

        File confFile = findConfigFile();
        if (confFile == null || !confFile.exists()) {
            Log.i(TAG, "No config file found on device. Creating new config.conf with default settings...");
            cfg.save();
            return cfg;
        }
        if (confFile.canRead()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(confFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#")) continue;
                    int idx = line.indexOf('=');
                    if (idx == -1) continue;
                    String k = line.substring(0, idx).trim();
                    String v = line.substring(idx + 1).trim();

                    switch (k) {
                        case "enabled": cfg.enabled = "1".equals(v) || "true".equalsIgnoreCase(v); break;
                        case "in_enabled": cfg.in_enabled = "1".equals(v) || "true".equalsIgnoreCase(v); break;
                        case "out_enabled": cfg.out_enabled = "1".equals(v) || "true".equalsIgnoreCase(v); break;
                        case "mode": cfg.mode = safeParseInt(v, 1, 1, 3); break;
                        case "strength": cfg.strength = safeParseInt(v, 70, 0, 100); break;
                        case "out_strength": cfg.out_strength = safeParseInt(v, 60, 0, 100); break;
                        case "atten_db": cfg.atten_db = safeParseInt(v, 24, 0, 60); break;
                        case "vad": cfg.vad = safeParseInt(v, 50, 0, 100); break;
                        case "highpass": cfg.highpass = "1".equals(v) || "true".equalsIgnoreCase(v); break;
                        case "autogain": cfg.autogain = "1".equals(v) || "true".equalsIgnoreCase(v); break;
                        case "loglevel": cfg.loglevel = safeParseInt(v, 1, 0, 3); break;
                        case "targets": if (!v.isEmpty()) cfg.targets = v; break;
                        case "out_targets": if (!v.isEmpty()) cfg.out_targets = v; break;
                    }
                }
                Log.i(TAG, "Config loaded from " + confFile.getAbsolutePath());
            } catch (Exception e) {
                Log.e(TAG, "Error reading config file: " + e.getMessage(), e);
            }
        }

        // Overlay properties if explicitly set
        String propEnabled = getProp("enabled");
        if (!propEnabled.isEmpty()) cfg.enabled = "1".equals(propEnabled) || "true".equalsIgnoreCase(propEnabled);

        String propIn = getProp("in_enabled");
        if (!propIn.isEmpty()) cfg.in_enabled = "1".equals(propIn) || "true".equalsIgnoreCase(propIn);

        String propOut = getProp("out_enabled");
        if (!propOut.isEmpty()) cfg.out_enabled = "1".equals(propOut) || "true".equalsIgnoreCase(propOut);

        String propMode = getProp("mode");
        if (!propMode.isEmpty()) cfg.mode = safeParseInt(propMode, cfg.mode, 1, 3);

        String propStr = getProp("strength");
        if (!propStr.isEmpty()) cfg.strength = safeParseInt(propStr, cfg.strength, 0, 100);

        String propOutStr = getProp("out_strength");
        if (!propOutStr.isEmpty()) cfg.out_strength = safeParseInt(propOutStr, cfg.out_strength, 0, 100);

        String propAtten = getProp("atten_db");
        if (!propAtten.isEmpty()) cfg.atten_db = safeParseInt(propAtten, cfg.atten_db, 0, 60);

        String propVad = getProp("vad");
        if (!propVad.isEmpty()) cfg.vad = safeParseInt(propVad, cfg.vad, 0, 100);

        String propHp = getProp("highpass");
        if (!propHp.isEmpty()) cfg.highpass = "1".equals(propHp) || "true".equalsIgnoreCase(propHp);

        String propAg = getProp("autogain");
        if (!propAg.isEmpty()) cfg.autogain = "1".equals(propAg) || "true".equalsIgnoreCase(propAg);

        String propTargets = getProp("targets");
        if (!propTargets.isEmpty()) cfg.targets = propTargets;

        String propOutTargets = getProp("out_targets");
        if (!propOutTargets.isEmpty()) cfg.out_targets = propOutTargets;

        return cfg;
    }

    public boolean save() {
        String confContent = buildConfigString();

        // Write to writable paths
        boolean written = false;
        written |= writeToFile(PRIMARY_CONF_PATH, confContent);
        written |= writeToFile(VENDOR_CONF_PATH, confContent);
        if (new File("/data/adb").exists()) {
            written |= writeToFile(ADB_CONF_PATH, confContent);
        }

        // Sync to system properties
        syncProperties();

        return written;
    }

    public void resetToDefaults() {
        enabled = true;
        in_enabled = true;
        out_enabled = false;
        mode = 1;
        strength = 70;
        out_strength = 60;
        atten_db = 24;
        vad = 50;
        highpass = true;
        autogain = false;
        loglevel = 1;
        targets = "mic,voice_comm,camcorder,voice_rec";
        out_targets = "music,voice_call,system";
        save();
    }

    private String buildConfigString() {
        return "enabled=" + (enabled ? 1 : 0) + "\n" +
                "in_enabled=" + (in_enabled ? 1 : 0) + "\n" +
                "out_enabled=" + (out_enabled ? 1 : 0) + "\n" +
                "mode=" + mode + "\n" +
                "strength=" + strength + "\n" +
                "out_strength=" + out_strength + "\n" +
                "atten_db=" + atten_db + "\n" +
                "vad=" + vad + "\n" +
                "targets=" + targets + "\n" +
                "out_targets=" + out_targets + "\n" +
                "highpass=" + (highpass ? 1 : 0) + "\n" +
                "autogain=" + (autogain ? 1 : 0) + "\n" +
                "loglevel=" + loglevel + "\n";
    }

    private static boolean writeToFile(String path, String content) {
        try {
            File f = new File(path);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
                parent.setReadable(true, false);
                parent.setWritable(true, false);
                parent.setExecutable(true, false);
            }
            try (FileWriter writer = new FileWriter(f)) {
                writer.write(content);
            }
            f.setReadable(true, false);
            f.setWritable(true, false);
            Log.i(TAG, "Config written successfully to " + path);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Could not write to " + path + ": " + e.getMessage());
            return false;
        }
    }

    private void syncProperties() {
        setProp("enabled", enabled ? "1" : "0");
        setProp("in_enabled", in_enabled ? "1" : "0");
        setProp("out_enabled", out_enabled ? "1" : "0");
        setProp("mode", String.valueOf(mode));
        setProp("strength", String.valueOf(strength));
        setProp("out_strength", String.valueOf(out_strength));
        setProp("atten_db", String.valueOf(atten_db));
        setProp("vad", String.valueOf(vad));
        setProp("highpass", highpass ? "1" : "0");
        setProp("autogain", autogain ? "1" : "0");
        setProp("loglevel", String.valueOf(loglevel));
        setProp("targets", targets);
        setProp("out_targets", out_targets);
    }

    private static void setProp(String key, String val) {
        try {
            SystemProperties.set(PROP_PREFIX_VENDOR + key, val);
        } catch (Exception ignored) {}
        try {
            SystemProperties.set(PROP_PREFIX_SYS + key, val);
        } catch (Exception ignored) {}
        try {
            SystemProperties.set(PROP_PREFIX_LEGACY + key, val);
        } catch (Exception ignored) {}
    }

    private static String getProp(String key) {
        String val = SystemProperties.get(PROP_PREFIX_VENDOR + key, "");
        if (val.isEmpty()) {
            val = SystemProperties.get(PROP_PREFIX_SYS + key, "");
        }
        if (val.isEmpty()) {
            val = SystemProperties.get(PROP_PREFIX_LEGACY + key, "");
        }
        return val;
    }

    private static File findConfigFile() {
        String[] paths = {PRIMARY_CONF_PATH, VENDOR_CONF_PATH, ADB_CONF_PATH, VENDOR_ETC_CONF_PATH};
        for (String p : paths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                return f;
            }
        }
        return null;
    }

    private static int safeParseInt(String val, int def, int min, int max) {
        try {
            int n = Integer.parseInt(val);
            if (n < min) return min;
            if (n > max) return max;
            return n;
        } catch (Exception e) {
            return def;
        }
    }
}
