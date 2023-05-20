import Capacitor
import Foundation
import AVFoundation
import MLKitVision
import MLKitBarcodeScanning
import Photos
import PhotosUI

internal enum CameraPermissionType: String, CaseIterable {
    case camera
    // preparation for a future version of the plugin
    // case photos
}

internal protocol CameraAuthorizationState {
    var authorizationState: String { get }
}

extension AVAuthorizationStatus: CameraAuthorizationState {
    var authorizationState: String {
        switch self {
        case .denied, .restricted:
            return "denied"
        case .authorized:
            return "granted"
        case .notDetermined:
            fallthrough
        @unknown default:
            return "prompt"
        }
    }
}

extension PHAuthorizationStatus: CameraAuthorizationState {
    var authorizationState: String {
        switch self {
        case .denied, .restricted:
            return "denied"
        case .authorized:
            return "granted"
        #if swift(>=5.3)
        // poor proxy for Xcode 12/iOS 14, should be removed once building with Xcode 12 is required
        case .limited:
            return "limited"
        #endif
        case .notDetermined:
            fallthrough
        @unknown default:
            return "prompt"
        }
    }
}

@objc(CapacitorCommunityBarcodeScanner)
public class CapacitorCommunityBarcodeScanner: CAPPlugin, AVCaptureVideoDataOutputSampleBufferDelegate {

    class CameraView: UIView {
        var videoPreviewLayer:AVCaptureVideoPreviewLayer?

        func interfaceOrientationToVideoOrientation(_ orientation : UIInterfaceOrientation) -> AVCaptureVideoOrientation {
            switch (orientation) {
            case UIInterfaceOrientation.portrait:
                return AVCaptureVideoOrientation.portrait
            case UIInterfaceOrientation.portraitUpsideDown:
                return AVCaptureVideoOrientation.portraitUpsideDown
            case UIInterfaceOrientation.landscapeLeft:
                return AVCaptureVideoOrientation.landscapeLeft
            case UIInterfaceOrientation.landscapeRight:
                return AVCaptureVideoOrientation.landscapeRight
            default:
                return AVCaptureVideoOrientation.portraitUpsideDown
            }
        }

        override func layoutSubviews() {
            super.layoutSubviews()
            if let sublayers = self.layer.sublayers {
                for layer in sublayers {
                    layer.frame = self.bounds
                }
            }
            
            if let interfaceOrientation = UIApplication.shared.windows.first(where: { $0.isKeyWindow })?.windowScene?.interfaceOrientation {
                self.videoPreviewLayer?.connection?.videoOrientation = interfaceOrientationToVideoOrientation(interfaceOrientation)
            }
        }


        func addPreviewLayer(_ previewLayer:AVCaptureVideoPreviewLayer?) {
            previewLayer!.videoGravity = AVLayerVideoGravity.resizeAspectFill
            previewLayer!.frame = self.bounds
            self.layer.addSublayer(previewLayer!)
            self.videoPreviewLayer = previewLayer
        }

        func removePreviewLayer() {
            if self.videoPreviewLayer != nil {
                self.videoPreviewLayer!.removeFromSuperlayer()
                self.videoPreviewLayer = nil
            }
        }
    }

    var cameraView: CameraView!
    var captureSession:AVCaptureSession?
    var captureVideoPreviewLayer:AVCaptureVideoPreviewLayer?
    var metaOutput: AVCaptureMetadataOutput?

    var currentCamera: AVCaptureDevice?
    var frontCamera: AVCaptureDevice?
    var backCamera: AVCaptureDevice?
    var barcodeScanner: BarcodeScanner?

    var isScanning: Bool = false
    var shouldRunScan: Bool = false
    var didRunCameraSetup: Bool = false
    var didRunCameraPrepare: Bool = false
    var isBackgroundHidden: Bool = false

    var savedCall: CAPPluginCall? = nil
    var scanningPaused: Bool = false
    var previousBackgroundColor: UIColor? = UIColor.white
    

    let serialBackgroundQueue = DispatchQueue(label: "capacitorBarcodeScannerQueue")


    var formats = [BarcodeFormat]()

    enum CaptureError: Error {
        case backCameraUnavailable
        case frontCameraUnavailable
        case couldNotCaptureInput(error: NSError)
    }

    public override func load() {
        self.cameraView = CameraView(frame: CGRect(x: 0, y: 0, width: UIScreen.main.bounds.width, height: UIScreen.main.bounds.height))
        self.cameraView.autoresizingMask = [.flexibleWidth, .flexibleHeight]
    }

    private func hasCameraPermission() -> Bool {
        let status = AVCaptureDevice.authorizationStatus(for: AVMediaType.video)
        if (status == AVAuthorizationStatus.authorized) {
            return true
        }
        return false
    }
    
    private func enableAutoFocus(device: AVCaptureDevice) throws {
        if(device.isFocusModeSupported(.continuousAutoFocus)) {
            try device.lockForConfiguration()
            device.focusMode = .continuousAutoFocus
            device.unlockForConfiguration()
        }
    }
    

    private func setupCamera(cameraDirection: String? = "back", zoom: Float?) -> Bool {
        do {
            var cameraDir = cameraDirection
            cameraView.backgroundColor = UIColor.clear
            self.webView!.superview!.insertSubview(cameraView, belowSubview: self.webView!)
            
            // load cameras in a given order to make sure triple is used on devices that are supported
            // JanMisker: sorry no; the triple camera is not really useful,
            // We should instead enable camera switching, exposing all cameras, and/or allow zooming
            let deviceDescoverySession = AVCaptureDevice.DiscoverySession.init(deviceTypes: [AVCaptureDevice.DeviceType.builtInDualCamera, AVCaptureDevice.DeviceType.builtInWideAngleCamera, AVCaptureDevice.DeviceType.builtInTripleCamera],
                                                                            mediaType: AVMediaType.video,
                                                                            position: AVCaptureDevice.Position.unspecified)
        
            for device in deviceDescoverySession.devices {
                if device.position == AVCaptureDevice.Position.back && backCamera == nil {
                    try enableAutoFocus(device: device)
                    backCamera = device
                }
                else if device.position == AVCaptureDevice.Position.front && frontCamera == nil {
                    try enableAutoFocus(device: device)
                    frontCamera = device
                }
            }
            // older iPods have no back camera
            if (cameraDir == "back") {
                if (backCamera == nil) {
                    cameraDir = "front"
                }
            } else {
                if (frontCamera == nil) {
                    cameraDir = "back"
                }
            }
            
            self.barcodeScanner = BarcodeScanner.barcodeScanner(options: BarcodeScannerOptions.init(formats: BarcodeFormat(self.formats)))
            let input: AVCaptureDeviceInput
            input = try self.createCaptureDeviceInput(cameraDirection: cameraDir)
            captureSession = AVCaptureSession()
            captureSession!.addInput(input)
            let videoOutput = AVCaptureVideoDataOutput()
            videoOutput.setSampleBufferDelegate(self as AVCaptureVideoDataOutputSampleBufferDelegate, queue: DispatchQueue(label: "capacitor barcode scanner buffer delegate", attributes: []))
            captureSession?.addOutput(videoOutput)
            captureVideoPreviewLayer = AVCaptureVideoPreviewLayer(session: captureSession!)
            cameraView.addPreviewLayer(captureVideoPreviewLayer)
            self.didRunCameraSetup = true
            if let cam = currentCamera, let _zoom = zoom {
                try cam.lockForConfiguration()
                cam.videoZoomFactor = CGFloat(_zoom);
                cam.unlockForConfiguration()
            }
            return true
        } catch CaptureError.backCameraUnavailable {
            //
        } catch CaptureError.frontCameraUnavailable {
            //
        } catch CaptureError.couldNotCaptureInput {
            //
        } catch {
            //
        }
        return false
    }

    private func createCaptureDeviceInput(cameraDirection: String? = "back") throws -> AVCaptureDeviceInput {
        var captureDevice: AVCaptureDevice
        if(cameraDirection == "back"){
            if(backCamera != nil){
                captureDevice = backCamera!
            } else {
                throw CaptureError.backCameraUnavailable
            }
        } else {
            if(frontCamera != nil){
                captureDevice = frontCamera!
            } else {
                throw CaptureError.frontCameraUnavailable
            }
        }
        currentCamera = captureDevice
        let captureDeviceInput: AVCaptureDeviceInput
        do {
            captureDeviceInput = try AVCaptureDeviceInput(device: captureDevice)
        } catch let error as NSError {
            throw CaptureError.couldNotCaptureInput(error: error)
        }
        return captureDeviceInput
    }

    private func dismantleCamera() {
        // opposite of setupCamera

        self.serialBackgroundQueue.async {
            if (self.captureSession != nil) {
                self.captureSession!.stopRunning()
                self.cameraView.removePreviewLayer()
                self.captureVideoPreviewLayer = nil
                self.metaOutput = nil
                self.captureSession = nil
                self.frontCamera = nil
                self.backCamera = nil
            }
        }

        self.isScanning = false
        self.didRunCameraSetup = false
        self.didRunCameraPrepare = false

        // If a call is saved and a scan will not run, free the saved call
        if (self.savedCall != nil && !self.shouldRunScan) {
            self.savedCall = nil
        }
    }

    private func prepare(_ call: CAPPluginCall? = nil) {
        // undo previous setup
        // because it may be prepared with a different config
        self.dismantleCamera()

        DispatchQueue.main.async {
            // setup camera with new config
            if self.setupCamera(cameraDirection: call?.getString("cameraDirection") ?? "back", zoom: call?.getFloat("zoom")) {
                // indicate this method was run
                self.didRunCameraPrepare = true

                if (self.shouldRunScan) {
                    self.scan()
                }
            } else {
                self.shouldRunScan = false
            }
        }
    }

    private func destroy() {
        self.showBackground()

        self.dismantleCamera()
    }

    private func scan() {
        if (!self.didRunCameraPrepare) {
            //In iOS 14 don't identify permissions needed, so force to ask it's better than nothing. Provisional.
            var iOS14min: Bool = false
            if #available(iOS 14.0, *) { iOS14min = true; }
            if (!self.hasCameraPermission() && !iOS14min) {
                // @TODO()
                // requestPermission()
            } else {
                DispatchQueue.main.async {
                    self.load();
                    self.shouldRunScan = true
                    self.prepare(self.savedCall)
                } 
            }
        } else {
            // JanM: this caused a crash when calling start on an already started camera
            // also doesn't seem to make sense to force prepare to run twice?
            // self.didRunCameraPrepare = false

            self.shouldRunScan = false

            var newFormats = [BarcodeFormat]();

            if ((savedCall?.options["formats"]) != nil) {
                let _formats = savedCall?.getArray("formats", String.self)

                if (_formats != nil && _formats?.count ?? 0 > 0) {
                    
                    _formats?.forEach { _format in
                        switch (_format) {
                        case "ALL":
                            newFormats.append(BarcodeFormat.all)
                            break
                        case "CODE_128":
                            newFormats.append(BarcodeFormat.code128)
                            break
                        case "CODE_39":
                            newFormats.append(BarcodeFormat.code39)
                            break
                        case "CODE_93":
                            newFormats.append(BarcodeFormat.code93)
                            break
                        case "CODA_BAR":
                            newFormats.append(BarcodeFormat.codaBar)
                            break
                        case "DATA_MATRIX":
                            newFormats.append(BarcodeFormat.dataMatrix)
                            break
                        case "EAN_13":
                            newFormats.append(BarcodeFormat.EAN13)
                            break
                        case "EAN_8":
                            newFormats.append(BarcodeFormat.EAN8)
                            break
                        case "ITF":
                            newFormats.append(BarcodeFormat.ITF)
                            break
                        case "QR_CODE":
                            newFormats.append(BarcodeFormat.qrCode)
                            break
                        case "UPC_A":
                            newFormats.append(BarcodeFormat.UPCA)
                            break
                        case "UPC_E":
                            newFormats.append(BarcodeFormat.UPCE)
                            break
                        case "PDF_417":
                            newFormats.append(BarcodeFormat.PDF417)
                            break
                        case "AZTEC":
                            newFormats.append(BarcodeFormat.aztec)
                            break
                        default:
                            // do nothing
                            break
                            
                        }
                    }
                }

            }

            if (newFormats.count == 0) {
                newFormats.append(BarcodeFormat.all)
            }
            
            self.formats = newFormats;
            
            self.serialBackgroundQueue.async {
                if (self.captureSession != nil) {
                    self.captureSession!.startRunning()
                }
                
            }
            

            self.hideBackground()

            self.isScanning = true
        }
    }

    private func hideBackground() {
        DispatchQueue.main.async {
            self.previousBackgroundColor = self.bridge?.webView!.backgroundColor
            self.bridge?.webView!.isOpaque = false
            self.bridge?.webView!.backgroundColor = UIColor.clear
            self.bridge?.webView!.scrollView.backgroundColor = UIColor.clear

            let javascript = "document.documentElement.style.backgroundColor = 'transparent';"

            self.bridge?.webView!.evaluateJavaScript(javascript)
        }
    }

    private func showBackground() {
        DispatchQueue.main.async {
            let javascript = "document.documentElement.style.backgroundColor = '';"

            self.bridge?.webView!.evaluateJavaScript(javascript) { (result, error) in
                self.bridge?.webView!.isOpaque = true
                self.bridge?.webView!.backgroundColor = self.previousBackgroundColor
                self.bridge?.webView!.scrollView.backgroundColor = self.previousBackgroundColor
            }
        }
    }

    public func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
 
        switch UIDevice.current.orientation {
        case .portrait,.unknown,.faceUp:
            // print("portrait")
            connection.videoOrientation = AVCaptureVideoOrientation.portrait
            break
        case .portraitUpsideDown,.faceDown:
            // print("portraitUpsideDown")
            connection.videoOrientation = AVCaptureVideoOrientation.portraitUpsideDown
            break
        case .landscapeLeft:
            // print("landscapeLeft")
            connection.videoOrientation = AVCaptureVideoOrientation.landscapeLeft
            break
        case .landscapeRight:
            // print("landscapeRight")
            connection.videoOrientation = AVCaptureVideoOrientation.landscapeRight
            break
        default:
            // print("do not set this")
            connection.videoOrientation = AVCaptureVideoOrientation.portrait            
        }
        
        if let barcodeScanner = self.barcodeScanner {
            
            let visionImage = VisionImage(buffer: sampleBuffer)
            barcodeScanner.process(visionImage) { features, error in
                guard error == nil, let features = features, !features.isEmpty else {
                    // print(error)
                    return
                }
                // Recognized barcodes
                for barcode in features {
                    if (!self.scanningPaused) {
                        var jsObject = PluginCallResultData()
                        jsObject["content"] = barcode.rawValue
                        jsObject["format"] = barcode.format.rawValue
                        jsObject["valueType"] = barcode.valueType
                        
                        var cornerPoints = [[Int]]()
                        for cp in barcode.cornerPoints! {
                            var value = [Int]()
                            value.append(Int(Float(cp.cgPointValue.x) / Float(UIScreen.main.scale)))
                            value.append(Int(Float(cp.cgPointValue.y) / Float(UIScreen.main.scale)))
                            cornerPoints.append(value)
                        }
                        jsObject["cornerPoints"] = cornerPoints
                        
                        if (self.savedCall != nil) {
                            // print("------------------------", cornerPoints[0])
                            self.savedCall!.resolve(jsObject)
                        }
                        
                    }
                }
            }
        }
    }


    @objc func start(_ call: CAPPluginCall) {
        self.savedCall = call
        call.keepAlive = true;
        scanningPaused = false
        self.scan()
    }

    @objc func pause(_ call: CAPPluginCall) {
        scanningPaused = true
        call.resolve()
    }


    @objc func resume(_ call: CAPPluginCall) {
        scanningPaused = false
        call.resolve()
    }

    @objc func stop(_ call: CAPPluginCall) {
        if (self.savedCall != nil) {
            savedCall = nil
        }

        self.destroy()
        call.resolve()
    }
    
    @objc func vibrate(_ call: CAPPluginCall) {
        let generator = UIImpactFeedbackGenerator(style: .heavy)
        generator.impactOccurred()
    }
    


    @objc func checkPermission(_ call: CAPPluginCall) {
        let force = call.getBool("force") ?? false

        var savedReturnObject = PluginCallResultData()

        DispatchQueue.main.async {
            switch AVCaptureDevice.authorizationStatus(for: .video) {
                case .authorized:
                    savedReturnObject["granted"] = true
                case .denied:
                    savedReturnObject["denied"] = true
                case .notDetermined:
                    savedReturnObject["neverAsked"] = true
                case .restricted:
                    savedReturnObject["restricted"] = true
                @unknown default:
                    savedReturnObject["unknown"] = true
            }

            if (force && savedReturnObject["neverAsked"] != nil) {
                savedReturnObject["asked"] = true

                AVCaptureDevice.requestAccess(for: .video) { (authorized) in
                    if (authorized) {
                        savedReturnObject["granted"] = true
                    } else {
                        savedReturnObject["denied"] = true
                    }
                    call.resolve(savedReturnObject)
                }
            } else {
                call.resolve(savedReturnObject)
            }
        }
    }

    @objc func openAppSettings(_ call: CAPPluginCall) {
      guard let settingsUrl = URL(string: UIApplication.openSettingsURLString) else {
          return
      }

      DispatchQueue.main.async {
          if UIApplication.shared.canOpenURL(settingsUrl) {
              UIApplication.shared.open(settingsUrl, completionHandler: { (success) in
                  call.resolve()
              })
          }
      }
    }

      @objc func enableTorch(_ call: CAPPluginCall) {
        guard let device = currentCamera else { return }
        guard device.hasTorch else { return }
        guard device.isTorchAvailable else { return }

        do {
            try device.lockForConfiguration()

            do {
                try device.setTorchModeOn(level: 1.0)
            } catch {
                print(error)
            }

            device.unlockForConfiguration()
        } catch {
            print(error)
        }

        call.resolve()
    }

    @objc func disableTorch(_ call: CAPPluginCall) {
        guard let device = currentCamera else { return }
        guard device.hasTorch else { return }
        guard device.isTorchAvailable else { return }

        do {
            try device.lockForConfiguration()
            device.torchMode = .off

            device.unlockForConfiguration()
        } catch {
            print(error)
        }

        call.resolve()
    }

    @objc func toggleTorch(_ call: CAPPluginCall) {
        guard let device = currentCamera else { return }
        guard device.hasTorch else { return }
        guard device.isTorchAvailable else { return }

        if (device.torchMode == .on) {
            self.disableTorch(call)
        } else {
            self.enableTorch(call)
        }
    }

    @objc func getTorchState(_ call: CAPPluginCall) {
        guard let device = currentCamera else { return }

        var result = PluginCallResultData()

        result["isEnabled"] = device.torchMode == .on

        call.resolve(result)
    }
    
    @objc override public func checkPermissions(_ call: CAPPluginCall) {
           var result: [String: Any] = [:]
           for permission in CameraPermissionType.allCases {
               let state: String
               switch permission {
               case .camera:
                   state = AVCaptureDevice.authorizationStatus(for: .video).authorizationState
//               case .photos:
//                   if #available(iOS 14, *) {
//                       state = PHPhotoLibrary.authorizationStatus(for: .readWrite).authorizationState
//                   } else {
//                       state = PHPhotoLibrary.authorizationStatus().authorizationState
//                   }
               }
               result[permission.rawValue] = state
           }
           call.resolve(result)
       }

       @objc override public func requestPermissions(_ call: CAPPluginCall) {
           // get the list of desired types, if passed
           let typeList = call.getArray("permissions", String.self)?.compactMap({ (type) -> CameraPermissionType? in
               return CameraPermissionType(rawValue: type)
           }) ?? []
           // otherwise check everything
           let permissions: [CameraPermissionType] = (typeList.count > 0) ? typeList : CameraPermissionType.allCases
           // request the permissions
           let group = DispatchGroup()
           for permission in permissions {
               switch permission {
               case .camera:
                   group.enter()
                   AVCaptureDevice.requestAccess(for: .video) { _ in
                       group.leave()
                   }
//               case .photos:
//                   group.enter()
//                   if #available(iOS 14, *) {
//                       PHPhotoLibrary.requestAuthorization(for: .readWrite) { (_) in
//                           group.leave()
//                       }
//                   } else {
//                       PHPhotoLibrary.requestAuthorization({ (_) in
//                           group.leave()
//                       })
//                   }
               }
           }
           group.notify(queue: DispatchQueue.main) { [weak self] in
               self?.checkPermissions(call)
           }
       }

    @objc public func getZoomState(_ call: CAPPluginCall) {
        guard let device = currentCamera else { return }

        var result: [String : Any] = [
            "zoom": device.videoZoomFactor,
            "minimum": device.minAvailableVideoZoomFactor,
            "maximum": device.maxAvailableVideoZoomFactor,
        ]
        if (device.virtualDeviceSwitchOverVideoZoomFactors.count > 0) {
            result["switchOver"] = device.virtualDeviceSwitchOverVideoZoomFactors.map { CGFloat(truncating: $0)  }
        }
        
        call.resolve(result)
    }
    
    @objc public func setZoom(_ call: CAPPluginCall) {
        guard let device = currentCamera else { return }
        
        let zoom = CGFloat(call.getFloat("zoom", 1))
        do {
            try device.lockForConfiguration()
            device.ramp(toVideoZoomFactor: zoom, withRate: 4.0)
            device.unlockForConfiguration()
        } catch {
            print(error)
        }
        
        return getZoomState(call)
    }
}
