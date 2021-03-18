import { WebPlugin } from '@capacitor/core';
import { BarcodeScannerPlugin, CheckPermissionOptions, CheckPermissionResult, ScanOptions, ScanResult } from './definitions';
export declare class BarcodeScannerWeb extends WebPlugin implements BarcodeScannerPlugin {
    constructor();
    prepare(): Promise<void>;
    hideBackground(): Promise<void>;
    showBackground(): Promise<void>;
    enableTorch(): Promise<void>;
    disableTorch(): Promise<void>;
    startScan(_options: ScanOptions): Promise<ScanResult>;
    stopScan(): Promise<void>;
    checkPermission(_options: CheckPermissionOptions): Promise<CheckPermissionResult>;
    openAppSettings(): Promise<void>;
}
declare const BarcodeScanner: BarcodeScannerWeb;
export { BarcodeScanner };
