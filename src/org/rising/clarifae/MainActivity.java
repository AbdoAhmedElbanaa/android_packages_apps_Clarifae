/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.rising.clarifae;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ClarifaeConfig mConfig;

    private TextView mBadgeStatus;
    private TextView mMasterSubtitle;
    private SwitchMaterial mSwitchMaster;

    private SwitchMaterial mSwitchInEnabled;
    private SwitchMaterial mSwitchOutEnabled;

    private View mCardMode1;
    private View mCardMode2;
    private View mCardMode3;
    private TextView mStatusModelRnnoise;
    private TextView mStatusModelDtln;
    private TextView mStatusModelDf;
    private TextView mHintFallback;

    private SeekBar mSeekStrength;
    private TextView mValStrength;
    private SeekBar mSeekOutStrength;
    private TextView mValOutStrength;
    private SeekBar mSeekAttenDb;
    private TextView mValAttenDb;
    private SeekBar mSeekVad;
    private TextView mValVad;
    private SwitchMaterial mSwitchHighpass;
    private SwitchMaterial mSwitchAutogain;

    private Chip mChipMic, mChipVoiceComm, mChipCamcorder, mChipVoiceRec;
    private Chip mChipMusic, mChipVoiceCall, mChipRing, mChipAlarm, mChipNotification, mChipSystem;

    private Button mBtnApply;
    private Button mBtnRestartAudio;
    private Button mBtnViewLog;
    private Button mBtnReset;

    private ImageButton mBtnRefreshStatus;
    private TableLayout mTableStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        loadAndApplyConfig();
        setupListeners();
        renderEngineStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        renderEngineStatus();
    }

    private void bindViews() {
        mBadgeStatus = findViewById(R.id.badge_status);
        mMasterSubtitle = findViewById(R.id.master_subtitle);
        mSwitchMaster = findViewById(R.id.switch_master);

        mSwitchInEnabled = findViewById(R.id.switch_in_enabled);
        mSwitchOutEnabled = findViewById(R.id.switch_out_enabled);

        mCardMode1 = findViewById(R.id.card_mode_1);
        mCardMode2 = findViewById(R.id.card_mode_2);
        mCardMode3 = findViewById(R.id.card_mode_3);
        mStatusModelRnnoise = findViewById(R.id.status_model_rnnoise);
        mStatusModelDtln = findViewById(R.id.status_model_dtln);
        mStatusModelDf = findViewById(R.id.status_model_df);
        mHintFallback = findViewById(R.id.hint_fallback);

        mSeekStrength = findViewById(R.id.seek_strength);
        mValStrength = findViewById(R.id.val_strength);
        mSeekOutStrength = findViewById(R.id.seek_out_strength);
        mValOutStrength = findViewById(R.id.val_out_strength);
        mSeekAttenDb = findViewById(R.id.seek_atten_db);
        mValAttenDb = findViewById(R.id.val_atten_db);
        mSeekVad = findViewById(R.id.seek_vad);
        mValVad = findViewById(R.id.val_vad);
        mSwitchHighpass = findViewById(R.id.switch_highpass);
        mSwitchAutogain = findViewById(R.id.switch_autogain);

        mChipMic = findViewById(R.id.chip_mic);
        mChipVoiceComm = findViewById(R.id.chip_voice_comm);
        mChipCamcorder = findViewById(R.id.chip_camcorder);
        mChipVoiceRec = findViewById(R.id.chip_voice_rec);

        mChipMusic = findViewById(R.id.chip_music);
        mChipVoiceCall = findViewById(R.id.chip_voice_call);
        mChipRing = findViewById(R.id.chip_ring);
        mChipAlarm = findViewById(R.id.chip_alarm);
        mChipNotification = findViewById(R.id.chip_notification);
        mChipSystem = findViewById(R.id.chip_system);

        mBtnApply = findViewById(R.id.btn_apply);
        mBtnRestartAudio = findViewById(R.id.btn_restart_audio);
        mBtnViewLog = findViewById(R.id.btn_view_log);
        mBtnReset = findViewById(R.id.btn_reset);

        mBtnRefreshStatus = findViewById(R.id.btn_refresh_status);
        mTableStatus = findViewById(R.id.table_status);
    }

    private void loadAndApplyConfig() {
        mConfig = ClarifaeConfig.load();
        updateUI();
    }

    private void updateUI() {
        mSwitchMaster.setChecked(mConfig.enabled);
        updateMasterText(mConfig.enabled);

        mSwitchInEnabled.setChecked(mConfig.in_enabled);
        mSwitchOutEnabled.setChecked(mConfig.out_enabled);

        selectMode(mConfig.mode, false);

        mSeekStrength.setProgress(mConfig.strength);
        mValStrength.setText(String.valueOf(mConfig.strength));

        mSeekOutStrength.setProgress(mConfig.out_strength);
        mValOutStrength.setText(String.valueOf(mConfig.out_strength));

        mSeekAttenDb.setProgress(mConfig.atten_db);
        mValAttenDb.setText(mConfig.atten_db + " dB");

        mSeekVad.setProgress(mConfig.vad);
        mValVad.setText(String.valueOf(mConfig.vad));

        mSwitchHighpass.setChecked(mConfig.highpass);
        mSwitchAutogain.setChecked(mConfig.autogain);

        List<String> inTargets = Arrays.asList(mConfig.targets.split(","));
        mChipMic.setChecked(inTargets.contains("mic"));
        mChipVoiceComm.setChecked(inTargets.contains("voice_comm"));
        mChipCamcorder.setChecked(inTargets.contains("camcorder"));
        mChipVoiceRec.setChecked(inTargets.contains("voice_rec"));

        List<String> outTargets = Arrays.asList(mConfig.out_targets.split(","));
        mChipMusic.setChecked(outTargets.contains("music") || outTargets.contains("media"));
        mChipVoiceCall.setChecked(outTargets.contains("voice_call") || outTargets.contains("calls"));
        mChipRing.setChecked(outTargets.contains("ring"));
        mChipAlarm.setChecked(outTargets.contains("alarm"));
        mChipNotification.setChecked(outTargets.contains("notification") || outTargets.contains("notif"));
        mChipSystem.setChecked(outTargets.contains("system"));
    }

    private void updateMasterText(boolean enabled) {
        if (enabled) {
            mBadgeStatus.setText(R.string.val_ready);
            mBadgeStatus.setTextColor(getColor(R.color.status_ok));
            mBadgeStatus.setBackgroundResource(R.drawable.bg_pill_ok);
            mMasterSubtitle.setText(R.string.master_sub_active);
        } else {
            mBadgeStatus.setText(R.string.master_sub_paused);
            mBadgeStatus.setTextColor(getColor(R.color.text_muted));
            mBadgeStatus.setBackgroundResource(R.drawable.bg_pill_exp);
            mMasterSubtitle.setText(R.string.master_sub_paused);
        }
    }

    private void selectMode(int mode, boolean markDirty) {
        mConfig.mode = mode;
        mCardMode1.setSelected(mode == 1);
        mCardMode2.setSelected(mode == 2);
        mCardMode3.setSelected(mode == 3);

        boolean dtlnReady = ClarifaeController.isModelDtlnReady();
        boolean dfReady = ClarifaeController.isModelDfReady();

        mStatusModelRnnoise.setText("● " + getString(R.string.val_installed));
        mStatusModelRnnoise.setTextColor(getColor(R.color.status_ok));

        mStatusModelDtln.setText(dtlnReady ? "● " + getString(R.string.val_ready) : "● " + getString(R.string.val_missing));
        mStatusModelDtln.setTextColor(dtlnReady ? getColor(R.color.status_ok) : getColor(R.color.text_muted));

        mStatusModelDf.setText(dfReady ? "● " + getString(R.string.val_ready) : "● " + getString(R.string.val_missing));
        mStatusModelDf.setTextColor(dfReady ? getColor(R.color.status_ok) : getColor(R.color.text_muted));

        if (mode == 2 && !dtlnReady) {
            mHintFallback.setText(R.string.hint_fallback_dtln);
            mHintFallback.setVisibility(View.VISIBLE);
        } else if (mode == 3 && !dfReady) {
            mHintFallback.setText(R.string.hint_fallback_df);
            mHintFallback.setVisibility(View.VISIBLE);
        } else {
            mHintFallback.setVisibility(View.GONE);
        }

        if (markDirty) {
            saveCurrentConfig();
        }
    }

    private void setupListeners() {
        mSwitchMaster.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mConfig.enabled = isChecked;
            updateMasterText(isChecked);
            saveCurrentConfig();
        });

        mSwitchInEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mConfig.in_enabled = isChecked;
            saveCurrentConfig();
        });

        mSwitchOutEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mConfig.out_enabled = isChecked;
            saveCurrentConfig();
        });

        mCardMode1.setOnClickListener(v -> selectMode(1, true));
        mCardMode2.setOnClickListener(v -> selectMode(2, true));
        mCardMode3.setOnClickListener(v -> selectMode(3, true));

        mSeekStrength.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mConfig.strength = progress;
                mValStrength.setText(String.valueOf(progress));
                if (fromUser) saveCurrentConfig();
            }
        });

        mSeekOutStrength.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mConfig.out_strength = progress;
                mValOutStrength.setText(String.valueOf(progress));
                if (fromUser) saveCurrentConfig();
            }
        });

        mSeekAttenDb.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mConfig.atten_db = progress;
                mValAttenDb.setText(progress + " dB");
                if (fromUser) saveCurrentConfig();
            }
        });

        mSeekVad.setOnSeekBarChangeListener(new SimpleSeekBarListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mConfig.vad = progress;
                mValVad.setText(String.valueOf(progress));
                if (fromUser) saveCurrentConfig();
            }
        });

        mSwitchHighpass.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mConfig.highpass = isChecked;
            saveCurrentConfig();
        });

        mSwitchAutogain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mConfig.autogain = isChecked;
            saveCurrentConfig();
        });

        setupChipListeners();

        mBtnApply.setOnClickListener(v -> {
            collectChipSelections();
            mConfig.save();
            ClarifaeController.restartAudio(this);
            Toast.makeText(this, R.string.toast_applied, Toast.LENGTH_SHORT).show();
            renderEngineStatus();
        });

        mBtnRestartAudio.setOnClickListener(v -> {
            ClarifaeController.restartAudio(this);
            Toast.makeText(this, R.string.toast_restarted, Toast.LENGTH_SHORT).show();
            renderEngineStatus();
        });

        mBtnViewLog.setOnClickListener(v -> showLogDialog());

        mBtnReset.setOnClickListener(v -> {
            mConfig.resetToDefaults();
            updateUI();
            ClarifaeController.restartAudio(this);
            Toast.makeText(this, R.string.toast_reset, Toast.LENGTH_SHORT).show();
            renderEngineStatus();
        });

        mBtnRefreshStatus.setOnClickListener(v -> renderEngineStatus());
    }

    private void setupChipListeners() {
        Chip.OnCheckedChangeListener chipListener = (buttonView, isChecked) -> {
            collectChipSelections();
            saveCurrentConfig();
        };

        mChipMic.setOnCheckedChangeListener(chipListener);
        mChipVoiceComm.setOnCheckedChangeListener(chipListener);
        mChipCamcorder.setOnCheckedChangeListener(chipListener);
        mChipVoiceRec.setOnCheckedChangeListener(chipListener);

        mChipMusic.setOnCheckedChangeListener(chipListener);
        mChipVoiceCall.setOnCheckedChangeListener(chipListener);
        mChipRing.setOnCheckedChangeListener(chipListener);
        mChipAlarm.setOnCheckedChangeListener(chipListener);
        mChipNotification.setOnCheckedChangeListener(chipListener);
        mChipSystem.setOnCheckedChangeListener(chipListener);
    }

    private void collectChipSelections() {
        List<String> inList = new ArrayList<>();
        if (mChipMic.isChecked()) inList.add("mic");
        if (mChipVoiceComm.isChecked()) inList.add("voice_comm");
        if (mChipCamcorder.isChecked()) inList.add("camcorder");
        if (mChipVoiceRec.isChecked()) inList.add("voice_rec");
        mConfig.targets = String.join(",", inList);

        List<String> outList = new ArrayList<>();
        if (mChipMusic.isChecked()) outList.add("music");
        if (mChipVoiceCall.isChecked()) outList.add("voice_call");
        if (mChipRing.isChecked()) outList.add("ring");
        if (mChipAlarm.isChecked()) outList.add("alarm");
        if (mChipNotification.isChecked()) outList.add("notification");
        if (mChipSystem.isChecked()) outList.add("system");
        mConfig.out_targets = String.join(",", outList);
    }

    private void saveCurrentConfig() {
        collectChipSelections();
        mConfig.save();
    }

    private void renderEngineStatus() {
        mTableStatus.removeAllViews();

        addStatusRow(getString(R.string.status_enabled), mConfig.enabled ? getString(R.string.val_yes) : getString(R.string.val_no), mConfig.enabled);
        addStatusRow(getString(R.string.status_in_enabled), mConfig.in_enabled ? getString(R.string.val_yes) : getString(R.string.val_no), mConfig.in_enabled);
        addStatusRow(getString(R.string.status_out_enabled), mConfig.out_enabled ? getString(R.string.val_yes) : getString(R.string.val_no), mConfig.out_enabled);

        boolean effectLoaded = ClarifaeController.isEffectLoaded();
        addStatusRow(getString(R.string.status_effect_loaded), effectLoaded ? getString(R.string.val_loaded) : getString(R.string.val_not_detected), effectLoaded);

        boolean xmlConfigured = ClarifaeController.isXmlConfigured();
        addStatusRow(getString(R.string.status_xml_patched), xmlConfigured ? getString(R.string.val_yes) : getString(R.string.val_no), xmlConfigured);

        boolean lib64 = ClarifaeController.isLib64Installed();
        addStatusRow(getString(R.string.status_lib64), lib64 ? getString(R.string.val_installed) : getString(R.string.val_missing), lib64);

        boolean lib32 = ClarifaeController.isLib32Installed();
        addStatusRow(getString(R.string.status_lib32), lib32 ? getString(R.string.val_installed) : getString(R.string.val_missing), lib32);

        boolean asrv = ClarifaeController.isAudioserverRunning();
        addStatusRow(getString(R.string.status_audioserver), asrv ? getString(R.string.val_running) : getString(R.string.val_stopped), asrv);

        addStatusRow(getString(R.string.status_rnnoise), getString(R.string.val_ready), true);

        boolean dtln = ClarifaeController.isModelDtlnReady();
        addStatusRow(getString(R.string.status_dtln), dtln ? getString(R.string.val_ready) : getString(R.string.val_missing), dtln);

        boolean df = ClarifaeController.isModelDfReady();
        addStatusRow(getString(R.string.status_df), df ? getString(R.string.val_ready) : getString(R.string.val_missing), df);
    }

    private void addStatusRow(String label, String value, boolean isOk) {
        TableRow row = new TableRow(this);
        row.setPadding(0, 6, 0, 6);

        TextView tvLabel = new TextView(this);
        tvLabel.setText(label);
        tvLabel.setTextColor(getColor(R.color.text_secondary));
        tvLabel.setTextSize(13);

        TextView tvVal = new TextView(this);
        tvVal.setText(value);
        tvVal.setGravity(Gravity.END);
        tvVal.setTextSize(13);
        tvVal.setTypeface(null, Typeface.BOLD);
        tvVal.setTextColor(isOk ? getColor(R.color.status_ok) : getColor(R.color.text_muted));

        row.addView(tvLabel);
        row.addView(tvVal);
        mTableStatus.addView(row);
    }

    private void showLogDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_log, null);
        TextView logContent = dialogView.findViewById(R.id.log_text);
        Button btnClose = dialogView.findViewById(R.id.btn_close_log);

        logContent.setText(ClarifaeController.readLog());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private abstract static class SimpleSeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
