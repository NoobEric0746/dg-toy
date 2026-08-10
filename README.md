# DG Toy — NeoForge 26.1.2 移植版

`dg-toy` 的 **NeoForge 26.1.2** 移植版（原仓库 [NoobEric0746/dg-toy](https://github.com/NoobEric0746/dg-toy) 基于 Minecraft Forge 1.20.1）。
本仓库由 MeadalFlute 维护，迁移原则是“最小代码入侵”：尽量通过构建脚本、元数据和兼容层适配，功能实现代码保持最小改动。

## 版本信息

- Minecraft：26.1.2
- NeoForge：26.1.2.74
- Java：25（Mojang 在 26.1.2 随游戏自带 Java 25）
- 构建工具：ModDevGradle（`net.neoforged.moddev` 2.0.143）

## 依赖

| 依赖 | 版本 | 说明 |
| --- | --- | --- |
| dglib | 1.3.3 | 原作者未发布 26.1.2 版本，已在本地迁移；构建用的 jar 随仓库放在 `libs/dglib-1.3.3.jar` |
| Curios | 15.0.0-beta.2+26.1.2 | NeoForge 版，来自 TheIllusiveC4 的 maven |
| zxing core | 3.5.3 | 已通过 jarjar 内嵌进 dglib jar，无需单独安装 |

## 构建

前置要求：JDK 25。

```bat
gradlew build
```

构建产物：`build/libs/dg_toy-1.0.jar`

## 运行

1. 安装 NeoForge 26.1.2.74 客户端。
2. 将 `dg_toy-1.0.jar` 与 `dglib-1.3.3.jar`（`libs/` 目录中，或从 Releases 下载）一起放入 `mods` 目录。
3. 安装依赖 mod：`curios-neoforge`（15.0.0-beta.2+26.1.2 或更新）。

## 与原版的主要差异（迁移摘要）

- 构建：ForgeGradle → ModDevGradle；`mods.toml` → `neoforge.mods.toml`（模板在 `src/main/templates/META-INF/neoforge.mods.toml`）。
- 注册 / 事件 / 网络：Forge API 替换为 NeoForge 对应 API（注册表、事件总线、SimpleChannel 网络通道）。
- 模型 / 渲染：物品、方块模型适配 26.1.2 格式；Curio 渲染适配新版 `ModelPart`/渲染管线。
- 爪印规则（pawprint）：修复了 26.1.2 下规则不生效的问题。

## 已知问题

- 项圈控制器（Remote Control）：绑定后没有粒子效果连线，也无法正常触发电击特效 —— 待排查。
- 详细迁移记录与人工确认点见后续提交说明 / PR 描述。

## 许可

All Rights Reserved（沿用原仓库）。