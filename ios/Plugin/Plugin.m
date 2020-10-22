#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

CAP_PLUGIN(CodeScanner, "CodeScanner",
    CAP_PLUGIN_METHOD(prepare, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(hideBackground, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(showBackground, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(startScan, CAPPluginReturnPromise);
    CAP_PLUGIN_METHOD(stopScan, CAPPluginReturnPromise);
)
