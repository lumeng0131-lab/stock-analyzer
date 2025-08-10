package com.stockanalyzer.dao;

import com.stockanalyzer.config.DatabaseConfig;
import com.stockanalyzer.model.StockPrice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 股票价格数据访问对象
 * 负责股票价格数据的CRUD操作
 */
public class StockPriceDao {
    private static final Logger logger = LoggerFactory.getLogger(StockPriceDao.class);
    private final JdbcTemplate jdbcTemplate;

    public StockPriceDao() {
        this.jdbcTemplate = DatabaseConfig.getInstance().getJdbcTemplate();
    }

    // 股票价格行映射器
    private static final RowMapper<StockPrice> STOCK_PRICE_ROW_MAPPER = (ResultSet rs, int rowNum) -> {
        StockPrice stockPrice = new StockPrice();
        stockPrice.setId(rs.getString("id"));
        stockPrice.setStockId(rs.getString("stock_id"));
        stockPrice.setDate(rs.getDate("date").toLocalDate());
        stockPrice.setOpen(rs.getBigDecimal("open"));
        stockPrice.setHigh(rs.getBigDecimal("high"));
        stockPrice.setLow(rs.getBigDecimal("low"));
        stockPrice.setClose(rs.getBigDecimal("close"));
        stockPrice.setVolume(rs.getLong("volume"));
        stockPrice.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return stockPrice;
    };

    /**
     * 保存股票价格
     */
    public StockPrice save(StockPrice stockPrice) {
        try {
            // 检查是否已存在该日期的价格数据
            Optional<StockPrice> existingPrice = findByStockIdAndDate(stockPrice.getStockId(), stockPrice.getDate());
            
            if (existingPrice.isPresent()) {
                // 更新现有记录
                jdbcTemplate.update(
                        "UPDATE stock_prices SET open = ?, high = ?, low = ?, close = ?, volume = ? WHERE id = ?",
                        stockPrice.getOpen(), stockPrice.getHigh(), stockPrice.getLow(), 
                        stockPrice.getClose(), stockPrice.getVolume(), existingPrice.get().getId());
                stockPrice.setId(existingPrice.get().getId());
                logger.debug("更新股票价格: {} {}", stockPrice.getStockId(), stockPrice.getDate());
            } else {
                // 新增记录
                if (stockPrice.getId() == null) {
                    stockPrice.setId(UUID.randomUUID().toString());
                }
                jdbcTemplate.update(
                        "INSERT INTO stock_prices (id, stock_id, date, open, high, low, close, volume) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        stockPrice.getId(), stockPrice.getStockId(), stockPrice.getDate(),
                        stockPrice.getOpen(), stockPrice.getHigh(), stockPrice.getLow(),
                        stockPrice.getClose(), stockPrice.getVolume());
                logger.debug("新增股票价格: {} {}", stockPrice.getStockId(), stockPrice.getDate());
            }
            return stockPrice;
        } catch (Exception e) {
            logger.error("保存股票价格失败", e);
            throw new RuntimeException("保存股票价格失败", e);
        }
    }

    /**
     * 批量保存股票价格
     */
    public void saveAll(List<StockPrice> stockPrices) {
        for (StockPrice stockPrice : stockPrices) {
            save(stockPrice);
        }
    }

    /**
     * 根据ID查询股票价格
     */
    public Optional<StockPrice> findById(String id) {
        try {
            StockPrice stockPrice = jdbcTemplate.queryForObject(
                    "SELECT * FROM stock_prices WHERE id = ?",
                    STOCK_PRICE_ROW_MAPPER,
                    id);
            return Optional.ofNullable(stockPrice);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 根据股票ID和日期查询股票价格
     */
    public Optional<StockPrice> findByStockIdAndDate(String stockId, LocalDate date) {
        try {
            StockPrice stockPrice = jdbcTemplate.queryForObject(
                    "SELECT * FROM stock_prices WHERE stock_id = ? AND date = ?",
                    STOCK_PRICE_ROW_MAPPER,
                    stockId, date);
            return Optional.ofNullable(stockPrice);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 查询股票的历史价格
     */
    public List<StockPrice> findByStockId(String stockId) {
        return jdbcTemplate.query(
                "SELECT * FROM stock_prices WHERE stock_id = ? ORDER BY date DESC",
                STOCK_PRICE_ROW_MAPPER,
                stockId);
    }

    /**
     * 查询股票在指定日期范围内的历史价格
     */
    public List<StockPrice> findByStockIdAndDateRange(String stockId, LocalDate startDate, LocalDate endDate) {
        return jdbcTemplate.query(
                "SELECT * FROM stock_prices WHERE stock_id = ? AND date BETWEEN ? AND ? ORDER BY date ASC",
                STOCK_PRICE_ROW_MAPPER,
                stockId, startDate, endDate);
    }

    /**
     * 查询最近N天的股票价格
     */
    public List<StockPrice> findRecentPrices(String stockId, int days) {
        return jdbcTemplate.query(
                "SELECT * FROM stock_prices WHERE stock_id = ? ORDER BY date DESC LIMIT ?",
                STOCK_PRICE_ROW_MAPPER,
                stockId, days);
    }

    /**
     * 删除股票价格
     */
    public boolean delete(String id) {
        int rows = jdbcTemplate.update("DELETE FROM stock_prices WHERE id = ?", id);
        return rows > 0;
    }

    /**
     * 删除股票的所有价格数据
     */
    public boolean deleteByStockId(String stockId) {
        int rows = jdbcTemplate.update("DELETE FROM stock_prices WHERE stock_id = ?", stockId);
        if (rows > 0) {
            logger.debug("删除股票价格: {} ({}条记录)", stockId, rows);
            return true;
        }
        return false;
    }
}