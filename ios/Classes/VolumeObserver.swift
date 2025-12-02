import Foundation
import AVFoundation
import MediaPlayer
import Flutter
import UIKit


public class VolumeObserver {   
    let volumeView = MPVolumeView()
    public func getVolume() -> Float? {
        let audioSession = AVAudioSession.sharedInstance()
        do {
            try audioSession.setCategory(AVAudioSession.Category.playback, options: [.mixWithOthers])
            try audioSession.setActive(true)
            return audioSession.outputVolume
        } catch let _ {
            return nil
        }
    }
    
    public func setVolume(volumeLevel:Double, showUI: Bool) {        
        if(!showUI){
            volumeView.frame = CGRect(x: -1000, y: -1000, width: 1, height: 1)
            volumeView.showsRouteButton = false
            UIApplication.shared.delegate!.window!?.rootViewController!.view.addSubview(volumeView);
        } else {
            volumeView.removeFromSuperview();
        }

        let slider = volumeView.subviews.first(where: { $0 is UISlider }) as? UISlider

        DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.01) {
            slider?.value = Float(volumeLevel)
        }
    }
}

public class VolumeListener: NSObject, FlutterStreamHandler {
    private let audioSession = AVAudioSession.sharedInstance()
    private let notification = NotificationCenter.default
    private var eventSink: FlutterEventSink?
    private var isObserving: Bool = false
    private let volumeKey = "outputVolume"


    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        eventSink = events
        registerVolumeObserver()
        let data = ["volumeLevel": audioSession.outputVolume]
        eventSink?(Utils.stringify(json: data))
        
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSink = nil
        removeVolumeObserver()
        
        return nil
    }
    
    private func registerVolumeObserver() {
        audioSessionObserver()
        notification.addObserver(
            self,
            selector: #selector(audioSessionObserver),
            name: UIApplication.didBecomeActiveNotification,
            object: nil)
    }
    
    @objc func audioSessionObserver(){
        do {
            try audioSession.setCategory(AVAudioSession.Category.playback, options: [.mixWithOthers])
            try audioSession.setActive(true)
            if !isObserving {
                audioSession.addObserver(self,
                                         forKeyPath: volumeKey,
                                         options: .new,
                                         context: nil)
                isObserving = true
            }
        } catch {
            print("error")
        }
    }
    
    private func removeVolumeObserver() {
        audioSession.removeObserver(self,
                                    forKeyPath: volumeKey)
        notification.removeObserver(self,
                                    name: UIApplication.didBecomeActiveNotification,
                                    object: nil)
        isObserving = false
    }

    
    override public func observeValue(forKeyPath keyPath: String?,
                                      of object: Any?,
                                      change: [NSKeyValueChangeKey: Any]?,
                                      context: UnsafeMutableRawPointer?) {
        if keyPath == "outputVolume" {
            let data = ["volumeLevel": audioSession.outputVolume]
            eventSink?(Utils.stringify(json: data))
        }
    }
}