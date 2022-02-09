package com.fubigemo.real_volume;

import static com.fubigemo.real_volume.Utils.EXTRA_VOLUME_STREAM_TYPE;
import static com.fubigemo.real_volume.Utils.VOLUME_CHANGED_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;

class VolumeChangeBroadcastReceiver extends BroadcastReceiver {
    private WeakReference<CustomAudioService> customAudioServiceWeakReference;

    public VolumeChangeBroadcastReceiver(CustomAudioService customAudioService) {
        customAudioServiceWeakReference = new WeakReference<>(customAudioService);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (VOLUME_CHANGED_ACTION.equals(intent.getAction())) {
            CustomAudioService customAudioService = customAudioServiceWeakReference.get();
            if (customAudioService != null) {
                int streamType = intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1);
                if(streamType >= 0 && streamType < 6) {
                    double volume = customAudioService.getCurrentVol(streamType);
                    if (volume >= 0) {
                        customAudioService.onVolumeChanged(streamType, volume);
                    }
                }
            }
        }

    }
}