package com.fubigemo.real_volume;

import io.flutter.plugin.common.EventChannel;

public class RingerModeStreamHandler implements EventChannel.StreamHandler {
    CustomAudioService customAudioService;

    public RingerModeStreamHandler(CustomAudioService customAudioService) {
        this.customAudioService = customAudioService;
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        customAudioService.registerRingerModeListener(eventSink);
    }

    @Override
    public void onCancel(Object o) {
        customAudioService.unregisterRingerModeListener();
    }
}
