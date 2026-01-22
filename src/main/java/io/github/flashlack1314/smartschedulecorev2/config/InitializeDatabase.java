package io.github.flashlack1314.smartschedulecorev2.config;

import com.xlf.utility.util.PasswordUtil;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库数据初始化类
 * 用于在表结构创建完成后初始化基础数据
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitializeDatabase {

    private final SystemAdminDAO adminDAO;
    private final SemesterDAO semesterDAO;
    private final DepartmentDAO departmentDAO;
    private final BuildingDAO buildingDAO;
    private final MajorDAO majorDAO;
    private final ClassroomDAO classroomDAO;
    private final ClassDAO classDAO;

    // 保存创建的学院UUID，供创建专业时使用
    private final List<String> departmentUuids = new ArrayList<>();

    /**
     * 初始化数据库数据
     * 在表结构创建完成后自动调用
     * 按照表依赖顺序初始化基础数据
     */
    public void initializeDatabase() {
        log.info("开始初始化数据库基础数据...");

        // 一级基础表（无外键）
        this.createSystemAdmin();
        this.createSemester();
        this.createDepartment();
        this.createBuilding();

        // 二级依赖
        this.createMajor();
        this.createClassroom();

        // 三级依赖
        this.createClass();

        log.info("数据库基础数据初始化完成");
    }

    /**
     * 创建默认系统管理员
     */
    private void createSystemAdmin() {
        log.info("正在创建系统管理员...");
        SystemAdminDO systemAdminDO = new SystemAdminDO();
        systemAdminDO.setAdminUuid(UuidUtil.generateUuidNoDash())
                .setAdminUsername("admin")
                .setAdminPassword(PasswordUtil.encrypt("qwer1234"));
        adminDAO.save(systemAdminDO);
        log.info("系统管理员创建成功 - 用户名: admin, 密码: qwer1234");
    }

    /**
     * 创建默认学期
     * 2026年两个学期
     */
    private void createSemester() {
        log.info("正在创建学期数据...");

        String[] semesterNames = {
                "2025-2026学年第一学期",
                "2025-2026学年第二学期"
        };

        for (String semesterName : semesterNames) {
            SemesterDO semesterDO = new SemesterDO();
            semesterDO.setSemesterUuid(UuidUtil.generateUuidNoDash())
                    .setSemesterName(semesterName);
            semesterDAO.save(semesterDO);
            log.info("创建学期: {}", semesterName);
        }

        log.info("学期数据创建成功，共创建 {} 个学期", semesterNames.length);
    }

    /**
     * 创建默认学院
     * 创建4个学院
     */
    private void createDepartment() {
        log.info("正在创建学院数据...");

        String[] departmentNames = {
                "计算机学院",
                "软件学院",
                "人工智能学院",
                "数据科学学院"
        };

        for (String departmentName : departmentNames) {
            String uuid = UuidUtil.generateUuidNoDash();
            DepartmentDO departmentDO = new DepartmentDO();
            departmentDO.setDepartmentUuid(uuid)
                    .setDepartmentName(departmentName);
            departmentDAO.save(departmentDO);
            departmentUuids.add(uuid);
            log.info("创建学院: {}", departmentName);
        }

        log.info("学院数据创建成功，共创建 {} 个学院", departmentNames.length);
    }

    /**
     * 创建默认教学楼
     * 创建4个教学楼
     */
    private void createBuilding() {
        log.info("正在创建教学楼数据...");

        String[] buildingNums = {"A01", "A02", "A03", "A04"};
        String[] buildingNames = {
                "第一教学楼",
                "第二教学楼",
                "第三教学楼",
                "第四教学楼"
        };

        for (int i = 0; i < buildingNums.length; i++) {
            BuildingDO buildingDO = new BuildingDO();
            buildingDO.setBuildingUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingNum(buildingNums[i])
                    .setBuildingName(buildingNames[i]);
            buildingDAO.save(buildingDO);
            log.info("创建教学楼: {} - {}", buildingNums[i], buildingNames[i]);
        }

        log.info("教学楼数据创建成功，共创建 {} 个教学楼", buildingNums.length);
    }

    /**
     * 创建默认专业
     * 为每个学院创建1个专业，共4个专业
     */
    private void createMajor() {
        log.info("正在创建专业数据...");

        if (departmentUuids.isEmpty()) {
            log.warn("没有可用的学院，跳过创建专业");
            return;
        }

        // 专业需要对应学院，这里为每个学院创建1个专业
        String[] majorNums = {"CS001", "SE001", "AI001", "DS001"};
        String[] majorNames = {
                "计算机科学与技术",
                "软件工程",
                "人工智能",
                "数据科学与大数据技术"
        };

        for (int i = 0; i < majorNums.length && i < departmentUuids.size(); i++) {
            MajorDO majorDO = new MajorDO();
            majorDO.setMajorUuid(UuidUtil.generateUuidNoDash())
                    .setDepartmentUuid(departmentUuids.get(i))
                    .setMajorNum(majorNums[i])
                    .setMajorName(majorNames[i]);
            majorDAO.save(majorDO);
            log.info("创建专业: {} - {}", majorNums[i], majorNames[i]);
        }

        log.info("专业数据创建成功，共创建 {} 个专业", majorNums.length);
    }

    /**
     * 创建默认教室
     */
    private void createClassroom() {
        // TODO: 实现教室初始化
        log.info("教室初始化暂未实现");
    }

    /**
     * 创建默认行政班级
     */
    private void createClass() {
        // TODO: 实现行政班级初始化
        log.info("行政班级初始化暂未实现");
    }
}