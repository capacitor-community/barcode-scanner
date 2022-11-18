/* eslint-disable @typescript-eslint/no-unused-vars */
import { WebPlugin } from '@capacitor/core';

import type {
  BarcodeScannerPlugin,
  CameraPermissionState,
  ScanOptions,
  ScanResult,
  TorchStateResult,
} from './definitions';

export class BarcodeScannerWeb
  extends WebPlugin
  implements BarcodeScannerPlugin {
  async start(
    _options: ScanOptions,
    _callback: (result: ScanResult, err?: any) => void,
  ): Promise<string> {
    throw this.unimplemented('Not implemented on web.');
  }

  async pause(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async resume(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async stop(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async checkPermissions(): Promise<CameraPermissionState> {
    throw this.unimplemented('Not implemented on web.');
  }

  async requestPermissions(): Promise<CameraPermissionState> {
    throw this.unimplemented('Not implemented on web.');
  }

  async enableTorch(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async disableTorch(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async toggleTorch(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async getTorchState(): Promise<TorchStateResult> {
    throw this.unimplemented('Not implemented on web.');
  }
}
