package com.stockanalyzer.dao;

import com.stockanalyzer.config.DatabaseConfig;
import com.stockanalyzer.model.Stock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 股票数据访问对象
 * 负责股票数据的CRUD操作
 */
public class StockDao {
    private static final Logger logger = LoggerFactory.getLogger(StockDao.class);
    private final JdbcTemplate jdbcTemplate;

    public StockDao() {
        this.jdbcTemplate = DatabaseConfig.getInstance().getJdbcTemplate();
    }

    // 股票行映射器
    private static final RowMapper<Stock> STOCK_ROW_MAPPER = (ResultSet rs, int rowNum) -> {
        Stock stock = new Stock();
        stock.setId(rs.getString("id"));
        stock.setSymbol(rs.getString("symbol"));
        stock.setName(rs.getString("name"));
        stock.setExchange(rs.getString("exchange"));
        stock.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        stock.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return stock;
    };

    /**
     * 保存股票信息
     */
    public Stock save(Stock stock) {
        if (stock.getId() == null) {
            // 新增
            stock.setId(UUID.randomUUID().toString());
            jdbcTemplate.update(
                    "INSERT INTO stocks (id, symbol, name, exchange) VALUES (?, ?, ?, ?)",
                    stock.getId(), stock.getSymbol(), stock.getName(), stock.getExchange());
            logger.debug("新增股票: {}", stock);
        } else {
            // 更新
            jdbcTemplate.update(
                    "UPDATE stocks SET symbol = ?, name = ?, exchange = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    stock.getSymbol(), stock.getName(), stock.getExchange(), stock.getId());
            logger.debug("更新股票: {}", stock);
        }
        return stock;
    }

    /**
     * 根据ID查询股票
     */
    public Optional<Stock> findById(String id) {
        try {
            Stock stock = jdbcTemplate.queryForObject(
                    "SELECT * FROM stocks WHERE id = ?",
                    STOCK_ROW_MAPPER,
                    id);
            return Optional.ofNullable(stock);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 根据股票代码查询股票
     */
    public Optional<Stock> findBySymbol(String symbol) {
        try {
            Stock stock = jdbcTemplate.queryForObject(
                    "SELECT * FROM stocks WHERE symbol = ?",
                    STOCK_ROW_MAPPER,
                    symbol);
            return Optional.ofNullable(stock);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 查询所有股票
     */
    public List<Stock> findAll() {
        return jdbcTemplate.query("SELECT * FROM stocks", STOCK_ROW_MAPPER);
    }

    /**
     * 删除股票
     */
    public boolean delete(String id) {
        int rows = jdbcTemplate.update("DELETE FROM stocks WHERE id = ?", id);
        if (rows > 0) {
            logger.debug("删除股票: {}", id);
            return true;
        }
        return false;
    }
}