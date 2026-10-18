/*
 * Copyright (C) 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.rising.clarifae;

import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

public class ClarifaeTileService extends TileService {

    @Override
    public void onStartListening() {
        super.onStartListening();
        updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();
        ClarifaeConfig cfg = ClarifaeConfig.load();
        cfg.enabled = !cfg.enabled;
        cfg.save();
        ClarifaeController.restartAudio(this);
        updateTile();
    }

    private void updateTile() {
        Tile tile = getQsTile();
        if (tile == null) return;

        ClarifaeConfig cfg = ClarifaeConfig.load();
        boolean active = cfg.enabled;

        tile.setState(active ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        tile.setLabel(getString(R.string.tile_label));
        tile.setSubtitle(active ? (cfg.in_enabled ? getString(R.string.in_enabled_title) : "Active")
                : getString(R.string.master_sub_paused));
        tile.setIcon(Icon.createWithResource(this, R.drawable.ic_clarifae));
        tile.updateTile();
    }
}
