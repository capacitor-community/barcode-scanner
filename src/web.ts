import { WebPlugin } from '@capacitor/core';

import type {
  BarcodeScannerPlugin,
  CheckPermissionOptions,
  CheckPermissionResult,
  ScanOptions,
  ScanResult,
  StopScanOptions,
} from './definitions';

export class BarcodeScannerWeb
  extends WebPlugin
  implements BarcodeScannerPlugin {
  async prepare(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async hideBackground(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async showBackground(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async enableTorch(): Promise<void> {
    throw new Error('method not available in web');
  }

  async disableTorch(): Promise<void> {
    throw new Error('method not available in web');
  }

  async startScan(_options: ScanOptions): Promise<ScanResult> {
    throw this.unimplemented('Not implemented on web.');
  }

  async stopScan(_options?: StopScanOptions): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async checkPermission(
    _options: CheckPermissionOptions,
  ): Promise<CheckPermissionResult> {
    throw this.unimplemented('Not implemented on web.');
  }

  async openAppSettings(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }
}
