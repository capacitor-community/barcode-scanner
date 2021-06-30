<p align="center"><br><img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" /></p>
<h3 align="center">Barcode Scanner</h3>
<p align="center"><strong><code>@capacitor-community/barcode-scanner</code></strong></p>
<p align="center">
  A fast and efficient (QR) barcode scanner for Capacitor.
</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2021?style=flat-square" />
  <!-- <a href="https://github.com/DutchConcepts/capacitor-barcode-scanner/actions?query=workflow%3A%22CI%22"><img src="https://img.shields.io/github/workflow/status/dutchconcepts/capacitor-barcode-scanner/CI?style=flat-square" /></a> -->
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

| Maintainer | GitHub                                | Social |
| ---------- | ------------------------------------- | ------ |
| tafelnl    | [tafelnl](https://github.com/tafelnl) |        |

## About

### Supported barcodes

On **iOS** this library makes use of Apple's own `AVFoundation`. This means **[this list of barcodes](https://developer.apple.com/documentation/avfoundation/avmetadatamachinereadablecodeobject/machine-readable_object_types)** should be supported.

On **Android** this library uses [`zxing-android-embedded`](https://github.com/journeyapps/zxing-android-embedded) which uses [`zxing`](https://github.com/zxing/zxing) under the hood. That means **[this list of barcodes](https://github.com/zxing/zxing/#supported-formats)** is supported.

### Note on supported Capacitor versions

`v2.x` supports Capacitor `v3.x`

`v1.x` supports Capacitor `v2.x`

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
<manifest
  xmlns:android="http://schemas.android.com/apk/res/android"
+  xmlns:tools="http://schemas.android.com/tools"
  package="com.example">

  <application
+    android:hardwareAccelerated="true"
  >
  </application>

+  <uses-permission android:name="android.permission.CAMERA" />

+  <uses-sdk tools:overrideLibrary="com.google.zxing.client.android" />
</manifest>
```

## Usage

The complete API reference can be found [here](./API_REFERENCE.md).

Scanning a (QR) barcode can be as simple as:

```js
import { BarcodeScanner } from '@capacitor-community/barcode-scanner';

const startScan = async () => {
  BarcodeScanner.hideBackground(); // make background of WebView transparent

  const result = await BarcodeScanner.startScan(); // start scanning and wait for a result

  // if the result has content
  if (result.hasContent) {
    console.log(result.content); // log the raw scanned content
  }
};
```

### Opacity of the WebView

Because of the fact that the Scanner View will be rendered behind the WebView, you will have to call `hideBackground()` to make the WebView and the `<html>` element transparent. Every other element that needs transparency, you will have to handle yourself.

The `<html>` element is made transparent by adding `background: 'transparent';` to the `style=""` attribute. So in theory it is possible that this is overwritten by some CSS property in your setup. Because this plugin does not aim to fix every single scenario out there, you will have to think of a workaround for this yourself, if this applies to you (probably not).

If you still cannot see the camera view, check if any other elements are blocking it. For more info on this see [here](#the-scanner-view-does-not-show-up).

### Stopping a scan

After `startScan()` is resolved, the Scanner View will be automatically destroyed to save battery. But if you want to cancel the scan before `startScan()` is resolved (AKA no code has been recognized yet), you will have to call `stopScan()` manually. Example:

```js
import { BarcodeScanner } from '@capacitor-community/barcode-scanner';

const stopScan = () => {
  BarcodeScanner.showBackground();
  BarcodeScanner.stopScan();
};
```

It is also important to think about cases where a users hits some sort of a back button (either hardware or software). It is advised to call `stopScan()` in these types of situations as well.

In Vue.js you could do something like this in a specific view where you use the scanner:

```vue
<script>
import { BarcodeScanner } from '@capacitor-community/barcode-scanner';

export default {
  methods: {
    stopScan() {
      BarcodeScanner.showBackground();
      BarcodeScanner.stopScan();
    },
  },

  deactivated() {
    this.stopScan();
  },

  beforeDestroy() {
    this.stopScan();
  },
};
</script>
```

### Preparing a scan

To boost performance and responsiveness (by just a bit), a `prepare()` method is available. If you know your script will call `startScan()` sometime very soon, you can call `prepare()` to make `startScan()` work even faster.

For example:

```js
import { BarcodeScanner } from '@capacitor-community/barcode-scanner';

const prepare = () => {
  BarcodeScanner.prepare();
};

const startScan = async () => {
  BarcodeScanner.hideBackground();
  const result = await BarcodeScanner.startScan();
  if (result.hasContent) {
    console.log(result.content);
  }
};

const stopScan = () => {
  BarcodeScanner.showBackground();
  BarcodeScanner.stopScan();
};

const askUser = () => {
  prepare();

  const c = confirm('Do you want to scan a barcode?');

  if (c) {
    startScan();
  } else {
    stopScan();
  }
};

askUser();
```

This is fully optional and would work the same as:

```js
import { BarcodeScanner } from '@capacitor-community/barcode-scanner';

const startScan = async () => {
  BarcodeScanner.hideBackground();
  const result = await BarcodeScanner.startScan();
  if (result.hasContent) {
    console.log(result.content);
  }
};

const askUser = () => {
  const c = confirm('Do you want to scan a barcode?');

  if (c) {
    startScan();
  }
};

askUser();
```

The latter will just appear a little slower to the user.

### Permissions

This plugin does not automatically handle permissions. But the plugin _does_ have a utility method to check and request the permission. You will have to request the permission from JavaScript. A simple example follows:

```js
import { BarcodeScanner } from '@capacitor-community/barcode-scanner';

const checkPermission = async () => {
  // check or request permission
  const status = await BarcodeScanner.checkPermission({ force: true });

  if (status.granted) {
    // the user granted permission
    return true;
  }

  return false;
};
```

A more detailed and more UX-optimized example:

```js
import { BarcodeScanner } from '@capacitor-community/barcode-scanner';

const didUserGrantPermission = async () => {
  // check if user already granted permission
  const status = await BarcodeScanner.checkPermission({ force: false });

  if (status.granted) {
    // user granted permission
    return true;
  }

  if (status.denied) {
    // user denied permission
    return false;
  }

  if (status.asked) {
    // system requested the user for permission during this call
    // only possible when force set to true
  }

  if (status.neverAsked) {
    // user has not been requested this permission before
    // it is advised to show the user some sort of prompt
    // this way you will not waste your only chance to ask for the permission
    const c = confirm(
      'We need your permission to use your camera to be able to scan barcodes',
    );
    if (!c) {
      return false;
    }
  }

  if (status.restricted || status.unknown) {
    // ios only
    // probably means the permission has been denied
    return false;
  }

  // user has not denied permission
  // but the user also has not yet granted the permission
  // so request it
  const statusRequest = await BarcodeScanner.checkPermission({ force: true });

  if (statusRequest.asked) {
    // system requested the user for permission during this call
    // only possible when force set to true
  }

  if (statusRequest.granted) {
    // the user did grant the permission now
    return true;
  }

  // user did not grant the permission, so he must have declined the request
  return false;
};

didUserGrantPermission();
```

If a user denied the permission for good, `status.denied` will be set to true. On Android this will happen only when the user checks the box `never ask again`. To get the permission anyway you will have to redirect the user to the settings of the app. This can be done simply be doing the following:

```js
import { BarcodeScanner } from '@capacitor-community/barcode-scanner';

const checkPermission = async () => {
  const status = await BarcodeScanner.checkPermission();

  if (status.denied) {
    // the user denied permission for good
    // redirect user to app settings if they want to grant it anyway
    const c = confirm(
      'If you want to grant permission for using your camera, enable it in the app settings.',
    );
    if (c) {
      BarcodeScanner.openAppSettings();
    }
  }
};
```

### Target only specific barcodes

You can setup the scanner to only recognize specific types of barcodes like this:

`BarcodeScanner.startScan({ targetedFormats: ['QR_CODE'] })`

If `targetedFormats` is _not specified_ or _left empty_, _all types_ of barcodes will be targeted.

The following types are supported:

<table>
  <thead>
    <tr>
      <th>Category</th>
      <th>Type</th>
      <th>Android</th>
      <th>iOS</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td rowspan="6">1D Product</td>
    </tr>
    <tr>
      <td>UPC_A</td>
      <td>✔</td>
      <td>✔**</td>
    </tr>
    <tr>
      <td>UPC_E</td>
      <td>✔</td>
      <td>✔</td>
    </tr>
    <tr>
      <td>UPC_EAN_EXTENSION</td>
      <td>✔</td>
      <td>✖</td>
    </tr>
    <tr>
      <td>EAN_8</td>
      <td>✔</td>
      <td>✔</td>
    </tr>
    <tr>
      <td>EAN_13</td>
      <td>✔</td>
      <td>✔</td>
    </tr>
    <tr>
      <td rowspan="8">1D Industrial</td>
    </tr>
    <tr>
      <td>CODE_39</td>
      <td>✔</td>
      <td>✔</td>
    </tr>
    <tr>
      <td>CODE_39_MOD_43</td>
      <td>✖</td>
      <td>✔</td>
    </tr>
    <tr>
      <td>CODE_93</td>
      <td>✔</td>
      <td>✔</td>
    </tr>
    <tr>
      <td>CODE_128</td>
      <td>✔</td>
      <td>✔</td>
    </tr>
    <tr>
      <td>CODABAR</td>
      <td>✔</td>
      <td>✖</td>
    </tr>
    <tr>
      <td>ITF</td>
      <td>✔</td>
      <td>✔</td>
    </tr>
    <tr>
      <td>ITF_14</td>
      <td>✖</td>
      <td>✔</td>
    </tr>
    <tr>
      <td rowspan="8">2D</td>
    </tr>
    <tr>
      <td>AZTEC</td>
      <td>✔</td>
      <td>✔</td>
    </tr>
    <tr>
      <td>DATA_MATRIX</td>
      <td>✔</td>
      <td>✔</td>
    </tr>
    <tr>
      <td>MAXICODE</td>
      <td>✔</td>
      <td>✖</td>
    </tr>
    <tr>
      <td>PDF_417</td>
      <td>✔</td>
      <td>✔</td>
    </tr>
    <tr>
      <td>QR_CODE</td>
      <td>✔</td>
      <td>✔</td>
    </tr>
    <tr>
      <td>RSS_14</td>
      <td>✔</td>
      <td>✖</td>
    </tr>
    <tr>
      <td>RSS_EXPANDED</td>
      <td>✔</td>
      <td>✖</td>
    </tr>
  </tbody>
</table>

\*\* `UPC_A` is supported on iOS, but according to the offical [Apple docs](https://developer.apple.com/documentation/avfoundation/avmetadataobject/objecttype/1618807-ean13) it is part of `EAN_13`. So you should specify `EAN_13` to be able to scan this. If you want to distinguish them from one another, you should manually do so after getting the result.

## Troubleshooting

### I have a `Error: Plugin BarcodeScanner does not respond to method call` error message on iOS

In Xcode click on `Product` > `Clean Build Folder` and try to build again.

### I have a `Cannot resolve symbol BarcodeScanner` error message in Android Studio

In Android Studio click `File` > `Sync Project with Gradle Files` and try to build again.

### The scanner view does not show up

First check that the camera permission is granted. If the scanner view does still not appear it is likely that some UI element is blocking it. Check out these issues for more information on how to resolve such an issue: [#7](https://github.com/capacitor-community/barcode-scanner/issues/7#issuecomment-744441148) and [#26](https://github.com/capacitor-community/barcode-scanner/issues/26)

## TODO

A non-exhaustive list of todos:

- Support for switching between cameras
- Support for toggling the flashlight
- Support for web
