
import type { PermissionState } from '@capacitor/core';


export type CameraPermissionState = PermissionState;
export interface BarcodeScannerPlugin {
  // TODO: I am not sure if this will make sense anymore in the ML Kit version
  // prepare(options?: ScanOptions): Promise<void>;

  // TODO: should be handled internally by the plugin and not be exposed to the user
  // hideBackground(): Promise<void>;
  // showBackground(): Promise<void>;

  // related to scanning
  /**
   * Start scanning for barcodes
   *
   * @since 3.0.0
   */
  start(
    options?: ScanOptions,
    callback?: (result: ScanResult, err?: any) => void,
  ): Promise<CallbackID>;

  /**
   * Pause scanning for barcodes
   *
   * @since 3.0.0
   */
  pause(): Promise<void>;

  /**
   * Resume paused scanning for barcodes
   *
   * @since 3.0.0
   */
  resume(): Promise<void>;

  /**
   * Stop scanning for barcodes
   *
   * @since 3.0.0
   */
  stop(): Promise<void>;

  // related to permissions
  /**
   * Check camera permissions
   *
   * @since 3.0.0
   */
  checkPermissions(): Promise<CameraPermissionState>;

  /**
   * Request camera permissions
   *
   * @since 3.0.0
   */
  requestPermissions(): Promise<CameraPermissionState>;

  // TODO: is this required for anything?
  // openAppSettings(): Promise<void>;

  // related to torch
  /**
   * Enables torch
   *
   * @since 3.0.0
   */
  enableTorch(): Promise<void>;

  /**
   * Disables torch
   *
   * @since 3.0.0
   */
  disableTorch(): Promise<void>;

  /**
   * Toggles torch
   *
   * @since 3.0.0
   */
  toggleTorch(): Promise<void>;

  /**
   * Get current torch state
   *
   * @since 3.0.0
   */
  getTorchState(): Promise<TorchStateResult>;
}

export type CallbackID = string;

export enum BarcodeFormat {
  UNKNOWN = 0,
  ALL = 0xffff,
  CODE_128 = 0x0001,
  CODE_39 = 0x0002,
  CODE_93 = 0x0004,
  CODA_BAR = 0x0008,
  DATA_MATRIX = 0x0010,
  EAN_13 = 0x0020,
  EAN_8 = 0x0040,
  ITF = 0x0080,
  QR_CODE = 0x0100,
  UPC_A = 0x0200,
  UPC_E = 0x0400,
  PDF_417 = 0x0800,
  AZTEC = 0x1000,
}

// TODO: Maybe it would make sense to rename this to CameraType and allow additional settings like "WIDE_ANGLE_CAMERA"?
export enum CameraDirection {
  FRONT = 'front',
  BACK = 'back',
}

export interface ScanOptions {
  /**
   * This parameter can be used to make the scanner only recognize a specific type of barcode.
   *  If `format` is not specified or left empty, all types of barcodes will be targeted.
   *
   * @since 3.0.0
   */
  format?: BarcodeFormat;
  /**
   * This parameter can be used to set the camera direction.
   *
   * @since 3.0.0
   */
  cameraDirection?: CameraDirection;
}

export interface ScanResult {
  /**
   * Content of the barcode
   *
   * @since 3.0.0
   */
  content: string;

  /**
   * Format of the scanned barcode
   *
   * @since 3.0.0
   */
  format: BarcodeFormat;

  /**
   * Position of the scanned barcode
   *
   * @since 3.0.0
   */
  position: {
    x: number;
    y: number;
    width: number;
    height: number;
  };
}

export interface TorchStateResult {
  /**
   * Whether or not the torch is currently enabled.
   *
   * @since 3.0.0
   */
  isEnabled: boolean;
}
