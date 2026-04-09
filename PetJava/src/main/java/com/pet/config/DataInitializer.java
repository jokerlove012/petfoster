package com.pet.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pet.entity.*;
import com.pet.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final InstitutionMapper institutionMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始检查数据库数据...");
        
        // 检查并添加缺失的字段
        log.info("检查数据库字段...");
        addMissingColumns();
        
        long userCount = userMapper.selectCount(new LambdaQueryWrapper<>());
        long instCount = institutionMapper.selectCount(new LambdaQueryWrapper<>());
        
        log.info("当前用户数: {}, 机构数: {}", userCount, instCount);
        
        // 只在没有管理员账号时才导入最小测试数据
        if (userCount == 0) {
            log.info("数据库为空，开始导入最小测试数据（仅管理员）...");
            executeSqlFile("sql/test_data_minimal.sql");
            log.info("最小测试数据导入完成！");
        }
        
        userCount = userMapper.selectCount(new LambdaQueryWrapper<>());
        instCount = institutionMapper.selectCount(new LambdaQueryWrapper<>());
        log.info("最终用户数: {}, 机构数: {}", userCount, instCount);
    }
    
    private void addMissingColumns() {
        try {
            // 检查notification表是否存在
            try {
                jdbcTemplate.queryForObject("SELECT 1 FROM notification LIMIT 1", Object.class);
                log.info("notification表已存在");
            } catch (Exception e) {
                log.info("创建notification表");
                try {
                    jdbcTemplate.execute("CREATE TABLE notification (" +
                        "id VARCHAR(36) PRIMARY KEY, " +
                        "user_id VARCHAR(36) NOT NULL, " +
                        "type VARCHAR(20) NOT NULL, " +
                        "title VARCHAR(200) NOT NULL, " +
                        "content TEXT, " +
                        "is_read TINYINT DEFAULT 0, " +
                        "link VARCHAR(500), " +
                        "data JSON, " +
                        "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                        "deleted TINYINT DEFAULT 0, " +
                        "INDEX idx_user_id (user_id), " +
                        "INDEX idx_type (type), " +
                        "INDEX idx_is_read (is_read)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                    log.info("notification表创建成功");
                } catch (Exception ex) {
                    log.warn("创建notification表失败: {}", ex.getMessage());
                }
            }
            
            // 检查booking表是否有user_deleted字段
            try {
                jdbcTemplate.queryForObject("SELECT user_deleted FROM booking LIMIT 1", Object.class);
                log.info("booking表已有user_deleted字段");
            } catch (Exception e) {
                log.info("添加user_deleted字段到booking表");
                try {
                    jdbcTemplate.execute("ALTER TABLE booking ADD COLUMN user_deleted TINYINT DEFAULT 0");
                } catch (Exception ex) {
                    log.warn("添加user_deleted字段失败: {}", ex.getMessage());
                }
            }
            
            // 检查booking表是否有institution_deleted字段
            try {
                jdbcTemplate.queryForObject("SELECT institution_deleted FROM booking LIMIT 1", Object.class);
                log.info("booking表已有institution_deleted字段");
            } catch (Exception e) {
                log.info("添加institution_deleted字段到booking表");
                try {
                    jdbcTemplate.execute("ALTER TABLE booking ADD COLUMN institution_deleted TINYINT DEFAULT 0");
                } catch (Exception ex) {
                    log.warn("添加institution_deleted字段失败: {}", ex.getMessage());
                }
            }
            
            // 检查user表是否有status字段
            try {
                jdbcTemplate.queryForObject("SELECT status FROM user LIMIT 1", Object.class);
                log.info("user表已有status字段");
            } catch (Exception e) {
                log.info("添加status字段到user表");
                try {
                    jdbcTemplate.execute("ALTER TABLE user ADD COLUMN status VARCHAR(20) DEFAULT 'active' COMMENT 'active, banned' AFTER role");
                    log.info("为现有用户设置默认状态");
                    jdbcTemplate.execute("UPDATE user SET status = 'active' WHERE status IS NULL");
                } catch (Exception ex) {
                    log.error("添加status字段失败: {}", ex.getMessage(), ex);
                }
            }
        } catch (Exception e) {
            log.warn("添加字段时出错: {}", e.getMessage());
        }
    }

    private void executeSqlFile(String resourcePath) throws Exception {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            if (!resource.exists()) {
                log.warn("SQL文件不存在: {}", resourcePath);
                return;
            }
            
            String sqlContent = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            
            log.info("导入测试数据...");
            List<String> statements = parseSqlStatements(sqlContent);
            int count = 0;
            for (String sql : statements) {
                String trimmedSql = sql.trim();
                if (!trimmedSql.isEmpty() && !trimmedSql.startsWith("--") && 
                    !trimmedSql.toUpperCase().startsWith("TRUNCATE") && 
                    !trimmedSql.toUpperCase().startsWith("DELETE FROM")) {
                    try {
                        jdbcTemplate.execute(trimmedSql);
                        count++;
                    } catch (Exception e) {
                        log.warn("执行SQL时跳过: {}, 错误: {}", trimmedSql.substring(0, Math.min(50, trimmedSql.length())), e.getMessage());
                    }
                }
            }
            log.info("执行了 {} 条SQL语句", count);
        } catch (Exception e) {
            log.error("执行SQL文件失败", e);
        }
    }
    
    private List<String> parseSqlStatements(String sqlContent) {
        List<String> statements = new ArrayList<>();
        StringBuilder currentStatement = new StringBuilder();
        String[] lines = sqlContent.split("\\r?\\n");
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                continue;
            }
            
            currentStatement.append(line).append("\n");
            
            if (trimmedLine.endsWith(";")) {
                String statement = currentStatement.toString();
                statement = statement.substring(0, statement.lastIndexOf(";")).trim();
                if (!statement.isEmpty()) {
                    statements.add(statement);
                }
                currentStatement = new StringBuilder();
            }
        }
        
        if (currentStatement.length() > 0) {
            String statement = currentStatement.toString().trim();
            if (!statement.isEmpty()) {
                statements.add(statement);
            }
        }
        
        return statements;
    }
}
