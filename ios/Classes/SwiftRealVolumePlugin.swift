import Flutter
import UIKit
import MediaPlayer

public class SwiftRealVolumePlugin: NSObject, FlutterPlugin {
  public static func register(with registrar: FlutterPluginRegistrar) {
    let channel = FlutterMethodChannel(name: "real_volume_method", binaryMessenger: registrar.messenger())
    let instance = SwiftRealVolumePlugin()
    registrar.addMethodCallDelegate(instance, channel: channel)
  }

  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
    let arg = call.arguments as? [String:Any]
    switch call.method {
      case "getCurrentVol":
        result(AVAudioSession.sharedInstance().outputVolume)
      case "setVolume":
            let volumeLevel = arg?["volumeLevel"] as? Float
            let showUI = (arg?["showUI"] as? Int) ?? 0
        MPVolumeView.setVolume(volumeLevel ?? 0.0, showUI == 1)
        result(true)
      case "getMinVol":
        result(0)
      case "getMaxVol":
        result(10)
      default:
        result(FlutterMethodNotImplemented)
    }
    result("iOS " + UIDevice.current.systemVersion)
  }
}

extension MPVolumeView {
    static func setVolume(_ volume: Float,_ showUI: Bool) {
        let volumeView = MPVolumeView()
        if(!showUI){
          volumeView.isHidden = false
          volumeView.alpha = 0.01
        }
        let slider = volumeView.subviews.first(where: { $0 is UISlider }) as? UISlider
        DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.01) {
            slider?.value = volume
        }
    }
}