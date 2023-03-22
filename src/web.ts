/* eslint-disable @typescript-eslint/no-unused-vars */
import { WebPlugin } from '@capacitor/core';

import type {
  BarcodeScannerPlugin,
  PermissionStates,
  ScanOptions,
  ScanResult,
  TorchStateResult,
  ZoomOptions,
  ZoomStateResult,
} from './definitions';

export class BarcodeScannerWeb extends WebPlugin implements BarcodeScannerPlugin {
  async start(_options: ScanOptions, _callback: (result: ScanResult, err?: any) => void): Promise<string> {
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

  async checkPermissions(): Promise<PermissionStates> {
    throw this.unimplemented('Not implemented on web.');
  }

  async requestPermissions(): Promise<PermissionStates> {
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

  async vibrate(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async getZoomState(): Promise<ZoomStateResult> {
    throw this.unimplemented('Not implemented on web.');
  }

  async setZoom(_zoomOptions: ZoomOptions): Promise<ZoomStateResult> {
    throw this.unimplemented('Not implemented on web.');
  }
}
