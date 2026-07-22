# InterKnot-Android

InterKnot 的 Android 原生客户端，使用 Kotlin + Jetpack Compose 开发。

## 技术栈

- **Kotlin 2.0.21**
- **Jetpack Compose** (Material 3)
- **Hilt** 依赖注入
- **Navigation Compose** 单 Activity 导航
- **Ktor Client** + `kotlinx.serialization` 网络与序列化
- **Coil** 图片加载
- **DataStore** 轻量持久化
- **Room** 本地缓存（预留）

## 快速开始

1. 确保 Android Studio 已安装 Android SDK 与模拟器/真机。
2. 用 Android Studio 打开本项目。
3. 同步 Gradle，编译运行 `app`。

## 项目结构

```
app/src/main/kotlin/dev/kawayilab/interknot/
├── MainActivity.kt          # 入口 Activity
├── InterknotApplication.kt  # Application 与 Hilt 入口
├── di/                      # Hilt 模块
├── navigation/              # 导航图与路由
├── ui/                      # Compose 屏幕与组件
├── data/                    # 数据层（API、Repository、Local）
└── model/                   # 数据模型
```

## 待办

- [ ] 接入后端 API（BASE_URL、认证、拦截器）
- [ ] 登录态持久化
- [ ] 首页瀑布流委托列表
- [ ] 富文本帖子详情与评论
