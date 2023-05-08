<p align="center"><br><img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" /></p>
<h3 align="center">Barcode Scanner</h3>
<p align="center"><strong><code>@capacitor-community/barcode-scanner</code></strong></p>
<p align="center">
  A fast and efficient (QR) barcode scanner for Capacitor based on ML-Kit.
</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2022?style=flat-square" />
  <a href="https://www.npmjs.com/package/@capacitor-community/barcode-scanner"><img src="https://img.shields.io/npm/l/@capacitor-community/barcode-scanner?style=flat-square" /></a>
<br>
  <a href="https://www.npmjs.com/package/@capacitor-community/barcode-scanner"><img src="https://img.shields.io/npm/dw/@capacitor-community/barcode-scanner?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/barcode-scanner"><img src="https://img.shields.io/npm/v/@capacitor-community/barcode-scanner?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
<a href="#contributors-"><img src="https://img.shields.io/badge/all%20contributors-1-orange?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:END -->
</p>

## Table of Contents

- [Maintainers](#maintainers)
- [About](#about)
- [Installation](#installation)
- [Usage](#usage)
- [Troubleshooting](#troubleshooting)

## Maintainers

| Maintainer | GitHub                                | Active |
| ---------- | ------------------------------------- | ------ |
| thegnuu    | [thegnuu](https://github.com/thegnuu) | yes    |
| tafelnl    | [tafelnl](https://github.com/tafelnl) | no     |

## About

### Supported barcodes

This libaray makes use of ML-Kit. This means **[this list of barcodes](https://developers.google.com/ml-kit/vision/barcode-scanning)** should be supported.


### Note on supported Capacitor versions

`v5.x` supports Capacitor `v5.x` (not based on ML-Kit)

`v4.x` supports Capacitor `v4.x`  (not based on ML-Kit)

`v3.x` supports Capacitor `v4.x`  (not based on ML-Kit)

`v2.x` supports Capacitor `v3.x`  (not based on ML-Kit)

`v1.x` supports Capacitor `v2.x`  (not based on ML-Kit)

All releases of this package can be found on [npm](https://www.npmjs.com/package/@capacitor-community/barcode-scanner?activeTab=versions) and on [GitHub Releases](https://github.com/capacitor-community/barcode-scanner/releases)

## Installation

```bash
npm install @capacitor-community/barcode-scanner
npx cap sync
```

### iOS

For iOS you need to set a usage description in your info.plist file.

This can be done by either adding it to the Source Code directly or by using Xcode Property List inspector.

**Adding it to the source code directly**

1. Open up the Info.plist (in Xcode right-click > Open As > Source Code)
2. With `<dict></dict>` change the following

```diff
<dict>
+  <key>NSCameraUsageDescription</key>
+  <string>To be able to scan barcodes</string>
</dict>
```

_NOTE:_ "To be able to scan barcodes" can be substituted for anything you like.

**Adding it by using Xcode Property List inspector**

1. Open up the Info.plist **in Xcode** (right-click > Open As > Property List)
2. Next to "Information Property List" click on the tiny `+` button.
3. Under `key`, type "Privacy - Camera Usage Description"
4. Under `value`, type "To be able to scan barcodes"

_NOTE:_ "To be able to scan barcodes" can be substituted for anything you like.

More info here: https://developer.apple.com/documentation/bundleresources/information_property_list/nscamerausagedescription

### Android

Within your `AndroidManifest.xml` file, change the following:

```diff
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

+  <uses-permission android:name="android.permission.CAMERA" />
+  <uses-permission android:name="android.permission.VIBRATE" />

</manifest>
```

## Usage

The complete API can be found [here](./src/definitions.ts).

Scanning a (QR) barcode can be as simple as:

```js
import { BarcodeScanner } from '@capacitor-community/barcode-scanner';

export class BarcodeExample {
  public async startScan() {
  await BarcodeScanner.requestPermission();
  BarcodeScanner.start({}, (result) => {
    console.log('barcode scanned', result);
  });
};
}


```

### Opacity of the WebView

The camera view will always be rendered behind the webview. You need to make sure that your viewport is transparent to see the camera.

If you are using Ionic you need to set some css variables as well, check [**here**](#ionic-css-variables)

If you still cannot see the camera view, check [**here**](#the-scanner-view-does-not-show-up)

### Permissions

This part of the readme will soon be updated.

## Troubleshooting

### Ionic CSS variables

Ionic will add additional CSS variables which will prevent the scanner from showing up.
To fix this issue add the following snippet at the end of your global css.

```css
body.scanner-active {
  --background: transparent;
  --ion-background-color: transparent;
}
```

Once this is done, you need to add this class to the body before using the scanner.

```typescript
document.querySelector('body').classList.add('scanner-active');
```

After your done with your scanning work, you can simply remove this class.

```typescript
document.querySelector('body').classList.remove('scanner-active');
```

### I have a `Error: Plugin BarcodeScanner does not respond to method call` error message on iOS

In Xcode click on `Product` > `Clean Build Folder` and try to build again.

### I have a `Cannot resolve symbol BarcodeScanner` error message in Android Studio

In Android Studio click `File` > `Sync Project with Gradle Files` and try to build again.

### The scanner view does not show up

If you cannot see the scanner in your viewport, please follow these steps:

1. Check if camera permissions are granted properly
2. Check if some DOM elements are rendered on top of the scanner
   - Search which element is causing the issue [#7](https://github.com/capacitor-community/barcode-scanner/issues/7#issuecomment-744441148)
   - Play with javascript [#26](https://github.com/capacitor-community/barcode-scanner/issues/26)

#### It doesn't appear

It could mean that you have missed a step by the [plugin configuration](#installation).

#### I did the configuration correctly

please [open an issue](https://github.com/capacitor-community/barcode-scanner/issues/new/choose)