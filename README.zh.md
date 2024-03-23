# OpenNote

OpenNote是一款完全使用Compose构建的现代Android记事本应用程序。
它使用Kotlin(Compose)进行开发，并使用Gradle和Kotlin DSL进行项目构建和依赖管理。

## 功能

- 创建、编辑和删除笔记
- 创建、编辑和删除文件夹
- 对笔记和文件夹进行排序和过滤
- 将笔记移动到不同的文件夹
- 将笔记移动到回收站
- 使用ML Kit和CameraX进行OCR文本识别

## 技术细节

- **编程语言**：Kotlin
- **构建工具**：Gradle和Kotlin DSL
- **Android版本**：应用程序目标为Android SDK版本34，并且与运行Android SDK版本29及以上的设备兼容。
- **Kotlin版本**：应用程序使用Kotlin版本1.5.10。
- **Java版本**：应用程序使用Java版本17。

## 库和框架

- Compose：用于构建原生Android UI的现代工具包。
- Hilt：Android的依赖注入库。
- KSP：Kotlin符号处理API。
- Room：提供SQLite抽象层的持久性库。
- Navigation：简化Android应用导航实现的库。
- Material Icons：提供Material Design图标的库。
- ML Kit：用于OCR文本识别的库。
- CameraX：用于自定义相机功能的库。

## 安装

要构建和运行此应用程序，您需要安装最新版本的Android Studio。然后，您可以从GitHub克隆此仓库并在Android Studio中打开它。

```bash
git clone https://github.com/YangDai2003/OpenNote.git
```

在Android Studio中，选择`Run > Run 'app'`来启动应用程序。

## 贡献

欢迎任何形式的贡献！如果您发现错误或有新的功能请求，请创建问题。如果您想直接向此项目贡献代码，您可以创建拉取请求。

## 参考

- [MaskAnim](https://github.com/setruth/MaskAnim)：用于使用遮罩动画来切换主题功能的实现。