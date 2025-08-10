package com.stockanalyzer.dao;

import com.stockanalyzer.config.DatabaseConfig;
import com.stockanalyzer.model.Notification;
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
 * 通知数据访问对象
 * 负责通知数据的CRUD操作
 */
public class NotificationDao {
    private static final Logger logger = LoggerFactory.getLogger(NotificationDao.class);
    private final JdbcTemplate jdbcTemplate;
    private final StockDao stockDao;

    public NotificationDao() {
        this.jdbcTemplate = DatabaseConfig.getInstance().getJdbcTemplate();
        this.stockDao = new StockDao();
    }

    // 通知行映射器
    private final RowMapper<Notification> NOTIFICATION_ROW_MAPPER = (ResultSet rs, int rowNum) -> {
        Notification notification = new Notification();
        notification.setId(rs.getString("id"));
        notification.setStockId(rs.getString("stock_id"));
        notification.setTitle(rs.getString("title"));
        notification.setContent(rs.getString("content"));
        notification.setNotificationType(rs.getString("notification_type"));
        notification.setStatus(rs.getString("status"));
        
        if (rs.getTimestamp("sent_at") != null) {
            notification.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
        }
        
        notification.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        
        // 加载关联的股票信息
        stockDao.findById(notification.getStockId()).ifPresent(notification::setStock);
        
        return notification;
    };

    /**
     * 保存通知
     */
    public Notification save(Notification notification) {
        if (notification.getId() == null) {
            // 新增
            notification.setId(UUID.randomUUID().toString());
            jdbcTemplate.update(
                    "INSERT INTO notifications (id, stock_id, title, content, notification_type, status) " +
                    "VALUES (?, ?, ?, ?, ?, ?)",
                    notification.getId(), notification.getStockId(), notification.getTitle(),
                    notification.getContent(), notification.getNotificationType(), notification.getStatus());
            logger.debug("新增通知: {}", notification.getTitle());
        } else {
            // 更新
            jdbcTemplate.update(
                    "UPDATE notifications SET stock_id = ?, title = ?, content = ?, " +
                    "notification_type = ?, status = ? WHERE id = ?",
                    notification.getStockId(), notification.getTitle(), notification.getContent(),
                    notification.getNotificationType(), notification.getStatus(), notification.getId());
            logger.debug("更新通知: {}", notification.getTitle());
        }
        return notification;
    }

    /**
     * 更新通知状态为已发送
     */
    public void markAsSent(String id) {
        jdbcTemplate.update(
                "UPDATE notifications SET status = ?, sent_at = ? WHERE id = ?",
                "SENT", LocalDateTime.now(), id);
        logger.debug("标记通知为已发送: {}", id);
    }

    /**
     * 更新通知状态为发送失败
     */
    public void markAsFailed(String id) {
        jdbcTemplate.update(
                "UPDATE notifications SET status = ? WHERE id = ?",
                "FAILED", id);
        logger.debug("标记通知为发送失败: {}", id);
    }

    /**
     * 根据ID查询通知
     */
    public Optional<Notification> findById(String id) {
        try {
            Notification notification = jdbcTemplate.queryForObject(
                    "SELECT * FROM notifications WHERE id = ?",
                    NOTIFICATION_ROW_MAPPER,
                    id);
            return Optional.ofNullable(notification);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    /**
     * 查询待发送的通知
     */
    public List<Notification> findPendingNotifications() {
        return jdbcTemplate.query(
                "SELECT * FROM notifications WHERE status = ? ORDER BY created_at ASC",
                NOTIFICATION_ROW_MAPPER,
                "PENDING");
    }

    /**
     * 查询特定股票的通知
     */
    public List<Notification> findByStockId(String stockId) {
        return jdbcTemplate.query(
                "SELECT * FROM notifications WHERE stock_id = ? ORDER BY created_at DESC",
                NOTIFICATION_ROW_MAPPER,
                stockId);
    }

    /**
     * 查询特定类型的通知
     */
    public List<Notification> findByType(String notificationType) {
        return jdbcTemplate.query(
                "SELECT * FROM notifications WHERE notification_type = ? ORDER BY created_at DESC",
                NOTIFICATION_ROW_MAPPER,
                notificationType);
    }

    /**
     * 查询最近的通知
     */
    public List<Notification> findRecent(int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM notifications ORDER BY created_at DESC LIMIT ?",
                NOTIFICATION_ROW_MAPPER,
                limit);
    }

    /**
     * 删除通知
     */
    public boolean delete(String id) {
        int rows = jdbcTemplate.update("DELETE FROM notifications WHERE id = ?", id);
        return rows > 0;
    }

    /**
     * 删除股票的所有通知
     */
    public boolean deleteByStockId(String stockId) {
        int rows = jdbcTemplate.update("DELETE FROM notifications WHERE stock_id = ?", stockId);
        if (rows > 0) {
            logger.debug("删除通知: {} ({}条记录)", stockId, rows);
            return true;
        }
        return false;
    }
}