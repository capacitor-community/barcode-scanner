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
  CODE_128 = 'CODE_128',
  CODE_39 = 'CODE_39',
  CODE_93 = 'CODE_93',
  CODA_BAR = 'CODA_BAR',
  DATA_MATRIX = 'DATA_MATRIX',
  EAN_13 = 'EAN_13',
  EAN_8 = 'EAN_8',
  ITF = 'ITF',
  QR_CODE = 'QR_CODE',
  UPC_A = 'UPC_A',
  UPC_E = 'UPC_E',
  PDF_417 = 'PDF_417',
  AZTEC = 'AZTEC',
}

// TODO: Maybe it would make sense to rename this to CameraType and allow additional settings like "WIDE_ANGLE_CAMERA"?
export enum CameraDirection {
  FRONT = 'front',
  BACK = 'back',
}

export interface ScanOptions {
  /**
   * This parameter can be used to make the scanner only recognize specific types of barcodes.
   *  If `targetedFormats` is _not specified_ or _left empty_, _all types_ of barcodes will be targeted.
   *
   * @since 3.0.0
   */
  formats?: BarcodeFormat[];
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
