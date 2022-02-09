package com.fubigemo.real_volume;

import static com.fubigemo.real_volume.Utils.RINGER_MODE_CHANGED;
import static com.fubigemo.real_volume.Utils.TAG;
import static com.fubigemo.real_volume.Utils.VOLUME_CHANGED_ACTION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodChannel;
/*
Interfaces for "streamType" in [getMaxVol, getCurrentVol, setVolume]

Used to identify the volume of audio streams for phone calls
AudioManager.STREAM_VOICE_CALL = AudioSystem.STREAM_VOICE_CALL = 0;

Used to identify the volume of audio streams for system sounds
AudioManager.STREAM_SYSTEM = AudioSystem.STREAM_SYSTEM = 1;

Used to identify the volume of audio streams for the phone ring
AudioManager.STREAM_RING = AudioSystem.STREAM_RING = 2;

Used to identify the volume of audio streams for music playback
AudioManager.STREAM_MUSIC = AudioSystem.STREAM_MUSIC = 3;

Used to identify the volume of audio streams for alarms
AudioManager.STREAM_ALARM = AudioSystem.STREAM_ALARM = 4;

Used to identify the volume of audio streams for notifications
AudioManager.STREAM_NOTIFICATION = AudioSystem.STREAM_NOTIFICATION = 5;
 */


/*
Interfaces for "mode" in functions [setMode, getMode]

Invalid audio mode.
MODE_INVALID = AudioSystem.MODE_INVALID = -2;

Current audio mode. Used to apply audio routing to current mode.
MODE_CURRENT = AudioSystem.MODE_CURRENT = -1;

Normal audio mode: not ringing and no call established.
MODE_NORMAL = AudioSystem.MODE_NORMAL = 0;

Ringing audio mode. An incoming is being signaled.
MODE_RINGTONE = AudioSystem.MODE_RINGTONE = 1;

In call audio mode. A telephony call is established.
MODE_IN_CALL = AudioSystem.MODE_IN_CALL = 2;

In communication audio mode. An audio/video chat or VoIP call is established.
MODE_IN_COMMUNICATION = AudioSystem.MODE_IN_COMMUNICATION = 3;

Call screening in progress. Call is connected and audio is accessible to call screening applications but other audio use cases are still possible.
MODE_CALL_SCREENING = AudioSystem.MODE_CALL_SCREENING = 4;
*/

/*
Interfaces for "ringerMode" in functions [setRingerMode, getRingerMode]

Ringer mode that will be silent and will not vibrate. (This overrides the vibrate setting.)
RINGER_MODE_SILENT = 0;

Ringer mode that will be silent and will vibrate. (This will cause the phone ringer to always vibrate, but the notification vibrate to only vibrate if set.)
RINGER_MODE_VIBRATE = 1;

Ringer mode that may be audible and may vibrate. It will be audible if the volume before changing out of this mode was audible. It will vibrate if the vibrate setting is on.
RINGER_MODE_NORMAL = 2;
 */

public class CustomAudioService {
    private EventChannel.EventSink volumeEventSink, ringerModeEventSink;
    private AudioManager audioManager;
    private Context context;
    private boolean volumeListenerRegistered, ringerModeListenerRegistered;
    private VolumeChangeBroadcastReceiver volumeChangeBroadcastReceiver;
    private RingerModeChangeBroadcastReceiver ringerModeChangeBroadcastReceiver;
    private NotificationManager notificationManager;

    public CustomAudioService(Context context) {
        this.context = context;
        this.volumeListenerRegistered = false;
        this.ringerModeListenerRegistered = false;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Returns the status of notification policy access for this app
     *
     * @return isPermissionGranted
     */
    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return notificationManager.isNotificationPolicyAccessGranted();
        } else {
            Log.d(TAG, "[permissionsGranted] - Android version is less than android.os.Build.VERSION_CODES.M");
            return false;
        }
    }

    /**
     * Opens do not disturb notification policy
     *
     * @param context
     */
    public boolean openDoNotDisturbSettings(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        } else {
            Log.d(TAG, "[launchSettings] - Android version is less than android.os.Build.VERSION_CODES.M");
            return false;
        }
    }

    /**
     * Returns the maximum volume for a given stream type
     *
     * @param streamType
     * @return maxVolume
     */
    public int getMaxVol(int streamType) {
        return audioManager.getStreamMaxVolume(streamType);
    }

    /**
     * Returns the minimum volume for a given stream type
     *
     * @param streamType
     * @return minVolume
     */
    @SuppressLint("NewApi")
    public int getMinVol(int streamType) {
        return audioManager.getStreamMinVolume(streamType);
    }

    /**
     * Returns the current volume for a given stream type
     *
     * @param streamType
     * @return currentVolume
     */
    @SuppressLint("NewApi")
    public double getCurrentVol(int streamType) {
        // Calculation is happening based on the min volume as well since some streamTypes may have min volume other than zero
        double maxVol = audioManager.getStreamMaxVolume(streamType);
        double minVol = audioManager.getStreamMinVolume(streamType);
        double curVol = (double) audioManager.getStreamVolume(streamType);
        double percentageVol = (curVol - minVol) / (maxVol - minVol);
        BigDecimal bd = BigDecimal.valueOf(percentageVol);
        bd = bd.setScale(3, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
    }

    /**
     * Returns the current audio mode
     *
     * @return mode
     */
    public int getAudioMode() {
        return audioManager.getMode();
    }

    /**
     * Returns the current ringer mode
     *
     * @return ringerMode
     */
    public int getRingerMode() {
        return audioManager.getRingerMode();
    }

    /**
     * Sets the ringer mode
     *
     * @param ringerMode
     * @return success
     */
    public Object setRingerMode(int ringerMode, boolean redirectToSettingsIfNeeded) {
        try {
            boolean isAccessGranted = isPermissionGranted();
            if (isAccessGranted) {
                audioManager.setRingerMode(ringerMode);
            } else {
                int previousRingerMode = getRingerMode();
                if((previousRingerMode == 0 && ringerMode != previousRingerMode) || (previousRingerMode != 0 && ringerMode == 0)){
                    if(redirectToSettingsIfNeeded) {
                        openDoNotDisturbSettings(context);
                    }
                    Log.e(TAG, "[setRingerMode] - You have insufficient permissions for this action. Please run 'openDoNotDisturbSetting()' to enable required permissions.");
                    return false;
                } else {
                    audioManager.setRingerMode(ringerMode);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "[setRingerMode] - Exception: " + e.getMessage());
            return e;
        }
    }

    /**
     * Sets the audio mode
     *
     * @param mode
     * @return success
     */
    public boolean setAudioMode(int mode) {
        try {
            audioManager.setMode(mode);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "[setAudioMode] - Exception: " + e.getMessage());
            return false;
        }
    }

    /**
     * Sets a given stream type volume
     *
     * @param streamType
     * @param value
     * @return success
     */
    @SuppressLint("NewApi")
    public boolean setVolume(int streamType, double value, int showUI) {
        double actualValue;
        if (value > 1) {
            actualValue = 1;
        } else {
            actualValue = Math.max(value, 0);
        }
        int maxVolume = audioManager.getStreamMaxVolume(streamType);
        int minVolume = audioManager.getStreamMinVolume(streamType);
        double rawVol = (actualValue * (maxVolume - minVolume) + minVolume);
        int volume = (int) Math.round(rawVol);
        if (audioManager != null) {
            try {
                audioManager.setStreamVolume(streamType, volume, showUI);
                if (volume < minVolume + 1) {
                    audioManager.adjustStreamVolume(streamType, AudioManager.ADJUST_LOWER, showUI);
                }
                return true;
            } catch (Exception e) {
                Log.e(TAG, "[setVolume] - Exception: " + e.getMessage());
                return false;
            }
        } else {
            Log.e(TAG, "[setVolume] - Exception: AudioManager is null");
            return false;
        }
    }

    public void registerVolumeListener(EventChannel.EventSink eventSink) {
        try {
            volumeChangeBroadcastReceiver = new VolumeChangeBroadcastReceiver(this);
            this.volumeEventSink = eventSink;
            IntentFilter filter = new IntentFilter();
            filter.addAction(VOLUME_CHANGED_ACTION);
            context.registerReceiver(volumeChangeBroadcastReceiver, filter);
            volumeListenerRegistered = true;
            Log.i(TAG, "Volume Listener registered");
        } catch (Exception e) {
            Log.e(TAG, "[registerListener-ringerMode] - Exception: " + e.getMessage());
        }
    }

    public void unregisterVolumeListener() {
        if (volumeListenerRegistered) {
            try {
                context.unregisterReceiver(volumeChangeBroadcastReceiver);
                this.volumeEventSink = null;
                volumeChangeBroadcastReceiver = null;
                volumeListenerRegistered = false;
                Log.i(TAG, "Volume Listener unregistered");
            } catch (Exception e) {
                Log.e(TAG, "[unregisterListener-ringerMode] - Exception: " + e.getMessage());
            }
        }
    }

    public void onVolumeChanged(int streamType, double volumeLevel) {
        if (volumeEventSink != null && streamType >= 0 && streamType < 6) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("streamType", streamType);
                obj.put("volumeLevel", volumeLevel);
                volumeEventSink.success(obj.toString(1));
            } catch (Exception e) {
                Log.e(TAG, "[onVolumeChanged] - Exception: " + e.getMessage());
            }
        }
    }

    public void registerRingerModeListener(EventChannel.EventSink eventSink) {
        try {
            ringerModeChangeBroadcastReceiver = new RingerModeChangeBroadcastReceiver(this);
            this.ringerModeEventSink = eventSink;
            IntentFilter filter = new IntentFilter();
            filter.addAction(AudioManager.RINGER_MODE_CHANGED_ACTION);
            context.registerReceiver(ringerModeChangeBroadcastReceiver, filter);
            ringerModeListenerRegistered = true;
            Log.i(TAG, "Ringer Mode Listener registered");
        } catch (Exception e) {
            Log.e(TAG, "[registerListener-ringerMode] - Exception: " + e.getMessage());
        }
    }

    public void unregisterRingerModeListener() {
        if (ringerModeListenerRegistered) {
            try {
                context.unregisterReceiver(ringerModeChangeBroadcastReceiver);
                this.ringerModeEventSink = null;
                ringerModeChangeBroadcastReceiver = null;
                ringerModeListenerRegistered = false;
                Log.i(TAG, "Ringer Mode Listener unregistered");
            } catch (Exception e) {
                Log.e(TAG, "[unregisterListener-ringerMode] - Exception: " + e.getMessage());
            }
        }
    }

    public void onRingerModeChanged(int ringerMode) {
        if (ringerModeEventSink != null && ringerMode >= 0 && ringerMode < 4) {
            try {
                ringerModeEventSink.success(ringerMode);
            } catch (Exception e) {
                Log.e(TAG, "[onRingerModeChanged] - Exception: " + e.getMessage());
            }
        }
    }
}
