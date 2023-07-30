/* eslint-disable @typescript-eslint/no-unused-vars */
import { WebPlugin } from '@capacitor/core';
import { BarcodeDetectorPolyfill } from '@undecaf/barcode-detector-polyfill';
import { CameraDirection } from './definitions';

import type {
  BarcodeFormat,
  BarcodeScannerPlugin,
  PermissionStates,
  ScanOptions,
  ScanResult,
  TorchStateResult,
  ZoomOptions,
  ZoomStateResult,
} from './definitions';


export class BarcodeScannerWeb extends WebPlugin implements BarcodeScannerPlugin {
  private static _FORWARD = { facingMode: 'user' };
  private static _BACK = { facingMode: 'environment' };
  //private _controls: IScannerControls | null = null;
  //private _torchState = false;
  private _video: HTMLVideoElement | null = null;
  private _options: ScanOptions | null = null;
  private _facingMode: MediaTrackConstraints = BarcodeScannerWeb._BACK;

  async start(_options: ScanOptions, _callback: (result?: ScanResult, err?: any) => void): Promise<string> {
    this._options = _options;

    if (!!_options?.cameraDirection) {
      this._facingMode = _options.cameraDirection === CameraDirection.BACK ? BarcodeScannerWeb._BACK : BarcodeScannerWeb._FORWARD;
    }

    const detector = this._getDetector();
    const video = await this._getVideoElement();

    if (!video) {
      throw this.unavailable('Missing video element');
    }
    while (true) {
      await new Promise(requestAnimationFrame) // TODO: This will suspend when app is in background, is that what we want?

      try {
        const detected = await detector.detect(video);

        if (!this._video) break; // TODO: When scanning was stopped should it really throw last scan result?
        if (detected.length === 0) continue;

        for (const barcode of detected) {
          _callback({
            format: barcode.format as BarcodeFormat,
            content: barcode.rawValue,
            contentType: '', // TODO: What should this be? Cannot find any documentation on it.
            cornerPoints: [
              [barcode.cornerPoints[0].x, barcode.cornerPoints[0].y],
              [barcode.cornerPoints[1].x, barcode.cornerPoints[1].y],
              [barcode.cornerPoints[2].x, barcode.cornerPoints[2].y],
              [barcode.cornerPoints[3].x, barcode.cornerPoints[3].y]
            ],
          });
        }
      }
      catch (e) {
        _callback(undefined, e);
      }

      await new Promise(resolve => setTimeout(resolve, 100)); // Nobody needs to scan more often, so do not waste resources
    }

    return ''; // TODO: What should this return?
  }

  async pause(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async resume(): Promise<void> {
    throw this.unimplemented('Not implemented on web.');
  }

  async stop(): Promise<void> {
    return this._stop();
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

  private _getDetector(): BarcodeDetectorPolyfill {
    const options = {
      formats: this._options!.formats?.map((format) => format.toLowerCase()),
    }
    try {
      (window as any).BarcodeDetector.getSupportedFormats();
      return new (window as any).BarcodeDetector(options);
    } catch {
      BarcodeDetectorPolyfill.getSupportedFormats()
      return new BarcodeDetectorPolyfill(options);
    }
  }

  private async _getVideoElement() {
    if (!this._video) {
      await this._startVideo();
    }
    return this._video;
  }

  private async _startVideo(): Promise<{}> {
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

      const video = document.getElementById('barcodeScannerVideo');

      if (!video) {
        const parent = document.createElement('div');
        parent.id = 'barcodeScannerWrapper';
        parent.setAttribute(
          'style',
          'position:absolute; top: 0; left: 0; width:100%; height: 100%; background-color: black;'
        );
        this._video = document.createElement('video');
        this._video.id = 'barcodeScannerVideo';
        // Don't flip video feed if camera is rear facing
        if (this._options?.cameraDirection !== CameraDirection.BACK) {
          this._video.setAttribute(
            'style',
            '-webkit-transform: scaleX(-1); transform: scaleX(-1); width:100%; height: 100%;'
          );
        } else {
          this._video.setAttribute('style', 'width:100%; height: 100%; object-fit: cover;');
        }

        const userAgent = navigator.userAgent.toLowerCase();
        const isSafari = userAgent.includes('safari') && !userAgent.includes('chrome');

        // Safari on iOS needs to have the autoplay, muted and playsinline attributes set for video.play() to be successful
        // Without these attributes this.video.play() will throw a NotAllowedError
        // https://developer.apple.com/documentation/webkit/delivering_video_content_for_safari
        if (isSafari) {
          this._video.setAttribute('autoplay', 'true');
          this._video.setAttribute('muted', 'true');
          this._video.setAttribute('playsinline', 'true');
        }

        parent.appendChild(this._video);
        document.body.appendChild(parent);

        if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
          const constraints: MediaStreamConstraints = {
            video: this._facingMode,
          };

          navigator.mediaDevices.getUserMedia(constraints).then(
            (stream) => {
              //video.src = window.URL.createObjectURL(stream);
              if (this._video) {
                this._video.srcObject = stream;
                this._video.play();
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

  private async _stop(): Promise<any> {
    if (this._video) {
      this._video.pause();

      const st: any = this._video.srcObject;
      const tracks = st.getTracks();

      for (var i = 0; i < tracks.length; i++) {
        var track = tracks[i];
        track.stop();
      }
      this._video.parentElement?.remove();
      this._video = null;
    }
  }
}
