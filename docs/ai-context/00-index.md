# NLtimer - AI 上下文索引

> **一句话定位：** Android 行为计时应用——用户创建活动（Activity）、打标签（Tag）、记录行为（Behavior），以网格/时间轴/日志三种布局查看当日行为流。

## 文档导航

| 文件 | 内容 | 何时读取 |
|------|------|---------|
| [01-architecture.md](01-architecture.md) | 模块关系 + 依赖方向 + 分层架构 | 首次理解项目时 |
| [02-data-model.md](02-data-model.md) | 核心实体 + ER 关系 + 数据库表 + 迁移 | 涉及数据层修改时 |
| [03-tech-stack.md](03-tech-stack.md) | 技术栈版本 + 关键依赖 + 构建约定 | 涉及依赖/构建修改时 |
| [04-module-detail.md](04-module-detail.md) | 各模块职责 + 关键类速查表 | 定位具体代码时 |
| [05-conventions.md](05-conventions.md) | 代码规范 + 命名约定 + 模式约定 | 编写/审查代码时 |
| [MAINTENANCE.md](MAINTENANCE.md) | 文档维护指南：修改代码后如何同步更新 | 修改代码后 |

## 项目基本信息

```
包名: com.nltimer
语言: Kotlin 2.3.21
UI: Jetpack Compose + Material3
最低SDK: 31 (Android 12)
目标SDK: 36
数据库: Room (当前版本 6)
DI: Hilt
构建: Gradle Kotlin DSL + Convention Plugins
```

## 目录结构速览

```
NLtimer/
├── app/                    # 壳模块：MainActivity + 导航 + Scaffold
├── core/
│   ├── data/               # 数据层：Room DB + Repository + SettingsPrefs
│   └── designsystem/       # 设计系统：主题 + 表单 + 调试组件
├── feature/
│   ├── home/               # 首页：网格行为视图 + 添加/完成行为
│   ├── sub/                # 副页（占位）
│   ├── stats/              # 统计页（占位）
│   ├── settings/           # 设置：主题 + 弹窗配置
│   ├── categories/         # 分类管理
│   ├── management_activities/ # 活动管理 CRUD
│   ├── tag_management/     # 标签管理 CRUD
│   └── debug/              # 调试工具（仅 debug 构建）
├── buildSrc/               # Convention Plugins
├── docs/                   # 设计文档 + 日志
└── gradle/                 # Version Catalog (libs.versions.toml)
```
