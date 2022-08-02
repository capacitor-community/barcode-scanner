#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

CAP_PLUGIN(CapacitorCommunityBarcodeScanner, "CapacitorCommunityBarcodeScanner",
    // CAP_PLUGIN_METHOD(prepare, CAPPluginReturnPromise);
    //CAP_PLUGIN_METHOD(hideBackground, CAPPluginReturnPromise);
    // CAP_PLUGIN_METHOD(showBackground, CAPPluginReturnPromise);
    // CAP_PLUGIN_METHOD(startScan, CAPPluginReturnPromise);    
    CAP_PLUGIN_METHOD(start, CAPPluginReturnCallback);
    CAP_PLUGIN_METHOD(stop, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(pause, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(resume, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(checkPermissions, CAPPluginReturnPromise);
    // CAP_PLUGIN_METHOD(openAppSettings, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(enableTorch, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(disableTorch, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(toggleTorch, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(getTorchState, CAPPluginReturnPromise);
)
