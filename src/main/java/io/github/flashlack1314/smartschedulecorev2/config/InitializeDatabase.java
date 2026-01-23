package io.github.flashlack1314.smartschedulecorev2.config;

import com.xlf.utility.util.PasswordUtil;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
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
    private final CourseTypeDAO courseTypeDAO;
    private final ClassroomTypeDAO classroomTypeDAO;
    private final CourseClassroomTypeDAO courseClassroomTypeDAO;
    private final MajorDAO majorDAO;
    private final ClassroomDAO classroomDAO;
    private final ClassDAO classDAO;
    private final TeacherDAO teacherDAO;
    private final AcademicAdminDAO academicAdminDAO;
    private final CourseDAO courseDAO;
    private final CourseQualificationDAO courseQualificationDAO;
    private final StudentDAO studentDAO;
    private final TeachingClassDAO teachingClassDAO;
    private final ScheduleDAO scheduleDAO;
    private final ScheduleConflictDAO scheduleConflictDAO;

    // 保存创建的UUID，供后续使用
    private final List<String> departmentUuids = new ArrayList<>();
    private final List<String> buildingUuids = new ArrayList<>();
    private final List<String> buildingNums = new ArrayList<>();
    private final List<String> courseTypeUuids = new ArrayList<>();
    private final List<String> classroomTypeUuids = new ArrayList<>();
    private final List<String> teacherUuids = new ArrayList<>();
    private final List<String> courseUuids = new ArrayList<>();

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
        this.createCourseType();
        this.createClassroomType();
        this.createTeacher();

        // 二级依赖（类型关联）
        this.createCourseClassroomTypeRelation();
        this.createAcademicAdmin();

        // 三级依赖
        this.createMajor();
        this.createClassroom();
        this.createCourse();
        this.createCourseQualificationRelation();

        // 四级依赖
        this.createClass();
        this.createStudent();

        // 五级依赖
        this.createTeachingClass();
        this.createSchedule();

        // 六级依赖
        this.createScheduleConflict();

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
            String uuid = UuidUtil.generateUuidNoDash();
            BuildingDO buildingDO = new BuildingDO();
            buildingDO.setBuildingUuid(uuid)
                    .setBuildingNum(buildingNums[i])
                    .setBuildingName(buildingNames[i]);
            buildingDAO.save(buildingDO);
            buildingUuids.add(uuid);
            // 保存教学楼编号
            this.buildingNums.add(buildingNums[i]);
            log.info("创建教学楼: {} - {}", buildingNums[i], buildingNames[i]);
        }

        log.info("教学楼数据创建成功，共创建 {} 个教学楼", buildingNums.length);
    }

    /**
     * 创建默认课程类型
     */
    private void createCourseType() {
        log.info("正在创建课程类型数据...");

        String[][] courseTypes = {
                {"理论课", "常规理论教学课程"},
                {"实验课", "需要实验设备的教学课程"},
                {"体育课", "体育运动教学课程"},
                {"艺术课", "艺术设计类教学课程"},
                {"语言课", "外语语言教学课程"},
                {"研讨课", "小组讨论研讨课程"},
                {"实践课", "实践操作类课程"},
                {"公共课", "公共基础课程，大班教学"}
        };

        for (String[] courseType : courseTypes) {
            String uuid = UuidUtil.generateUuidNoDash();
            CourseTypeDO courseTypeDO = new CourseTypeDO();
            courseTypeDO.setCourseTypeUuid(uuid)
                    .setTypeName(courseType[0])
                    .setTypeDescription(courseType[1]);
            courseTypeDAO.save(courseTypeDO);
            courseTypeUuids.add(uuid);
            log.info("创建课程类型: {}", courseType[0]);
        }

        log.info("课程类型数据创建成功，共创建 {} 个课程类型", courseTypes.length);
    }

    /**
     * 创建默认教室类型
     */
    private void createClassroomType() {
        log.info("正在创建教室类型数据...");

        String[][] classroomTypes = {
                {"普通教室", "适合常规理论课程教学"},
                {"多媒体教室", "配备投影仪、音响、电脑等多媒体设备"},
                {"计算机实验室", "配备计算机，适合编程、实验课程"},
                {"阶梯教室", "大容量教室，适合公共课、讲座"},
                {"语音教室", "配备语音教学设备，适合外语教学"},
                {"绘图教室", "配备绘图桌，适合建筑设计、艺术设计类专业"},
                {"理科实验室", "物理、化学等理科实验专用"},
                {"研讨室", "小型教室，适合研讨课、小组讨论"}
        };

        for (String[] classroomType : classroomTypes) {
            String uuid = UuidUtil.generateUuidNoDash();
            ClassroomTypeDO classroomTypeDO = new ClassroomTypeDO();
            classroomTypeDO.setClassroomTypeUuid(uuid)
                    .setTypeName(classroomType[0])
                    .setTypeDescription(classroomType[1]);
            classroomTypeDAO.save(classroomTypeDO);
            classroomTypeUuids.add(uuid);
            log.info("创建教室类型: {}", classroomType[0]);
        }

        log.info("教室类型数据创建成功，共创建 {} 个教室类型", classroomTypes.length);
    }

    /**
     * 创建课程类型-教室类型关联关系
     */
    private void createCourseClassroomTypeRelation() {
        log.info("正在创建课程类型-教室类型关联关系...");

        // 定义关联关系（索引对应 courseTypeUuids 和 classroomTypeUuids）
        // 理论课(0): 普通教室(0), 多媒体教室(1), 阶梯教室(3)
        this.createRelation(0, 0);
        this.createRelation(0, 1);
        this.createRelation(0, 3);

        // 实验课(1): 计算机实验室(2), 理科实验室(6)
        this.createRelation(1, 2);
        this.createRelation(1, 6);

        // 艺术课(3): 绘图教室(5)
        this.createRelation(3, 5);

        // 语言课(4): 语音教室(4)
        this.createRelation(4, 4);

        // 研讨课(5): 研讨室(7), 多媒体教室(1)
        this.createRelation(5, 7);
        this.createRelation(5, 1);

        // 实践课(6): 计算机实验室(2), 理科实验室(6)
        this.createRelation(6, 2);
        this.createRelation(6, 6);

        // 公共课(7): 阶梯教室(3)
        this.createRelation(7, 3);

        log.info("课程类型-教室类型关联关系创建成功");
    }

    /**
     * 创建单个关联关系
     */
    private void createRelation(int courseTypeIndex, int classroomTypeIndex) {
        if (courseTypeIndex >= courseTypeUuids.size() || classroomTypeIndex >= classroomTypeUuids.size()) {
            log.warn("索引越界，跳过创建关联: courseType={}, classroomType={}", courseTypeIndex, classroomTypeIndex);
            return;
        }

        CourseClassroomTypeDO relationDO = new CourseClassroomTypeDO();
        relationDO.setRelationUuid(UuidUtil.generateUuidNoDash())
                .setCourseTypeUuid(courseTypeUuids.get(courseTypeIndex))
                .setClassroomTypeUuid(classroomTypeUuids.get(classroomTypeIndex));
        courseClassroomTypeDAO.save(relationDO);
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
     * 为每个教学楼创建不同类型的教室
     */
    private void createClassroom() {
        log.info("正在创建教室数据...");

        if (buildingUuids.isEmpty() || classroomTypeUuids.isEmpty()) {
            log.warn("没有可用的教学楼或教室类型，跳过创建教室");
            return;
        }

        int classroomCount = 0;

        // 为每个教学楼创建教室
        for (int i = 0; i < buildingUuids.size(); i++) {
            String buildingUuid = buildingUuids.get(i);
            // 获取教学楼编号
            String buildingNum = this.buildingNums.get(i);

            // 每个教学楼创建：2个普通教室 + 1个多媒体教室 + 1个阶梯教室
            // 普通教室 (索引0)
            for (int j = 1; j <= 2; j++) {
                String uuid = UuidUtil.generateUuidNoDash();
                String classroomName = buildingNum + String.format("%02d", j);
                ClassroomDO classroomDO = new ClassroomDO();
                classroomDO.setClassroomUuid(uuid)
                        .setBuildingUuid(buildingUuid)
                        .setClassroomName(classroomName)
                        .setClassroomCapacity(40)
                        // 普通教室
                        .setClassroomTypeUuid(classroomTypeUuids.get(0));
                classroomDAO.save(classroomDO);
                log.info("创建教室: {} (普通教室, 容量40人)", classroomName);
                classroomCount++;
            }

            // 多媒体教室 (索引1)
            String uuid1 = UuidUtil.generateUuidNoDash();
            String multimediaName = buildingNum + "03";
            ClassroomDO multimediaRoom = new ClassroomDO();
            multimediaRoom.setClassroomUuid(uuid1)
                    .setBuildingUuid(buildingUuid)
                    .setClassroomName(multimediaName)
                    .setClassroomCapacity(60)
                    // 多媒体教室
                    .setClassroomTypeUuid(classroomTypeUuids.get(1));
            classroomDAO.save(multimediaRoom);
            log.info("创建教室: {} (多媒体教室, 容量60人)", multimediaName);
            classroomCount++;

            // 阶梯教室 (索引3)
            String uuid2 = UuidUtil.generateUuidNoDash();
            String lectureHallName = buildingNum + "04";
            ClassroomDO lectureHall = new ClassroomDO();
            lectureHall.setClassroomUuid(uuid2)
                    .setBuildingUuid(buildingUuid)
                    .setClassroomName(lectureHallName)
                    .setClassroomCapacity(120)
                    // 阶梯教室
                    .setClassroomTypeUuid(classroomTypeUuids.get(3));
            classroomDAO.save(lectureHall);
            log.info("创建教室: {} (阶梯教室, 容量120人)", lectureHallName);
            classroomCount++;
        }

        log.info("教室数据创建成功，共创建 {} 个教室", classroomCount);
    }

    /**
     * 创建默认行政班级
     */
    private void createClass() {
        // TODO: 实现行政班级初始化
        log.info("行政班级初始化暂未实现");
    }

    /**
     * 创建默认教师
     */
    private void createTeacher() {
        // TODO: 实现教师初始化
        log.info("教师初始化暂未实现");
    }

    /**
     * 创建默认教务管理员
     */
    private void createAcademicAdmin() {
        // TODO: 实现教务管理员初始化
        log.info("教务管理员初始化暂未实现");
    }

    /**
     * 创建默认课程
     */
    private void createCourse() {
        // TODO: 实现课程初始化
        log.info("课程初始化暂未实现");
    }

    /**
     * 创建课程-教师资格关联关系
     */
    private void createCourseQualificationRelation() {
        // TODO: 实现课程教师资格关联初始化
        log.info("课程教师资格关联初始化暂未实现");
    }

    /**
     * 创建默认学生
     */
    private void createStudent() {
        // TODO: 实现学生初始化
        log.info("学生初始化暂未实现");
    }

    /**
     * 创建默认教学班
     */
    private void createTeachingClass() {
        // TODO: 实现教学班初始化
        log.info("教学班初始化暂未实现");
    }

    /**
     * 创建默认排课记录
     */
    private void createSchedule() {
        // TODO: 实现排课记录初始化
        log.info("排课记录初始化暂未实现");
    }

    /**
     * 创建默认排课冲突记录
     */
    private void createScheduleConflict() {
        // TODO: 实现排课冲突记录初始化
        log.info("排课冲突记录初始化暂未实现");
    }
}