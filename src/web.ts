import { WebPlugin } from '@capacitor/core';

import type {
  BarcodeScannerPlugin,
  ScanOptions,
  ScanResult,
  CheckPermissionOptions,
  CheckPermissionResult,
} from './definitions';

export class BarcodeScannerWeb
  extends WebPlugin
  implements BarcodeScannerPlugin {
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
