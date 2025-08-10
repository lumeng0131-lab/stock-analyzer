package com.stockanalyzer.dao;

import com.stockanalyzer.config.DatabaseConfig;
import com.stockanalyzer.model.StockNews;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 股票新闻数据访问对象
 * 负责股票新闻数据的CRUD操作
 */
public class StockNewsDao {
    private static final Logger logger = LoggerFactory.getLogger(StockNewsDao.class);
    private final JdbcTemplate jdbcTemplate;
    private final StockDao stockDao;

    public StockNewsDao() {
        this.jdbcTemplate = DatabaseConfig.getInstance().getJdbcTemplate();
        this.stockDao = new StockDao();
    }

    // 股票新闻行映射器
    private final RowMapper<StockNews> STOCK_NEWS_ROW_MAPPER = (ResultSet rs, int rowNum) -> {
        StockNews news = new StockNews();
        news.setId(rs.getString("id"));
        news.setStockId(rs.getString("stock_id"));
        news.setTitle(rs.getString("title"));
        news.setSource(rs.getString("source"));
        news.setUrl(rs.getString("url"));
        
        if (rs.getTimestamp("publish_date") != null) {
            news.setPublishDate(rs.getTimestamp("publish_date").toLocalDateTime());
        }
        
        news.setSummary(rs.getString("summary"));
        news.setSentiment(rs.getString("sentiment"));
        news.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        // 加载关联的股票信息
        if (news.getStockId() != null) {
            stockDao.findById(news.getStockId()).ifPresent(news::setStock);
        }
        
        return news;
    };

    /**
     * 保存股票新闻
     */
    public StockNews save(StockNews news) {
        if (news.getId() == null) {
            // 新增
            news.setId(UUID.randomUUID().toString());
            jdbcTemplate.update(
                    "INSERT INTO stock_news (id, stock_id, title, source, url, publish_date, summary, sentiment) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    news.getId(), news.getStockId(), news.getTitle(), news.getSource(),
                    news.getUrl(), news.getPublishDate(), news.getSummary(), news.getSentiment());
            logger.debug("新增股票新闻: {}", news.getTitle());
        } else {
            // 更新
            jdbcTemplate.update(
                    "UPDATE stock_news SET stock_id = ?, title = ?, source = ?, url = ?, " +
                    "publish_date = ?, summary = ?, sentiment = ? WHERE id = ?",
                    news.getStockId(), news.getTitle(), news.getSource(), news.getUrl(),
                    news.getPublishDate(), news.getSummary(), news.getSentiment(), news.getId());
            logger.debug("更新股票新闻: {}", news.getTitle());
        }
        return news;
    }

    /**
     * 根据ID查询股票新闻
     */
    public Optional<StockNews> findById(String id) {
        try {
            StockNews news = jdbcTemplate.queryForObject(
                    "SELECT * FROM stock_news WHERE id = ?",
                    STOCK_NEWS_ROW_MAPPER,
                    id);
            return Optional.ofNullable(news);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 查询特定股票的新闻
     */
    public List<StockNews> findByStockId(String stockId) {
        return jdbcTemplate.query(
                "SELECT * FROM stock_news WHERE stock_id = ? ORDER BY publish_date DESC",
                STOCK_NEWS_ROW_MAPPER,
                stockId);
    }

    /**
     * 查询特定情感的新闻
     */
    public List<StockNews> findBySentiment(String sentiment) {
        return jdbcTemplate.query(
                "SELECT * FROM stock_news WHERE sentiment = ? ORDER BY publish_date DESC",
                STOCK_NEWS_ROW_MAPPER,
                sentiment);
    }

    /**
     * 查询最近的新闻
     */
    public List<StockNews> findRecent(int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM stock_news ORDER BY publish_date DESC LIMIT ?",
                STOCK_NEWS_ROW_MAPPER,
                limit);
    }

    /**
     * 查询特定日期范围内的新闻
     */
    public List<StockNews> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return jdbcTemplate.query(
                "SELECT * FROM stock_news WHERE publish_date BETWEEN ? AND ? ORDER BY publish_date DESC",
                STOCK_NEWS_ROW_MAPPER,
                startDate, endDate);
    }

    /**
     * 查询包含特定关键词的新闻
     */
    public List<StockNews> findByKeyword(String keyword) {
        String searchPattern = "%" + keyword + "%";
        return jdbcTemplate.query(
                "SELECT * FROM stock_news WHERE title LIKE ? OR summary LIKE ? ORDER BY publish_date DESC",
                STOCK_NEWS_ROW_MAPPER,
                searchPattern, searchPattern);
    }

    /**
     * 删除新闻
     */
    public boolean delete(String id) {
        int rows = jdbcTemplate.update("DELETE FROM stock_news WHERE id = ?", id);
        return rows > 0;
    }

    /**
     * 删除股票的所有新闻
     */
    public boolean deleteByStockId(String stockId) {
        int rows = jdbcTemplate.update("DELETE FROM stock_news WHERE stock_id = ?", stockId);
        if (rows > 0) {
            logger.debug("删除股票新闻: {} ({}条记录)", stockId, rows);
            return true;
        }
        return false;
    }
}