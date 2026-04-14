# 数据库目录说明

## 正式迁移机制

后端已经接入 Flyway。应用启动时会读取 `System/src/main/resources/db/migration` 下的版本化 SQL，并在目标 MySQL 库中维护 `flyway_schema_history` 元数据表。

当前约定：

- 正式迁移目录：`System/src/main/resources/db/migration`
- 命名规则：`V版本号__说明.sql`，例如 `V2__create_problem_supported_language.sql`
- 当前默认只支持已有库增量同步，不承诺空库从零初始化
- `System/src/main/resources/V1__add_p0_tables.sql` 是旧位置副本，不作为正式迁移入口

## 首次接入说明

当前项目已有的 `V1__add_p0_tables.sql` 依赖 `user`、`problem`、`submit` 等基础表，且包含非完全幂等的 `ALTER TABLE ADD COLUMN` 和普通 `INSERT`。因此，已有数据库首次接入 Flyway 时默认使用：

- `baseline-on-migrate=true`
- `baseline-version=1`

这样 Flyway 会把现有数据库标记为已具备 `V1` 基础状态，并只执行后续 `V2/V3/...` 增量迁移。

如果需要临时关闭自动迁移，可以在启动后端前设置：

```powershell
$env:DB_MIGRATION_ENABLED="false"
```

如果需要关闭自动 baseline，可以设置：

```powershell
$env:DB_MIGRATION_BASELINE_ON_MIGRATE="false"
```

## 启动前备份

对已有数据库执行迁移前，先把数据库备份到仓库外目录，避免把个人数据或大 SQL 备份误提交进仓库：

```powershell
mysqldump -u root -p programming_system > D:\Desktop\programming_system_backup.sql
```

如果你的库名、账号或端口不同，请按 `System/src/main/resources/application.yml` 中的 `DB_NAME`、`DB_USER`、`DB_PORT` 环境变量对应调整。

## 验证迁移结果

启动后端后，可在 MySQL 中执行：

```sql
SHOW TABLES LIKE 'flyway_schema_history';
SHOW TABLES LIKE 'problem_supported_language';
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;
SELECT COUNT(*) FROM problem_supported_language;
```

预期结果：

- `flyway_schema_history` 存在
- `problem_supported_language` 存在
- `V2__create_problem_supported_language.sql` 和 `V3__backfill_problem_supported_language.sql` 记录为成功
- `problem_supported_language` 中有从 `problem.language` 回填的支持语言记录

## seed 目录

`db/seed` 只放需要人工确认后执行的数据补丁或演示数据，不会随应用启动自动执行。

例如 `db/seed/20260407_selected_problem_multilang_seed.sql` 会删除并重写部分题目的多语言配置和参考答案，适合准备演示数据时手动执行，不适合每次启动自动迁移。

## archive 目录

- `archive/legacy/`：历史一次性 SQL、旧版初始化脚本、手工插入脚本
- `archive/backups/`：历史备份 SQL，不建议继续作为主流程脚本使用

## 后续约定

- 结构变更优先新增到 `System/src/main/resources/db/migration`
- 可重复演示数据或手动数据补丁放到 `db/seed`
- 排障或历史留存脚本放到 `db/archive/legacy`
- 全量数据库备份不要继续放入仓库，统一保存在仓库外
