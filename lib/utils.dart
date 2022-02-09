import 'real_volume.dart';

AudioMode getAudioModefromId(int id) {
  switch (id) {
    case -2:
      return AudioMode.INVALID;
    case -1:
      return AudioMode.CURRENT;
    case 0:
      return AudioMode.NORMAL;
    case 1:
      return AudioMode.RINGTONE;
    case 2:
      return AudioMode.IN_CALL;
    case 3:
      return AudioMode.IN_COMMUNICATION;
    case 4:
      return AudioMode.CALL_SCREENING;
    default:
      return AudioMode.INVALID;
  }
}

RingerMode getRingerModefromIndex(int index) {
  switch (index) {
    case 0:
      return RingerMode.SILENT;
    case 1:
      return RingerMode.VIBRATE;
    case 2:
      return RingerMode.NORMAL;
    default:
      return RingerMode.NORMAL;
  }
}
