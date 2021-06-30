# API Reference 🔌

Below is an index of all the methods available.

<docgen-index>

- [`prepare()`](#prepare)
- [`hideBackground()`](#hidebackground)
- [`showBackground()`](#showbackground)
- [`startScan(...)`](#startscan)
- [`stopScan(...)`](#stopscan)
- [`checkPermission(...)`](#checkpermission)
- [`openAppSettings()`](#openappsettings)
- [Interfaces](#interfaces)
- [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### prepare()

```typescript
prepare() => Promise<void>
```

---

### hideBackground()

```typescript
hideBackground() => Promise<void>
```

---

### showBackground()

```typescript
showBackground() => Promise<void>
```

---

### startScan(...)

```typescript
startScan(options?: ScanOptions | undefined) => Promise<ScanResult>
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code><a href="#scanoptions">ScanOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#scanresult">ScanResult</a>&gt;</code>

---

### stopScan(...)

```typescript
stopScan(options?: StopScanOptions | undefined) => Promise<void>
```

| Param         | Type                                                        |
| ------------- | ----------------------------------------------------------- |
| **`options`** | <code><a href="#stopscanoptions">StopScanOptions</a></code> |

---

### checkPermission(...)

```typescript
checkPermission(options?: CheckPermissionOptions | undefined) => Promise<CheckPermissionResult>
```

| Param         | Type                                                                      |
| ------------- | ------------------------------------------------------------------------- |
| **`options`** | <code><a href="#checkpermissionoptions">CheckPermissionOptions</a></code> |

**Returns:** <code>Promise&lt;<a href="#checkpermissionresult">CheckPermissionResult</a>&gt;</code>

---

### openAppSettings()

```typescript
openAppSettings() => Promise<void>
```

---

### Interfaces

#### ScanResult

| Prop             | Type                 | Description                                                                                                                                                                                                         | Since |
| ---------------- | -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| **`hasContent`** | <code>boolean</code> | This indicates whether or not the scan resulted in readable content. When stopping the scan with `resolveScan` set to `true`, for example, this parameter is set to `false`, because no actual content was scanned. | 1.0.0 |
| **`content`**    | <code>string</code>  | This holds the content of the barcode if available.                                                                                                                                                                 | 1.0.0 |

#### ScanOptions

| Prop                  | Type                           | Description                                                                                                                                                                                  | Since |
| --------------------- | ------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| **`targetedFormats`** | <code>SupportedFormat[]</code> | This parameter can be used to make the scanner only recognize specific types of barcodes. If `targetedFormats` is _not specified_ or _left empty_, _all types_ of barcodes will be targeted. | 1.2.0 |

#### StopScanOptions

| Prop              | Type                 | Description                                                                                                                                                                                          | Default           | Since |
| ----------------- | -------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------- | ----- |
| **`resolveScan`** | <code>boolean</code> | If this is set to `true`, the `startScan` method will resolve. Additionally `hasContent` will be `false`. For more information see: https://github.com/capacitor-community/barcode-scanner/issues/17 | <code>true</code> | 2.1.0 |

#### CheckPermissionResult

| Prop             | Type                 | Description                                                                                                                                                | Since |
| ---------------- | -------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------- | ----- |
| **`granted`**    | <code>boolean</code> | When set to `true`, the ermission is granted.                                                                                                              |       |
| **`denied`**     | <code>boolean</code> | When set to `true`, the permission is denied and cannot be prompted for. The `openAppSettings` method should be used to let the user grant the permission. | 1.0.0 |
| **`asked`**      | <code>boolean</code> | When this is set to `true`, the user was just prompted the permission. Ergo: a dialog, asking the user to grant the permission, was shown.                 | 1.0.0 |
| **`neverAsked`** | <code>boolean</code> | When this is set to `true`, the user has never been prompted the permission.                                                                               | 1.0.0 |
| **`restricted`** | <code>boolean</code> | iOS only When this is set to `true`, the permission cannot be requested for some reason.                                                                   | 1.0.0 |
| **`unknown`**    | <code>boolean</code> | iOS only When this is set to `true`, the permission status cannot be retrieved.                                                                            | 1.0.0 |

#### CheckPermissionOptions

| Prop        | Type                 | Description                                                                                                                                                                                                                                                              | Default            | Since |
| ----------- | -------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------ | ----- |
| **`force`** | <code>boolean</code> | If this is set to `true`, the user will be prompted for the permission. The prompt will only show if the permission was not yet granted and also not denied completely yet. For more information see: https://github.com/capacitor-community/barcode-scanner#permissions | <code>false</code> | 1.0.0 |

### Enums

#### SupportedFormat

| Members                 | Value                            | Description                                                   |
| ----------------------- | -------------------------------- | ------------------------------------------------------------- |
| **`UPC_A`**             | <code>'UPC_A'</code>             | Android only, UPC_A is part of EAN_13 according to Apple docs |
| **`UPC_E`**             | <code>'UPC_E'</code>             |                                                               |
| **`UPC_EAN_EXTENSION`** | <code>'UPC_EAN_EXTENSION'</code> | Android only                                                  |
| **`EAN_8`**             | <code>'EAN_8'</code>             |                                                               |
| **`EAN_13`**            | <code>'EAN_13'</code>            |                                                               |
| **`CODE_39`**           | <code>'CODE_39'</code>           |                                                               |
| **`CODE_39_MOD_43`**    | <code>'CODE_39_MOD_43'</code>    | iOS only                                                      |
| **`CODE_93`**           | <code>'CODE_93'</code>           |                                                               |
| **`CODE_128`**          | <code>'CODE_128'</code>          |                                                               |
| **`CODABAR`**           | <code>'CODABAR'</code>           | Android only                                                  |
| **`ITF`**               | <code>'ITF'</code>               |                                                               |
| **`ITF_14`**            | <code>'ITF_14'</code>            | iOS only                                                      |
| **`AZTEC`**             | <code>'AZTEC'</code>             |                                                               |
| **`DATA_MATRIX`**       | <code>'DATA_MATRIX'</code>       |                                                               |
| **`MAXICODE`**          | <code>'MAXICODE'</code>          | Android only                                                  |
| **`PDF_417`**           | <code>'PDF_417'</code>           |                                                               |
| **`QR_CODE`**           | <code>'QR_CODE'</code>           |                                                               |
| **`RSS_14`**            | <code>'RSS_14'</code>            | Android only                                                  |
| **`RSS_EXPANDED`**      | <code>'RSS_EXPANDED'</code>      | Android only                                                  |

</docgen-api>
