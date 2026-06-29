# microg-carrier-services

Open-source compatibility stub for `com.google.android.ims` (Google Carrier Services), designed for LineageOS / microG environments.  
为 LineageOS / microG 环境设计的 `com.google.android.ims` (Google Carrier Services) 开源兼容存根层。

## What This Is / 这是什么
This project serves as an open-source compatibility layer for Carrier Services. It provides the necessary stubs and interfaces required by modern Android messaging apps (like Google Messages) to enable RCS (Rich Communication Services) and EAP-AKA authentication without relying on proprietary Google binaries.  
本项目是 Carrier Services 的开源兼容层，提供现代 Android 消息应用（如 Google Messages）所需的必要存根和接口，旨在无需依赖 Google 闭源二进制文件即可实现 RCS（富通讯服务）和 EAP-AKA 鉴权功能。

## What This Is NOT / 这不是什么
This repository **does not** contain any proprietary Google closed-source code, binaries, or reverse-engineered proprietary algorithms. It is built strictly as a clean-room implementation based on open 3GPP standards (like TS.43). This project is intended for educational and open-source community use, and is **not** for commercial purposes.  
本仓库 **不包含** 任何 Google 闭源代码、二进制文件或逆向工程获取的专有算法。本项目完全基于开放的 3GPP 标准（如 TS.43）进行净室实现（Clean-room implementation）。本项目仅供教育和开源社区使用，**非商业用途**。

## Architecture / 架构说明
The project is structured into four core modules / 项目由四个核心模块组成:

- **EapAkaCrypto**: Implements FIPS 186-2 Pseudo-Random Function (PRF) and HMAC-SHA1 MAC calculation for cryptographic operations. / 实现了 FIPS 186-2 伪随机函数 (PRF) 和 HMAC-SHA1 MAC 计算，用于加密操作。
- **SimResponseParser**: A 3GPP TS 31.102 compliant APDU response parser that processes SIM card authentication results. / 符合 3GPP TS 31.102 标准的 APDU 响应解析器，用于处理 SIM 卡鉴权结果。
- **Ts43HttpClient**: A lightweight HTTP client handling the 3-round EAP-AKA JSON relay session with carrier endpoints. / 轻量级 HTTP 客户端，负责处理与运营商端点的三轮 EAP-AKA JSON 中继会话。
- **Ts43Orchestrator**: The central orchestrator that is hardware-decoupled, interfacing with SIM hardware via the abstract `IccAuthenticator` interface. / 核心编排器，通过抽象的 `IccAuthenticator` 接口与 SIM 硬件解耦。

## Status / 项目状态
- [x] Carrier Services stub skeleton
- [x] TS.43 EAP-AKA software protocol layer
- [x] Unit tests passing
- [ ] Hardware validation (pending physical SIM card test)

## Related / 相关引用
- microg/GmsCore PR #3508

## License
Apache 2.0 — By CoAxon Labs
