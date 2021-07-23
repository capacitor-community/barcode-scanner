import { WebPlugin } from '@capacitor/core';
import type { BarcodeScannerPlugin, ScanOptions, ScanResult, CheckPermissionOptions, CheckPermissionResult, StopScanOptions } from './definitions';
export declare class BarcodeScannerWeb extends WebPlugin implements BarcodeScannerPlugin {
    prepare(): Promise<void>;
    hideBackground(): Promise<void>;
    showBackground(): Promise<void>;
    startScan(_options: ScanOptions): Promise<ScanResult>;
    stopScan(_options?: StopScanOptions): Promise<void>;
    checkPermission(_options: CheckPermissionOptions): Promise<CheckPermissionResult>;
    openAppSettings(): Promise<void>;
}
