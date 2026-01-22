package io.github.flashlack1314.smartschedulecorev2.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库初始化配置类
 * 系统启动时检查表结构是否完整
 *
 * @author flash
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseInitializationConfig {

    private final JdbcTemplate jdbcTemplate;
    private final DatabaseInitProperties properties;
    private final InitializeDatabase initializeDatabase;

    @Bean
    @Order(1)
    public ApplicationRunner databaseInitializer() {
        return args -> {
            // 检查是否启用数据库初始化
            if (!properties.isEnabled()) {
                log.info("数据库初始化功能已禁用");
                return;
            }
            log.info("开始检查数据库表结构...");
            try {
                // 检查表是否完整
                List<String> missingTables = this.checkTables();
                // 场景1: 配置强制重建
                if (properties.isDropAndCreate()) {
                    log.warn("强制重新创建模式：将删除所有表后重新创建");
                    this.dropAllTables();
                    this.createAllTables();
                    log.info("强制重新创建模式完成");
                    log.info("初始化数据库数据");
                    initializeDatabase.initializeDatabase();
                    return;
                }
                // 场景2: 表都存在
                if (missingTables.isEmpty()) {
                    log.info("数据库表结构检查通过，所有必需的表都存在");
                    log.info("========================================");
                    return;
                }
                // 场景3: 表不完整
                log.warn("发现缺失的表: {}", missingTables);
                if (properties.isDropAllOnMissing()) {
                    log.warn("drop-all-on-missing模式：删除所有表后重新创建");
                    this.dropAllTables();
                    this.createAllTables();
                    initializeDatabase.initializeDatabase();
                } else {
                    this.createMissingTables(missingTables);
                }
                log.info("表结构修复完成");
                log.info("========================================");

            } catch (Exception e) {
                log.error("========================================");
                log.error("数据库初始化失败", e);
                log.error("========================================");
                if (properties.isFailFast()) {
                    throw new RuntimeException("数据库初始化失败", e);
                }
            }
        };
    }

    /**
     * 检查必需的表是否存在
     */
    private List<String> checkTables() throws Exception {
        List<String> missingTables = new ArrayList<>();

        if (jdbcTemplate.getDataSource() != null) {
            try (Connection connection = jdbcTemplate.getDataSource().getConnection()) {
                DatabaseMetaData metaData = connection.getMetaData();

                for (String tableName : properties.getTables()) {
                    try (ResultSet tables = metaData.getTables(null, "public", tableName, new String[]{"TABLE"})) {
                        if (!tables.next()) {
                            missingTables.add(tableName);
                            log.warn("表 {} 不存在", tableName);
                        } else {
                            log.debug("表 {} 已存在", tableName);
                        }
                    }
                }
            }
        }

        return missingTables;
    }

    /**
     * 删除所有配置的表（按反向顺序删除以避免外键约束）
     */
    private void dropAllTables() {
        log.info("开始删除所有表...");

        List<String> failedTables = new ArrayList<>();

        // 按反向顺序删除表
        for (int i = properties.getTables().size() - 1; i >= 0; i--) {
            String tableName = properties.getTables().get(i);

            try {
                jdbcTemplate.execute("DROP TABLE IF EXISTS public." + tableName + " CASCADE");
                log.info("✓ 已删除表: {}", tableName);
            } catch (Exception e) {
                String errorMsg = String.format("删除表 %s 失败: %s", tableName, e.getMessage());
                log.error(errorMsg);
                failedTables.add(tableName);
            }
        }

        if (!failedTables.isEmpty()) {
            String message = String.format("删除表时发生错误，失败的表: %s", String.join(", ", failedTables));
            throw new RuntimeException(message);
        }

        log.info("所有表删除完成");
    }

    /**
     * 创建所有表
     */
    private void createAllTables() throws Exception {
        log.info("开始创建所有表（共{}个）", properties.getTables().size());
        for (int i = 0; i < properties.getTables().size(); i++) {
            String tableName = properties.getTables().get(i);
            String sqlFile = properties.getSqlFiles().get(i);

            log.info("正在创建表: {} ({}/{})", tableName, i + 1, properties.getTables().size());
            this.executeSqlFile(sqlFile);
            log.info("✓ 创建表成功: {}", tableName);
        }
        log.info("所有表创建完成");
    }

    /**
     * 创建缺失的表
     */
    private void createMissingTables(List<String> missingTables) throws Exception {
        log.info("开始创建缺失的表: {}", missingTables);
        for (int i = 0; i < properties.getTables().size(); i++) {
            String tableName = properties.getTables().get(i);
            String sqlFile = properties.getSqlFiles().get(i);

            if (missingTables.contains(tableName)) {
                log.info("正在创建缺失的表: {}", tableName);
                this.executeSqlFile(sqlFile);
                log.info("✓ 创建表成功: {}", tableName);
            }
        }
        log.info("缺失表创建完成");
    }

    /**
     * 执行SQL文件
     */
    private void executeSqlFile(String sqlFilePath) throws Exception {
        log.debug("执行SQL文件: {}", sqlFilePath);

        // 移除 classpath: 前缀
        String resourcePath = sqlFilePath.replace("classpath:", "");
        ClassPathResource resource = new ClassPathResource(resourcePath);

        if (!resource.exists()) {
            throw new RuntimeException("SQL文件不存在: " + sqlFilePath);
        }

        // 读取SQL文件内容
        String sqlContent = FileCopyUtils.copyToString(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)
        );

        // 先移除所有注释行，再分割SQL语句
        StringBuilder cleanedSql = new StringBuilder();
        for (String line : sqlContent.split("\n")) {
            String trimmedLine = line.trim();
            // 跳过纯注释行
            if (!trimmedLine.startsWith("--")) {
                // 移除行尾注释
                int commentIndex = trimmedLine.indexOf("--");
                if (commentIndex > 0) {
                    trimmedLine = trimmedLine.substring(0, commentIndex).trim();
                }
                if (!trimmedLine.isEmpty()) {
                    cleanedSql.append(trimmedLine).append(" ");
                }
            }
        }

        // 分割SQL语句（以分号结尾）
        String[] sqlStatements = cleanedSql.toString().split(";");

        int statementCount = 0;
        for (String sql : sqlStatements) {
            sql = sql.trim();

            // 跳过空语句
            if (sql.isEmpty()) {
                continue;
            }

            this.executeSql(sql);
            statementCount++;
        }

        if (statementCount > 0) {
            log.debug("成功执行 {} 条SQL语句", statementCount);
        }
    }

    /**
     * 执行单条SQL语句
     */
    private void executeSql(String sql) {
        try {
            log.debug("执行SQL: {}", sql.substring(0, Math.min(50, sql.length())) + "...");
            jdbcTemplate.execute(sql);
        } catch (Exception e) {
            log.error("执行SQL失败: {}\nSQL: {}", e.getMessage(), sql);
            throw e;
        }
    }
}
