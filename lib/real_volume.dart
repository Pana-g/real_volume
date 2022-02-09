// ignore_for_file: constant_identifier_names

import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:real_volume/utils.dart';

class RealVolume {
  static const MethodChannel _methodChannel =
      MethodChannel('real_volume_method');
  static const EventChannel _volumeEventChannel =
      EventChannel('real_volume_change_event');
  static const EventChannel _ringerModeEventChannel =
      EventChannel('real_volume_ringer_mode_change_event');

  static Stream<VolumeObj> get onVolumeChanged =>
      _volumeEventChannel.receiveBroadcastStream().map((event) {
        final obj = jsonDecode(event);
        double vol = 0.0;
        if (obj['volumeLevel'] is int) {
          vol = (obj['volumeLevel'] as int).toDouble();
        } else {
          vol = obj['volumeLevel'];
        }
        return VolumeObj(
            streamType: StreamType.values[obj['streamType']], volumeLevel: vol);
      });

  static Stream<RingerMode> get onRingerModeChanged =>
      _ringerModeEventChannel.receiveBroadcastStream().map((event) {
        return RingerMode.values[event];
      });

  static Future<int?> getMaxVol(StreamType streamType) async {
    final int? version = await _methodChannel
        .invokeMethod('getMaxVol', {'streamType': streamType.index});
    return version;
  }

  static Future<int?> getMinVol(StreamType streamType) async {
    final int? version = await _methodChannel
        .invokeMethod('getMinVol', {'streamType': streamType.index});
    return version;
  }

  static Future<double?> getCurrentVol(StreamType streamType) async {
    final double? currentVolume = await _methodChannel
        .invokeMethod('getCurrentVol', {'streamType': streamType.index});
    return currentVolume;
  }

  static Future<AudioMode?> getAudioMode() async {
    if (Platform.isIOS) return null;
    final int mode = (await _methodChannel.invokeMethod('getAudioMode')) ?? -2;
    return getAudioModefromId(mode);
  }

  static Future<RingerMode?> getRingerMode() async {
    if (Platform.isIOS) return null;
    final int ringerMode =
        (await _methodChannel.invokeMethod('getRingerMode')) ?? 3;
    return getRingerModefromIndex(ringerMode);
  }

  static Future<bool?> isPermissionGranted() async {
    if (Platform.isIOS) return null;
    final bool result =
        (await _methodChannel.invokeMethod('isPermissionGranted')) ?? false;
    return result;
  }

  static Future<bool?> openDoNotDisturbSettings() async {
    if (Platform.isIOS) return null;
    final bool result =
        (await _methodChannel.invokeMethod('openDoNotDisturbSettings')) ??
            false;
    return result;
  }

  static Future<bool?> setAudioMode(AudioMode audioMode) async {
    if (Platform.isIOS) return false;
    final bool? success = await _methodChannel
        .invokeMethod('setAudioMode', {'audioMode': audioMode.id});
    return success;
  }

  static Future<bool?> setRingerMode(RingerMode ringerMode,
      {bool redirectIfNeeded = true}) async {
    if (Platform.isIOS) return false;
    try {
      return await _methodChannel.invokeMethod('setRingerMode', {
        'ringerMode': ringerMode.index,
        'redirectIfNeeded': redirectIfNeeded
      });
    } catch (e) {
      print('Error: ' + e.toString());
    }
    return false;
  }

  static Future<bool?> setVolume(double volumeLevel,
      {StreamType streamType = StreamType.SYSTEM, bool showUI = false}) async {
    if (Platform.isIOS) return false;
    final bool? success = await _methodChannel.invokeMethod('setVolume', {
      'streamType': streamType.index,
      'volumeLevel': volumeLevel,
      'showUI': showUI ? 1 : 0
    });
    return success;
  }
}

/*
Interfaces for StreamType

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
enum StreamType { VOICE_CALL, SYSTEM, RING, MUSIC, ALARM, NOTIFICATION }

/*
ENUMS FOR MODE

INVALID -> Invalid audio mode.

CURRENT -> Current audio mode. Used to apply audio routing to current mode.

NORMAL -> Normal audio mode: not ringing and no call established.

RINGTONE -> Ringing audio mode. An incoming is being signaled.

IN_CALL -> In call audio mode. A telephony call is established.

IN_COMMUNICATION -> In communication audio mode. An audio/video chat or VoIP call is established.

CALL_SCREENING -> Call screening in progress. Call is connected and audio is accessible to call
screening applications but other audio use cases are still possible.
*/
enum AudioMode {
  INVALID,
  CURRENT,
  NORMAL,
  RINGTONE,
  IN_CALL,
  IN_COMMUNICATION,
  CALL_SCREENING
}

extension ModeExtension on AudioMode {
  int get id {
    switch (this) {
      case AudioMode.INVALID:
        return -2;
      case AudioMode.CURRENT:
        return -1;
      case AudioMode.NORMAL:
        return 0;
      case AudioMode.RINGTONE:
        return 1;
      case AudioMode.IN_CALL:
        return 2;
      case AudioMode.IN_COMMUNICATION:
        return 3;
      case AudioMode.CALL_SCREENING:
        return 4;
      default:
        return -1;
    }
  }
}

/*
ENUMS for RingerMode

Ringer mode that will be silent and will not vibrate. (This overrides the vibrate setting.)
RINGER_MODE_SILENT = 0;

Ringer mode that will be silent and will vibrate. (This will cause the phone ringer to always vibrate, but the notification vibrate to only vibrate if set.)
RINGER_MODE_VIBRATE = 1;

Ringer mode that may be audible and may vibrate. It will be audible if the volume before changing out of this mode was audible. It will vibrate if the vibrate setting is on.
RINGER_MODE_NORMAL = 2;
 */
enum RingerMode { SILENT, VIBRATE, NORMAL }

class VolumeObj {
  StreamType streamType;
  double volumeLevel;
  VolumeObj({
    required this.streamType,
    required this.volumeLevel,
  });
}
