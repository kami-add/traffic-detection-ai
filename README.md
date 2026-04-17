# 网危智鉴（Link Risk Intelligence Platform）

一个可离线演示的“网站风险分析与危害预测平台”。

## 1. 项目整体结构（前后端目录）

```text
traffic-detection-ai/
├─ frontend/                      # Vue3 + Element Plus + Tailwind + ECharts
│  ├─ public/
│  ├─ src/
│  │  ├─ api/analyze.js           # 调用后端 /api/analyze
│  │  ├─ styles/tailwind.css      # 暗黑安全风样式
│  │  ├─ App.vue                  # 单页Dashboard
│  │  └─ main.js
│  ├─ index.html
│  ├─ package.json
│  ├─ tailwind.config.js
│  ├─ postcss.config.js
│  └─ vite.config.js
├─ backend/                       # Spring Boot + MySQL
│  ├─ src/main/java/com/linkrisk/
│  │  ├─ LinkRiskApplication.java
│  │  ├─ controller/AnalysisController.java
│  │  ├─ dto/{AnalyzeRequest,AnalyzeResponse}.java
│  │  ├─ entity/{DetectionRecord,RiskRule}.java
│  │  ├─ repository/{DetectionRecordRepository,RiskRuleRepository}.java
│  │  ├─ service/AnalysisService.java
│  │  └─ service/impl/AnalysisServiceImpl.java
│  ├─ src/main/resources/
│  │  ├─ application.yml
│  │  ├─ schema.sql
│  │  └─ data.sql
│  └─ pom.xml
└─ README.md
```

---

## 2. 前端代码（Vue组件，页面完整可运行）

已实现关键能力：
- URL输入与一键检测
- 流畅加载动效（科技感）
- 风险结果卡片（风险等级 + 三大概率）
- 攻击路径模拟（步骤化）
- ECharts 仪表盘 + 雷达图
- 特征与规则日志透明化
- 防护建议自动展示
- 暗黑模式 + 高对比警示色

核心页面：`frontend/src/App.vue`

---

## 3. 后端代码（Controller + Service + 实现逻辑）

接口：
- `POST /api/analyze`

请求：
```json
{
  "url": "http://example.com"
}
```

响应示例：
```json
{
  "score": 82,
  "level": "high",
  "features": {
    "urlLength": 66,
    "containsIp": "否",
    "specialChars": 5,
    "subdomainCount": 2,
    "https": "否",
    "suspiciousKeywordCount": 3,
    "digitRatio": "0.09"
  },
  "probabilities": {
    "phishing": 0.78,
    "malware": 0.65,
    "leak": 0.55
  },
  "radar": {
    "phishing": 78,
    "malware": 65,
    "leak": 55,
    "xss": 52,
    "csrf": 61,
    "domainFraud": 70,
    "privacyTracking": 58
  },
  "logs": [
    "命中多个敏感关键词 → 风险+20",
    "未使用HTTPS加密 → 风险+14"
  ],
  "advice": [
    "立即关闭该页面，避免进一步交互。",
    "禁止输入账号密码、短信验证码、银行卡信息。"
  ]
}
```

---

## 4. 风险评分算法实现

后端实现文件：`backend/src/main/java/com/linkrisk/service/impl/AnalysisServiceImpl.java`

### 特征提取
- URL长度
- 是否IP地址
- 特殊字符数量（`@-_=%&`）
- 子域名数量
- 是否HTTPS
- 敏感关键词数量（`login / verify / bank / secure / password / account / update`）
- 数字占比

### 评分（0-100）
通过规则加权：
- URL过长：+10 / +18
- IP地址伪装：+20
- 特殊字符偏多：+8 / +15
- 子域名过深：+12
- 非HTTPS：+14
- 命中敏感词：+10 / +20
- 数字占比过高：+6

分级：
- `0-44`：low
- `45-74`：medium
- `75-100`：high

### AI危害预测（离线模拟）
使用**规则 + Sigmoid函数**模拟轻量逻辑回归，输出：
- phishing
- malware
- leak

并生成7维雷达（含XSS/CSRF/域名欺诈/隐私追踪）。

---

## 5. 示例数据

数据库初始化：
- `schema.sql` 创建两张表：
  - `detection_record(url, score, level, created_at)`
  - `risk_rule(rule_key, weight, description)`
- `data.sql` 内置3条基础规则示例

---

## 6. 本地运行步骤（非常详细）

### 环境准备
- Node.js 18+
- npm 9+
- JDK 17+
- Maven 3.9+
- MySQL 8+

### 第一步：创建数据库
```sql
CREATE DATABASE link_risk DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

如需自定义用户名密码，请修改：
- `backend/src/main/resources/application.yml`

### 第二步：启动后端
```bash
cd backend
mvn spring-boot:run
```

默认监听：`http://localhost:8080`

### 第三步：启动前端
新开终端：
```bash
cd frontend
npm install
npm run dev
```

默认访问：`http://localhost:5173`

> Vite 已配置 `/api` 代理到 `8080`，可直接联调。

### 第四步：演示建议（比赛场景）
1. 输入高风险URL（如：`http://192.168.1.5/secure-login-verify/account/update`）
2. 点击“一键检测”
3. 展示：
   - 高风险评分卡
   - AI概率
   - 攻击路径
   - 仪表盘 + 雷达图
   - 规则日志
   - 防护建议

### 第五步：稳定演示建议
- 全部逻辑本地运行，不依赖外部AI/API
- 如断网环境，项目仍可完整演示
- 可提前准备3组URL样本，保证演示节奏

---

## 附：接口文档简版

### POST /api/analyze

**Req**
```json
{ "url": "http://example.com/login" }
```

**Resp**
```json
{
  "score": 76,
  "level": "high",
  "features": {},
  "probabilities": {
    "phishing": 0.76,
    "malware": 0.63,
    "leak": 0.58
  },
  "radar": {},
  "logs": [],
  "advice": []
}
```

---

如果你希望，我可以在下一步继续补上：
- 历史记录分页页签
- 风险规则可视化编辑后台
- 登录鉴权 + 管理员角色
- Docker Compose 一键启动（MySQL + SpringBoot + Vue）

---

## Windows 一键脚本（D:\\weburl）

项目已提供 `windows-scripts/`：

- `copy-to-d-weburl.bat`：一键复制整个项目到 `D:\weburl`
- `start-backend.bat`：启动后端（8080）
- `start-frontend.bat`：启动前端（5173）
- `start-all.bat`：同时拉起前后端

### 使用顺序

1. 双击运行：`windows-scripts\\copy-to-d-weburl.bat`
2. 复制完成后，到 `D:\weburl\\windows-scripts` 双击：`start-all.bat`
3. 浏览器打开：`http://localhost:5173`

> 首次启动前端会自动执行 `npm install`。
