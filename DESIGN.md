# 技术设计文档：AI 问题分类数据飞轮与人工校验系统

## 1. 摘要 (Summary)

本文档描述了一个“AI 问题分类数据飞轮系统”的设计方案。该系统旨在通过人工介入（Human-in-the-Loop）的方式，对 AI 模型的分类预测结果进行批量校验。系统提供管理员上传与任务分发功能，普通用户进行人工审核的界面，以及一个全局仪表盘用于监控模型准确率、人工校验进度及数据分布情况。该系统是数据闭环中的关键一环，旨在持续提升 AI 模型的预测能力。

## 2. 背景与目标 (Background & Goals)

### 背景
随着 AI 业务的扩展，模型对问题分类的预测需要持续监控与优化。目前缺乏一个统一的平台来管理模型预测结果的评估工作，人工校验过程分散且难以量化。

### 目标
- **构建校验流**：实现从“数据上传”到“任务分发”再到“人工校验”的完整闭环。
- **量化指标**：实时计算并展示 AI 的预测准确率、Top 5 错误类别等关键指标。
- **提升效率**：提供直观的用户界面，加速人工校验过程。
- **数据沉淀**：将人工校验后的高质量数据结构化存储，为模型迭代提供“真值”数据（Ground Truth）。

## 3. 非目标 (Non-Goals)
- **模型训练/推理**：本系统不负责运行 AI 模型进行实时预测，仅接收预测后的结果数据（JSON）。
- **通用标注平台**：本系统专用于“分类预测校验”场景，而非通用的图像或文本标注工具。

## 4. 需求分析 (Requirements)

### 4.1 功能需求 (Functional Requirements)

**角色：管理员 (Admin)**
1.  **批次管理**：上传包含 AI 预测结果的 JSON 文件，生成待分发批次。
2.  **任务分发**：将批次中的问题分配给指定的普通用户（或用户组）。
3.  **全局监控**：查看全局仪表盘，掌握整体进度和质量。
4.  **数据导出**：导出经过人工校验的高质量数据（CSV/JSON），用于模型迭代。

**角色：普通用户 (User)**
1.  **任务列表**：查看分配给自己的校验任务，包含批次信息。
2.  **人工校验**：针对具体问题，查看 AI 预测类别及**所属聚类总结**，交互式选择“准确”或“不准确”（可选修正类别及理由）。
3.  **进度查看**：查看当前分类任务下的剩余问题数。
4.  **全局监控**：有权查看全局仪表盘，了解整体项目进展与模型表现。

**全局仪表盘 (Dashboard)**
1.  **核心指标**：AI 预测总数、批次总数、AI 准确率、人工矫正进度（已校验/总量）、待分发批次数量。
2.  **错误分析**：展示 AI 预测错误的 Top 5 类别（即错误率最高的预测类别）。
3.  **数据分布**：展示所有类别的词云图。

### 4.2 非功能需求 (Non-functional Requirements)
- **响应速度**：校验操作（提交结果）延迟应 < 200ms。
- **并发性**：支持多用户同时进行校验工作。
- **数据一致性**：确保校验结果不会因并发操作而丢失或冲突。
- **易用性**：校验界面应尽可能减少点击次数，支持快捷键。

## 5. 系统架构 (Architecture Overview)

### 5.1 系统组件
系统采用经典的前后端分离架构：
- **Frontend (Web SPA)**: 基于 React/Vue 构建，提供 Admin 和 User 界面及可视化仪表盘。
- **Backend Service**: 提供 RESTful API，处理业务逻辑、权限控制和数据聚合。
- **Database**: 关系型数据库 (PostgreSQL/MySQL) 存储用户、批次、问题数据及校验结果。
- **Object Storage (Optional)**: 存储原始上传的 JSON 文件备份（可视需求简化直接存 DB）。

### 5.2 架构图 (Architecture Diagram)

```ascii
+----------------+      +----------------+
|   Admin Web    |      |    User Web    |
+-------+--------+      +-------+--------+
        |                       |
        |   HTTPS / REST API    |
        v                       v
+----------------------------------------+
|           API Gateway / LB             |
+-------------------+--------------------+
                    |
          +---------v----------+
          |  Backend Service   |
          | (Core Logic, Auth) |
          +----+----------+----+
               |          |
      +--------v--+    +--v-----------+
      |  Database |    | File Storage |
      | (SQL DB)  |    | (JSON Logs)  |
      +-----------+    +--------------+
```

## 6. 详细设计 (Detailed Design)

### 6.2 模块交互 (Module Interaction)

#### 用例图 (Use Case Diagram)
```ascii
+-----------------------+
|  AI Classification    |
|  Flywheel System      |
+-----------------------+
|                       |
|   +---------------+   |
|   |  Upload Batch |<-------+
|   +---------------+   |    |
|                       |  Admin
|   +---------------+   |    |
|   | Dispatch Task |<-------+
|   +---------------+   |    |
|                       |    |
|   +---------------+   |    |
|   | View Dashboard|<-------+
|   +---------------+   |    |
|                       |  User
|                       |    |
|   +---------------+   |    |
|   |   View Task   |<-------+
|   +---------------+   |    |
|                       |    |
|   +---------------+   |    |
|   |  Verify Issue |<-------+
|   +---------------+   |
|                       |
+-----------------------+
```

#### 前端界面设计 (UI Wireframes)

**1. 我的矫正任务 (My Verification Tasks)**
展示一个分组表格，以**聚类组 (Cluster Group)** 为维度，展开后显示组内具体问题。

```ascii
+------------------------------------------------------------------------------------------------------------------+
| 我的矫正任务                                                                                                     |
+------------------------------------------------------------------------------------------------------------------+
| [SPDT] | [IPMT] | [聚类类别]     | [聚类总结]           | [问题数]| [操作区 (Action)]                              |
+------------------------------------------------------------------------------------------------------------------+
| 分组A  | XX     | 组网协议异常   | 网络中断，无法连接...| 2       |                                                  |
|        |        |                |                      |         | +----------------------------------------------+ |
|        |        |                |                      |         | | PROD | DETAILS     | ISSUE_NO | TYPE       | |
|        |        |                |                      |         | +----------------------------------------------+ |
|        |        |                |                      |         | | P1   | Error 0x88F | NO-001   | 协议异常   | |
|        |        |                |                      |         | |      | [AI准确] [我要纠错]                 | |
|        |        |                |                      |         | +----------------------------------------------+ |
|        |        |                |                      |         | | P2   | Res alloc.. | NO-002   | 协议异常   | |
|        |        |                |                      |         | |      | [AI准确] [我要纠错]                 | |
|        |        |                |                      |         | +----------------------------------------------+ |
+------------------------------------------------------------------------------------------------------------------+
| 分组B  | YY     | 数据同步问题   | 传输缓慢...          | 1       | ...                                            |
+------------------------------------------------------------------------------------------------------------------+
```
*交互说明：*
*   每行代表一个聚类组，包含 SPDT, IPMT, 类别, 总结等元信息。
*   右侧操作区默认展开（或点击展开）显示该组下的所有问题。
*   每个问题下方提供两个快速操作按钮：
    *   **[AI准确]**：点击后标记为 Correct，状态变更为 OK。
    *   **[我要纠错]**：点击后弹出对话框或下拉输入框，填写 `humanLabel` (纠正类别) 和 `humanReasoning` (纠正说明)。

#### 校验任务时序图 (Sequence Diagram: Verification Task)

```ascii
Admin         Backend Service          Database           User
  |                  |                    |                |
  | 1. Upload JSON   |                    |                |
  +----------------->|                    |                |
  |                  | 2. Parse & Save    |                |
  |                  +------------------->|                |
  |                  |                    |                |
  | 3. Dispatch Task |                    |                |
  +----------------->| 4. Update Assignee |                |
  |                  +------------------->|                |
  |                  |                    |                |
  |                  |                    | 5. Get Task    |
  |                  |<------------------------------------+
  |                  | 6. Return Task List|                |
  |                  +------------------------------------>|
  |                  |                    |                |
  |                  |                    | 7. Submit Verify
  |                  |<------------------------------------+
  |                  | 8. Save Result     |                |
  |                  +------------------->|                |
  |                  | 9. Update Stats    |                |
  |                  +--+                 |                |
  |                  |  | (Async)         |                |
  |                  +<-+                 |                |
  |                  |                    |                |
```

### 6.3 核心流程 (Core Processes)
1.  **上传流程**：管理员上传 JSON -> 后端解析 -> 存入 `analysis_batches` 表 -> 解析问题存入 `raw_problems` 表（状态：待分发）。
2.  **分发流程**：管理员选择批次 -> 选择用户 -> 后端更新 `analysis_batches` 表的 `assigned_user_id` -> 关联用户可查看批次下的 `raw_problems`。
3.  **校验流程**：用户获取任务 -> 提交校验结果 (IsCorrect) -> 后端更新 `raw_problems` 状态为 `CORRECTED` -> 触发统计更新（或异步计算）。

## 7. 数据库设计 (Database Design)

### 7.1 ER 图 (简述)
- `users` (1) <--- (N) `analysis_batches` (assigned_user_id)
- `users` (1) <--- (N) `raw_problems` (operator_id)
- `analysis_batches` (1) <--- (N) `ai_clusters_group`
- `ai_clusters_group` (1) <--- (N) `raw_problems`

### 7.2 表结构定义

**1. users (用户表)**
| Field | Type | Description |
|---|---|---|
| id | INT | PK |
| username | VARCHAR | 用户名 (UNIQUE) |
| role | ENUM | 'ADMIN', 'USER' |
| password_hash | VARCHAR | 密码哈希 |

**2. analysis_batches (批次表)**
| Field | Type | Description |
|---|---|---|
| id | BIGINT | PK |
| batch_no | VARCHAR | 批次号 |
| source_channel | VARCHAR | 数据来源 |
| status | ENUM | 'UPLOADED', 'DISTRIBUTED', 'COMPLETED' |
| assigned_user_id | VARCHAR | FK -> users.username (被指派用户名) |
| total_count | INT | 总问题数 |
| created_at | DATETIME | 上传时间 |

**3. ai_clusters_group (AI 聚类表)**
| Field | Type | Description |
|---|---|---|
| id | BIGINT | PK (自增主键) |
| batch_id | BIGINT | FK -> analysis_batches.id (关联批次) |
| spdt | VARCHAR | SPDT字段 |
| ipmt | VARCHAR | IPMT字段 |
| ai_cluster_category | VARCHAR | AI 聚类类别 |
| ai_cluster_summary | TEXT | AI 聚类总结 |
| problem_count | INT | 该组包含的问题数 |

**4. raw_problems (原始问题与矫正表)**
| Field | Type | Description |
|---|---|---|
| id | BIGINT | PK (自增主键) |
| cluster_id | BIGINT | FK -> ai_clusters_group.id (关联的聚类组ID) |

| prod_en_name | VARCHAR |  |
| resolution_detail | TEXT |  |
| issue_details | TEXT |  |
| issue_no | VARCHAR |  |
| issue_type | VARCHAR |  |

| correction_status | ENUM | 'PENDING', 'CORRECTED', 'SKIPPED' (矫正状态) |
| human_label | VARCHAR | 用户输入的矫正类别 |
| human_reasoning | TEXT | 用户输入的矫正理由 |
| operator_id | VARCHAR | FK -> users.username (操作人工号) |
| updated_at | DATETIME | 更新时间 |

### 7.3 索引策略
- `analysis_batches(assigned_user_id, status)`: 加速用户查询待处理批次。
- `ai_clusters_group(batch_id)`: 加速批次下聚类组的加载。
- `raw_problems(cluster_id)`: 加速聚类下问题的加载。
- `raw_problems(operator_id, correction_status)`: 统计用户工作量。

## 8. 数据模型 (Data Models)

### 输入文件格式 (JSON)
```json
[
  {
    "RESOLUTION_SUMMARY": "Replaced the faulty component.",
    "ROOT_CAUSE": "Defect-Huawei Product-Software",
    "PROBLEM_DETAIL": "System crashed after update.",
    "RESOLUTION_DETAIL": "Patched the kernel module.",
    "ISSUE_DETAILS": "Error code 500 received.",
    "TYPE": "Bug"
  },
  ...
]
```

## 9. API 设计 (API Spec)

### 9.1 批次上传 (Admin)
- **POST** `/api/admin/batches`
- **Request**: Multipart file (JSON)
- **Response**:
```json
{
  "batch_id": 101,
  "total_imported": 500,
  "status": "success"
}
```

### 9.2 任务分发 (Admin)
- **POST** `/api/admin/batches/{batchId}/assign`
- **Request**:
```json
{
  "user_ids": [1, 2, 3],
  "strategy": "AVERAGE" // 平均分配
}
```

### 9.3 获取校验任务 (User)
- **GET** `/api/tasks?status=PENDING`
- **Response**:
```json
{
  "code": 200,
  "data": [
    {
      "groupId": 101,
      "aiCategory": "组网协议异常",
      "aiSummary": "用户反馈连接中断...",
      "spdt": "分组A",
      "ipmt": "XX",
      "issueCount": 2,
      "items": [
        {
          "issueId": 1001,
          "prodEnName": "DSP9800",
          "issueDetails": "Error 0x88F...",
          "correctionStatus": "OK",
          "humanLabel": null,
          "humanReasoning": null
        },
        {
          "issueId": 1002,
          "prodEnName": "DSP9811",
          "issueDetails": "Resource alloc failed...",
          "correctionStatus": "EDIT",
          "humanLabel": "硬件故障",
          "humanReasoning": "日志显示物理损坏"
        }
      ]
    }
  ]
}
```

### 9.4 提交校验 (User)
- **POST** `/api/tasks/{taskId}/verify`
- **Request**:
```json
{
  "correction_status": "CORRECTED", // 或 SKIPPED
  "human_label": "Technical_Support", // 若 AI 预测准确则可不填或填原值
  "human_reasoning": "The issue mentions database connection..." // 可选
}
```

### 9.5 仪表盘统计 (Global)
- **GET** `/api/dashboard/stats`
- **Response**:
```json
{
  "total_predicted": 10000,
  "total_batches": 20,
  "ai_accuracy": 0.85, 
  "progress_percentage": 0.60,
  "pending_batches_count": 2,
  "top_errors": [
    {"category": "Billing", "error_count": 150},
    {"category": "Login", "error_count": 120}
  ],
  "word_cloud": [
    {"text": "Billing", "value": 500},
    {"text": "Account", "value": 300}
  ]
}
```

### 9.6 数据导出 (Admin)
- **GET** `/api/admin/batches/{batchId}/export`
- **Response**: CSV/JSON File Download

## 10. 异常处理 (Error Handling)
- **上传错误**：校验 JSON 格式，若格式非法返回 `400 Bad Request` 并提示具体行号。
- **并发冲突**：若通过 Optimistic Lock (乐观锁) 发现任务已被他人校验（极少见，因为已预分配），提示用户“该任务已完成”并自动跳转下一题。
- **系统错误**：统一返回 `500` 并在后端记录 Stack Trace。

## 11. 日志与监控 (Logging & Observability)
- **操作日志**：记录 Admin 的分发操作、User 的登录与校验时间。
- **性能监控**：监控 `/verify` 接口的 P99 延迟，确保校验体验流畅。
- **业务监控**：定期统计“未完成任务”积压情况，通过邮件或系统通知提醒管理员。

## 12. 性能与扩展性 (Performance & Scalability)
- **数据库**：随着问题数量增加 (百万级)，`problems` 表需按 `created_at` 或 `batch_id` 进行分区 (Partitioning)。
- **缓存**：仪表盘的聚合数据（如 Accuracy, Word Cloud）计算成本高，应在 Redis 中缓存，每 5-10 分钟刷新一次，或在校验提交时异步增量更新。

## 13. 安全性与权限 (Security & Access Control)
- **RBAC**：严格区分 Admin 和 User 权限。Admin 接口需校验 `role=ADMIN`。
- **数据脱敏**：若问题内容包含敏感 PII 信息，需在入库前或展示前进行掩码处理。
- **CSRF/XSS**：Web 端需开启标准防护。

## 14. 部署方案 (Deployment Plan)
- **容器化**：Backend 和 Frontend 构建为 Docker 镜像。
- **编排**：使用 Kubernetes 或 Docker Compose 部署。
- **CI/CD**：代码提交 -> 自动测试 -> 构建镜像 -> 自动部署到 Staging 环境。

## 15. 测试方案 (Test Strategy)
- **单元测试**：覆盖准确率计算逻辑、文件解析逻辑。
- **集成测试**：模拟完整的“上传-分发-校验-统计”流程，确保数据库状态流转正确。
- **UI 测试**：使用 Cypress/Selenium 测试校验按钮的交互响应。

## 17. 数据飞轮机制 (Data Flywheel Implementation)

本系统不仅仅是“人工校验工具”，更是“数据飞轮”的核心引擎。通过持续的“预测-校验-训练”闭环，实现模型能力的自我进化。

### 17.1 飞轮闭环流程
```ascii
      +-----------------+       (1) Predict        +----------------+
      |   Raw Issues    | -----------------------> |   AI Model     |
      +--------+--------+                          +--------+-------+
               ^                                            |
               |                                            | (2) Output
      (5) Re-train / Fine-tune                              v
               |                                   +--------+-------+
      +--------+--------+                          |   Check System |
      |  Model Training | <----------------------- | (Human Verify) |
      +-----------------+       (4) Export GT      +----------------+
                               (Ground Truth)
```

### 17.2 关键步骤设计

**阶段一：数据接入与预测 (Inference)**
- 管理员上传待分类问题（Raw Data）。
- 调用当前版本 AI 模型进行批量预测，生成初步分类标签及置信度。
- **策略**：对于置信度 < 0.8 的低信度数据，标记为“优先校验”。

**阶段二：人工清洗与沉淀 (Cleaning)**
- 用户在校验界面对 AI 预测结果进行审核。
- **正向反馈**：点击 [AI准确]，确认该样本为正确样本。
- **负向反馈**：点击 [我要纠错]，提供修正后的真实标签 (Label) 及修正理由 (Reasoning)。
- **数据分级**：系统自动将校验后的数据标记为 `Golden Dataset`（高价值真值）。

**阶段三：数据导出与迭代 (Export & Iterate)**
- 管理员通过 `/api/admin/batches/{batchId}/export` 接口导出已完成校验的批次数据。
- 数据格式清洗为训练格式（如 `{"text": "...", "label": "..."}`）。
- **自动化触发 (Future)**：当积累的 `Golden Dataset` 达到一定阈值（如 1000 条），自动触发触发 CI/CD流水线中的模型重训练 (Retrain) 或微调 (Fine-tune) 任务。

**阶段四：模型部署与评估 (Deploy)**
- 新模型上线替换旧模型。
- **效果验证**：使用新模型对历史困难样本进行回测，验证准确率是否提升（如 Top 5 错误率是否下降）。
- 飞轮转动：新模型用于下一批数据的预测，预期人工修正率应逐渐降低。

