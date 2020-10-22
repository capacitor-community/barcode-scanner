<p align="center"><br><img src="https://user-images.githubusercontent.com/236501/85893648-1c92e880-b7a8-11ea-926d-95355b8175c7.png" width="128" height="128" /></p>
<h3 align="center">Code Scanner</h3>
<p align="center"><strong><code>@capacitor-community/code-scanner</code></strong></p>
<p align="center">
  Capacitor community plugin for something awesome.
</p>

<p align="center">
  <img src="https://img.shields.io/maintenance/yes/2020?style=flat-square" />
  <a href="https://github.com/capacitor-community/code-scanner/actions?query=workflow%3A%22CI%22"><img src="https://img.shields.io/github/workflow/status/capacitor-community/code-scanner/CI?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/code-scanner"><img src="https://img.shields.io/npm/l/@capacitor-community/code-scanner?style=flat-square" /></a>
<br>
  <a href="https://www.npmjs.com/package/@capacitor-community/code-scanner"><img src="https://img.shields.io/npm/dw/@capacitor-community/code-scanner?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@capacitor-community/code-scanner"><img src="https://img.shields.io/npm/v/@capacitor-community/code-scanner?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
<a href="#contributors-"><img src="https://img.shields.io/badge/all%20contributors-0-orange?style=flat-square" /></a>
<!-- ALL-CONTRIBUTORS-BADGE:END -->
</p>

## Maintainers

| Maintainer | GitHub                                | Social |
| ---------- | ------------------------------------- | ------ |
| tafelnl    | [tafelnl](https://github.com/tafelnl) |        |

## Installation

```bash
npm install git+https://github.com/DutchConcepts/capacitor-code-scanner.git#v1.0.0-alpha.1
npx cap sync
```

### iOS

On iOS, no further steps are needed.

### Android

On Android, register the plugin in your main activity:

```java
import com.dutchconcepts.capacitor.codescanner.CodeScanner;

public class MainActivity extends BridgeActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Initializes the Bridge
    this.init(
        savedInstanceState,
        new ArrayList<Class<? extends Plugin>>() {
          {
            // Additional plugins you've installed go here
            // Ex: add(TotallyAwesomePlugin.class);
            add(CodeScanner.class);
          }
        }
      );
  }
}

```

## Configuration

### iOS

For iOS you need to set a usage description in your info.plist file.

This can be done by either adding it to the Source Code directly or by using Xcode Property List inspector.

**Adding it to the source code directly**

1. Open up the Info.plist (in Xcode right-click > Open As > Source Code)
2. With `<dict></dict>` change the following

```diff
<dict>
+  <key>NSCameraUsageDescription</key>
+  <string>To be able to scan QR-codes</string>
</dict>
```

_NOTE:_ "To be able to scan QR-codes" can be substituted for anything you like.

**Adding it by using Xcode Property List inspector**

1. Open up the Info.plist **in Xcode** (right-click > Open As > Property List)
2. Next to "Information Property List" click on the tiny `+` button.
3. Under `key`, type "Privacy - Camera Usage Description"
4. Under `value`, type "To be able to scan QR-codes"

_NOTE:_ "To be able to scan QR-codes" can be substituted for anything you like.

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

Scanning a (QR) code can be as simple as:

```js
import { Plugins } from '@capacitor/core';

const startScan = async () => {
  const { CodeScanner } = Plugins;

  CodeScanner.hideBackground(); // make background of WebView transparent

  const result = await CodeScanner.startScan(); // start scanning and wait for a result

  // if the result has content
  if (result.hasContent) {
    console.log(result.content); // log the raw scanned content
  }
};
```

### Opacity of the WebView

Because of the fact that the Scanner View will be rendered behind the WebView, you will have to call `hideBackground()` to make the WebView and the `<html>` element transparent. Every other element that needs transparency, you will have to handle yourself.

The `<html>` element is made transparent by adding `background: 'transparent';` to the `style=""` attribute. So in theory it is possible that this is overwritten by some CSS property in your setup. Because this plugins does not aim to fix every single scenario out there, you will have to think of a workaround for this yourself, if this applies to you (probably not).

### Stopping a scan

After `startScan()` is resolved, the Scanner View will be automatically destroyed to save battery. But if you want to cancel the scan before `startScan()` is resolved (AKA no code has been recognized yet), you will have to call `stopScan()` manually. Example:

```js
import { Plugins } from '@capacitor/core';

const stopScan = () => {
  const { CodeScanner } = Plugins;
  CodeScanner.showBackground();
  CodeScanner.stopScan();
};
```

It is also important to think about cases where a users hits some sort of a back button (either hardware or software). It is advised to call `stopScan()` in these types of situations as well.

In Vue.js you could do something like this in a specific view where you use the scanner:

```vue
<script>
import { Plugins } from '@capacitor/core';

export default {
  methods: {
    stopScan() {
      const { CodeScanner } = Plugins;
      CodeScanner.showBackground();
      CodeScanner.stopScan();
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
import { Plugins } from '@capacitor/core';

const prepare = () => {
  const { CodeScanner } = Plugins;
  CodeScanner.prepare();
};

const startScan = async () => {
  const { CodeScanner } = Plugins;
  CodeScanner.hideBackground();
  const result = await CodeScanner.startScan();
  if (result.hasContent) {
    console.log(result.content);
  }
};

const stopScan = () => {
  const { CodeScanner } = Plugins;
  CodeScanner.showBackground();
  CodeScanner.stopScan();
};

const askUser = () => {
  prepare();

  const c = confirm('Do you want to scan a QR-code?');

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
const startScan = async () => {
  const { CodeScanner } = Plugins;
  CodeScanner.hideBackground();
  const result = await CodeScanner.startScan();
  if (result.hasContent) {
    console.log(result.content);
  }
};

const askUser = () => {
  const c = confirm('Do you want to scan a QR-code?');

  if (c) {
    startScan();
  }
};

askUser();
```

The latter will just appear a little slower to the user.

### Permissions

This plugin does not handle permissions (yet). Your app will not crash when you call this API without having the right permission granted. But it will not be able to start up the camera. So you will have to take care of permissions yourself.

## TODO

A non-exhaustive list of todos:

- Support for switching between cameras
- Support for toggling the flashlight
- Support for web
- Support for different types of codes (it now only support 'normal' or inverted QR-codes)
