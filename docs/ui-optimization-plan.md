# InterKnot Android UI 优化计划

> 参考小红书 APP 信息流设计，以苹果工程师的精度标准打磨每一个细节

---

## 一、现状分析

### 已有的好基础

瀑布流首页（`LazyVerticalStaggeredGrid`）已对标小红书核心布局；MVVM + Hilt + Ktor + Coil 技术栈成熟稳定；Material 3 主题已搭建，签名色 `#BFFF09` 绝区零辨识度高；卡片按压动画、扩展颜色体系等细节已具备雏形。

### 核心问题清单

| # | 问题 | 严重度 |
|---|------|--------|
| 1 | **纯暗色主题**：`Theme.kt` 第 55-57 行硬编码 `darkTheme -> DarkColorScheme; else -> DarkColorScheme`，无视系统浅色模式。小红书是浅色优先 | 高 |
| 2 | **字体单一**：全部使用 `FontFamily.Default`，无层级区分。Apple 风格要求精确的字号/字重梯度 | 高 |
| 3 | **Explore 页面废弃**：未注册到导航图，Level 页面是空壳 stub | 高 |
| 4 | **发布页极简**：仅支持标题+正文，后端已支持多图封面、分类、标签、富文本，前端全部缺失 | 高 |
| 5 | **后端能力浪费**：Follow 关注系统、Favorite 收藏、Check-in 签到、Emote 表情、Mention @提及、WebSocket 实时私信、SSE 实时通知、Direct Upload 直传、Avatar/Card 装扮系统 —— APP 均未接入 | 高 |
| 6 | **通知靠轮询**：`KnockViewModel` 每 15 秒 HTTP 轮询，后端已有 SSE 推送 (`/knock/stream`) 和 WebSocket (`/dm/socket`) | 中 |
| 7 | **PostCard 标题遮盖图片**：标题用半透明黑色渐变叠加在图片底部，小红书做法是图片下方独立文本区域 | 中 |
| 8 | **无骨架屏**：加载态只有文字提示，缺少 shimmer 骨架占位 | 中 |
| 9 | **无下拉刷新**：首页缺少 pull-to-refresh 交互 | 中 |
| 10 | **底部导航 center 按钮粗糙**：一个 `Surface(CircleShape)` + `shadowElevation = 4.dp`，缺少精致感 | 低 |
| 11 | **无屏幕转场动画**：Navigation 3 跳转无 shared element / container transform | 低 |
| 12 | **Room 已声明依赖但完全未使用**：无本地缓存，每次冷启动都要网络请求 | 低 |

---

## 二、设计理念

### 核心原则：Apple Engineer Precision + Xiaohongshu Aesthetics

1. **像素级精确**：8dp 间距网格，所有 padding/margin 必须是 4 的倍数。绝不允许 10dp、13dp 这类"差不多"的值出现在布局中
2. **物理感动效**：使用 spring 物理动画而非线性/贝塞尔。按压缩放 0.96（当前 0.97 偏小），回弹要带微弱过冲
3. **克制的色彩**：浅色模式下大面积留白，暗色模式下保持 `#0A0A0A` 深黑。强调色 `#BFFF09` 仅用于关键 CTA 和选中态，不做装饰性使用
4. **信息层级清晰**：标题 > 正文 > 元信息，靠字号+字重区分而非颜色。读过的标题降饱和度而非换色
5. **每个状态都要设计**：loading skeleton、empty illustration、error retry、no-network —— 不能只有 success 态
6. **手势优先**：下拉刷新、左滑返回、长按弹出菜单、双击点赞、图片捏合缩放

### 小红书参考要点

| 小红书特征 | 对应本项目 |
|-----------|-----------|
| 浅色优先 + 暗色跟随系统 | 新增 `LightColorScheme`，删除 dark-only 硬编码 |
| 瀑布流卡片：图上文下 | PostCard 重构：移除 TitleOverlay 渐变，标题放到图片下方 |
| 顶部悬浮搜索栏 | 首页顶部 Search bar（已有 toggleable search，优化为常驻搜索图标 + 点击展开） |
| 分类 chip 横滑 | 已有 `FilterChip` row，优化视觉 |
| 底部 5 tab + center 凸起 | 已有结构，优化 center 按钮造型 |
| 个人主页：头像+统计+帖子网格 | Profile 页全面重构 |
| 发布页：选图+编辑+标签 | Create 页全面重构 |

---

## 三、分阶段实施计划

### Phase 1: 设计系统重构（基础层）

> 这是所有后续工作的地基。没有好的 design token 体系，后面的屏幕打磨都是空中楼阁。

#### 1.1 浅色/暗色双主题

**文件**：`ui/theme/Color.kt` + `ui/theme/Theme.kt`

新增 `LightColorScheme`，保持暗色为 ZZZ 氛围色，浅色为干净的白/灰体系：

```
浅色模式:
  background     = #FFFFFF (纯白)
  surface        = #F5F5F5 (浅灰底)
  surfaceContainer = #FFFFFF (卡片白)
  surfaceContainerHigh = #FAFAFA
  surfaceContainerHighest = #F0F0F0
  onSurface      = #1A1A1A
  onSurfaceVariant = #6B6B6B
  outline        = #E0E0E0
  outlineVariant = #EEEEEE
  primary        = #BFFF09 → 浅色下调暗为 #A6D900 (避免刺眼)
  onPrimary      = #0A0A0A

暗色模式 (保持现有):
  background     = #0A0A0A
  surface        = #1A1A1A
  ... (现有值不动)
  primary        = #BFFF09 (保持)
```

删除 `Theme.kt` 第 55-57 行的 `else -> DarkColorScheme` 硬编码，改为：
```kotlin
val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
```

扩展 `InterknotExtendedColors` 增加浅色变体：
```kotlin
data class InterknotExtendedColors(
    val titleRead: Color,       // 浅: #9E9E9E  暗: #9E9E9E (不变)
    val titleUnread: Color,     // 浅: #1A8CFF  暗: #2196F3
    val link: Color,            // 浅: #4A7AFF  暗: #6F9CFF
    val online: Color,          // 浅: #34C759  暗: #4ADE80 (Apple green)
    val knockBadge: Color,      // 浅: #FF3B30  暗: #FF3838 (Apple red)
    // 新增
    val shimmerBase: Color,     // 骨架屏底色
    val shimmerHighlight: Color // 骨架屏高光色
)
```

#### 1.2 排版系统精修

**文件**：`ui/theme/Type.kt`

问题：当前全部用 `FontFamily.Default` + 手动设字号。Apple 做法是极简字号梯度，靠字重和行高创造层级。

保持 `FontFamily.Default`（Android 上即 Roboto，足够好），但调整数值对齐 Apple HIG：

```
displayLarge  → 不使用（社区 APP 不需要 32sp 巨字）
displayMedium → 不使用
displaySmall  → 24sp / 32sp line / Bold    (仅个人主页大数字)
headlineLarge → 22sp / 28sp / Bold          (帖子标题)
headlineMedium→ 20sp / 26sp / Bold          (评论区标题)
headlineSmall → 18sp / 24sp / Semibold      (页面标题)
titleLarge    → 17sp / 22sp / Semibold      (卡片标题 — 对齐 Apple NavigationBar Title)
titleMedium   → 15sp / 20sp / Medium        (列表项标题)
titleSmall    → 14sp / 18sp / Medium        (小标题)
bodyLarge     → 16sp / 24sp / Regular        (帖子正文 — 对齐 Apple Body)
bodyMedium    → 14sp / 20sp / Regular        (评论正文)
bodySmall     → 13sp / 18sp / Regular        (元信息)
labelLarge    → 14sp / 18sp / Medium         (按钮文字)
labelMedium   → 12sp / 16sp / Medium         (标签/chip)
labelSmall    → 11sp / 14sp / Medium         (时间戳/计数)
```

关键变化：`headlineSmall` 从 18sp 改为 Semibold 而非 Bold；`titleLarge` 从 Bold 改为 Semibold（Apple 的 NavigationBar title 就是 Semibold）。

#### 1.3 间距系统

新建 `ui/theme/Spacing.kt`：

```kotlin
object Spacing {
    val xs = 4.dp    // 图标内间距、极小间隔
    val sm = 8.dp    // 卡片内元素间距
    val md = 12.dp  // 卡片 padding
    val lg = 16.dp  // 屏幕水平 padding、section 间距
    val xl = 24.dp  // 大区块间距
    val xxl = 32.dp // 页面间距
}
```

全局审计：把 `PostCard.kt` 里的 `10.dp`（第 173 行）、`12.dp`（第 154 行）等非标准值统一到 8/12/16 体系。

#### 1.4 动效规格

新建 `ui/theme/Motion.kt`：

```kotlin
object Motion {
    // Spring 配置 — 对齐 Apple UIKit default spring
    val pressSpring = SpringSpec<Float>(dampingRatio = 0.7f, stiffness = Spring.StiffnessMedium)
    val expandSpring = SpringSpec<Float>(dampingRatio = 0.8f, stiffness = Spring.StiffnessLow)
    
    // 页面转场
    val pageTransitionDuration = 300 // ms, standard
}
```

当前 PostCard 的 `animateFloatAsState` 缺少 spring spec，改用物理弹簧。

#### 1.5 Shape 微调

**文件**：`ui/theme/Shape.kt`

当前值合理，微调以对齐 Apple 的连续圆角感：

```kotlin
val InterknotShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // 4→6, chip 更圆润
    small = RoundedCornerShape(10.dp),        // 8→10, 输入框
    medium = RoundedCornerShape(14.dp),       // 12→14, 卡片
    large = RoundedCornerShape(18.dp),        // 16→18, 底部弹窗
    extraLarge = RoundedCornerShape(28.dp)     // 24→28, 对齐 Apple sheet
)
```

---

### Phase 2: 首页信息流打磨（核心体验）

> 首页是用户停留最久的页面，直接决定 APP 的品质感。

#### 2.1 PostCard 重构

**文件**：`ui/components/post/PostCard.kt`

**当前问题**：标题用黑色渐变叠加在图片底部（第 141-165 行 `TitleOverlay`），遮挡内容。小红书做法是图片下方独立文字区域。

**新设计**：

```
┌─────────────────┐
│                 │
│     封面图片      │  ← aspectRatio 保留原始比例
│                 │
├─────────────────┤
│ 帖子标题(2行)    │  ← 独立文本区，onSurface 色
│ 👤 作者名  ❤ 88 │  ← 元信息行
└─────────────────┘
```

具体改动：
- 删除 `TitleOverlay` composable（渐变叠层）
- `PostMeta` 移到图片下方，增加标题文本
- 卡片 padding 统一为 `Spacing.md`（12dp）
- 作者头像从 24dp 改为 20dp（更精致）
- 点赞数加粗用 `FontWeight.SemiBold`
- 无封面时显示纯文字卡片（标题+作者），不显示图片占位

#### 2.2 骨架屏 Loading

新建 `ui/components/common/ShimmerBox.kt` + `ShimmerEffect.kt`：

```
加载态: 显示 6 个灰底卡片占位，封面区 shimmer 动画，文字区灰色条
```

使用 `Brush.linearGradient` + `animateFloat` 实现 shimmer 效果。扩展色 `shimmerBase` / `shimmerHighlight` 已在 1.1 中定义。

#### 2.3 下拉刷新

首页外层包 `PullToRefreshBox`（Material 3 1.3+）：

- 拉取阈值 64dp
- 刷新指示器使用 `primary` 色
- 刷新中旋转动画
- 释放后 spring 回弹

#### 2.4 顶部栏优化

**文件**：`ui/screens/home/HomeScreen.kt` 中的 `HomeTopBar`

当前：可切换搜索框 + 两个 tab + 分类 chip 行

优化为：
- 顶部常驻搜索图标按钮（点击展开全屏搜索页，而非 inline 输入框）
- Tab "推荐" / "关注" 使用 Material 3 `PrimaryTabRow`，指示器为 primary 色下划线
- 分类 chip 横滑：选中态填色 primary + onPrimary 文字，未选中态 outline
- 搜索框改为独立 Explore 页面入口

#### 2.5 无限滚动底部加载指示器

底部添加 `LinearProgressIndicator`（indeterminate），当 `hasMore && isLoading` 时显示。

#### 2.6 Empty / Error 状态

- Empty: 居中插画 + "还没有帖子，去发布第一条吧" + primary 色按钮
- Error: 居中错误图标 + "加载失败" + "重试" 按钮
- 无网络: 顶部横条提示 "网络不可用" + dismiss

---

### Phase 3: 帖子详情页打磨

#### 3.1 共享元素转场

从首页卡片点击进入详情页时，封面图片做 shared element transition。

使用 Compose `SharedTransitionLayout` + `Modifier.sharedElement()`（需要 Compose 1.7+ 的实验 API）。

#### 3.2 图片轮播增强

**文件**：`PostDetailScreen.kt` 中的 `CoverPager`

- 增加 pinch-to-zoom（使用 `Modifier.transformable`）
- 双击双指缩放
- 页码指示器从底部圆点改为 "1/5" 文字式（更简洁）
- 长按图片弹出菜单：保存到相册、举报

#### 3.3 作者信息行

- 头像 36dp（当前偏小），圆形 clip
- 作者名 + Lv 徽章 + 关注按钮（已登录时显示）
- 时间 + 浏览量靠右，labelSmall 灰色

#### 3.4 评论 UX 重构

**文件**：`PostDetailScreen.kt` 中的 `CommentItem`

当前问题：嵌套回复层级不清晰，缺少楼层号引导

新设计：
- 楼层号 `#2` 用 outline chip 标注在头像右侧
- 置顶评论顶部固定，带 `📌` 图标 + "置顶" 标签
- 嵌套回复缩进 32dp，左侧 2dp 竖线（outlineVariant 色）
- 点赞按钮在右侧，点赞后图标变 primary 色 + 弹簧缩放动画
- 点击评论可回复，底部弹出输入框 + "回复 @作者名" 前缀
- 表情系统：后端已有 `GET /emotes/manifest`，接入表情面板

#### 3.5 底部操作栏

当前：评论输入 + 点赞/收藏/星/丁尼按钮

优化为：
- 输入框用 `Surface` 圆角包裹（`Shape.large`），而非 `OutlinedTextField`
- 点赞按钮使用 `FilledIconButton` 态，已点赞 → primary 填色
- 收藏按钮同理
- "投丁尼" 改为独立弹窗（当前直接在底栏操作太重）
- 底栏高度 56dp + safeDrawing insets

---

### Phase 4: 发布流程重建

> 后端已支持多图封面、分类、标签、富文本编辑器状态。当前前端仅标题+正文，差距巨大。

#### 4.1 全新发布页

**文件**：`ui/screens/create/CreateScreen.kt`

参考小红书发布流程：

```
┌─────────────────────────────────────┐
│  ← 草稿自动保存        发布按钮      │
├─────────────────────────────────────┤
│                                     │
│  [图片1] [图片2] [图片3] [+]        │  ← 横滑图片选择器
│                                     │
├─────────────────────────────────────┤
│  标题输入(单行, 50字限制)            │
├─────────────────────────────────────┤
│                                      │
│  正文输入(多行, 8000字限制)          │
│  带字数统计                          │
│                                      │
├─────────────────────────────────────┤
│  📁 选择分类    ›                    │
│  🏷 添加标签    ›                    │
│  👤 匿名发布    [开关]                │
├─────────────────────────────────────┤
│           预览模式  /  发布           │
└─────────────────────────────────────┘
```

#### 4.2 图片选择与上传

接入后端 Direct Upload 流程：
1. `POST /direct-upload/sign` 获取预签名 URL
2. 客户端直接 PUT 到 S3 (七牛云 im.tiwat.cn)
3. `POST /direct-upload/complete` 注册文件

图片选择器：使用 `ActivityResultContracts.PickMultipleVisualMedia`（Android 照片选择器 API）

限制：根据用户 level 查询 `GET /benefits/me` 获取 `articleMaxImages` 上限

#### 4.3 分类选择器

`GET /categories/list` 获取分类列表 → 底部弹窗 `ModalBottomSheet` 选择

#### 4.4 标签输入

后端已有标签系统：联想 `GET /articles/suggest?type=tag`（需确认后端是否有标签 suggest 端点，如没有可用 `/articles/suggest?q=` 变通）

UI: chips + 联想下拉，已在 web 端实现，可直接参考

#### 4.5 草稿自动保存

利用 DataStore 定时保存编辑状态，APP 重启后恢复

---

### Phase 5: 探索/搜索页（替代废弃的 Explore）

#### 5.1 新 Explore 页面

**文件**：`ui/screens/explore/ExploreScreen.kt`（完全重写）

注册到导航图，替换当前 Level 的位置（Level 改为 Profile 的子页）。

内容结构：
```
┌─────────────────────────────────────┐
│  🔍 搜索框(点击展开全屏搜索)          │
├─────────────────────────────────────┤
│  热门帖子 (横向滚动卡片)              │
│  ┌────┐ ┌────┐ ┌────┐              │
│  │帖1 │ │帖2 │ │帖3 │              │
│  └────┘ └────┘ └────┘              │
├─────────────────────────────────────┤
│  分类浏览                            │
│  [攻略] [同人图] [讨论] [资讯] ...    │
├─────────────────────────────────────┤
│  热门搜索词 (tag cloud 或列表)       │
├─────────────────────────────────────┤
│  全部帖子瀑布流(默认推荐流)           │
│  (复用 PostCard)                    │
└─────────────────────────────────────┘
```

数据来源：
- 热门帖子：复用首页推荐流 API `GET /articles/list?feed=recommend`
- 分类浏览：`GET /categories/list`
- 搜索：`GET /articles/search?q=`

#### 5.2 搜索体验

- 点击搜索框展开全屏搜索页
- 输入时实时联想 `GET /articles/suggest?q=`
- 搜索历史本地存储（DataStore）
- 搜索结果瀑布流展示
- 高亮匹配关键词

---

### Phase 6: 个人主页与作者页

#### 6.1 Profile 页全面重构

**文件**：`ui/screens/profile/ProfileScreen.kt`

当前：仅显示用户名/邮箱/等级/丁尼余额/退出按钮 —— 太简陋

参考小红书个人主页：

```
┌─────────────────────────────────────┐
│          [头像 80dp 圆形]            │
│          用户名 (headlineSmall)      │
│          Lv.3 · 签到3天              │
│                                     │
│  帖子 12    关注 28    粉丝 56      │
├─────────────────────────────────────┤
│  [编辑资料]  [设置]                  │
├─────────────────────────────────────┤
│  📌 置顶帖子 (横向滚动)              │
├─────────────────────────────────────┤
│  Tab: 帖子 | 喜欢 | 收藏             │
│  ┌────┐ ┌────┐ ┌────┐              │
│  │帖1 │ │帖2 │ │帖3 │  ← 瀑布流     │
│  └────┘ └────┘ └────┘              │
└─────────────────────────────────────┘
```

接入后端：
- `GET /me/profile` 用户信息
- `GET /me/profile/pinned-articles` 置顶帖子
- `GET /me/exp` 等级经验
- `GET /user-denny` 丁尼余额
- `GET /articles/my` 我的帖子
- `GET /articles/my/published` 已发布
- `GET /articles/my/drafts` 草稿
- `GET /likes/my-list` 我点赞的
- `GET /favorites/list` 我收藏的
- `GET /check-in/status` 签到状态 → `POST /check-in` 签到
- `GET /me/avatars` 头像装扮
- `GET /me/business-cards` 名片装扮

#### 6.2 作者主页

新建 `ui/screens/author/AuthorScreen.kt`

- `GET /profiles/:documentId` 作者信息
- `GET /profiles/:documentId/articles` 作者帖子
- `GET /profiles/:documentId/comments` 作者评论
- `POST /follows/toggle` 关注/取关
- `GET /follows/check` 检查关注状态

#### 6.3 等级与福利页

把 Level stub 页实现为真正的等级页：

- `GET /me/exp` 当前经验/等级
- `GET /me/exp/daily` 每日经验明细
- `GET /benefits/me` 等级福利矩阵
- 等级进度条（Apple 风格圆角进度条）
- 福利对比表

#### 6.4 设置页

新建 `ui/screens/settings/SettingsScreen.kt`

- `GET /me/security` 安全信息
- `PUT /me/profile/name` 修改昵称
- `PUT /me/profile/bio` 修改简介
- `PUT /me/profile/visibility` 主页可见性
- `PUT /me/avatars/equip` 装备头像
- `PUT /me/business-cards/equip` 装备名片
- `GET /me/uploads` 我的上传
- 主题切换（浅色/暗色/跟随系统）
- `POST /reports` 举报入口
- `GET /user-blocks/my-list` 拉黑管理
- 关于 InterKnot

---

### Phase 7: 消息与私信实时化

#### 7.1 SSE 实时通知

**文件**：`ui/screens/knock/KnockViewModel.kt`

当前：每 15 秒 HTTP 轮询 `GET /notifications/unread-count` + `GET /knock/conversations`

改为 SSE 长连接：
- `GET /knock/stream` (EventSource, 支持 `?token=` 查询参数鉴权)
- 事件类型：`notification.created`, `notification.read`, `notification.read.bulk`
- ViewModel 中用 OkHttp EventSource 建立连接
- 收到事件后增量更新 UI，不再全量拉取
- 15 秒轮询降级为 fallback（SSE 断线时兜底）

#### 7.2 Knock 页 UI 优化

- 会话列表：头像 + 名称 + 未读红点 + 最后消息预览 + 时间
- 消息详情：气泡式布局（己方右对齐 primary 浅色，对方左对齐 surface）
- AI 角色对话标记（`isAiAgent` 的消息用特殊头像/边框）
- 滑动会话标记已读

#### 7.3 WebSocket 实时私信

后端已有完整 WebSocket 基础设施 (`/dm/socket`)：
- `POST /dm/socket/ticket` 获取 30 秒一次性 ticket
- `wss://.../dm/socket?ticket=<ticket>` 连接
- 服务端推送：`message.created`, `message.edited`, `message.deleted`, `conversation.updated`, `dm.typing`

APP 端实现：
- 新建 `data/ws/WebSocketClient.kt`
- 连接管理：ticket 获取 → WS 连接 → 心跳 → 断线重连
- 消息收发实时化，替代当前轮询
- Typing indicator：对方正在输入时显示 "..." 动画

#### 7.4 私信页面

新建 `ui/screens/dm/DMScreen.kt`

- 会话列表页（复用 Knock 的会话数据，DM + 通知合并展示）
- 消息详情页：气泡 + 输入框 + 发送按钮
- 图片消息支持
- 消息撤回（5 分钟内，`DELETE /dm/messages/:id`）
- 消息编辑（5 分钟内，`PATCH /dm/messages/:id`）
- 回复消息（`replyTo` 字段）
- AI 角色对话特殊样式

---

### Phase 8: 导航与动效打磨

#### 8.1 底部导航栏精修

**文件**：`ui/components/navigation/InterknotBottomNav.kt`

当前 center 按钮是一个 40dp 圆形 Surface + 4dp shadow —— 太朴素。

优化：
- center 按钮改为 48dp 圆形 + 8dp 外发光阴影
- 点击时 spring 弹跳动画（scale 1.0 → 1.15 → 1.0）
- 5 个 tab 调整为：首页 / 探索 / 发布 / 敲敲 / 我的
  - 用 Explore 替换 Level（Level 移入 Profile 子页）
  - "推送" → "首页"（更直觉）
- 选中态图标用 primary 色 + 下方 3dp 圆点指示器
- 未选中态用 onSurfaceVariant 色
- NavigationBar 容器用 `surfaceContainer` + 顶部 0.5dp hairline border（`outlineVariant`）

#### 8.2 屏幕转场动画

使用 Navigation 3 的 `EntryTransition` / `ExitTransition`：

- 前进：slide from right (300ms, standard decelerate) + fade in
- 后退：slide to right (200ms, standard accelerate) + fade out
- 底部 tab 切换：fade crossfade (200ms)
- 详情页进入：container transform（如果 SharedTransition 不可用则用 fade+slide）

#### 8.3 手势支持

- 预测性返回手势（Android 14+ predictive back）
- 首页下拉刷新
- 图片详情双指缩放
- 评论左滑回复
- 长按帖子弹出快捷菜单（点赞/收藏/举报/不感兴趣）

#### 8.4 Haptic Feedback

关键交互添加触觉反馈：
- 点赞成功：light tick
- 发布成功：medium tick
- 下拉刷新触发：light tick
- 底部 tab 切换：selection tick
- 错误提示：double light tick

使用 `HapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)` 等 API。

#### 8.5 统一状态组件

新建 `ui/components/common/`：

```
EmptyState.kt     — 居中插画 + 文字 + 按钮
ErrorState.kt     — 错误图标 + 文字 + 重试按钮
LoadingState.kt   — 骨架屏 / spinner
OfflineBanner.kt  — 顶部离线提示条
```

---

## 四、后端能力对接清单

下表列出后端已有但 APP 未接入的 API，按优先级排序：

| 优先级 | 后端能力 | API 端点 | 对接阶段 |
|--------|---------|---------|---------|
| P0 | Direct Upload 直传 | `POST /direct-upload/sign` + `/complete` | Phase 4 |
| P0 | 分类列表 | `GET /categories/list` | Phase 4 |
| P0 | 文章搜索 | `GET /articles/search` | Phase 5 |
| P0 | 搜索联想 | `GET /articles/suggest` | Phase 5 |
| P0 | 用户资料 | `GET /me/profile` | Phase 6 |
| P0 | 我的帖子 | `GET /articles/my` | Phase 6 |
| P0 | 签到 | `GET /check-in/status` + `POST /check-in` | Phase 6 |
| P0 | 关注系统 | `POST /follows/toggle` + `GET /follows/check` | Phase 6 |
| P1 | SSE 实时通知 | `GET /knock/stream` | Phase 7 |
| P1 | WebSocket 私信 | `POST /dm/socket/ticket` + `ws://` | Phase 7 |
| P1 | 消息发送 | `POST /dm/conversations/:id/messages` | Phase 7 |
| P1 | 消息列表 | `GET /dm/conversations/:id/messages` | Phase 7 |
| P1 | 收藏列表 | `GET /favorites/list` | Phase 6 |
| P1 | 点赞列表 | `GET /likes/my-list` | Phase 6 |
| P2 | 表情系统 | `GET /emotes/manifest` | Phase 3 |
| P2 | 作者主页 | `GET /profiles/:documentId` + articles | Phase 6 |
| P2 | 等级福利 | `GET /benefits/me` | Phase 6 |
| P2 | 头像装扮 | `GET /me/avatars` + `PUT /me/avatars/equip` | Phase 6 |
| P2 | 名片装扮 | `GET /me/business-cards` + `PUT /me/business-cards/equip` | Phase 6 |
| P2 | 阅读记录 | `POST /article-reads/batch` | Phase 3 |
| P3 | 一键三连 | `POST /articles/triple` | Phase 3 |
| P3 | 举报 | `POST /reports` | Phase 3 |
| P3 | 拉黑 | `POST /user-blocks/toggle` | Phase 6 |
| P3 | 米游社绑定 | `POST /auth/mihoyo/qr` + polling | Phase 6 |
| P3 | 在线状态 | `GET /presence/online` | Phase 6 |

---

## 五、技术要点

### 5.1 依赖升级

| 当前 | 建议升级 | 原因 |
|------|---------|------|
| Compose BOM 2024.09.03 | 最新 BOM | SharedTransitionLayout 等 API |
| Navigation3 1.0.0 | 最新 alpha | 更稳定的转场 API |
| Coil 2.7.0 | 3.x | 更好的缓存控制 |
| — | + OkHtp EventSource | SSE 支持 |
| — | + accompanist-permissions | 相机/存储权限 |

### 5.2 Room 本地缓存

当前 Room 已声明依赖但未使用。建议接入：
- 首页文章列表缓存（首次加载秒开，后台刷新）
- 用户信息缓存
- 搜索历史
- 草稿编辑状态

### 5.3 预览函数

每个新 Composable 必须带 `@Preview`，浅色/暗色双预览。当前只有 PostCard 有 Preview。

### 5.4 架构边界

- 新屏幕统一 `ui/screens/<name>/` 目录，含 `Screen.kt` + `ViewModel.kt`
- 新 DTO 放 `data/api/dto/`，带 `toDomain()` 映射
- 新 API 方法定义在 `InterknotApi.kt` interface 中，实现写在 `InterknotApiImpl.kt`
- Repository 方法写在 `InterknotRepository.kt`，返回 `Result<T>`

---

## 六、实施优先级

```
Phase 1 (设计系统)  ████████  地基，必须先做
       ↓
Phase 2 (首页打磨)  ████████  核心体验，用户最先感知
       ↓
Phase 8.1-8.5 (导航+状态) ████  底座打磨
       ↓
Phase 3 (帖子详情)  ██████   核心阅读体验
       ↓
Phase 6 (个人主页)  ██████   用户身份认同
       ↓
Phase 5 (探索搜索)  █████    发现机制
       ↓
Phase 4 (发布重建)  █████    创作闭环
       ↓
Phase 7 (实时消息)  ████     即时性体验
       ↓
Phase 8 其余 (动效)  ███     最后的 1% 精度
```

建议按此顺序执行，每个 Phase 完成后做一次完整走查（浅色/暗色双模式 + 不同屏幕尺寸），确保品质达标再进入下一阶段。
