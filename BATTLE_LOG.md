# 战役日志 (Battle Log)

## 2026-06-29
### 创世纪 (Genesis)
- 项目正式立项开建。
- 确立了本仓库作为 `com.google.android.ims` 开源存根 (Stub) 的核心定位。
- K80 真机环境验证，成功提取原版 Carrier Services 的包结构和权限要求。

### K80 原版数据提取结果 (Original Carrier Services Dump)
#### 关键权限声明 (Key Permissions)
- `com.google.android.ims.providers.ACCESS_DATA` (normal)
- 需要系统特权：`android.permission.READ_PRIVILEGED_PHONE_STATE`、`com.google.android.apps.messaging.services.ACCESS_JIBESERVICE`

#### 关键暴露服务 (Exposed Services)
1. **`MessagingService`** (Action: `android.service.carrier.CarrierMessagingService`) 
   - 需权限：`android.permission.BIND_CARRIER_SERVICES`
2. **`CarrierServicesImsService`** (Action: `android.telephony.ims.ImsService`)
   - 需权限：`android.permission.BIND_IMS_SERVICE`
3. **`RcsService`** (Action: `com.google.android.ims.START_RCS_ENGINE`)

#### 关键 ContentProvider (Exposed Providers)
- `com.google.android.ims.rcs.client.businessinfo`
- `com.google.android.ims.providers.carrierserviceslogprovider`

### 第一版开源骨架生成 (First Version Skeleton)
- `app/src/main/AndroidManifest.xml`: 注入了伪装包名、必要权限以及三大Service、两大Provider。
- `app/src/main/java/com/google/android/ims/services/CarrierMessagingServiceStub.java`: 拦截 `android.service.carrier.CarrierMessagingService`
- `app/src/main/java/com/google/android/ims/services/CarrierServicesImsServiceStub.java`: 拦截 `android.telephony.ims.ImsService`
- `app/src/main/java/com/google/android/ims/services/RcsEngineServiceStub.java`: 拦截 `com.google.android.ims.START_RCS_ENGINE`
- `app/src/main/java/com/google/android/ims/providers/BusinessInfoContentProviderStub.java`
- `app/src/main/java/com/google/android/ims/providers/CarrierServicesLogProviderStub.java`
- **编译状态**: 已通过本地 Gradle (AGP 8.2.0 + JDK 17) 环境编译测试 (`BUILD SUCCESSFUL`)。

### TS.43 握手流程模拟 (TS.43 Handshake Simulation)
- 为 `Ts43Orchestrator` 编写了完整的单元测试 `Ts43OrchestratorTest.java`。
- 使用 Mock 的 `IccAuthenticator` 注入了预设的 EAP-AKA 响应数据（模拟返回包含 `0xDB` tag 的 SIM 鉴权通过数据）。
- 在本地 PC 环境跑通了完整的 TS.43 握手流程，验证了 EAP-AKA 的编解码、PRF 计算和摘要认证逻辑，单元测试运行通过。
- 为 `app` 模块引入了 `robolectric` 依赖用于支持包含 Android `Base64` 和 `Log` 等依赖的纯本地单元测试。
