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

#### **Permissions**

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



### API

Get `Ringer Mode`:
 
```dart
import 'package:real_volume/real_volume.dart';

RingerMode? ringerMode = await RealVolume.getRingerMode();
print(ringerMode + 'Unknown');
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
Set current `Volume Level` for **Media** :
 
```dart
import 'package:real_volume/real_volume.dart';

await RealVolume.setVolume(0.75, showUI: true, streamType: StreamType.Media);
```

Get `Audio Mode`:
 
```dart
import 'package:real_volume/real_volume.dart';

AudioMode? audioMode = await RealVolume.getAudioMode();
print(audioMode ?? 'Unknown');
```
Listener for `Volume level`:
 
```dart
import 'package:real_volume/real_volume.dart';

RealVolume.onVolumeChanged.listen((event) async {
    int minVol = (await RealVolume.getMinVol(event.streamType)) ?? 0;
    int maxVol = (await RealVolume.getMaxVol(event.streamType)) ?? 10;

    setState(() {
      minVolume = minVol;
      maxVolume = maxVol;
      currentVolume = event.volumeLevel;
      sliderDivisions = maxVol - minVol;
      selectedStreamType = event.streamType;
    });
});
```

## TODO
- Add support for iOS
- Add listener for `Audio Mode`
- Ensure that `Set Audio Mode` works for every case