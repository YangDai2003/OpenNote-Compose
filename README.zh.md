- [简体中文](README.zh.md)
- [English](README.md)
- [Italiano](README.it.md)
- [Türkçe](README.tr.md)

# OpenNote

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/static/v1?style=for-the-badge&message=Jetpack+Compose&color=4285F4&logo=Jetpack+Compose&logoColor=FFFFFF&label=)
![Material](https://custom-icon-badges.demolab.com/badge/material%20you-lightblue?style=for-the-badge&logoColor=333&logo=material-you)
![LaTeX](https://img.shields.io/badge/latex-%23008080.svg?style=for-the-badge&logo=latex&logoColor=white)
![Markdown](https://img.shields.io/badge/markdown-%23000000.svg?style=for-the-badge&logo=markdown&logoColor=white)

OpenNote是一款完全使用Compose构建的现代化Android记事本应用程序。

[<img alt="Get it on Google Play" src="screenshots/google_play.png" width="200px">](https://play.google.com/store/apps/details?id=com.yangdai.opennote)
[<img alt="Get it on IzzyOnDroid" src="screenshots/izzyondroid.png" width="200px">](https://apt.izzysoft.de/fdroid/index/apk/com.yangdai.opennote)
[<img alt="Get it on GitHub" src="screenshots/github.png" width="200px">](https://github.com/YangDai2003/OpenNote-Compose/releases)

## 📃 功能

### 核心功能
- **创建、编辑和删除笔记**：用户可以轻松创建、编辑和删除笔记。
- **创建、编辑和删除文件夹**：使用文件夹管理功能有效地组织笔记。
- **排序和过滤**：根据各种条件轻松排序和过滤笔记和文件夹。
- **移动笔记**：在不同文件夹之间无缝移动笔记以更好地组织。
- **垃圾箱**：将笔记安全地移至垃圾箱，以便在永久删除之前临时存储。

### 进阶功能
- **笔记侧边栏**: 根据标题层级自动生成笔记大纲，点击即可滚动到对应位置。字数、行数等信息一目了然。
- **笔记内搜索与替换**：笔记太长找不到某段文本的位置? 想要批量修改，一个个找太蛮烦? 试试这个功能吧。
- **支持笔记模板**: 支持将 Templates 文件夹中的笔记作为模板插入，并自动格式化时间和日期。
- **支持本地图片、视频和音频**：支持在笔记中直接导入设备中的本地图片、视频和音频，并在预览中查看。
- **Markdown 支持**：支持 CommonMark 和 GitHub Flavored Markdown (GFM) 语法，以实现多种格式选项。
- **LaTeX Math 支持**：支持 LaTeX Math 语法，用于数学方程。
- **Mermaid 支持**：支持 Mermaid 语法，用于创建图表和流程图。

### 两种模式
- **轻量模式**：提供基本的 Markdown 语法和所见即所得的显示效果。
- **经典模式**：提供全面的 Markdown 语法和精准的渲染效果，分为编辑区域和阅读区域。

### 其他特点
- **接受分享的文本**：应用可以直接接收来自其它应用分享的文本，并创建为笔记。
- **直接打开文档**：应用可以被选择作为所有类型文本文档（.txt、.md、.html）的打开方式，并创建为笔记。
- **导出选项**：笔记可以以各种格式导出，包括 TXT、Markdown、PDF 和 HTML，以实现多种共享和使用。
- **Material 3 Design**：遵循 Material Design 3 指南，打造现代且具有凝聚力的用户界面。
- **支持鼠标和物理键盘**：应用对使用鼠标和外接物理键盘时的操作做了充足的适配，确保了高生产力表现。
- **响应式设计**：针对不同屏幕尺寸和方向的设备进行了优化，在手机、平板、折叠屏甚至搭载了ChromeOS的设备上都具有良好的体验。

## 🖼️ 屏幕截图

<div style="overflow-x: auto; white-space: nowrap;">

<img src="screenshots/Anim_lock.gif" width="15%" alt=""/>
<img src="screenshots/MainScreen.png" width="15%" alt=""/>
<img src="screenshots/Drawer.png" width="15%" alt=""/>
<img src="screenshots/Folders.png" width="15%" alt=""/>
<img src="screenshots/Editor.png" width="15%" alt=""/>
<img src="screenshots/ReadView.png" width="15%" alt=""/>
<img src="screenshots/Settings.png" width="15%" alt=""/>
<img src="screenshots/Widget.png" width="15%" alt=""/>
<img src="screenshots/Screenshot_Math_Edit.png" width="15%" alt=""/>
<img src="screenshots/Screenshot_Math_Preview.png" width="15%" alt=""/>
<img src="screenshots/Screenshot_Mermaid_Edit.png" width="15%" alt=""/>
<img src="screenshots/Screenshot_Mermaid_Preview.png" width="15%" alt=""/>
<img src="screenshots/MainScreen_Large.png" width="30%" alt=""/>
<img src="screenshots/Editor_Large.png" width="30%" alt=""/>
<img src="screenshots/Settings_Large.png" width="30%" alt=""/>

</div>

## 🌎 翻译

目前支持中文、英文、德语和土耳其语。

## 💡 如何使用 Markdown, LaTeX Math 和 Mermaid 图表的语法在 OpenNote 中编写文档？

您可以在[指南](Guide.zh.md)中了解有关如何使用 Markdown，LaTeX Math 和 Mermaid 图表的语法在 OpenNote
中编写文档的更多信息。

## 🔎 技术细节

- **编程语言**：Kotlin
- **构建工具**：Gradle 和 Kotlin DSL
- **Android版本**：应用程序目标为 Android SDK 版本35，并且与运行 Android SDK 版本29及以上的设备兼容。
- **Kotlin版本**：2.1.20。
- **Java版本**：JVM Target 17。

## 🛠️ 架构

- **MVVM（模型-视图-视图模型）**：将用户界面逻辑与业务逻辑分开，提供清晰的关注点分离。
- **干净架构**：强调关注点和抽象层的分离，使应用程序更加模块化、可扩展和可维护。

## 📚 库和框架

- **Compose**：用于构建本机 Android UI 的现代化工具包。
- **Hilt**：Android 的依赖注入库。
- **KSP（Kotlin 符号处理 API）**：通过额外的元数据处理增强 Kotlin 编译。
- **Room**：一个持久性库，提供 SQLite 上的抽象层。
- **Compose Navigation**：简化应用程序中不同屏幕间导航的实现。
- **Material Icons**：提供材质设计图标以实现一致的视觉元素。
- **CommonMark**： 用于 Markdown 解析和渲染。

## 🔐 隐私政策和所需权限

您可以在[隐私政策](PRIVACY_POLICY.md)中找到隐私政策和所需权限。

## 📦 安装

要构建和运行此应用程序，您需要安装最新版本的Android Studio。然后，您可以从GitHub克隆此仓库并在Android
Studio中打开它。

```bash
git clone https://github.com/YangDai2003/OpenNote.git
```

在Android Studio中，选择`Run > Run 'app'`来启动应用程序。

## 🎈 贡献

欢迎任何形式的贡献！如果您发现错误或有新的功能请求，请创建问题。如果您想直接向此项目贡献代码，您可以创建拉取请求。

## 🔗 参考

- [MaskAnim](https://github.com/setruth/MaskAnim)：用于使用遮罩动画来切换主题功能的实现。