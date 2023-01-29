import { WebPlugin } from '@capacitor/core';
import { BarcodeFormat, BrowserQRCodeReader, IScannerControls } from '@zxing/browser';
import { DecodeHintType } from '@zxing/library';

import {
  BarcodeScannerPlugin,
  ScanOptions,
  ScanResult,
  CheckPermissionOptions,
  CheckPermissionResult,
  StopScanOptions,
  TorchStateResult,
  CameraDirection,
} from './definitions';

export class BarcodeScannerWeb extends WebPlugin implements BarcodeScannerPlugin {
  private formats: number[] = [];
  private controls: IScannerControls | null = null;
  private torchState = false;
  private video: HTMLVideoElement | null = null;
  private options: ScanOptions | null = null;

  async prepare(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async hideBackground(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async showBackground(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async startScan(_options: ScanOptions): Promise<ScanResult> {
    this.options = _options;
    this.formats = [];
    _options.targetedFormats?.forEach((format) => {
      const formatIndex = Object.keys(BarcodeFormat).indexOf(format);
      if (formatIndex >= 0) {
        this.formats.push(0);
      } else {
        console.log(format + ' not supported on web');
      }
    });
    const video = await this.getVideoElement();
    if (video) {
      return this.getFirstResultFromReader();
    } else {
      throw this.unavailable('Missing video element');
    }
  }

  async startScanning(_options: ScanOptions, _callback: any): Promise<string> {
    throw this.unimplemented('Not implemented on web.');
  }

  async pauseScanning(): Promise<void> {
    if (this.controls) {
      this.controls.stop();
      this.controls = null;
    }
  }

  async resumeScanning(): Promise<void> {
    this.getFirstResultFromReader();
  }

  async stopScan(_options?: StopScanOptions): Promise<void> {
    this.stop();
    if (this.controls) {
      this.controls.stop();
      this.controls = null;
    }
  }

  async checkPermission(_options: CheckPermissionOptions): Promise<CheckPermissionResult> {
    throw this.unimplemented('Not implemented on web.');
  }

  async openAppSettings(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async disableTorch(): Promise<void> {
    if (this.controls && this.controls.switchTorch) {
      this.controls.switchTorch(false);
      this.torchState = false;
    }
  }

  async enableTorch(): Promise<void> {
    if (this.controls && this.controls.switchTorch) {
      this.controls.switchTorch(true);
      this.torchState = true;
    }
  }

  async toggleTorch(): Promise<void> {
    if (this.controls && this.controls.switchTorch) {
      this.controls.switchTorch(true);
    }
  }

  async getTorchState(): Promise<TorchStateResult> {
    return { isEnabled: this.torchState };
  }

  private async getVideoElement() {
    if (!this.video) {
      await this.startVideo();
    }
    return this.video;
  }

  private async getFirstResultFromReader() {
    const videoElement = await this.getVideoElement();
    return new Promise<ScanResult>(async (resolve) => {
      if (videoElement) {
        let hints;
        if (this.formats.length) {
          hints = new Map();
          hints.set(DecodeHintType.POSSIBLE_FORMATS, this.formats);
        }
        const reader = new BrowserQRCodeReader(hints);
        this.controls = await reader.decodeFromVideoElement(videoElement, (result, error, controls) => {
          if (!error && result) {
            resolve({
              hasContent: !!result.getText(),
              content: result.getText(),
              format: result.getBarcodeFormat().toString(),
            });
            controls.stop();
            this.controls = null;
            this.stop();
          }
          if (error && error.message) {
            console.error(error.message);
          }
        });
      }
    });
  }

  private async startVideo(): Promise<{}> {
    return new Promise(async (resolve, reject) => {
      await navigator.mediaDevices
        .getUserMedia({
          audio: false,
          video: true,
        })
        .then((stream: MediaStream) => {
          // Stop any existing stream so we can request media with different constraints based on user input
          stream.getTracks().forEach((track) => track.stop());
        })
        .catch((error) => {
          reject(error);
        });

      const body = document.body;
      const video = document.getElementById('video');

      if (!video) {
        const parent = document.createElement('div');
        parent.setAttribute(
          'style',
          'position:absolute; top: 0; left: 0; width:100%; height: 100%; background-color: black;'
        );
        this.video = document.createElement('video');
        this.video.id = 'video';
        // Don't flip video feed if camera is rear facing
        if (this.options?.cameraDirection !== CameraDirection.BACK) {
          this.video.setAttribute(
            'style',
            '-webkit-transform: scaleX(-1); transform: scaleX(-1); width:100%; height: 100%;'
          );
        } else {
          this.video.setAttribute('style', 'width:100%; height: 100%;');
        }

        const userAgent = navigator.userAgent.toLowerCase();
        const isSafari = userAgent.includes('safari') && !userAgent.includes('chrome');

        // Safari on iOS needs to have the autoplay, muted and playsinline attributes set for video.play() to be successful
        // Without these attributes this.video.play() will throw a NotAllowedError
        // https://developer.apple.com/documentation/webkit/delivering_video_content_for_safari
        if (isSafari) {
          this.video.setAttribute('autoplay', 'true');
          this.video.setAttribute('muted', 'true');
          this.video.setAttribute('playsinline', 'true');
        }

        parent.appendChild(this.video);
        body.appendChild(parent);

        if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
          const constraints: MediaStreamConstraints = {
            video: {},
          };

          navigator.mediaDevices.getUserMedia(constraints).then(
            (stream) => {
              //video.src = window.URL.createObjectURL(stream);
              if (this.video) {
                this.video.srcObject = stream;
                this.video.play();
              }
              resolve({});
            },
            (err) => {
              reject(err);
            }
          );
        }
      } else {
        reject({ message: 'camera already started' });
      }
    });
  }
  private async stop(): Promise<any> {
    if (this.video) {
      this.video.pause();

      const st: any = this.video.srcObject;
      const tracks = st.getTracks();

      for (var i = 0; i < tracks.length; i++) {
        var track = tracks[i];
        track.stop();
      }
      this.video.parentElement?.remove();
      this.video = null;
    }
  }
}
