import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

# 设置 Pandas 显示格式，方便看数据
pd.set_option('display.max_columns', None)
pd.set_option('display.width', 1000)

# ==========================================
# 模块 1：数据生成 (Mock Data Generation)
# 模拟一个真实的“脏数据”场景：主力合约换月导致价格跳空
# ==========================================
def generate_mock_data():
    dates = pd.date_range(start='2024-01-01', periods=100, freq='B')
    
    # --- 1. 模拟黄金 (AU) ---
    # 前50天：合约 AU2406，价格在 500 附近震荡
    price_old = np.random.normal(500, 2, 50)
    # 后50天：合约 AU2408，价格在 510 附近震荡 (由于升水，强行高开 10 块钱)
    price_new = np.random.normal(510, 2, 50) 
    
    au_close = np.concatenate([price_old, price_new])
    # 模拟开盘价 (在收盘价附近随机波动)
    au_open = au_close + np.random.normal(0, 1, 100) 
    
    df_au = pd.DataFrame({
        'trade_date': dates,
        'symbol': ['AU2406'] * 50 + ['AU2408'] * 50, # 模拟合约代码切换
        'open': au_open,
        'close': au_close,
        'asset': 'AU'
    })
    
    # --- 2. 模拟白银 (AG) ---
    # 白银走势模拟得平滑一点，方便算金银比
    ag_close = np.random.normal(6000, 50, 100)
    ag_open = ag_close + np.random.normal(0, 20, 100)
    
    df_ag = pd.DataFrame({
        'trade_date': dates,
        'symbol': ['AG2406'] * 50 + ['AG2408'] * 50,
        'open': ag_open,
        'close': ag_close,
        'asset': 'AG'
    })
    
    return df_au, df_ag

# ==========================================
# 模块 2：ETL 清洗核心 (Back Adjustment Algorithm)
# ==========================================
def adjust_futures_gap(df):
    """
    实现巴拿马法复权：
    1. 找到合约切换日 (Symbol Change)
    2. 计算切换日的价差 Gap = New_Open - Old_Close (简化算法)
    3. 将 Gap 累加，并在历史数据中减去这个 Gap
    """
    df = df.copy()
    df = df.sort_values('trade_date').reset_index(drop=True)
    
    # 1. 标记换月点
    df['symbol_changed'] = df['symbol'] != df['symbol'].shift(1)
    
    # 2. 计算缺口 (Gap)
    # 逻辑：只有在 symbol 变了的那一行才计算 gap
    df['gap'] = 0.0
    mask = df['symbol_changed'] & (df.index > 0)
    # 缺口 = 今天开盘(新) - 昨天收盘(旧)
    df.loc[mask, 'gap'] = df.loc[mask, 'open'] - df.loc[mask, 'close'].shift(1)
    
    # 3. 计算“累积缺口” (需向后倒推)
    # 我们希望最新的价格是真实的，历史价格是调整过的。
    # 所以这是一个反向累加的过程。
    # 技巧：反转数组 -> cumsum -> 再反转回来 -> shift(-1) 对齐
    cumulative_gap = df['gap'][::-1].cumsum()[::-1].shift(-1).fillna(0)
    
    # 4. 修正价格 (复权)
    # 历史价格 = 原始价格 - 累积缺口 (把历史太低的价格抬上来，或者把太高的压下去)
    df['adj_close'] = df['close'] - cumulative_gap
    df['adj_open'] = df['open'] - cumulative_gap
    
    return df

# ==========================================
# 模块 3：特征工程 (Feature Engineering)
# ==========================================
def calculate_features(df_au, df_ag):
    # 先做数据对齐 (Merge)
    df = pd.merge(
        df_au[['trade_date', 'adj_close', 'adj_open']].rename(columns={'adj_close': 'au_close', 'adj_open': 'au_open'}),
        df_ag[['trade_date', 'adj_close']].rename(columns={'adj_close': 'ag_close'}),
        on='trade_date'
    )
    
    # --- Feature A: 金银比 ---
    # 注意：实际交易中黄金是元/克，白银是元/千克，需要量纲统一。这里演示直接除。
    df['gold_silver_ratio'] = (df['au_close'] * 1000) / df['ag_close']
    
    # --- Feature B: 滚动波动率 (20日) ---
    # 1. 计算对数收益率: ln(P_t / P_t-1)
    df['log_ret'] = np.log(df['au_close'] / df['au_close'].shift(1))
    # 2. 滚动标准差
    df['volatility_20d'] = df['log_ret'].rolling(window=20).std()
    
    # --- Feature C: 日内动量 ---
    # 反映当天多空博弈结果
    df['intraday_mom'] = (df['au_close'] - df['au_open']) / df['au_open']
    
    return df

# ==========================================
# 主程序 (Main Execution)
# ==========================================

# 1. 获取脏数据
raw_au, raw_ag = generate_mock_data()
print(">>> 原始数据中点的断层 (第48-52行):")
print(raw_au.iloc[48:53][['trade_date', 'symbol', 'close']])

# 2. 执行 ETL 清洗
clean_au = adjust_futures_gap(raw_au)
clean_ag = adjust_futures_gap(raw_ag)

print("\n>>> 清洗后的连续数据 (注意 adj_close 变得平滑了):")
print(clean_au.iloc[48:53][['trade_date', 'symbol', 'close', 'adj_close', 'gap']])

# 3. 计算特征
final_df = calculate_features(clean_au, clean_ag)
print("\n>>> 最终生成的因子集 (前5行有效数据):")
print(final_df.dropna().head())

# 4. 可视化验证 (Visual Verification)
plt.figure(figsize=(12, 6))

# 子图1: 价格对比
plt.subplot(2, 1, 1)
# 1. 先画蓝线 (复权后的平滑曲线) 作为底部参照
plt.plot(clean_au['trade_date'], clean_au['adj_close'], 'b-', label='Adjusted Close (Smooth)', linewidth=2)
# 2. 后画红线 (原始带缺口的曲线) 压在上面，去掉 alpha=0.5 让它更清晰
plt.plot(clean_au['trade_date'], clean_au['close'], 'r--', label='Raw Close (With Gap)')
plt.title('Futures Rollover: Raw vs Adjusted Price')
plt.legend()
plt.grid(True)

# 子图2: 波动率特征
plt.subplot(2, 1, 2)
plt.plot(final_df['trade_date'], final_df['volatility_20d'], 'g-', label='20-Day Volatility')
plt.title('Feature: Rolling Volatility')
plt.legend()
plt.grid(True)

plt.tight_layout()
plt.show()