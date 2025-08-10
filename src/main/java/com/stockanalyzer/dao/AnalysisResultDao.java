package com.stockanalyzer.dao;

import com.stockanalyzer.config.DatabaseConfig;
import com.stockanalyzer.model.AnalysisResult;
import com.stockanalyzer.model.Stock;
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
 * 分析结果数据访问对象
 * 负责分析结果数据的CRUD操作
 */
public class AnalysisResultDao {
    private static final Logger logger = LoggerFactory.getLogger(AnalysisResultDao.class);
    private final JdbcTemplate jdbcTemplate;
    private final StockDao stockDao;

    public AnalysisResultDao() {
        this.jdbcTemplate = DatabaseConfig.getInstance().getJdbcTemplate();
        this.stockDao = new StockDao();
    }

    // 分析结果行映射器
    private final RowMapper<AnalysisResult> ANALYSIS_RESULT_ROW_MAPPER = (ResultSet rs, int rowNum) -> {
        AnalysisResult result = new AnalysisResult();
        result.setId(rs.getString("id"));
        result.setStockId(rs.getString("stock_id"));
        result.setAnalysisDate(rs.getDate("analysis_date").toLocalDate());
        result.setAnalysisType(rs.getString("analysis_type"));
        result.setSignal(rs.getString("signal"));
        result.setConfidence(rs.getBigDecimal("confidence"));
        result.setDescription(rs.getString("description"));
        result.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        // 加载关联的股票信息
        stockDao.findById(result.getStockId()).ifPresent(result::setStock);
        
        return result;
    };

    /**
     * 保存分析结果
     */
    public AnalysisResult save(AnalysisResult result) {
        if (result.getId() == null) {
            // 新增
            result.setId(UUID.randomUUID().toString());
            jdbcTemplate.update(
                    "INSERT INTO analysis_results (id, stock_id, analysis_date, analysis_type, signal, confidence, description) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)",
                    result.getId(), result.getStockId(), result.getAnalysisDate(),
                    result.getAnalysisType(), result.getSignal(), result.getConfidence(),
                    result.getDescription());
            logger.debug("新增分析结果: {} {}", result.getStockId(), result.getAnalysisDate());
        } else {
            // 更新
            jdbcTemplate.update(
                    "UPDATE analysis_results SET stock_id = ?, analysis_date = ?, analysis_type = ?, " +
                    "signal = ?, confidence = ?, description = ? WHERE id = ?",
                    result.getStockId(), result.getAnalysisDate(), result.getAnalysisType(),
                    result.getSignal(), result.getConfidence(), result.getDescription(),
                    result.getId());
            logger.debug("更新分析结果: {} {}", result.getStockId(), result.getAnalysisDate());
        }
        return result;
    }

    /**
     * 根据ID查询分析结果
     */
    public Optional<AnalysisResult> findById(String id) {
        try {
            AnalysisResult result = jdbcTemplate.queryForObject(
                    "SELECT * FROM analysis_results WHERE id = ?",
                    ANALYSIS_RESULT_ROW_MAPPER,
                    id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 查询股票的所有分析结果
     */
    public List<AnalysisResult> findByStockId(String stockId) {
        return jdbcTemplate.query(
                "SELECT * FROM analysis_results WHERE stock_id = ? ORDER BY analysis_date DESC",
                ANALYSIS_RESULT_ROW_MAPPER,
                stockId);
    }

    /**
     * 查询特定日期的分析结果
     */
    public List<AnalysisResult> findByDate(LocalDate date) {
        return jdbcTemplate.query(
                "SELECT * FROM analysis_results WHERE analysis_date = ?",
                ANALYSIS_RESULT_ROW_MAPPER,
                date);
    }

    /**
     * 查询特定股票和日期的分析结果
     */
    public Optional<AnalysisResult> findByStockIdAndDate(String stockId, LocalDate date) {
        try {
            AnalysisResult result = jdbcTemplate.queryForObject(
                    "SELECT * FROM analysis_results WHERE stock_id = ? AND analysis_date = ?",
                    ANALYSIS_RESULT_ROW_MAPPER,
                    stockId, date);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 查询特定类型的分析结果
     */
    public List<AnalysisResult> findByAnalysisType(String analysisType) {
        return jdbcTemplate.query(
                "SELECT * FROM analysis_results WHERE analysis_type = ? ORDER BY analysis_date DESC",
                ANALYSIS_RESULT_ROW_MAPPER,
                analysisType);
    }

    /**
     * 查询特定信号的分析结果
     */
    public List<AnalysisResult> findBySignal(String signal) {
        return jdbcTemplate.query(
                "SELECT * FROM analysis_results WHERE signal = ? ORDER BY analysis_date DESC",
                ANALYSIS_RESULT_ROW_MAPPER,
                signal);
    }

    /**
     * 查询最近的分析结果
     */
    public List<AnalysisResult> findRecent(int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM analysis_results ORDER BY analysis_date DESC LIMIT ?",
                ANALYSIS_RESULT_ROW_MAPPER,
                limit);
    }

    /**
     * 删除分析结果
     */
    public boolean delete(String id) {
        int rows = jdbcTemplate.update("DELETE FROM analysis_results WHERE id = ?", id);
        return rows > 0;
    }

    /**
     * 删除股票的所有分析结果
     */
    public boolean deleteByStockId(String stockId) {
        int rows = jdbcTemplate.update("DELETE FROM analysis_results WHERE stock_id = ?", stockId);
        if (rows > 0) {
            logger.debug("删除分析结果: {} ({}条记录)", stockId, rows);
            return true;
        }
        return false;
    }
}