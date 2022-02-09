import 'package:flutter/material.dart';
import 'dart:async';

import 'package:real_volume/real_volume.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  StreamType selectedStreamType = StreamType.VOICE_CALL;
  double currentVolume = 0;

  late List<StreamType> menuItems = StreamType.values;
  int sliderDivisions = 10;
  int minVolume = 0;
  int maxVolume = 10;
  RingerMode ringerMode = RingerMode.NORMAL;
  bool showUI = false;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    RealVolume.onRingerModeChanged.listen((event) async {
      setState(() {
        ringerMode = event;
      });
      if (selectedStreamType == StreamType.NOTIFICATION ||
          selectedStreamType == StreamType.RING) {
        double curVol =
            (await RealVolume.getCurrentVol(selectedStreamType)) ?? 0;
        setState(() {
          currentVolume = curVol;
        });
      }
    });
    RealVolume.onVolumeChanged.listen((event) {
      onStreamTypeChanged(event.streamType);
    });
    RingerMode rMode = (await RealVolume.getRingerMode())!;
    setState(() {
      ringerMode = rMode;
    });
    onStreamTypeChanged(selectedStreamType);
    if (!mounted) return;
  }

  onStreamTypeChanged(streamType) async {
    int minVol = (await RealVolume.getMinVol(streamType)) ?? 0;
    int maxVol = (await RealVolume.getMaxVol(streamType)) ?? 10;
    double currentVol = (await RealVolume.getCurrentVol(streamType)) ?? 0;
    setState(() {
      minVolume = minVol;
      maxVolume = maxVol;
      currentVolume = currentVol;
      sliderDivisions = maxVol - minVol;
      selectedStreamType = streamType;
    });
  }

  onValueChanged(double val) {
    RealVolume.setVolume(val, showUI: showUI, streamType: selectedStreamType);
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Real Volume plugin'),
        ),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Padding(
                    padding: EdgeInsets.only(right: 8.0),
                    child: Text('Volume Stream Type:'),
                  ),
                  DropdownButton<StreamType>(
                    value: selectedStreamType,
                    items: menuItems.map((StreamType streamType) {
                      return DropdownMenuItem<StreamType>(
                        value: streamType,
                        child: Text(streamType.name),
                      );
                    }).toList(),
                    onChanged: onStreamTypeChanged,
                  ),
                ],
              ),
              SizedBox(
                width: 150,
                child: CheckboxListTile(
                    title: const Text('Show UI'),
                    value: showUI,
                    onChanged: (val) {
                      setState(() {
                        showUI = val!;
                      });
                    }),
              ),
              Slider(
                divisions: sliderDivisions,
                value: currentVolume,
                onChanged: onValueChanged,
                // onChangeEnd: onValueChanged,
              ),
              Text('Volume: $currentVolume%'),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Padding(
                    padding: EdgeInsets.only(right: 8.0),
                    child: Text('Ringer Mode:'),
                  ),
                  DropdownButton<RingerMode>(
                      value: ringerMode,
                      items: RingerMode.values.map((RingerMode ringerMode) {
                        return DropdownMenuItem<RingerMode>(
                          value: ringerMode,
                          child: Text(ringerMode.name),
                        );
                      }).toList(),
                      onChanged: (value) async {
                        await RealVolume.setRingerMode(value!,
                            redirectIfNeeded: false);
                        RingerMode? rMode = (await RealVolume.getRingerMode());
                        if (rMode != null) {
                          setState(() {
                            ringerMode = rMode;
                          });
                        }
                      }),
                ],
              ),
              MaterialButton(
                elevation: 2,
                color: Colors.grey[100],
                onPressed: () => RealVolume.openDoNotDisturbSettings(),
                child: const Text('Open DND Settings'),
              )
            ],
          ),
        ),
      ),
    );
  }
}
