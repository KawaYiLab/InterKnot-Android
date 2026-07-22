# InterKnot-Android 重建任务

## Context
项目 `KawaYiLab/InterKnot-Android` 需要删除旧 Flutter 迁移过程中创建的 Compose 骨架，并基于 Devin 仓库内的 Material 3、adaptive、edge-to-edge、navigation-3、testing-setup 技能重新搭建 Android 原生项目结构。

## Task Description
使用 Kotlin + Jetpack Compose + Navigation 3 + Material 3 Adaptive + Edge-to-edge + Hilt + Ktor/DataStore 重新搭建单 `app` 模块论坛应用骨架，确保 `./gradlew :app:testDebugUnitTest` 与 `./gradlew :app:assembleDebug` 通过。

## Project Overview
- applicationId: `dev.kawayilab.interknot`
- compileSdk / targetSdk: 35
- minSdk: 26
- namespace: `dev.kawayilab.interknot`

## Analysis
- 当前项目已是单 `app` 模块 Gradle 项目，依赖 Hilt、Ktor、DataStore、Coil、Room 等。
- Material 3 skill 要求 Compose-first、动态配色、8dp spacing、自适应 scaffold 与 type-safe navigation。
- adaptive skill 要求使用 Jetpack Navigation 3 与 `NavigationSuiteScaffold`。
- edge-to-edge skill 要求 `enableEdgeToEdge()`、`windowSoftInputMode="adjustResize"` 并通过 Scaffold PaddingValues / safeDrawing 应用 insets。
- navigation-3 skill 提供 type-safe `NavKey`、自定义 BackStack、`NavDisplay`、`entryProvider`、登录条件导航等示例。
- testing-setup skill 建议配置 JUnit + Hilt + Robolectric + Mockk + Compose UI Test。

## Proposed Solutions
- 采用 Jetpack Navigation 3（`androidx.navigation3:navigation3-runtime/ui:1.2.0-alpha06`）替换 Navigation 2。
- 使用 `androidx.compose.material3:material3-adaptive-navigation-suite:1.3.2` 实现自适应底部导航栏/导航轨。
- 主题使用 Material 3 Dynamic Color（API 31+）并 fallback 到 seed `#FBC02D`。
- 论坛结构：顶层 `Home` / `Explore` / `Profile`，二级 `Login` / `PostDetail`；`Profile` 与 `PostDetail` 在未登录时重定向到 `Login`。
- 保留单 `app` 模块，按 package 分层：`ui`、`navigation`、`data`、`model`、`di`。
- 测试：JUnit 4 + Robolectric + Mockk + 基础 ExampleUnitTest。

## Implementation Plan
1. [删除旧代码并更新构建配置，review:false] 删除 `app/src/main/kotlin` 与 `app/src/main/res` 旧文件；更新 `libs.versions.toml` 与 `app/build.gradle.kts` 加入 Navigation 3、adaptive navigation suite、lifecycle-viewmodel-navigation3、Robolectric、Mockk 等依赖。
2. [创建 final_review_gate.py，review:false] 将 RIPER-5 附录脚本写入项目根目录。
3. [重建主题、Application 与 MainActivity，review:false] 创建 `ui/theme/Color.kt`、`Theme.kt`、`Type.kt`；`InterknotApplication.kt`；`MainActivity.kt`（`enableEdgeToEdge()`）。
4. [重建 Navigation 3 路由、BackStack 与 NavHost，review:false] 创建 `navigation/InterknotRoute.kt`、`InterknotBackStack.kt`、`InterknotNavHost.kt`；使用 `NavDisplay` + `entryProvider` + `NavigationSuiteScaffold` + 登录拦截。
5. [重建屏幕与数据层，review:false] 创建 `ui/screens/home/Explore/Profile/Login/PostDetail`；`data/api/InterknotApi.kt`、`repository/InterknotRepository.kt`、`local/UserPreferences.kt`；`di/AppModule.kt`；`model/User.kt`、`Post.kt`。
6. [更新 AndroidManifest 与测试，review:false] 设置 `windowSoftInputMode="adjustResize"`；创建 `ExampleUnitTest`。
7. [构建、测试与审查，review:true] 运行 `./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebug`；修复编译错误；提交并推送；启动 `final_review_gate.py`。

## Task Progress
- [x] 删除旧代码与资源
- [x] 更新构建配置
- [x] 创建 final_review_gate.py
- [x] 创建主题、Application、MainActivity
- [x] 创建 Navigation 3 路由、BackStack、NavHost
- [x] 创建屏幕与数据层
- [x] 更新 AndroidManifest 与测试
- [x] 构建测试与审查

## Final Review
待执行。
