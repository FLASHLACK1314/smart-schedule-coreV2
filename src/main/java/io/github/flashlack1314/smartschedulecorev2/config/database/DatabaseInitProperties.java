package io.github.flashlack1314.smartschedulecorev2.config.database;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库初始化配置属性
 * 从 application-dev.yaml 读取配置
 *
 * @author flash
 */
@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "database.init")
public class DatabaseInitProperties {

    /**
     * 是否启用数据库初始化
     */
    private boolean enabled = false;

    /**
     * 是否强制重新创建表（生产环境请勿开启）
     */
    private boolean dropAndCreate = false;

    /**
     * 是否在发现缺失表时删除所有表重建
     */
    private boolean dropAllOnMissing = true;

    /**
     * 是否初始化失败时快速失败（终止应用启动）
     */
    private boolean failFast = true;

    /**
     * 需要检查的表（按依赖顺序）
     */
    private List<String> tables = new ArrayList<>();

    /**
     * SQL文件路径（与tables顺序一一对应）
     */
    private List<String> sqlFiles = new ArrayList<>();

    @PostConstruct
    public void init() {
        // 如果配置为空，使用默认的表依赖顺序
        if (tables.isEmpty()) {
            initializeDefaultTables();
        }
        log.info("数据库初始化配置加载完成:");
        log.info("  enabled: {}", enabled);
        log.info("  dropAndCreate: {}", dropAndCreate);
        log.info("  dropAllOnMissing: {}", dropAllOnMissing);
        log.info("  failFast: {}", failFast);
        log.info("  表数量: {}", tables.size());
    }

    /**
     * 初始化默认的表依赖顺序
     */
    private void initializeDefaultTables() {
        // 基础表（无外键）
        tables.add("sc_department");
        tables.add("sc_building");
        tables.add("sc_semester");
        tables.add("sc_course_type");
        tables.add("sc_classroom_type");
        tables.add("sc_teacher");
        tables.add("sc_system_admin");

        // 二级依赖
        tables.add("sc_major");
        tables.add("sc_academic_admin");

        // 三级依赖（包含外键关联）
        tables.add("sc_course_classroom_type");
        tables.add("sc_course");
        tables.add("sc_course_qualification");
        tables.add("sc_classroom");

        // 四级依赖
        tables.add("sc_class");
        tables.add("sc_student");

        // 五级依赖（教学班表必须在其关联表之前创建）
        tables.add("sc_teaching_class");
        tables.add("sc_teaching_class_class");
        tables.add("sc_schedule");

        // 六级依赖
        tables.add("sc_schedule_conflict");

        // 初始化SQL文件路径
        for (String table : tables) {
            sqlFiles.add("classpath:sql/" + table + ".sql");
        }

        log.info("使用默认表配置，共 {} 个表", tables.size());
    }
}
