# 股票分析器

## 项目介绍

股票分析器是一个Java应用程序，用于定时抓取国内或美国互联网企业股票的数据，分析相关的股票指标和业绩新闻数据，生成交易指令，并通过微信推送通知用户。该应用程序特别适合3-5天的短线交易，帮助用户抓住股票波段机会。

## 功能特点

- **自动数据抓取**：定时从Alpha Vantage API获取股票历史价格数据
- **技术指标分析**：使用移动平均线、波段交易等技术分析方法生成交易信号
- **新闻情感分析**：抓取并分析股票相关新闻，评估其对股价的潜在影响
- **价格波动监控**：实时监控股票价格波动，当波动超过阈值时发送通知
- **微信推送通知**：通过Server酱（ServerChan）将交易信号、重要新闻和价格波动推送到微信
- **本地数据存储**：使用H2数据库存储股票数据、分析结果和通知记录
- **命令行界面**：提供简单的命令行界面，方便用户手动执行任务和查看结果

## 系统要求

- Java 17或更高版本
- Maven 3.6或更高版本
- 互联网连接（用于获取股票数据和发送微信通知）

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/stock-analyzer.git
cd stock-analyzer
```

### 2. 配置应用

编辑`src/main/resources/application.properties`文件，设置以下必要配置：

```properties
# Alpha Vantage API配置
api.key=YOUR_ALPHA_VANTAGE_API_KEY

# 微信推送配置（Server酱）
wechat.key=YOUR_SERVERCHAN_KEY
```

### 3. 编译项目

```bash
mvn clean package
```

### 4. 运行应用

```bash
java -jar target/stock-analyzer-1.0.0.jar
```

## 使用指南

启动应用后，将显示命令行界面，提供以下功能：

1. **查看所有股票**：显示系统中已添加的股票列表
2. **添加新股票**：添加新的股票到系统中进行监控和分析
3. **获取股票历史数据**：手动获取特定股票或所有股票的历史价格数据
4. **分析股票**：手动执行股票分析，生成交易信号
5. **查看最近分析结果**：查看最近的股票分析结果
6. **获取股票新闻**：手动获取特定股票的相关新闻
7. **发送待发送通知**：手动发送队列中的通知
8. **查看最近通知**：查看最近发送的通知记录
9. **启动所有定时任务**：启动自动数据抓取、分析和通知发送任务

## 定时任务说明

应用程序包含以下定时任务：

- **数据抓取任务**：每个交易日（周一至周五）收盘后执行，获取当日股票数据
- **数据分析任务**：在数据抓取完成后执行，分析股票数据并生成交易信号
- **新闻抓取任务**：每天多次执行，获取最新的股票相关新闻
- **通知发送任务**：定期检查并发送待发送的通知
- **价格监控任务**：在交易时间内监控股票价格波动，当波动超过阈值时发送通知

## 微信通知示例

应用程序会发送以下类型的通知：

1. **分析结果通知**：包含股票的交易信号（买入/卖出/持有）、置信度和分析详情
2. **价格波动通知**：当股票价格波动超过设定阈值时发送，包含当前价格和变动百分比
3. **新闻通知**：当有重要新闻（积极或消极情感）时发送，包含新闻标题、来源、摘要和链接

## 项目结构

```
src/main/java/com/stockanalyzer/
├── StockAnalyzerApplication.java  # 应用程序入口
├── cli/                          # 命令行界面
├── config/                       # 配置类
├── dao/                          # 数据访问对象
├── job/                          # 任务调度
├── model/                        # 数据模型
├── scheduler/                    # 定时任务调度器
└── service/                      # 业务逻辑服务
```

## 配置选项

在`application.properties`文件中可以配置以下选项：

```properties
# 应用基本信息
app.name=StockAnalyzer
app.version=1.0.0
app.author=Your Name

# 数据库配置
db.url=jdbc:h2:./stockdb;AUTO_SERVER=TRUE
db.username=sa
db.password=

# API配置
api.url=https://www.alphavantage.co/query
api.key=YOUR_ALPHA_VANTAGE_API_KEY

# 微信推送配置
wechat.url=https://sc.ftqq.com/
wechat.key=YOUR_SERVERCHAN_KEY

# 定时任务配置
schedule.data_fetch_time=16:00
schedule.analysis_time=16:30
schedule.news_interval_hours=4
schedule.notification_interval_minutes=5
schedule.price_monitoring_interval_minutes=15
schedule.price_alert_threshold=3.0

# 股票列表配置
stocks.default=BABA,BIDU,JD,PDD,NTES

# 交易策略配置
strategy.ma_short=5
strategy.ma_medium=10
strategy.ma_long=20
strategy.swing_threshold=5.0
```

## 注意事项

1. Alpha Vantage API有调用频率限制，免费版每分钟最多5次调用，每天最多500次调用
2. 确保Server酱的SCKEY有效，否则无法发送微信通知
3. 首次运行时，系统会自动创建数据库和表结构
4. 可以在配置文件中添加默认股票列表，系统启动时会自动初始化这些股票

## 许可证

[MIT License](LICENSE)