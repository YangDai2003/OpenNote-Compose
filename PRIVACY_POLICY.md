## Privacy Policy of Open Note

Open Note is an open-source Android app developed by Yang Dai.  
The source code is available on GitHub under the GPL-3.0 license.

### Data Collection

Open Note does not collect any personal or confidential information such as addresses, names, or email addresses.

### Permissions Requested

The app requires the following permissions, as listed in the `AndroidManifest.xml` file:

https://github.com/YangDai2003/OpenNote-Compose/blob/4bc1cafa7368d81c539a09374e95d9859ab170a4/app/src/main/AndroidManifest.xml#L4-L7

| Permission                         | Purpose                                                                                                       |
|------------------------------------|---------------------------------------------------------------------------------------------------------------|
| `android.permission.USE_BIOMETRIC` | Enables biometric authentication to provide secure access to protected notes                                  |
| `android.permission.INTERNET`      | Allows network connectivity for loading web content (images, videos, and other media) when rendering markdown |

### Dependencies

The app uses the following dependencies:

- **Room**: For local database management.
- **Hilt**: For dependency injection.
- **Compose**: For building the UI.
- **CommonMark**: For markdown rendering and parsing.
- **ColorPicker**: For color picking functionalities.
- **Glance**: For creating app widgets.

### Data Sharing

Open Note does not share any personal or sensitive user data with third parties.

### Data Deletion

Usually all data is stored locally and can be cleared by the user at any time.  
Users can also add personal cloud storage and upload data, in which case the application is not responsible for the actions of third parties.

---

If you have any questions about this policy or personal information protection, please send your inquiries, opinions, or suggestions to: dy15800837435@gmail.com