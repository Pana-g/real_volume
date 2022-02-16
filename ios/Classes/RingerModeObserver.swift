import Foundation
import AVFoundation
import Mute
import Flutter
import UIKit

public class RingerModeObserver {   
    var ringerMode: Int = 2
    public func getRingerMode() -> Int {
        Mute.shared.notify = { 
          [weak self] m in
          self?.ringerMode = m ? 0 : 2
        }
        return ringerMode
    }
    
}

public class RingerModeListener: NSObject, FlutterStreamHandler {
    private let muteShared = Mute.shared
    private let notification = NotificationCenter.default
    private var eventSink: FlutterEventSink?
    var ringerMode: Int = 2
    var previousRingerMode: Int = 2
    var isFirstTime: Bool = true

    public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
        eventSink = events
         muteShared.notify = { 
            [weak self] m in
            self?.previousRingerMode = self?.ringerMode ?? 0
            self?.ringerMode = m ? 0 : 2
            if(self?.previousRingerMode != self?.ringerMode || self?.isFirstTime ?? false){
                self?.eventSink?(self?.ringerMode)
                self?.isFirstTime = false;
            }
        }
        muteShared.checkInterval = 0.5
        muteShared.alwaysNotify = true
        muteShared.isPaused = false
        
        return nil
    }
    
    public func onCancel(withArguments arguments: Any?) -> FlutterError? {
        eventSink = nil
        isFirstTime = true;
        muteShared.isPaused = true
        
        return nil
    }
}