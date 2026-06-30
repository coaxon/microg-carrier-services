# 社区主动招募计划与文案草稿 (Community Outreach Drafts)

## 1. 目标讨论渠道与现状摘要

### 渠道 A: r/microg & r/LineageOS (Reddit)
*   **讨论现状**：RCS 在定制 ROM 上是公认的“老大难”问题。大量用户发帖抱怨 Google Messages 卡在 "Connecting"（连接中），并且随着 Google 逐渐收紧 Play Integrity 策略，传统通过假冒 GApps 的偏门方法越来越不稳定。许多注重隐私的用户被迫在放弃 RCS 或安装完整谷歌全家桶之间做痛苦抉择。
*   **切入时机**：这些用户对 RCS 需求极高，且普遍具备刷机/Root 能力，是绝佳的测试受众。

### 渠道 B: XDA-Developers 论坛
*   **讨论现状**：开发者和极客的聚集地，经常有讨论如何绕过 Carrier Services 限制的帖子，部分用户尝试修改 `RcsSettingsActivity` 或使用 LSPosed 模块，但缺乏底层 EAP-AKA 的替代方案。
*   **切入时机**：XDA 用户对刷入 Magisk 模块和抓取 Logcat 轻车熟路，技术门槛最低。

### 渠道 C: microg/GmsCore 官方 Issue #2994
*   **讨论现状**：这是 1.5 万美金 RCS 悬赏的“主战场”。跟帖的不仅有印度小哥等直接参与者，还有大量长年蹲守在此（甚至凑钱加码悬赏）的硬核用户。
*   **切入时机**：这是转化率最高的地方，这批用户本来就在等一个能彻底解决 RCS 的方案，完全不需要进行前置的背景解释。

---

## 2. 定制招募文案草稿

### 方案 A：面向 Reddit / XDA 的口语化发帖 (Casual / Direct)
> **[当前唯一推荐执行的渠道]**
**标题**: [Testing Request] We built a standalone RCS auth stub for microG, but we need testers with EU SIMs + Root!
*(【测试请求】我们为 microG 做了一个独立的 RCS 鉴权插件，但我们需要有 EU SIM 卡和 Root 的人帮忙测试！)*

**正文**:
Hey folks,

We all know RCS on microG has been a huge headache (stuck at "Connecting", failing Play Integrity, etc.). We've been working on a clean-room, standalone implementation of the Carrier Services EAP-AKA authentication flow that doesn't rely on proprietary Google binaries.

The software side (cryptography, SIM APDU parsing) is fully built and passing unit tests. However, we are stuck at the final hardware validation stage. **We need physical verification against real carrier networks before we can push this forward.**

**Who we need:**
1. You have a SIM card from a carrier with native RCS support (specifically looking for EU carriers like Giffgaff, EE, O2, Vodafone, but major US carriers are welcome too).
2. You have a rooted device (Magisk) or a custom ROM like LineageOS for microG. (The app needs to be installed to `/system/priv-app/` to get the `READ_PRIVILEGED_PHONE_STATE` permission to talk to the SIM card).

**What to do:**
You just need to install a simple Magisk module containing our stub, open the "EAP-AKA Test" app, and grab the logcat for us (we just need to see if the SIM returns the Base64 response packet). 

If you want to help get native RCS working on microG, check out the full instructions on our repo here: `[Link to microg-carrier-services README]`

Thanks for the help!

---

### 方案 B：面向 GmsCore Issue #2994 的精准留言 (Targeted / Technical)

> ⚠️ **已废弃 (2026-06-30)**：经核实 GmsCore Issue #2994 评论区(559条评论)，未发现任何用户表达过协助测试的意愿，且该issue为高敏感度主悬赏帖，在缺乏实机验证证据前发布此类言论存在信誉风险。保留此草稿仅作记录，不应被实际使用。

**回复区正文**:
Hey everyone following this bounty,

While the main GmsCore integration is being discussed, we are tackling the underlying TS.43 EAP-AKA hardware authentication via a standalone, clean-room compatibility stub. The HMAC-SHA1 cryptography and SIM APDU parsing layers are complete and passing tests, but we lack the physical European SIM cards to validate the final hardware responses.

If anyone watching this thread has a rooted device and a native RCS SIM (especially Giffgaff, O2, Vodafone, or EE), we could really use your help right now.

You'll just need to install our APK as a priv-app (via a provided Magisk module) to grant it the required Telephony permissions and run a quick test activity to pull the logcat. Your logs will help us finalize the hardware integration before we start hooking it up to the Binder interfaces.

Instructions are on our repo: `[Link to microg-carrier-services]`
Let's get this hardware layer verified!

---

**TODO**: 等待用户本人确认措辞后，由用户本人账号手动发布到目标渠道，AI不代为发布。
