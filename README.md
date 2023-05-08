<p align="center"><br><img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" /></p>
<h3 align="center">Barcode Scanner</h3>
<p align="center"><strong><code>@capacitor-community/barcode-scanner</code></strong></p>
<p align="center">
  A fast and efficient (QR) barcode scanner for Capacitor.
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

On **iOS** this library makes use of Apple's own `AVFoundation`. This means **[this list of barcodes](https://developer.apple.com/documentation/avfoundation/avmetadatamachinereadablecodeobject/machine-readable_object_types)** should be supported.

On **Android** this library uses [`zxing-android-embedded`](https://github.com/journeyapps/zxing-android-embedded) which uses [`zxing`](https://github.com/zxing/zxing) under the hood. That means **[this list of barcodes](https://github.com/zxing/zxing/#supported-formats)** is supported.

On **Web** this library uses [`zxing/browser`](https://github.com/zxing-js/browser). That means **[this list of barcodes](https://github.com/zxing/zxing/#supported-formats)** is supported. The web implementation is currently in development, there might be issues and not all features are currently supported!

### Note on supported Capacitor versions

`v5.x.x-beta.x` pre-release based on ML-Kit that supports Capacitor `v5.x`

`v4.x` supports Capacitor `v5.x`

`v3.x` supports Capacitor `v4.x`

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

The complete API reference can be found [here](./src/definitions.ts).

Scanning a (QR) barcode can be as simple as:

```js
import { BarcodeScanner } from '@capacitor-community/barcode-scanner';

const startScan = async () => {
  // Check camera permission
  // This is just a simple example, check out the better checks below
  await BarcodeScanner.checkPermission({ force: true });

  // make background of WebView transparent
  // note: if you are using ionic this might not be enough, check below
  BarcodeScanner.hideBackground();

  const result = await BarcodeScanner.startScan(); // start scanning and wait for a result

  // if the result has content
  if (result.hasContent) {
    console.log(result.content); // log the raw scanned content
  }
};
```

### Opacity of the WebView

`hideBackground()` will make the `<html>` element transparent by adding `background: 'transparent';` to the `style` attribute.

If you are using Ionic you need to set some css variables as well, check [**here**](#ionic-css-variables)

If you still cannot see the camera view, check [**here**](#the-scanner-view-does-not-show-up)

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
    const c = confirm('We need your permission to use your camera to be able to scan barcodes');
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
    const c = confirm('If you want to grant permission for using your camera, enable it in the app settings.');
    if (c) {
      BarcodeScanner.openAppSettings();
    }
  }
};
```

### Target only specific barcodes

You can setup the scanner to only recognize specific types of barcodes like this:

```ts
import { BarcodeScanner, SupportedFormat } from '@capacitor-community/barcode-scanner';

BarcodeScanner.startScan({ targetedFormats: [SupportedFormat.QR_CODE] }); // this will now only target QR-codes
```

If `targetedFormats` is _not specified_ or _left empty_, _all types_ of barcodes will be targeted.

Targeting only specific types can have the following benefits:

- Improved performance (since the decoder only has to look for the specified barcodes)
- Improved User Experience (since scanning a barcode that is not supported by your case, will not work)

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
2. Check if the scanner element does appear inside the DOM, somewhere within the `body` tag
   - [It's not there](#i-do-not-find-the-scanner-in-the-dom)
3. Check if some DOM elements are rendered on top of the scanner
   - Search which element is causing the issue [#7](https://github.com/capacitor-community/barcode-scanner/issues/7#issuecomment-744441148)
   - Play with javascript [#26](https://github.com/capacitor-community/barcode-scanner/issues/26)

### I do not find the scanner in the DOM

This should appear in the DOM when running the `BarcodeScanner.startScan()` method.

```html
<body>
  <!-- ... -->
  <div style="position: absolute; left: 0px; top: -2px; height: 1px; overflow: hidden; visibility: hidden; width: 1px;">
    <span
      style="position: absolute; font-size: 300px; width: auto; height: auto; margin: 0px; padding: 0px; font-family: Roboto, Arial, sans-serif;"
      >BESbswy</span
    >
  </div>
  <!-- ... -->
</body>
```

If it does not, it may be a bug due to the component being loaded to deep inside the DOM tree.
You can try to see if the plugin is working properly by adding the following in your `app.component.ts` file.

```typescript
BarcodeScanner.hideBackground();
const result = await BarcodeScanner.startScan();
```

#### It doesn't appear

It could mean that you have missed a step by the [plugin configuration](#installation).

#### I did the configuration correctly

please [open an issue](https://github.com/capacitor-community/barcode-scanner/issues/new/choose)

## TODO

A non-exhaustive list of todos:

- Support for switching between cameras
- Support for web
