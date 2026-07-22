# InterKnot-Android 仿小红书论坛 App 实现计划

> 目标：以 `ikserver` 后端能力为基础，将 `InterKnot-Android` 升级为类小红书（瀑布流图文社区）的论坛客户端。
> 本计划与现有 `docs/UI_SPEC.md`（设计 token、组件规范）和 `docs/ui-optimization-plan.md`（UI 阶段路线图）配套阅读。

---

## 1. 目标与范围

- **产品形态**：双列瀑布流首页、搜索发现、帖子详情、评论互动、发布委托、个人主页、消息通知、私信、等级签到。
- **后端依据**：`KawaYiLab/ikserver`（Strapi v5 + 自定义控制器）提供的 REST/SSE/WebSocket 接口。
- **前端参考**：`KawaYiLab/InterKnot-Web`（Nuxt 3 / Vue 3）的 `useApi.ts`、`format-body.ts`、`types/entities.ts`、`sse.ts`、`useDmStream.ts` 作为数据与交互权威实现。
- **Android 基础**：已存在 Jetpack Compose + Material 3 + Hilt + Ktor + Coil + DataStore + Room(ksp) + Navigation3 的骨架。

本阶段只交付 **计划/设计文档**，不直接写业务代码。

---

## 2. 后端能力总览（ikserver）

API Base：`http://43.248.77.159:31338/api/`

### 2.1 核心实体与约束

| 实体 | 关键字段 / 约束 |
|---|---|
| **Article（委托/帖子）** | `title` 1-50 字；`text` 富文本/markdown，最大 8000 字；`cover` 多图（数量受等级权益限制）；`isAnonymous`、`anonymousSeed`、`isHidden`、`bumpedAt`；`views/likescount/commentscount/denny_count/favoritesCount`；`blocks` 动态区（media/quote/rich-text/slider）；`editorState` JSON。 |
| **Comment** | `content` 最大 2000 字；`images` 多图；`parent/replies` 嵌套；`pinnedAt` 置顶；`floor` 楼号；`isAnonymous`、`isHidden`；`likescount`。 |
| **User / Author** | `level` 1-7、`exp`、`denny` 0-9999、`lastCheckInDate`、`consecutiveCheckInDays`、`examPassed`、`profileHidden`、`equippedAvatar`、`equippedCard`；与 `author` 一对一。 |
| **Category** | `name/slug/order/adminOnly`，作为首页/发现的分类标签。 |
| **Like / Favorite / Follow** | 互动动词，均提供 toggle + check + list。 |
| **Notification / Knock** | `notification` 聚合为 pseudo 会话，`/knock/stream` 提供 SSE 实时推送。 |
| **DM Conversation / Message** | 真实私聊；`POST /dm/socket/ticket` 换取一次性 ticket 后通过 WebSocket `/dm/socket` 连接。 |
| **Direct Upload** | `POST /direct-upload/sign` 返回 S3 presigned URL；客户端 PUT 文件后 `POST /direct-upload/complete`；支持 `contentHash` 去重。 |
| **Exam / Check-in / Benefit** | 入站考试、每日签到、等级权益（发帖图数/字数、评论图数上限）。 |
| **Report / User-block** | 举报帖子/评论/用户；拉黑/取消拉黑。 |

### 2.2 主要 API 端点清单

| 领域 | 端点 | 方法 | 说明 |
|---|---|---|---|
| **Auth** | `/auth/local` | POST | 邮箱/用户名 + 密码登录 |
| | `/auth/register-with-code` | POST | 邮箱验证码注册 |
| | `/auth/send-register-code`、`/auth/send-reset-code` | POST | 发验证码 |
| | `/auth/reset-password`、`/auth/renew` | POST | 重置密码、续签 JWT |
| | `/auth/mihoyo/qr`、`/auth/mihoyo/qr/status`、`/auth/mihoyo/binding` | GET/POST | 米游社扫码登录与绑定 |
| **Article** | `/articles/list?feed&category&start&limit` | GET | 首页/关注/收藏 瀑布流 |
| | `/articles/detail/:documentId` | GET | 帖子详情 |
| | `/articles/search?q&category&start&limit` | GET | Meilisearch 全文搜索 |
| | `/articles/suggest?q&category&limit` | GET | 搜索联想 |
| | `/articles/my`、`/articles/my/drafts`、`/articles/my/published` | GET | 我的文章 |
| | `/articles`（`?status=draft`/`published`） | POST/PUT | 创建/更新文章或草稿 |
| | `/articles/:id/publish`、`/unpublish`、`/discard-draft` | POST | 发布/下架/丢弃草稿 |
| | `/articles/:id` | DELETE | 删除 |
| | `/articles/:documentId/view` | POST | 记录浏览 |
| | `/articles/triple` | POST | 一键三连（点赞+收藏+投币） |
| | `/article-reads/batch` | POST | 批量标记已读 |
| **Comment** | `/comments/list?articleDocumentId&start&limit` | GET | 评论列表（含置顶+嵌套回复） |
| | `/comments` | POST | 发表评论（支持 `parentDocumentId`） |
| | `/comments/:id` | DELETE | 删除评论 |
| | `/comments/:documentId/pin`、`/unpin` | POST | 置顶/取消置顶 |
| **Social** | `/likes/toggle`、`/likes/check`、`/likes/my-list` | POST/GET | 点赞 |
| | `/favorites/toggle`、`/favorites/check`、`/favorites/list` | POST/GET | 收藏 |
| | `/follows/toggle`、`/follows/check`、`/follows/following` | POST/GET | 关注 |
| **Profile** | `/profiles/:documentId` | GET | 作者主页 |
| | `/profiles/:documentId/articles`、`/profiles/:documentId/comments` | GET | 作者文章/评论分页 |
| | `/authors/search` | GET | @ 提及/作者搜索 |
| **Me** | `/me/profile` | GET | 当前用户基础信息 |
| | `/me/profile/name`、`/me/profile/bio`、`/me/profile/visibility`、`/me/profile/pinned-articles` | PUT | 修改昵称/签名/可见性/置顶帖 |
| | `/me/exp`、`/me/exp/daily` | GET | 经验值与每日来源 |
| | `/me/avatars`、`/me/avatars/equip`、`/me/avatars/upload-custom` | GET/PUT | 头像装扮 |
| | `/me/business-cards`、`/me/business-cards/equip` | GET/PUT | 名片装扮 |
| | `/me/uploads` | GET | 个人上传图库 |
| | `/me/security`、`/me/email/send-code`、`/me/email` | GET/PUT | 账号安全与邮箱绑定 |
| | `/user-denny`、`/user-denny/give` | GET/POST | 丁尼余额与投币 |
| **Notification / Knock** | `/notifications/list`、`/notifications/unread-count` | GET | 通知列表/未读数 |
| | `/notifications/:id/mark-read`、`/notifications/mark-all-read` | PUT | 已读 |
| | `/knock/conversations`、`/knock/conversations/:id/messages` | GET | 伪会话/消息 |
| | `/knock/conversations/:id/mark-read` | POST | 会话已读 |
| | `/knock/stream` | SSE | 实时推送 |
| **DM** | `/dm/conversations` | GET | 会话列表 |
| | `/dm/conversations/direct`、`/dm/conversations/group` | POST | 创建直接/群聊会话 |
| | `/dm/conversations/:id` | GET/PATCH | 会话详情/更新 |
| | `/dm/conversations/:id/read`、`/dm/read-all` | PATCH/POST | 已读 |
| | `/dm/conversations/:id/messages` | GET/POST | 历史消息/发送消息 |
| | `/dm/messages/:id` | PATCH/DELETE | 编辑/撤回消息（5 分钟内） |
| | `/dm/conversations/:id/leave`、`/reset-context` | POST | 离开群聊/重置上下文 |
| | `/dm/socket/ticket` + `/dm/socket` | POST + WS | WebSocket 实时私聊 |
| **Upload** | `/direct-upload/sign`、`/direct-upload/complete` | POST | S3 直传签名与完成 |
| **Exam** | `/exam/status`、`/exam/start`、`/exam/submit`、`/exam/review` | GET/POST | 入站考试 |
| **Check-in** | `/check-in`、`/check-in/status` | POST/GET | 每日签到 |
| **Benefit** | `/benefits/me` | GET | 当前等级权益 |
| **Report / Block** | `/reports`、`/reports/check`、`/reports/my-list` | POST/GET | 举报 |
| | `/user-blocks/toggle`、`/user-blocks/check`、`/user-blocks/my-list` | POST/GET | 拉黑 |
| **Presence** | `/presence/ping`、`/presence/online` | POST/GET | 在线心跳/在线列表 |
| **Emote** | `/emotes/manifest` | GET | 表情配置 |

---

## 3. 小红书功能 ↔ 后端端点映射

| 小红书式功能 | 后端端点 / 行为 | 备注 |
|---|---|---|
| 首页双列瀑布流 | `GET /articles/list?feed=recommend` + `category` | 含热帖池注入 |
| 分类 Tab | `GET /categories/list` | 按 `order` 排序，过滤 `adminOnly` |
| 关注流 | `GET /articles/list?feed=following` | 需登录 |
| 收藏流 | `GET /articles/list?feed=favorites` 或 `/favorites/list` | 可复用列表组件 |
| 搜索 | `GET /articles/search`（Meilisearch） + `/articles/suggest` | 联想与结果两套接口 |
| 帖子详情 | `GET /articles/detail/:id`、`POST /articles/:id/view`、`GET /comments/list` | 详情已内联 `liked/favorited/hasGivenDenny/isRead` |
| 点赞 | `POST /likes/toggle` | 支持 `article`/`comment` |
| 收藏 | `POST /favorites/toggle` | 仅文章 |
| 一键三连 | `POST /articles/triple` | 原子点赞+收藏+投币 |
| 投币 | `POST /user-denny/give` | 消耗 1 Denny/篇，不可自投 |
| 评论 | `POST /comments`（`parentDocumentId` 为回复） | 评论也支持图片 |
| 关注作者 | `POST /follows/toggle` | 个人页/帖子头部均调用 |
| 发布帖子 | 图片 `direct-upload` → `POST /articles?status=draft` → `POST /articles/:id/publish` | 创建/更新文章需通过入站考试 |
| 个人主页 | `GET /profiles/:id` + `/profiles/:id/articles` + `/profiles/:id/comments` | 含 `isFollowing/followersCount/stats` |
| 我的资料 | `GET /me/profile` + 各种 `/me/*` | 头像/名片/置顶帖/安全 |
| 等级/签到 | `GET /me/exp`、`GET /benefits/me`、`POST /check-in` | 等级上限 7 级 |
| 通知 | `GET /notifications/list` + `SSE /knock/stream` | SSE 需 Bearer token，POST 方式 |
| 敲敲（伪私信） | `GET /knock/conversations` + `/knock/conversations/:id/messages` | 由通知聚合而成 |
| 真实私信 | `GET /dm/conversations` + WebSocket `/dm/socket` | ticket 鉴权，应用层 ping |
| 举报/拉黑 | `POST /reports`、`POST /user-blocks/toggle` | 拉黑后双向取消关注并隐藏内容 |

---

## 4. Android 现状与缺口

### 4.1 已具备的基础

- **导航**：`InterknotRoute` + `InterknotBottomNav`（5 个 Tab），Navigation3 路由。
- **网络**：`InterknotApi.kt` 接口 + `InterknotApiImpl.kt`（Ktor）+ `TokenManager`。
- **UI 组件**：`PostCard.kt`（基础卡片）、`HomeScreen`、`PostDetailScreen`、`CreateScreen`、`LoginScreen`、`ProfileScreen`、`KnockScreen`。
- **设计系统**：`Color.kt`、`Spacing.kt`、`Motion.kt`、`Type.kt`、`Shape.kt`，已定义深色/浅色两套 token。
- **依赖**：Hilt、KSP、Kotlinx Serialization、Coil、DataStore、Room、Navigation3。

### 4.2 核心缺口

| 层 | 缺口 |
|---|---|
| **API 接口** | `InterknotApi` 仅实现约 14 个方法，缺少 follow/favorite/triple/report/block/profile/dm/check-in/exam/benefits/avatar/business-card/direct-upload 等端点。 |
| **数据模型** | 现有 `Article`/`Comment`/`User` 等模型字段不齐，缺少 `coverNsfwStatus`、`isOwner`、`favoritesCount`、评论回复、装备名片等。 |
| **图片上传** | 发布页只有标题与纯文本，没有图片选择器，也未接入 S3 直传。 |
| **富文本渲染** | 后端 `text` 是 markdown，需安全渲染；`editorState` 结构化块可选支持。 |
| **实时通信** | Knock 目前使用轮询，需接入 `/knock/stream` SSE；DM 完全未实现。 |
| **搜索/发现** | `ExploreScreen` 是 stub，`SearchScreen` 缺失。 |
| **个人/等级** | `ProfileScreen` 较简单，未接入 `/profiles/:id` 完整数据、`/check-in`、`/benefits/me`、装扮等。 |
| **发布流程** | 缺少草稿自动保存、分类选择、匿名开关、考试拦截、等级权益校验。 |
| **缓存/离线** | Room 已配置但尚未持久化列表/详情/搜索/用户数据。 |

### 4.3 需先修复的小问题

- `model/Article.kt` 中 `coverUrl` 字段重复声明，需清理。
- `InterknotApi` 参数中 `isAnonymous: boolean` 与 `Boolean` 混用，需统一为 Kotlin `Boolean`。
- API 基址为 `http://`，Android 9+ 需 `network_security_config` 允许明文传输，或建议后端切 HTTPS。

---

## 5. 架构与数据层设计

### 5.1 整体架构

```
UI Layer (Compose Screens / ViewModels / StateFlow)
        │
Domain Layer (UseCase / Model / Repository Interface)
        │
Data Layer
├── Remote: InterknotApi (Ktor) + DTO + Mappers
├── Local:  Room (feed/search/profile/message cache) + DataStore (session/settings/search history)
└── Upload: DirectUploadManager (S3 presign + PUT + complete)
        │
Platform: TokenManager, HttpClient, ImageLoader (Coil), SSE, WebSocket, MarkdownRenderer
```

### 5.2 关键模块职责

| 模块 | 职责 |
|---|---|
| `InterknotApi` | 声明所有后端端点的 Kotlin 接口，返回 `Result<T>`。 |
| `InterknotApiImpl` | Ktor 实现，复用 `authGet`/`authPost` 并处理 JWT、分页、错误码。 |
| `InterknotRepository` | 业务聚合：远端获取 + Room 缓存 + 本地搜索历史 + 乐观更新。 |
| `TokenManager` | JWT 存取，登录/注册写入，过期/续签逻辑。 |
| `DirectUploadManager` | 计算 SHA-256 → `/direct-upload/sign` → HTTP PUT → `/direct-upload/complete`；返回 `UploadedFile`。 |
| `MarkdownRenderer` | 将后端 `text` 安全渲染为 Compose；过滤危险 HTML/样式。 |
| `KnockSseClient` | 基于 OkHttp 或 Ktor 的 SSE 客户端，连接 `/knock/stream`，分发 `notification.*` 事件。 |
| `DmWebSocketClient` | 基于 Ktor `WebSocket` 的客户端：先 `/dm/socket/ticket`，再连接 `/dm/socket`，ping/pong、重连。 |
| `ImageLoader` | Coil 配置，统一添加 small webp 变换、NSFW 模糊、GIF 支持。 |
| `PagingSource` / 自定义分页 | 首页/搜索使用 `start+limit` offset 分页；DM 消息使用 `before` cursor 分页。 |

### 5.3 本地缓存策略

| 数据 | 存储 | 策略 |
|---|---|---|
| JWT / 用户 ID | DataStore | 启动读取，401 时清空。 |
| 首页列表、搜索结果 | Room | 写入缓存，下拉刷新先展示缓存再请求远端。 |
| 帖子详情、用户资料 | Room | 按 `documentId` 缓存，过期 5 分钟。 |
| 搜索历史 | DataStore | 最多 20 条，去重。 |
| 草稿（标题/正文/图片 ID） | Room / DataStore | 自动保存，失败时本地兜底。 |
| 私信消息 | Room | 按 conversation 分页存储，WebSocket 增量更新。 |

---

## 6. 数据模型 / DTO 映射

以 `InterKnot-Web/app/types/entities.ts` 为权威参考，Android 端应建立对应的 `data/dto/*.kt` 与 `model/*.kt`，再通过 `toDomain()` 转换。

### 6.1 核心领域模型建议

```kotlin
data class Article(
    val documentId: String,
    val title: String,
    val text: String?,                 // markdown 正文
    val editorState: List<BlockNode>?, // 可选结构化块
    val coverUrl: String?,             // 首图 small webp
    val coverImages: List<ImageMeta>,
    val coverNsfwStatus: NsfwStatus?,
    val coverWidth: Int?,
    val coverHeight: Int?,
    val author: Author?,
    val category: Category?,
    val views: Int,
    val likesCount: Int,
    val commentsCount: Int,
    val favoritesCount: Int,
    val dennyCount: Int,
    val liked: Boolean,
    val favorited: Boolean,
    val hasGivenDenny: Boolean,
    val isRead: Boolean,
    val isAnonymous: Boolean,
    val isHidden: Boolean,
    val isOwner: Boolean,
    val createdAt: String?,
    val updatedAt: String?,
    val editedAt: String?,
    val publishedAt: String?
)

data class Author(
    val documentId: String,
    val name: String,
    val avatar: String?,
    val level: Int?,
    val isFollowing: Boolean = false,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val isSelf: Boolean = false
)

data class Comment(
    val documentId: String,
    val content: String,
    val images: List<ImageMeta>,
    val author: Author?,
    val replies: List<Comment>,
    val liked: Boolean,
    val likesCount: Int,
    val isPinned: Boolean,
    val pinnedAt: String?,
    val floor: Int?,
    val createdAt: String?
)

data class Profile(
    val documentId: String,
    val name: String?,
    val bio: String?,
    val avatar: String?,
    val level: Int,
    val exp: Int,
    val isSelf: Boolean,
    val isFollowing: Boolean,
    val followersCount: Int,
    val followingCount: Int,
    val stats: ProfileStats?,
    val equippedCard: BusinessCard?,
    val equippedAvatar: Avatar?,
    val isBlockedByMe: Boolean,
    val hasBlockedMe: Boolean,
    val isHidden: Boolean,
    val profileHidden: Boolean
)

data class DmMessage(
    val documentId: String,
    val content: String,
    val kind: String, // text | image
    val sender: Author?,
    val replyTo: DmMessage?,
    val createdAt: String,
    val editedAt: String?,
    val deletedAt: String?,
    val isMine: Boolean
)
```

### 6.2 DTO 与映射要点

- 后端列表接口统一包装 `{ data: [...], meta: { pagination: { start, limit, total, pageCount } } }`。
- 详情接口包装 `{ data: {...} }`，字段比列表多 `text`、`editorState`、`liked`、`favorited` 等。
- 列表中 `cover` 可能是字符串 URL 或对象；统一走 `withSmallWebp` 逻辑取 `url`、`width`、`height`、`nsfwStatus`。
- 匿名作者：后端会替换为 `{ name: "匿名用户", avatar: null }`，不要在前端额外处理种子。
- `ImageMeta` 字段：`id`/`documentId`、`url`、`width`、`height`、`nsfwStatus`。

---

## 7. UI/UX 与屏幕流程

### 7.1 底部导航建议

小红书标准 5 Tab 为「首页/发现/发布/消息/我的」。建议将现有导航调整为：

| Tab | 当前 | 建议 | 说明 |
|---|---|---|---|
| 1 | 推送 | **首页** | 瀑布流推荐/关注 |
| 2 | 敲敲 | **消息** | 合并 Knock 通知与 DM 会话 |
| 3 | 发布 | **发布** | 保持中央悬浮发布按钮 |
| 4 | 等级 | **探索** | 搜索/发现/分类 |
| 5 | 我的 | **我的** | 个人主页与设置 |

> 等级/签到业务可下沉到「我的 > 等级权益」。若保留 5 Tab，可改为「首页/探索/发布/消息/我的」。

### 7.2 屏幕地图

| 屏幕 | 核心内容 | 入口 |
|---|---|---|
| **HomeScreen** | 顶部搜索图标 + 通知角标；分类 Chip 横向滚动；`LazyVerticalStaggeredGrid` 瀑布流卡片。 | 首页 Tab |
| **ExploreScreen** | 热门分类宫格、 trending 搜索词、搜索历史。 | 探索 Tab |
| **SearchScreen** | 搜索框 + 联想列表 + 搜索结果瀑布流；支持分类过滤。 | 顶部搜索、探索页 |
| **PostDetailScreen** | 图片 Pager、作者行（关注）、标题、markdown 正文、互动栏、评论列表（置顶 + 嵌套回复）、评论输入框。 | PostCard 点击 |
| **CreateScreen** | 图片选择/预览（上限按等级）、标题、正文编辑器、分类选择、匿名开关、草稿/发布按钮。 | 发布 Tab |
| **ProfileScreen** | 头像/名称/签名/等级/统计、关注按钮、Tab（帖子/收藏/评论）、装扮卡片。 | 我的 Tab / 点击作者 |
| **LevelScreen** | 经验圆环、当前等级权益、每日经验来源、签到按钮。 | 我的 > 等级 |
| **NotificationsScreen** | 通知列表、全部已读、未读角标。 | 消息 Tab / 首页铃铛 |
| **KnockConversationScreen** | pseudo 会话消息流。 | 消息列表 |
| **DmConversationListScreen** | 真实私聊会话列表。 | 消息 Tab 切换 |
| **DmChatScreen** | 单聊/群聊消息流、发送图片、回复、撤回、编辑。 | 会话列表 |
| **SettingsScreen** | 修改昵称/签名/可见性、头像/名片装备、账号安全、黑名单、关于/退出。 | 我的 > 设置 |

### 7.3 关键 UI 细节（对齐 `UI_SPEC.md`）

- 背景 `#0A0A0A`（深色），卡片 `#1A1A1A`，卡片内 `#222222`。
- 品牌色 `#BFFF09`，强调色 `#00E5FF`，错误 `#FF4D4F`，未读标题 `#2196F3`。
- PostCard 圆角：`24dp/24dp/0dp/24dp`（左上/右上/右下/左下）。
- 瀑布流列宽自适应 `160-180dp`，Item 间距 `Spacing.md`。
- 底部导航 58dp + 安全区，发布按钮 54dp 圆形渐变。
- 所有按压反馈使用 `Motion.pressSpec()` 弹簧动画。

---

## 8. 关键技术点

### 8.1 S3 直传图片上传

参考 `InterKnot-Web/app/composables/useApi.ts`：

1. 读取本地图片，计算 SHA-256（可选，用于服务端去重）。
2. `POST /direct-upload/sign` 传 `filename`、`mimeType`、`size`、`contentHash`。
3. 若返回 `existing` 直接复用；否则用返回的 `method`/`uploadUrl`/`headers` 上传文件到 S3。
4. 成功后 `POST /direct-upload/complete` 传 `uploadToken` + 宽高。
5. 得到 `UploadedFile` 后，发布时传 `cover: [documentId]` 或 `coverId`。

Android 端可用 `OkHttp` 直接 PUT，或使用 Ktor `HttpClient` 不带认证直接请求 `uploadUrl`。

### 8.2 Markdown 安全渲染

- 后端 `text` 字段为用户输入的 markdown；详情中通过 `text` 返回。
- Android 可选方案：
  - **Markwon** + `TextView` 嵌入 Compose `AndroidView`。
  - 自定义 `MarkdownParser` 将简单 markdown 转换为 `AnnotatedString` / `Composable`。
- 安全过滤：禁止 `script/iframe/form/input/style/link/object/embed/svg` 及 `javascript:` 链接、`on*` 事件、`position:fixed/absolute` 样式。Web 端 `format-body.ts` 的白名单可直接对应 Android 的 HTML 标签白名单。

### 8.3 实时通信

#### SSE（Knock/通知）
- 端点：`/api/knock/stream`。
- 方法：POST，需带 `Authorization: Bearer <jwt>` 或 `?token=<jwt>`。
- 帧格式：`event: <type>\ndata: <json>\n\n`。
- 关键事件：`hello`、`notification.created`、`notification.read`、`notification.read.bulk`、`bye`。
- 连接断开需指数退避重连。

#### WebSocket（DM）
- 先 `POST /api/dm/socket/ticket` 获取 `{ ticket, ttlSec }`。
- 将 API base 的 `http` 替换为 `ws` 构建 `ws://43.248.77.159:31338/dm/socket?ticket=xxx`。
- 应用层每 20s 发送 `{ type: "ping" }`，服务端回 `pong`。
- 关键事件：`hello`、`pong`、`message.created`、`message.edited`、`message.deleted`、`conversation.read`、`typing` 等。
- 重连：指数退避，最多 6 次。

### 8.4 分页策略

- **Offset 分页**（文章列表、搜索、评论、个人页文章）：`start` 从 0 开始，`limit` 默认 20，根据 `total` 或返回数量判断 `hasMore`。
- **Cursor 分页**（DM 消息）：使用 `before`（`base64url(createdAt|documentId)`）向前翻页，接口返回 `meta.nextCursor`。

### 8.5 等级权益与限制

- 发布前调用 `GET /benefits/me` 获取当前等级权益。
- 客户端按 `articleMaxImages`/`commentMaxImages`/`articleMaxBody` 限制输入。
- 后端同样会做校验，但前端提前限制可提升体验。
- 发布文章/评论前检查 `examPassed`，未通过跳转入站考试流程。

---

## 9. 实施路线图

### P0 基础层（1 周）

- [ ] 扩展 `InterknotApi` 接口，补齐所有缺失端点声明。
- [ ] 完善 DTO 与 `toDomain()` mapper。
- [ ] 接入 `direct-upload` 签名/上传/完成流程。
- [ ] 实现 `MarkdownRenderer` 安全渲染 markdown。
- [ ] 配置 Room 表：`CachedArticle`、`CachedSearch`、`CachedProfile`、`CachedMessage`。
- [ ] 修复 `Article` 重复字段、`Boolean` 拼写问题。

### P1 首页 + 帖子详情 + 互动（2 周）

- [ ] 首页分类 Tab、feed 切换、`LazyVerticalStaggeredGrid` 瀑布流。
- [ ] `PostCard` 完善：封面比例、作者头像、已读标题色、NSFW 模糊。
- [ ] 帖子详情：cover Pager、标题、markdown 正文、互动栏（点赞/收藏/三连/投币）。
- [ ] 评论列表：置顶、嵌套回复、楼层号、评论点赞、评论发布。
- [ ] 关注/收藏/举报/拉黑 弹窗与状态同步。

### P2 搜索与发现（1 周）

- [ ] `ExploreScreen` 分类宫格 + trending。
- [ ] `SearchScreen` 搜索框、联想、搜索结果瀑布流。
- [ ] DataStore 搜索历史。
- [ ] 搜索结果缓存。

### P3 发布流程（2 周）

- [ ] `CreateScreen` 图片选择器、已选图预览/拖拽排序、删除。
- [ ] 分类选择、匿名开关、标题/正文输入。
- [ ] 草稿自动保存（本地 + 远端）。
- [ ] 发布前校验：入站考试 `/exam/status`、等级权益 `/benefits/me`。
- [ ] 入站考试界面：`/exam/start`、`/exam/submit`、`/exam/review`。

### P4 个人主页 + 等级签到（1.5 周）

- [ ] `ProfileScreen` 完整数据：`/profiles/:id`、`/follows/toggle`、tab 切换。
- [ ] 我的：资料编辑、头像/名片装备、置顶帖、黑名单。
- [ ] 等级页：`/me/exp`、`/benefits/me`、签到 `/check-in`。
- [ ] Denny 余额与投币入口。

### P5 消息 + 私信（2 周）

- [ ] 通知列表与未读数角标。
- [ ] SSE `/knock/stream` 接入，实时刷新通知与 Knock 会话。
- [ ] DM 会话列表与消息列表。
- [ ] WebSocket `/dm/socket` 接入，收发/编辑/撤回消息，发送图片。

### P6 打磨与性能（1 周）

- [ ] 图片预加载、placeholder、NSFW 模糊策略。
- [ ] 离线缓存命中、错误/空状态/骨架屏。
- [ ] 深色/浅色主题完整覆盖，折叠屏/平板适配。
- [ ] 动画与转场（与 `Motion.kt` 对齐）。
- [ ] 可访问性（内容描述、焦点）。

---

## 10. 验收标准

- [ ] 未登录可浏览推荐瀑布流、搜索、打开帖子详情。
- [ ] 登录后可点赞/收藏/三连/投币/评论/回复/关注。
- [ ] 通过入站考试后可发布带图帖子，图片走 S3 直传。
- [ ] 可查看自己与他人主页，含统计与关注状态。
- [ ] 可签到、查看等级权益、丁尼余额。
- [ ] 通知通过 SSE 实时到达并更新角标。
- [ ] 可与其它用户进行 WebSocket 私信，支持图片与回复。
- [ ] 举报/拉黑入口可用，拉黑后双向隐藏内容与关注关系。

---

## 11. 风险与注意事项

1. **HTTP 明文 API**：`build.gradle.kts` 中 `API_BASE_URL` 为 `http://`，生产环境应切 HTTPS，否则需在 `network_security_config.xml` 中允许明文。
2. **Markdown 安全**：Android 没有 DOMPurify，需显式白名单过滤 HTML 标签与 `style` 危险值。
3. **匿名作者**：不要在前端尝试从 `anonymousSeed` 还原身份；后端已做匿名化输出。
4. **WebSocket Ticket**：ticket 30 秒有效，必须在拿到 ticket 后立即连接，过期需重新申请。
5. **SSE 认证**：`/knock/stream` 使用 POST 且需要 Bearer token，标准 `EventSource` 不支持；需用 OkHttp/Ktor 实现 SSE 解析器。
6. **NSFW 图片**：feed 与详情需根据 `coverNsfwStatus`（`safe`/`sensitive`/`error`）做模糊或提示处理。
7. **等级权益校验**：仅前端限制不够，需同时处理后端 400/403 错误提示。
8. **发布权限**：创建文章/评论均要求 `examPassed`，入口需提前拦截。
9. **图片压缩**：移动上传前建议压缩/缩放，避免大原图直传 S3 导致流量与审核问题。
10. **无明确 Tag 系统**：后端以 `Category` 替代 Tag，搜索与发布均使用分类。

---

## 12. 参考文件

- 后端路由与控制器：`KawaYiLab/ikserver/src/api/**/routes/*.ts`、`controllers/*.ts`
- Web 前端数据层：`KawaYiLab/InterKnot-Web/app/composables/useApi.ts`
- Web 前端安全渲染：`KawaYiLab/InterKnot-Web/app/utils/format-body.ts`
- Web 前端类型定义：`KawaYiLab/InterKnot-Web/app/types/entities.ts`
- Android 设计规范：`KawaYiLab/InterKnot-Android/docs/UI_SPEC.md`
- Android UI 优化计划：`KawaYiLab/InterKnot-Android/docs/ui-optimization-plan.md`
- Android 当前代码：`app/build.gradle.kts`、`data/api/InterknotApi.kt`、`ui/screens/*`、`ui/theme/*`
