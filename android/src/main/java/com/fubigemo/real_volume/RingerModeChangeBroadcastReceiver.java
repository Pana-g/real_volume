package com.fubigemo.real_volume;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import java.lang.ref.WeakReference;

class RingerModeChangeBroadcastReceiver extends BroadcastReceiver {
    private WeakReference<CustomAudioService> customAudioServiceWeakReference;

    public RingerModeChangeBroadcastReceiver(CustomAudioService customAudioService) {
        customAudioServiceWeakReference = new WeakReference<>(customAudioService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.RINGER_MODE_CHANGED_ACTION.equals(intent.getAction())) {
            CustomAudioService customAudioService = customAudioServiceWeakReference.get();
            if (customAudioService != null) {
                int ringerMode = customAudioService.getRingerMode();
                if (ringerMode >= 0 && ringerMode < 4) {
                    customAudioService.onRingerModeChanged(ringerMode);
                }
            }
        }

    }
}