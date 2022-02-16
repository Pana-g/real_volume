# real_volume
A Flutter audio/volume plugin which provides easy access to volume/audio related info like `Volume Level` for different `Stream Types`, `Ringer Mode` or even `Audio Mode`.

## Features
- Get volume level based on stream type
- Use listener to detect volume level changes
- Change volume level for any stream type
- Get audio mode
- Change audio mode(?)
- Get ringer mode
- Use listener to detect ringer mode changes
- Change ringer mode
- Open do not disturb settings
- Check status of Do Not Disturb permission access

## Usage
### Android

**Permissions**

No permissions are required for the majority of this plugin in Android. However, in order to switch _Do Not Disturb Mode_ on/off you will need to enable this permission. To do so first you need to add the following to `AndroidManifest.xml`. This will make the app to appear in the **Do Not Disturb Access** list.
```xml
<manifest ... >
    <uses-permission android:name="android.permission.ACCESS_NOTIFICATION_POLICY"/>

    <application ... >
    ...
</manifest>
```
#### _For Android 6.0 and above_
For devices with Android 6.0 and above, additionally to the above it is required for the user to grant **Do No Disturb Access**. 

To check if the user has granted the permissions and prompt for approval
```dart
import 'package:real_volume/real_volume.dart';

bool? isPermissionGranted = await RealVolume.isPermissionGranted();

if (!isPermissionGranted!) {
  // Opens Do Not Disturb Access settings to grant the access
  await RealVolume.openDoNotDisturbSettings();
}
``` 

### iOS
#### _Supported Fucntions for iOS devices_

- `getMaxVol`
- `getMinVol`
- `getCurrentVol`
- `setVolume`
- `getRingerMode` *(will return either RingerMode.`SILENT` or RingerMode.`NORMAL`)*

#### _Supported Listeners for iOS devices_
- `onVolumeChanged`
- `onRingerModeChanged` *(event can be either RingerMode.`SILENT` or RingerMode.`NORMAL`)*

> **_NOTE_1:_**  For iOS devices `streamType` is completely ignored when it passed as an argument and null when it is returned.

> **_NOTE_2:_**  For iOS devices if you call `getRingerMode` **AFTER** you have subscribed to `onRingerModeChanged` listener the listener will stop working. So keep in mind that if you want to use both of them in the same project you need to cancel and re-subscribe to `onRingerModeChanged` listener **AFTER** you have called `getRingerMode` function.

### API

Get `Ringer Mode`:
 
```dart
import 'package:real_volume/real_volume.dart';

RingerMode? ringerMode = await RealVolume.getRingerMode();
print(ringerMode?.name ?? 'Unknown');
```

Set `Ringer Mode`:
 
```dart
import 'package:real_volume/real_volume.dart';

// It will redirect the user to grant DND access if 'redirectIfNeeded' is set to true(default behavior)
await RealVolume.setRingerMode(RingerMode.SILENT, redirectIfNeeded: false);
```
Get current `Volume Level` for **Notifications** :
 
```dart
import 'package:real_volume/real_volume.dart';

double notificationVolume = (await RealVolume.getCurrentVol(StreamType.NOTIFICATION)) ?? 0.0;
print(notificationVolume+'');
```
Set current `Volume Level` for **Music** :
 
```dart
import 'package:real_volume/real_volume.dart';

await RealVolume.setVolume(0.75, showUI: true, streamType: StreamType.Music);
```

Get `Audio Mode`:
 
```dart
import 'package:real_volume/real_volume.dart';

AudioMode? audioMode = await RealVolume.getAudioMode();
print(audioMode?.name ?? 'Unknown');
```
Listener for `Volume level`:
 
```dart
import 'package:real_volume/real_volume.dart';

RealVolume.onVolumeChanged.listen((event) async {
    setState(() {
      currentVolume = event.volumeLevel;
      selectedStreamType = event.streamType;
    });
});
```

## TODO
- Add listener for `Audio Mode`
- Ensure that `Set Audio Mode` works for every case