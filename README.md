# MicroG Carrier Services (Stub)

## 项目定位 (Project Scope)
本项目是 Google Carrier Services (`com.google.android.ims`) 的开源兼容层存根 (Stub)。
主要设计目的是在非谷歌官方授权的 Android 环境（如 LineageOS、GrapheneOS 或配备 MicroG 的定制 ROM）中充当占位符。

**⚠️ 法律与免责声明 (Legal Disclaimer):**
本项目硬编码使用 `com.google.android.ims` 包名以满足特定应用（如 Google Messages）的硬性依赖检查。
本项目**完全非商业用途**，不包含任何 Google 的专有闭源代码，仅作为 API 占位层供开发者研究、学习与兼容性测试使用。

## 核心目标 (Core Objectives)
1. **包名占位**：提供合法的 `com.google.android.ims` 环境。
2. **AIDL 空壳**：暴露必要的 AIDL 接口以防止调用方崩溃。
3. **权限接管**：声明系统所需的 IMS 绑定权限，将真正的网络和鉴权逻辑引导至 GmsCore 或底层服务。
