import { registerWebPlugin, WebPlugin } from '@capacitor/core';
import {
  BarcodeScannerPlugin,


  CheckPermissionOptions,
  CheckPermissionResult, ScanOptions,
  ScanResult
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

  async enableTorch(): Promise<void> {
    throw new Error('method not available in web');
  }

  async disableTorch(): Promise<void> {
    throw new Error('method not available in web');
  }

  async startScan(_options: ScanOptions): Promise<ScanResult> {
    throw new Error('method not available in web');
  }

  async stopScan(): Promise<void> {
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

registerWebPlugin(BarcodeScanner);
