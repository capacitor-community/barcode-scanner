#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

CAP_PLUGIN(BarcodeScanner, "BarcodeScanner",
    CAP_PLUGIN_METHOD(prepare, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(hideBackground, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(showBackground, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(startScan, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(startScanning, CAPPluginReturnCallback);
    CAP_PLUGIN_METHOD(stopScan, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(checkPermission, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(openAppSettings, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(pauseScanning, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(resumeScanning, CAPPluginReturnPromise);
)
