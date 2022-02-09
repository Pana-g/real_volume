package com.fubigemo.real_volume;

import static com.fubigemo.real_volume.Utils.TAG;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.util.Objects;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * RealVolumePlugin
 */
public class RealVolumePlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    private MethodChannel methodChannel;
    private EventChannel volumeEventChannel, ringerModeEventChannel;
    private RingerModeStreamHandler ringerModeStreamHandler;
    private CustomAudioService audioService;
    private Context context;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.context = flutterPluginBinding.getApplicationContext();
        audioService = new CustomAudioService(flutterPluginBinding.getApplicationContext());

        methodChannel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "real_volume_method");
        methodChannel.setMethodCallHandler(this);

        volumeEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "real_volume_change_event");
        volumeEventChannel.setStreamHandler(this);

        ringerModeStreamHandler = new RingerModeStreamHandler(audioService);
        ringerModeEventChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "real_volume_ringer_mode_change_event");
        ringerModeEventChannel.setStreamHandler(ringerModeStreamHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("getMaxVol")) {
            result.success(audioService.getMaxVol(Objects.requireNonNull(call.argument("streamType"))));
        } else if (call.method.equals("getMinVol")) {
            result.success(audioService.getMinVol(Objects.requireNonNull(call.argument("streamType"))));
        } else if (call.method.equals("getCurrentVol")) {
            result.success(audioService.getCurrentVol(Objects.requireNonNull(call.argument("streamType"))));
        } else if (call.method.equals("getAudioMode")) {
            result.success(audioService.getAudioMode());
        } else if (call.method.equals("setAudioMode")) {
            result.success(audioService.setAudioMode(Objects.requireNonNull(call.argument("audioMode"))));
        } else if (call.method.equals("getRingerMode")) {
            result.success(audioService.getRingerMode());
        } else if (call.method.equals("isPermissionGranted")) {
            result.success(audioService.isPermissionGranted());
        } else if (call.method.equals("openDoNotDisturbSettings")) {
            result.success(audioService.openDoNotDisturbSettings(context));
        } else if (call.method.equals("setRingerMode")) {
            Object res = audioService.setRingerMode(Objects.requireNonNull(call.argument("ringerMode")), Objects.requireNonNull(call.argument("redirectIfNeeded")));
            if(res instanceof Boolean) {
                result.success(res);
            } else {
                result.error(TAG,"[setRingerMode]",((Exception)res).getMessage());
            }
        } else if (call.method.equals("setVolume")) {
            result.success(audioService.setVolume(
                    Objects.requireNonNull(call.argument("streamType")),
                    Objects.requireNonNull(call.argument("volumeLevel")),
                    Objects.requireNonNull(call.argument("showUI"))
            ));
        } else {
            result.notImplemented();
        }
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
        volumeEventChannel.setStreamHandler(null);
        ringerModeEventChannel.setStreamHandler(null);
        audioService.unregisterVolumeListener();
        audioService.unregisterRingerModeListener();
    }

    @Override
    public void onListen(Object o, EventChannel.EventSink eventSink) {
        audioService.registerVolumeListener(eventSink);
    }

    @Override
    public void onCancel(Object o) {
        audioService.unregisterVolumeListener();
    }

}
