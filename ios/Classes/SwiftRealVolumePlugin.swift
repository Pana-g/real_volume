import Flutter
import UIKit
import MediaPlayer
import Mute

public class SwiftRealVolumePlugin: NSObject, FlutterPlugin {

  private let volumeObserver = VolumeObserver()
  private let ringerModeObserver = RingerModeObserver()

  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "real_volume_method", binaryMessenger: registrar.messenger())
    let instance = SwiftRealVolumePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)

    // Volume changed listener
    let volumeEventChannel:FlutterEventChannel = FlutterEventChannel(name: "real_volume_change_event", binaryMessenger: registrar.messenger())
    volumeEventChannel.setStreamHandler(VolumeListener())

    // Ringer Mode changed listener
    let ringerModeEventChannel:FlutterEventChannel = FlutterEventChannel(name: "real_volume_ringer_mode_change_event", binaryMessenger: registrar.messenger())
    ringerModeEventChannel.setStreamHandler(RingerModeListener())
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arg = call.arguments as? [String:Any]
    switch call.method {
      case "getCurrentVol":
        result(volumeObserver.getVolume())
      case "setVolume":
        let volumeLevel = ( arg?["volumeLevel"] as? Double) ?? 0
        let showUI = arg?["showUI"] as? Int
        volumeObserver.setVolume(volumeLevel: volumeLevel, showUI: showUI == 1)
        result(true)
      case "getMinVol":
        result(0)
      case "getMaxVol":
        result(16)
      case "getRingerMode":
        result(ringerModeObserver.getRingerMode())
      default:
        result(FlutterMethodNotImplemented)
    }
    result("iOS " + UIDevice.current.systemVersion)
  }
}