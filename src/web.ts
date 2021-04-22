import { WebPlugin } from '@capacitor/core';
import {
  BarcodeScannerPlugin,
  ScanOptions,
  ScanResult,
  CheckPermissionOptions,
  CheckPermissionResult,
  StopScanOptions,
} from './definitions';

export class BarcodeScannerWeb
  extends WebPlugin
  implements BarcodeScannerPlugin {
  constructor() {
    super({
      name: 'BarcodeScanner',
      platforms: ['web'],
    });
  }

  async prepare(): Promise<void> {
    throw new Error('method not available in web');
  }

  async hideBackground(): Promise<void> {
    throw new Error('method not available in web');
  }

  async showBackground(): Promise<void> {
    throw new Error('method not available in web');
  }

  async startScan(_options: ScanOptions): Promise<ScanResult> {
    throw new Error('method not available in web');
  }

  async stopScan(_options: StopScanOptions): Promise<void> {
    throw new Error('method not available in web');
  }

  async checkPermission(
    _options: CheckPermissionOptions,
  ): Promise<CheckPermissionResult> {
    throw new Error('method not available in web');
  }

  async openAppSettings(): Promise<void> {
    throw new Error('method not available in web');
  }
}

const BarcodeScanner = new BarcodeScannerWeb();

export { BarcodeScanner };

import { registerWebPlugin } from '@capacitor/core';
registerWebPlugin(BarcodeScanner);
