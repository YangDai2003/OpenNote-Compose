- [English](README.md)
- [简体中文](README.zh.md)

# OpenNote

OpenNote is a modern Android note-taking application built entirely with Compose.  
It is developed using Kotlin (Compose) and follows the MVVM (Model-View-ViewModel) architecture pattern along with Clean Architecture principles.

<a href="https://play.google.com/store/apps/details?id=com.yangdai.opennote">
      <img alt="Get it on Google Play" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="100">
</a>

<a href="https://github.com/YangDai2003/OpenNote-Compose/tree/master/app/release">
      <img alt="Get it on GitHub" src="https://raw.githubusercontent.com/deckerst/common/main/assets/get-it-on-github.png" height="100">
</a>

## How to Use OpenNote with Markdown and LaTeX?

You can know more about how to use OpenNote with Markdown and LaTeX in the [Guide](Guide.md).

## Notice

This project is still in early stages of construction and features may change, be added or removed. If you have any suggestions or questions please feel free to let me know.

## Features

- **Create, Edit, and Delete Notes**: Users can create, edit, and delete notes effortlessly.
- **Create, Edit, and Delete Folders**: Organize notes efficiently with folder management functionalities.
- **Sorting and Filtering**: Easily sort and filter notes and folders based on various criteria.
- **Move Notes**: Seamlessly move notes between different folders for better organization.
- **Trash Bin**: Safely move notes to the trash for temporary storage before permanent deletion.
- **OCR Text Recognition**: Utilizes ML Kit and CameraX for optical character recognition (OCR) directly from images.
- **Markdown Support**: Supports both CommonMark and GitHub Flavored Markdown (GFM) syntax for versatile formatting options.
- **Latex Math Support**: Supports Latex math syntax for mathematical equations.
- **Rich Text Mode**: Offers a simplified writing experience with basic rich text editing capabilities.
- **Export Options**: Notes can be exported in various formats including TXT, MD (Markdown), and HTML for versatile sharing and usage.
- **Material 3 Design**: Adheres to Material Design guidelines for a modern and cohesive user interface.
- **Responsive Design**: Optimized for devices with different screen sizes and orientations.

## Screenshots

<div style="overflow-x: auto; white-space: nowrap;">

<img src="https://github.com/YangDai2003/OpenNote-Compose/blob/master/screenshots/Screenshot_1.png" width="15%" alt=""/>
<img src="https://github.com/YangDai2003/OpenNote-Compose/blob/master/screenshots/Screenshot_2.png" width="15%" alt=""/>
<img src="https://github.com/YangDai2003/OpenNote-Compose/blob/master/screenshots/Screenshot_3.png" width="15%" alt=""/>
<img src="https://github.com/YangDai2003/OpenNote-Compose/blob/master/screenshots/Screenshot_4.png" width="15%" alt=""/>
<img src="https://github.com/YangDai2003/OpenNote-Compose/blob/master/screenshots/Screenshot_5.png" width="15%" alt=""/>
<img src="https://github.com/YangDai2003/OpenNote-Compose/blob/master/screenshots/Screenshot_6.png" width="15%" alt=""/>
<img src="https://github.com/YangDai2003/OpenNote-Compose/blob/master/screenshots/Screenshot_7.png" width="15%" alt=""/>
<img src="https://github.com/YangDai2003/OpenNote-Compose/blob/master/screenshots/Screenshot_8.png" width="15%" alt=""/>
<img src="https://github.com/YangDai2003/OpenNote-Compose/blob/master/screenshots/Screenshot_9.png" width="15%" alt=""/>
<img src="https://github.com/YangDai2003/OpenNote-Compose/blob/master/screenshots/Screenshot_10.png" width="15%" alt=""/>

</div>

## Technical Details

- **Programming Languages**: Kotlin
- **Build Tool**: Gradle with Kotlin DSL
- **Android Version**: The application targets Android SDK version 34 and is compatible with devices running Android SDK version 29 and above.
- **Kotlin Version**: The application uses Kotlin version 1.5.11.
- **Java Version**: The application uses Java version 17.

## Architecture

- **MVVM (Model-View-ViewModel)**: Separates the user interface logic from the business logic, providing a clear separation of concerns.
- **Clean Architecture**: Emphasizes separation of concerns and layers of abstraction, making the application more modular, scalable, and maintainable.

## Libraries and Frameworks

- **Compose**: A modern toolkit for building native Android UI.
- **Hilt**: A dependency injection library for Android.
- **KSP (Kotlin Symbol Processing API)**: Enhances Kotlin compilation with additional metadata processing.
- **Room**: A persistence library providing an abstraction layer over SQLite.
- **Compose Navigation**: Simplifies the implementation of navigation between screens.
- **Material Icons**: Provides Material Design icons for consistent visual elements.
- **ML Kit**: Utilized for OCR text recognition.
- **CameraX**: Used for custom camera functionality.

## Privacy Policy and Required Permissions

You can find the Privacy Policy and Required Permissions in the [Privacy Policy](PRIVACY_POLICY.md).

## Installation

To build and run this application, you need to install the latest version of Android Studio. Then, you can clone this repository from GitHub and open it in Android Studio.

```bash
git clone https://github.com/YangDai2003/OpenNote.git
```

In Android Studio, select `Run > Run 'app'` to start the application.

## Contribution

Any form of contribution is welcome! If you find a bug or have a new feature request, please create an issue. If you want to contribute code directly to this project, you can create a pull request.

## References

- [MaskAnim](https://github.com/setruth/MaskAnim): Implementation of the theme switching function using mask animation.
