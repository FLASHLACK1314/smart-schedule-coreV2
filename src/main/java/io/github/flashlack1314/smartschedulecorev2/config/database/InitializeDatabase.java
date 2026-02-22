package io.github.flashlack1314.smartschedulecorev2.config.database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xlf.utility.util.PasswordUtil;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private final DepartmentDAO departmentDAO;
    private final BuildingDAO buildingDAO;
    private final CourseTypeDAO courseTypeDAO;
    private final ClassroomTypeDAO classroomTypeDAO;
    private final SemesterDAO semesterDAO;
    private final MajorDAO majorDAO;
    private final AcademicAdminDAO academicAdminDAO;
    private final CourseClassroomTypeDAO courseClassroomTypeDAO;
    private final CourseDAO courseDAO;
    private final ClassroomDAO classroomDAO;
    private final TeacherDAO teacherDAO;
    private final ClassDAO classDAO;
    private final StudentDAO studentDAO;
    private final CourseQualificationDAO courseQualificationDAO;
    private final TeachingClassDAO teachingClassDAO;
    private final TeachingClassClassDAO teachingClassClassDAO;
    private final ScheduleDAO scheduleDAO;

    /**
     * 初始化数据库数据
     * 在表结构创建完成后自动调用
     */
    public void initializeDatabase() {
        log.info("开始初始化数据库基础数据...");

        // 第一层：基础数据（无外键依赖）
        List<DepartmentDO> departments = this.initializeDepartments();
        List<BuildingDO> buildings = this.initializeBuildings();
        List<CourseTypeDO> courseTypes = this.initializeCourseTypes();
        List<ClassroomTypeDO> classroomTypes = this.initializeClassroomTypes();
        List<SemesterDO> semesters = this.initializeSemesters();

        // 第二层：依赖基础数据的表
        List<MajorDO> majors = this.initializeMajors(departments);
        List<AcademicAdminDO> academicAdmins = this.initializeAcademicAdmins(departments);

        // 第三层：核心业务数据
        List<CourseClassroomTypeDO> courseClassroomTypes = this.initializeCourseClassroomTypes(courseTypes, classroomTypes);
        List<CourseDO> courses = this.initializeCourses(courseTypes);
        List<ClassroomDO> classrooms = this.initializeClassrooms(buildings, classroomTypes);
        List<ClassDO> classes = this.initializeClasses(majors);
        List<TeacherDO> teachers = this.initializeTeachers(departments);
        List<StudentDO> students = this.initializeStudents(classes);

        // 第四层：关联数据
        this.initializeCourseQualifications(courses, teachers);

        // 第五层：教学班和排课记录
        List<TeachingClassDO> teachingClasses = this.initializeTeachingClasses(courses, teachers, semesters);
        this.initializeTeachingClassClasses(teachingClasses, classes);
        this.initializeSchedules(teachingClasses, semesters, classrooms, courses, teachers);

        // 系统管理员
        this.createSystemAdmin();

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
     * 初始化学院数据
     */
    private List<DepartmentDO> initializeDepartments() {
        log.info("正在初始化学院数据...");

        List<DepartmentDO> departments = new ArrayList<>();

        DepartmentDO dept1 = new DepartmentDO();
        dept1.setDepartmentUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentName("计算机科学与技术学院");
        departments.add(dept1);

        DepartmentDO dept2 = new DepartmentDO();
        dept2.setDepartmentUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentName("电子信息工程学院");
        departments.add(dept2);

        DepartmentDO dept3 = new DepartmentDO();
        dept3.setDepartmentUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentName("机械工程学院");
        departments.add(dept3);

        DepartmentDO dept4 = new DepartmentDO();
        dept4.setDepartmentUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentName("经济管理学院");
        departments.add(dept4);

        departmentDAO.saveBatch(departments);
        log.info("学院数据初始化完成，共 {} 条记录", departments.size());
        return departments;
    }

    /**
     * 初始化教学楼数据
     */
    private List<BuildingDO> initializeBuildings() {
        log.info("正在初始化教学楼数据...");

        List<BuildingDO> buildings = new ArrayList<>();

        BuildingDO building1 = new BuildingDO();
        building1.setBuildingUuid(UuidUtil.generateUuidNoDash())
                .setBuildingNum("A")
                .setBuildingName("第一教学楼");
        buildings.add(building1);

        BuildingDO building2 = new BuildingDO();
        building2.setBuildingUuid(UuidUtil.generateUuidNoDash())
                .setBuildingNum("B")
                .setBuildingName("第二教学楼");
        buildings.add(building2);

        BuildingDO building3 = new BuildingDO();
        building3.setBuildingUuid(UuidUtil.generateUuidNoDash())
                .setBuildingNum("C")
                .setBuildingName("实验楼");
        buildings.add(building3);

        BuildingDO building4 = new BuildingDO();
        building4.setBuildingUuid(UuidUtil.generateUuidNoDash())
                .setBuildingNum("D")
                .setBuildingName("信息楼");
        buildings.add(building4);

        BuildingDO building5 = new BuildingDO();
        building5.setBuildingUuid(UuidUtil.generateUuidNoDash())
                .setBuildingNum("E")
                .setBuildingName("第三教学楼");
        buildings.add(building5);

        BuildingDO building6 = new BuildingDO();
        building6.setBuildingUuid(UuidUtil.generateUuidNoDash())
                .setBuildingNum("F")
                .setBuildingName("第四教学楼");
        buildings.add(building6);

        BuildingDO building7 = new BuildingDO();
        building7.setBuildingUuid(UuidUtil.generateUuidNoDash())
                .setBuildingNum("G")
                .setBuildingName("艺术楼");
        buildings.add(building7);

        BuildingDO building8 = new BuildingDO();
        building8.setBuildingUuid(UuidUtil.generateUuidNoDash())
                .setBuildingNum("H")
                .setBuildingName("综合楼");
        buildings.add(building8);

        buildingDAO.saveBatch(buildings);
        log.info("教学楼数据初始化完成，共 {} 条记录", buildings.size());
        return buildings;
    }

    /**
     * 初始化课程类型数据
     */
    private List<CourseTypeDO> initializeCourseTypes() {
        log.info("正在初始化课程类型数据...");

        List<CourseTypeDO> courseTypes = new ArrayList<>();

        CourseTypeDO ct1 = new CourseTypeDO();
        ct1.setCourseTypeUuid(UuidUtil.generateUuidNoDash())
                .setTypeName("理论课")
                .setTypeDescription("以理论讲授为主的课程");
        courseTypes.add(ct1);

        CourseTypeDO ct2 = new CourseTypeDO();
        ct2.setCourseTypeUuid(UuidUtil.generateUuidNoDash())
                .setTypeName("实验课")
                .setTypeDescription("需要实验设备的课程");
        courseTypes.add(ct2);

        CourseTypeDO ct3 = new CourseTypeDO();
        ct3.setCourseTypeUuid(UuidUtil.generateUuidNoDash())
                .setTypeName("实践课")
                .setTypeDescription("实践操作类课程");
        courseTypes.add(ct3);

        CourseTypeDO ct4 = new CourseTypeDO();
        ct4.setCourseTypeUuid(UuidUtil.generateUuidNoDash())
                .setTypeName("体育课")
                .setTypeDescription("体育类课程");
        courseTypes.add(ct4);

        courseTypeDAO.saveBatch(courseTypes);
        log.info("课程类型数据初始化完成，共 {} 条记录", courseTypes.size());
        return courseTypes;
    }

    /**
     * 初始化教室类型数据
     */
    private List<ClassroomTypeDO> initializeClassroomTypes() {
        log.info("正在初始化教室类型数据...");

        List<ClassroomTypeDO> classroomTypes = new ArrayList<>();

        ClassroomTypeDO crt1 = new ClassroomTypeDO();
        crt1.setClassroomTypeUuid(UuidUtil.generateUuidNoDash())
                .setTypeName("普通教室")
                .setTypeDescription("标准多媒体教室");
        classroomTypes.add(crt1);

        ClassroomTypeDO crt2 = new ClassroomTypeDO();
        crt2.setClassroomTypeUuid(UuidUtil.generateUuidNoDash())
                .setTypeName("机房")
                .setTypeDescription("计算机实验室");
        classroomTypes.add(crt2);

        ClassroomTypeDO crt3 = new ClassroomTypeDO();
        crt3.setClassroomTypeUuid(UuidUtil.generateUuidNoDash())
                .setTypeName("实验室")
                .setTypeDescription("专业实验教室");
        classroomTypes.add(crt3);

        ClassroomTypeDO crt4 = new ClassroomTypeDO();
        crt4.setClassroomTypeUuid(UuidUtil.generateUuidNoDash())
                .setTypeName("体育馆")
                .setTypeDescription("体育场馆");
        classroomTypes.add(crt4);

        ClassroomTypeDO crt5 = new ClassroomTypeDO();
        crt5.setClassroomTypeUuid(UuidUtil.generateUuidNoDash())
                .setTypeName("阶梯教室")
                .setTypeDescription("大型阶梯教室");
        classroomTypes.add(crt5);

        classroomTypeDAO.saveBatch(classroomTypes);
        log.info("教室类型数据初始化完成，共 {} 条记录", classroomTypes.size());
        return classroomTypes;
    }

    /**
     * 初始化学期数据
     */
    private List<SemesterDO> initializeSemesters() {
        log.info("正在初始化学期数据...");

        List<SemesterDO> semesters = new ArrayList<>();

        SemesterDO sem1 = new SemesterDO();
        sem1.setSemesterUuid(UuidUtil.generateUuidNoDash())
                .setSemesterName("2024-2025学年第一学期")
                .setSemesterWeeks(18)
                .setStartDate(LocalDate.of(2024, 9, 1))
                .setEndDate(LocalDate.of(2025, 1, 20));
        semesters.add(sem1);

        SemesterDO sem2 = new SemesterDO();
        sem2.setSemesterUuid(UuidUtil.generateUuidNoDash())
                .setSemesterName("2024-2025学年第二学期")
                .setSemesterWeeks(18)
                .setStartDate(LocalDate.of(2025, 2, 20))
                .setEndDate(LocalDate.of(2025, 7, 10));
        semesters.add(sem2);

        semesterDAO.saveBatch(semesters);
        log.info("学期数据初始化完成，共 {} 条记录", semesters.size());
        return semesters;
    }

    /**
     * 初始化专业数据
     */
    private List<MajorDO> initializeMajors(List<DepartmentDO> departments) {
        log.info("正在初始化专业数据...");

        List<MajorDO> majors = new ArrayList<>();

        // 使用第一个学院(计算机科学与技术学院)创建2个专业
        MajorDO major1 = new MajorDO();
        major1.setMajorUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentUuid(departments.get(0).getDepartmentUuid())
                .setMajorNum("CS001")
                .setMajorName("计算机科学与技术");
        majors.add(major1);

        MajorDO major2 = new MajorDO();
        major2.setMajorUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentUuid(departments.get(0).getDepartmentUuid())
                .setMajorNum("SE001")
                .setMajorName("软件工程");
        majors.add(major2);

        // 使用第二个学院创建1个专业
        MajorDO major3 = new MajorDO();
        major3.setMajorUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentUuid(departments.get(1).getDepartmentUuid())
                .setMajorNum("EE001")
                .setMajorName("电子信息工程");
        majors.add(major3);

        // 使用第三个学院创建1个专业
        MajorDO major4 = new MajorDO();
        major4.setMajorUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentUuid(departments.get(2).getDepartmentUuid())
                .setMajorNum("ME001")
                .setMajorName("机械设计制造");
        majors.add(major4);

        // 使用第一个学院创建1个专业（网络工程）
        MajorDO major5 = new MajorDO();
        major5.setMajorUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentUuid(departments.get(0).getDepartmentUuid())
                .setMajorNum("NE001")
                .setMajorName("网络工程");
        majors.add(major5);

        // 使用第二个学院创建1个专业（通信工程）
        MajorDO major6 = new MajorDO();
        major6.setMajorUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentUuid(departments.get(1).getDepartmentUuid())
                .setMajorNum("CE001")
                .setMajorName("通信工程");
        majors.add(major6);

        // 使用第三个学院创建1个专业（自动化）
        MajorDO major7 = new MajorDO();
        major7.setMajorUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentUuid(departments.get(2).getDepartmentUuid())
                .setMajorNum("AU001")
                .setMajorName("自动化");
        majors.add(major7);

        // 使用第四个学院创建1个专业（工商管理）
        MajorDO major8 = new MajorDO();
        major8.setMajorUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentUuid(departments.get(3).getDepartmentUuid())
                .setMajorNum("BA001")
                .setMajorName("工商管理");
        majors.add(major8);

        majorDAO.saveBatch(majors);
        log.info("专业数据初始化完成，共 {} 条记录", majors.size());
        return majors;
    }

    /**
     * 初始化教务管理员数据
     */
    private List<AcademicAdminDO> initializeAcademicAdmins(List<DepartmentDO> departments) {
        log.info("正在初始化教务管理员数据...");

        List<AcademicAdminDO> academicAdmins = new ArrayList<>();

        AcademicAdminDO admin1 = new AcademicAdminDO();
        admin1.setAcademicUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentUuid(departments.get(0).getDepartmentUuid())
                .setAcademicNum("A001")
                .setAcademicName("李教务")
                .setAcademicPassword(PasswordUtil.encrypt("qwer1234"));
        academicAdmins.add(admin1);

        AcademicAdminDO admin2 = new AcademicAdminDO();
        admin2.setAcademicUuid(UuidUtil.generateUuidNoDash())
                .setDepartmentUuid(departments.get(1).getDepartmentUuid())
                .setAcademicNum("A002")
                .setAcademicName("王教务")
                .setAcademicPassword(PasswordUtil.encrypt("qwer1234"));
        academicAdmins.add(admin2);

        academicAdminDAO.saveBatch(academicAdmins);
        log.info("教务管理员数据初始化完成，共 {} 条记录", academicAdmins.size());
        return academicAdmins;
    }

    /**
     * 初始化课程类型-教室类型关联数据
     */
    private List<CourseClassroomTypeDO> initializeCourseClassroomTypes(List<CourseTypeDO> courseTypes, List<ClassroomTypeDO> classroomTypes) {
        log.info("正在初始化课程类型-教室类型关联数据...");

        List<CourseClassroomTypeDO> relations = new ArrayList<>();

        // 理论课 -> 普通教室
        CourseClassroomTypeDO rel1 = new CourseClassroomTypeDO();
        rel1.setRelationUuid(UuidUtil.generateUuidNoDash())
                .setCourseTypeUuid(courseTypes.get(0).getCourseTypeUuid())
                .setClassroomTypeUuid(classroomTypes.get(0).getClassroomTypeUuid());
        relations.add(rel1);

        // 实验课 -> 实验室
        CourseClassroomTypeDO rel2 = new CourseClassroomTypeDO();
        rel2.setRelationUuid(UuidUtil.generateUuidNoDash())
                .setCourseTypeUuid(courseTypes.get(1).getCourseTypeUuid())
                .setClassroomTypeUuid(classroomTypes.get(2).getClassroomTypeUuid());
        relations.add(rel2);

        // 实践课 -> 机房
        CourseClassroomTypeDO rel3 = new CourseClassroomTypeDO();
        rel3.setRelationUuid(UuidUtil.generateUuidNoDash())
                .setCourseTypeUuid(courseTypes.get(2).getCourseTypeUuid())
                .setClassroomTypeUuid(classroomTypes.get(1).getClassroomTypeUuid());
        relations.add(rel3);

        // 体育课 -> 体育馆
        CourseClassroomTypeDO rel4 = new CourseClassroomTypeDO();
        rel4.setRelationUuid(UuidUtil.generateUuidNoDash())
                .setCourseTypeUuid(courseTypes.get(3).getCourseTypeUuid())
                .setClassroomTypeUuid(classroomTypes.get(3).getClassroomTypeUuid());
        relations.add(rel4);

        courseClassroomTypeDAO.saveBatch(relations);
        log.info("课程类型-教室类型关联数据初始化完成，共 {} 条记录", relations.size());
        return relations;
    }

    /**
     * 初始化课程数据
     * 扩充到15门课程
     */
    private List<CourseDO> initializeCourses(List<CourseTypeDO> courseTypes) {
        log.info("正在初始化课程数据...");

        List<CourseDO> courses = new ArrayList<>();

        // 课程类型UUID引用
        String theoryType = courseTypes.get(0).getCourseTypeUuid(); // 理论课
        String labType = courseTypes.get(1).getCourseTypeUuid(); // 实验课
        String practiceType = courseTypes.get(2).getCourseTypeUuid(); // 实践课
        String peType = courseTypes.get(3).getCourseTypeUuid(); // 体育课

        // 原有课程（6门）
        CourseDO course1 = new CourseDO();
        course1.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("CS101")
                .setCourseName("数据结构")
                .setCourseTypeUuid(theoryType)
                .setCourseCredit(new BigDecimal("4.0"));
        courses.add(course1);

        CourseDO course2 = new CourseDO();
        course2.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("CS102")
                .setCourseName("操作系统")
                .setCourseTypeUuid(theoryType)
                .setCourseCredit(new BigDecimal("3.5"));
        courses.add(course2);

        CourseDO course3 = new CourseDO();
        course3.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("CS201")
                .setCourseName("Java程序设计")
                .setCourseTypeUuid(labType)
                .setCourseCredit(new BigDecimal("3.0"));
        courses.add(course3);

        CourseDO course4 = new CourseDO();
        course4.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("EE101")
                .setCourseName("电路原理")
                .setCourseTypeUuid(theoryType)
                .setCourseCredit(new BigDecimal("4.0"));
        courses.add(course4);

        CourseDO course5 = new CourseDO();
        course5.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("PE101")
                .setCourseName("大学体育")
                .setCourseTypeUuid(peType)
                .setCourseCredit(new BigDecimal("1.0"));
        courses.add(course5);

        CourseDO course6 = new CourseDO();
        course6.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("CS301")
                .setCourseName("数据库系统")
                .setCourseTypeUuid(theoryType)
                .setCourseCredit(new BigDecimal("3.0"));
        courses.add(course6);

        // 新增课程（9门）
        CourseDO course7 = new CourseDO();
        course7.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("CS103")
                .setCourseName("计算机网络")
                .setCourseTypeUuid(theoryType)
                .setCourseCredit(new BigDecimal("3.5"));
        courses.add(course7);

        CourseDO course8 = new CourseDO();
        course8.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("CS104")
                .setCourseName("编译原理")
                .setCourseTypeUuid(theoryType)
                .setCourseCredit(new BigDecimal("3.0"));
        courses.add(course8);

        CourseDO course9 = new CourseDO();
        course9.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("SE101")
                .setCourseName("软件工程")
                .setCourseTypeUuid(theoryType)
                .setCourseCredit(new BigDecimal("3.0"));
        courses.add(course9);

        CourseDO course10 = new CourseDO();
        course10.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("EE102")
                .setCourseName("通信原理")
                .setCourseTypeUuid(theoryType)
                .setCourseCredit(new BigDecimal("4.0"));
        courses.add(course10);

        CourseDO course11 = new CourseDO();
        course11.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("EE103")
                .setCourseName("信号与系统")
                .setCourseTypeUuid(theoryType)
                .setCourseCredit(new BigDecimal("3.5"));
        courses.add(course11);

        CourseDO course12 = new CourseDO();
        course12.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("ME101")
                .setCourseName("机械制图")
                .setCourseTypeUuid(practiceType)
                .setCourseCredit(new BigDecimal("2.5"));
        courses.add(course12);

        CourseDO course13 = new CourseDO();
        course13.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("ME102")
                .setCourseName("自动控制原理")
                .setCourseTypeUuid(theoryType)
                .setCourseCredit(new BigDecimal("3.5"));
        courses.add(course13);

        CourseDO course14 = new CourseDO();
        course14.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("AR101")
                .setCourseName("音乐鉴赏")
                .setCourseTypeUuid(practiceType)
                .setCourseCredit(new BigDecimal("1.5"));
        courses.add(course14);

        CourseDO course15 = new CourseDO();
        course15.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("BA101")
                .setCourseName("管理学")
                .setCourseTypeUuid(theoryType)
                .setCourseCredit(new BigDecimal("3.0"));
        courses.add(course15);

        courseDAO.saveBatch(courses);
        log.info("课程数据初始化完成，共 {} 条记录", courses.size());
        return courses;
    }

    /**
     * 初始化教室数据
     * 扩充到142个教室
     */
    private List<ClassroomDO> initializeClassrooms(List<BuildingDO> buildings, List<ClassroomTypeDO> classroomTypes) {
        log.info("正在初始化教室数据...");

        List<ClassroomDO> classrooms = new ArrayList<>();

        // 教室类型UUID引用
        String normalRoomType = classroomTypes.get(0).getClassroomTypeUuid(); // 普通教室
        String computerRoomType = classroomTypes.get(1).getClassroomTypeUuid(); // 机房
        String labRoomType = classroomTypes.get(2).getClassroomTypeUuid(); // 实验室
        String gymRoomType = classroomTypes.get(3).getClassroomTypeUuid(); // 体育馆
        String lectureRoomType = classroomTypes.get(4).getClassroomTypeUuid(); // 阶梯教室

        // A栋（第一教学楼）- 20个教室
        String buildingA = buildings.get(0).getBuildingUuid();
        // 普通教室 A101-A115（15个，60人容量）
        for (int i = 1; i <= 15; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingA)
                    .setClassroomName("A" + String.format("%02d", 100 + i))
                    .setClassroomCapacity(60)
                    .setClassroomTypeUuid(normalRoomType);
            classrooms.add(room);
        }
        // 阶梯教室 A201-A205（5个，120人容量）
        for (int i = 1; i <= 5; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingA)
                    .setClassroomName("A" + String.format("%02d", 200 + i))
                    .setClassroomCapacity(120)
                    .setClassroomTypeUuid(lectureRoomType);
            classrooms.add(room);
        }

        // B栋（第二教学楼）- 20个教室
        String buildingB = buildings.get(1).getBuildingUuid();
        // 普通教室 B101-B115（15个，50人容量）
        for (int i = 1; i <= 15; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingB)
                    .setClassroomName("B" + String.format("%02d", 100 + i))
                    .setClassroomCapacity(50)
                    .setClassroomTypeUuid(normalRoomType);
            classrooms.add(room);
        }
        // 阶梯教室 B201-B205（5个，100人容量）
        for (int i = 1; i <= 5; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingB)
                    .setClassroomName("B" + String.format("%02d", 200 + i))
                    .setClassroomCapacity(100)
                    .setClassroomTypeUuid(lectureRoomType);
            classrooms.add(room);
        }

        // C栋（实验楼）- 20个教室
        String buildingC = buildings.get(2).getBuildingUuid();
        // 机房 C101-C108（8个，40人容量）
        for (int i = 1; i <= 8; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingC)
                    .setClassroomName("C" + String.format("%02d", 100 + i))
                    .setClassroomCapacity(40)
                    .setClassroomTypeUuid(computerRoomType);
            classrooms.add(room);
        }
        // 实验室 C109-C120（12个，30人容量）
        for (int i = 9; i <= 20; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingC)
                    .setClassroomName("C" + String.format("%02d", 100 + i))
                    .setClassroomCapacity(30)
                    .setClassroomTypeUuid(labRoomType);
            classrooms.add(room);
        }

        // D栋（信息楼）- 2个教室（体育馆）
        String buildingD = buildings.get(3).getBuildingUuid();
        ClassroomDO d101 = new ClassroomDO();
        d101.setClassroomUuid(UuidUtil.generateUuidNoDash())
                .setBuildingUuid(buildingD)
                .setClassroomName("D101")
                .setClassroomCapacity(100)
                .setClassroomTypeUuid(gymRoomType);
        classrooms.add(d101);

        ClassroomDO d102 = new ClassroomDO();
        d102.setClassroomUuid(UuidUtil.generateUuidNoDash())
                .setBuildingUuid(buildingD)
                .setClassroomName("D102")
                .setClassroomCapacity(80)
                .setClassroomTypeUuid(gymRoomType);
        classrooms.add(d102);

        // E栋（第三教学楼）- 20个教室
        String buildingE = buildings.get(4).getBuildingUuid();
        // 普通教室 E101-E115（15个，60人容量）
        for (int i = 1; i <= 15; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingE)
                    .setClassroomName("E" + String.format("%02d", 100 + i))
                    .setClassroomCapacity(60)
                    .setClassroomTypeUuid(normalRoomType);
            classrooms.add(room);
        }
        // 阶梯教室 E201-E205（5个，120人容量）
        for (int i = 1; i <= 5; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingE)
                    .setClassroomName("E" + String.format("%02d", 200 + i))
                    .setClassroomCapacity(120)
                    .setClassroomTypeUuid(lectureRoomType);
            classrooms.add(room);
        }

        // F栋（第四教学楼）- 20个教室
        String buildingF = buildings.get(5).getBuildingUuid();
        // 普通教室 F101-F115（15个，50人容量）
        for (int i = 1; i <= 15; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingF)
                    .setClassroomName("F" + String.format("%02d", 100 + i))
                    .setClassroomCapacity(50)
                    .setClassroomTypeUuid(normalRoomType);
            classrooms.add(room);
        }
        // 阶梯教室 F201-F205（5个，100人容量）
        for (int i = 1; i <= 5; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingF)
                    .setClassroomName("F" + String.format("%02d", 200 + i))
                    .setClassroomCapacity(100)
                    .setClassroomTypeUuid(lectureRoomType);
            classrooms.add(room);
        }

        // G栋（艺术楼）- 20个教室（需要新增艺术类教室类型，这里使用多媒体教室类型）
        String buildingG = buildings.get(6).getBuildingUuid();
        // 音乐教室 G101-G105（5个，40人容量）
        for (int i = 1; i <= 5; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingG)
                    .setClassroomName("G" + String.format("%02d", 100 + i))
                    .setClassroomCapacity(40)
                    .setClassroomTypeUuid(normalRoomType); // 使用普通教室类型
            classrooms.add(room);
        }
        // 美术教室 G106-G110（5个，40人容量）
        for (int i = 6; i <= 10; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingG)
                    .setClassroomName("G" + String.format("%02d", 100 + i))
                    .setClassroomCapacity(40)
                    .setClassroomTypeUuid(normalRoomType);
            classrooms.add(room);
        }
        // 多媒体教室 G111-G120（10个，50人容量）
        for (int i = 11; i <= 20; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingG)
                    .setClassroomName("G" + String.format("%02d", 100 + i))
                    .setClassroomCapacity(50)
                    .setClassroomTypeUuid(computerRoomType); // 使用机房类型
            classrooms.add(room);
        }

        // H栋（综合楼）- 20个教室
        String buildingH = buildings.get(7).getBuildingUuid();
        // 普通教室 H101-H115（15个，60人容量）
        for (int i = 1; i <= 15; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingH)
                    .setClassroomName("H" + String.format("%02d", 100 + i))
                    .setClassroomCapacity(60)
                    .setClassroomTypeUuid(normalRoomType);
            classrooms.add(room);
        }
        // 报告厅 H201-H205（5个，150人容量）
        for (int i = 1; i <= 5; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingH)
                    .setClassroomName("H" + String.format("%02d", 200 + i))
                    .setClassroomCapacity(150)
                    .setClassroomTypeUuid(lectureRoomType);
            classrooms.add(room);
        }

        classroomDAO.saveBatch(classrooms);
        log.info("教室数据初始化完成，共 {} 条记录", classrooms.size());
        return classrooms;
    }

    /**
     * 初始化行政班级数据
     * 扩充到20个班级
     */
    private List<ClassDO> initializeClasses(List<MajorDO> majors) {
        log.info("正在初始化行政班级数据...");

        List<ClassDO> classes = new ArrayList<>();

        // 专业索引：0-计科, 1-软件, 2-电子, 3-机械, 4-网络, 5-通信, 6-自动化, 7-工商

        // 计算机科学与技术专业：计科2101-2105班（5个）
        for (int i = 1; i <= 5; i++) {
            ClassDO cls = new ClassDO();
            cls.setClassUuid(UuidUtil.generateUuidNoDash())
                    .setMajorUuid(majors.get(0).getMajorUuid())
                    .setClassName("计科210" + i + "班");
            classes.add(cls);
        }

        // 软件工程专业：软件2101-2104班（4个）
        for (int i = 1; i <= 4; i++) {
            ClassDO cls = new ClassDO();
            cls.setClassUuid(UuidUtil.generateUuidNoDash())
                    .setMajorUuid(majors.get(1).getMajorUuid())
                    .setClassName("软件210" + i + "班");
            classes.add(cls);
        }

        // 电子信息工程专业：电子2101-2103班（3个）
        for (int i = 1; i <= 3; i++) {
            ClassDO cls = new ClassDO();
            cls.setClassUuid(UuidUtil.generateUuidNoDash())
                    .setMajorUuid(majors.get(2).getMajorUuid())
                    .setClassName("电子210" + i + "班");
            classes.add(cls);
        }

        // 机械设计制造专业：机械2101班（1个）
        ClassDO mechClass = new ClassDO();
        mechClass.setClassUuid(UuidUtil.generateUuidNoDash())
                .setMajorUuid(majors.get(3).getMajorUuid())
                .setClassName("机械2101班");
        classes.add(mechClass);

        // 网络工程专业：网络2101-2102班（2个）
        for (int i = 1; i <= 2; i++) {
            ClassDO cls = new ClassDO();
            cls.setClassUuid(UuidUtil.generateUuidNoDash())
                    .setMajorUuid(majors.get(4).getMajorUuid())
                    .setClassName("网络210" + i + "班");
            classes.add(cls);
        }

        // 通信工程专业：通信2101-2102班（2个）
        for (int i = 1; i <= 2; i++) {
            ClassDO cls = new ClassDO();
            cls.setClassUuid(UuidUtil.generateUuidNoDash())
                    .setMajorUuid(majors.get(5).getMajorUuid())
                    .setClassName("通信210" + i + "班");
            classes.add(cls);
        }

        // 自动化专业：自动化2101-2102班（2个）
        for (int i = 1; i <= 2; i++) {
            ClassDO cls = new ClassDO();
            cls.setClassUuid(UuidUtil.generateUuidNoDash())
                    .setMajorUuid(majors.get(6).getMajorUuid())
                    .setClassName("自动化210" + i + "班");
            classes.add(cls);
        }

        // 工商管理专业：工商2101-2102班（2个）
        for (int i = 1; i <= 2; i++) {
            ClassDO cls = new ClassDO();
            cls.setClassUuid(UuidUtil.generateUuidNoDash())
                    .setMajorUuid(majors.get(7).getMajorUuid())
                    .setClassName("工商210" + i + "班");
            classes.add(cls);
        }

        classDAO.saveBatch(classes);
        log.info("行政班级数据初始化完成，共 {} 条记录", classes.size());
        return classes;
    }

    /**
     * 初始化教师数据
     * 扩充到20位教师
     */
    private List<TeacherDO> initializeTeachers(List<DepartmentDO> departments) {
        log.info("正在初始化教师数据...");

        List<TeacherDO> teachers = new ArrayList<>();

        // 学院UUID引用
        String csDeptUuid = departments.get(0).getDepartmentUuid(); // 计算机科学与技术学院
        String eeDeptUuid = departments.get(1).getDepartmentUuid(); // 电子信息工程学院
        String meDeptUuid = departments.get(2).getDepartmentUuid(); // 机械工程学院
        String baDeptUuid = departments.get(3).getDepartmentUuid(); // 经济管理学院

        // 计算机科学与技术学院（7人）
        TeacherDO t1 = new TeacherDO();
        t1.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1001")
                .setTeacherName("张教授")
                .setTitle("教授")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(20)
                .setLikeTime("{\"1\":[1,2,3,4],\"2\":[1,2,3,4],\"3\":[1,2,3,4]}")
                .setIsActive(true);
        teachers.add(t1);

        TeacherDO t2 = new TeacherDO();
        t2.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1002")
                .setTeacherName("王副教授")
                .setTitle("副教授")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(18)
                .setLikeTime("{\"2\":[5,6,7,8],\"4\":[5,6,7,8]}")
                .setIsActive(true);
        teachers.add(t2);

        TeacherDO t3 = new TeacherDO();
        t3.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1003")
                .setTeacherName("李讲师")
                .setTitle("讲师")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"3\":[1,2],\"5\":[1,2]}")
                .setIsActive(true);
        teachers.add(t3);

        TeacherDO t4 = new TeacherDO();
        t4.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1004")
                .setTeacherName("赵助教")
                .setTitle("助教")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(12)
                .setLikeTime("{\"1\":[3,4],\"4\":[3,4]}")
                .setIsActive(true);
        teachers.add(t4);

        TeacherDO t5 = new TeacherDO();
        t5.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1005")
                .setTeacherName("刘教授")
                .setTitle("教授")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(20)
                .setLikeTime("{\"1\":[1,2],\"3\":[1,2]}")
                .setIsActive(true);
        teachers.add(t5);

        TeacherDO t6 = new TeacherDO();
        t6.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1006")
                .setTeacherName("陈副教授")
                .setTitle("副教授")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(18)
                .setLikeTime("{\"2\":[3,4],\"4\":[3,4]}")
                .setIsActive(true);
        teachers.add(t6);

        TeacherDO t7 = new TeacherDO();
        t7.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1007")
                .setTeacherName("周讲师")
                .setTitle("讲师")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"5\":[3,4]}")
                .setIsActive(true);
        teachers.add(t7);

        // 电子信息工程学院（5人）
        TeacherDO t8 = new TeacherDO();
        t8.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T2001")
                .setTeacherName("孙教授")
                .setTitle("教授")
                .setDepartmentUuid(eeDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(20)
                .setLikeTime("{\"1\":[5,6],\"3\":[5,6]}")
                .setIsActive(true);
        teachers.add(t8);

        TeacherDO t9 = new TeacherDO();
        t9.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T2002")
                .setTeacherName("马副教授")
                .setTitle("副教授")
                .setDepartmentUuid(eeDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(18)
                .setLikeTime("{\"2\":[1,2],\"4\":[1,2]}")
                .setIsActive(true);
        teachers.add(t9);

        TeacherDO t10 = new TeacherDO();
        t10.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T2003")
                .setTeacherName("郑副教授")
                .setTitle("副教授")
                .setDepartmentUuid(eeDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(18)
                .setLikeTime("{\"3\":[3,4],\"5\":[3,4]}")
                .setIsActive(true);
        teachers.add(t10);

        TeacherDO t11 = new TeacherDO();
        t11.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T2004")
                .setTeacherName("冯讲师")
                .setTitle("讲师")
                .setDepartmentUuid(eeDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"1\":[7,8],\"4\":[7,8]}")
                .setIsActive(true);
        teachers.add(t11);

        TeacherDO t12 = new TeacherDO();
        t12.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T2005")
                .setTeacherName("袁助教")
                .setTitle("助教")
                .setDepartmentUuid(eeDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(12)
                .setLikeTime("{\"2\":[7,8],\"5\":[7,8]}")
                .setIsActive(true);
        teachers.add(t12);

        // 机械工程学院（3人）
        TeacherDO t13 = new TeacherDO();
        t13.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T3001")
                .setTeacherName("吴讲师")
                .setTitle("讲师")
                .setDepartmentUuid(meDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"1\":[1,2],\"2\":[1,2]}")
                .setIsActive(true);
        teachers.add(t13);

        TeacherDO t14 = new TeacherDO();
        t14.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T3002")
                .setTeacherName("钱助教")
                .setTitle("助教")
                .setDepartmentUuid(meDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(12)
                .setLikeTime("{\"3\":[5,6],\"4\":[5,6]}")
                .setIsActive(true);
        teachers.add(t14);

        TeacherDO t15 = new TeacherDO();
        t15.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T3003")
                .setTeacherName("徐讲师")
                .setTitle("讲师")
                .setDepartmentUuid(meDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"1\":[3,4],\"5\":[5,6]}")
                .setIsActive(true);
        teachers.add(t15);

        // 经济管理学院（2人）
        TeacherDO t16 = new TeacherDO();
        t16.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T4001")
                .setTeacherName("林教授")
                .setTitle("教授")
                .setDepartmentUuid(baDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(20)
                .setLikeTime("{\"2\":[3,4],\"4\":[3,4]}")
                .setIsActive(true);
        teachers.add(t16);

        TeacherDO t17 = new TeacherDO();
        t17.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T4002")
                .setTeacherName("曹副教授")
                .setTitle("副教授")
                .setDepartmentUuid(baDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(18)
                .setLikeTime("{\"1\":[5,6],\"3\":[5,6]}")
                .setIsActive(true);
        teachers.add(t17);

        // 体艺部教师（3人，使用计算机学院作为归属）
        TeacherDO t18 = new TeacherDO();
        t18.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T5001")
                .setTeacherName("何老师")
                .setTitle("讲师")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"1\":[7,8],\"3\":[7,8]}")
                .setIsActive(true);
        teachers.add(t18);

        TeacherDO t19 = new TeacherDO();
        t19.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T5002")
                .setTeacherName("许助教")
                .setTitle("助教")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(12)
                .setLikeTime("{\"2\":[5,6],\"4\":[5,6]}")
                .setIsActive(true);
        teachers.add(t19);

        TeacherDO t20 = new TeacherDO();
        t20.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T5003")
                .setTeacherName("高老师")
                .setTitle("讲师")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"5\":[1,2],\"5\":[3,4]}")
                .setIsActive(true);
        teachers.add(t20);

        teacherDAO.saveBatch(teachers);
        log.info("教师数据初始化完成，共 {} 条记录", teachers.size());
        return teachers;
    }

    /**
     * 初始化学生数据
     * 扩充到100个学生，均匀分布在20个班级（每班5人）
     */
    private List<StudentDO> initializeStudents(List<ClassDO> classes) {
        log.info("正在初始化学生数据...");

        List<StudentDO> students = new ArrayList<>();

        // 学生姓名池 - 扩大以避免重复
        String[] surnames = {
                "张", "李", "王", "赵", "钱", "孙", "周", "吴", "郑", "冯",
                "陈", "褚", "卫", "蒋", "沈", "韩", "杨", "朱", "秦", "尤",
                "许", "何", "吕", "施", "张", "孔", "曹", "严", "华", "金",
                "魏", "陶", "姜", "戚", "谢", "邹", "喻", "柏", "水", "窦",
                "章", "云", "苏", "潘", "葛", "奚", "范", "彭", "鲁", "韦"
        };

        String[] names = {
                "伟", "芳", "娜", "敏", "静", "秀英", "丽", "强", "磊", "军",
                "洋", "勇", "艳", "杰", "娟", "涛", "明", "超", "秀兰", "霞",
                "平", "刚", "桂英", "玉兰", "萍", "鹏", "华", "红", "鑫", "健",
                "国庆", "建国", "卫国", "建设", "文明", "和平", "五一", "胜利", "保华", "向东"
        };

        int studentIndex = 0;

        // 为每个班级生成5个学生
        for (int classIndex = 0; classIndex < classes.size(); classIndex++) {
            ClassDO classDO = classes.get(classIndex);
            String classUuid = classDO.getClassUuid();

            // 生成班级编号（如 202101 表示 计科2101班）
            String classCode = "2021" + String.format("%02d", classIndex + 1);

            for (int i = 1; i <= 5; i++) {
                // 组合生成唯一姓名：姓氏索引 + 名字索引的组合
                int surnameIndex = (studentIndex / names.length) % surnames.length;
                int nameIndex = studentIndex % names.length;

                StudentDO student = new StudentDO();
                student.setStudentUuid(UuidUtil.generateUuidNoDash())
                        .setStudentId(classCode + String.format("%03d", i))
                        .setStudentName(surnames[surnameIndex] + names[nameIndex])
                        .setClassUuid(classUuid)
                        .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
                students.add(student);
                studentIndex++;
            }
        }

        studentDAO.saveBatch(students);
        log.info("学生数据初始化完成，共 {} 条记录", students.size());
        return students;
    }

    /**
     * 初始化课程教师资格关联数据
     * 扩充到更多关联记录
     */
    private void initializeCourseQualifications(List<CourseDO> courses, List<TeacherDO> teachers) {
        log.info("正在初始化课程教师资格关联数据...");

        List<CourseQualificationDO> qualifications = new ArrayList<>();

        // 教师索引分组
        // 计算机学院教师(0-6): 张教授、王副教授、李讲师、赵助教、刘教授、陈副教授、周讲师
        // 电子信息学院教师(7-11): 孙教授、马副教授、郑副教授、冯讲师、袁助教
        // 机械学院教师(12-14): 吴讲师、钱助教、徐讲师
        // 经管学院教师(15-16): 林教授、曹副教授
        // 体艺部教师(17-19): 何老师、许助教、高老师

        // 原有课程关联（保留）
        // 课程1(数据结构) -> 张教授(T1001, 索引0)
        CourseQualificationDO qual1 = new CourseQualificationDO();
        qual1.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(0).getCourseUuid())
                .setTeacherUuid(teachers.get(0).getTeacherUuid());
        qualifications.add(qual1);

        // 课程2(操作系统) -> 张教授(T1001, 索引0)
        CourseQualificationDO qual2 = new CourseQualificationDO();
        qual2.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(1).getCourseUuid())
                .setTeacherUuid(teachers.get(0).getTeacherUuid());
        qualifications.add(qual2);

        // 课程2(操作系统) -> 王副教授(T1002, 索引1)
        CourseQualificationDO qual3 = new CourseQualificationDO();
        qual3.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(1).getCourseUuid())
                .setTeacherUuid(teachers.get(1).getTeacherUuid());
        qualifications.add(qual3);

        // 课程3(Java程序设计) -> 李讲师(T1003, 索引2)
        CourseQualificationDO qual4 = new CourseQualificationDO();
        qual4.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(2).getCourseUuid())
                .setTeacherUuid(teachers.get(2).getTeacherUuid());
        qualifications.add(qual4);

        // 课程4(电路原理) -> 王副教授(T1002, 索引1)
        CourseQualificationDO qual5 = new CourseQualificationDO();
        qual5.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(3).getCourseUuid())
                .setTeacherUuid(teachers.get(1).getTeacherUuid());
        qualifications.add(qual5);

        // 课程5(大学体育) -> 赵助教(T1004, 索引3)
        CourseQualificationDO qual6 = new CourseQualificationDO();
        qual6.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(4).getCourseUuid())
                .setTeacherUuid(teachers.get(3).getTeacherUuid());
        qualifications.add(qual6);

        // 课程6(数据库系统) -> 李讲师(T1003, 索引2)
        CourseQualificationDO qual7 = new CourseQualificationDO();
        qual7.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(5).getCourseUuid())
                .setTeacherUuid(teachers.get(2).getTeacherUuid());
        qualifications.add(qual7);

        // 新增课程关联
        // 课程7(计算机网络) -> 刘教授(T1005, 索引4)
        CourseQualificationDO qual8 = new CourseQualificationDO();
        qual8.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(6).getCourseUuid())
                .setTeacherUuid(teachers.get(4).getTeacherUuid());
        qualifications.add(qual8);

        // 课程7(计算机网络) -> 陈副教授(T1006, 索引5)
        CourseQualificationDO qual9 = new CourseQualificationDO();
        qual9.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(6).getCourseUuid())
                .setTeacherUuid(teachers.get(5).getTeacherUuid());
        qualifications.add(qual9);

        // 课程8(编译原理) -> 周讲师(T1007, 索引6)
        CourseQualificationDO qual10 = new CourseQualificationDO();
        qual10.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(7).getCourseUuid())
                .setTeacherUuid(teachers.get(6).getTeacherUuid());
        qualifications.add(qual10);

        // 课程9(软件工程) -> 刘教授(T1005, 索引4)
        CourseQualificationDO qual11 = new CourseQualificationDO();
        qual11.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(8).getCourseUuid())
                .setTeacherUuid(teachers.get(4).getTeacherUuid());
        qualifications.add(qual11);

        // 课程10(通信原理) -> 孙教授(T2001, 索引7)
        CourseQualificationDO qual12 = new CourseQualificationDO();
        qual12.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(9).getCourseUuid())
                .setTeacherUuid(teachers.get(7).getTeacherUuid());
        qualifications.add(qual12);

        // 课程10(通信原理) -> 马副教授(T2002, 索引8)
        CourseQualificationDO qual13 = new CourseQualificationDO();
        qual13.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(9).getCourseUuid())
                .setTeacherUuid(teachers.get(8).getTeacherUuid());
        qualifications.add(qual13);

        // 课程11(信号与系统) -> 郑副教授(T2003, 索引9)
        CourseQualificationDO qual14 = new CourseQualificationDO();
        qual14.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(10).getCourseUuid())
                .setTeacherUuid(teachers.get(9).getTeacherUuid());
        qualifications.add(qual14);

        // 课程12(机械制图) -> 吴讲师(T3001, 索引12)
        CourseQualificationDO qual15 = new CourseQualificationDO();
        qual15.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(11).getCourseUuid())
                .setTeacherUuid(teachers.get(12).getTeacherUuid());
        qualifications.add(qual15);

        // 课程13(自动控制原理) -> 徐讲师(T3003, 索引14)
        CourseQualificationDO qual16 = new CourseQualificationDO();
        qual16.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(12).getCourseUuid())
                .setTeacherUuid(teachers.get(14).getTeacherUuid());
        qualifications.add(qual16);

        // 课程14(音乐鉴赏) -> 何老师(T5001, 索引17)
        CourseQualificationDO qual17 = new CourseQualificationDO();
        qual17.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(13).getCourseUuid())
                .setTeacherUuid(teachers.get(17).getTeacherUuid());
        qualifications.add(qual17);

        // 课程15(管理学) -> 林教授(T4001, 索引15)
        CourseQualificationDO qual18 = new CourseQualificationDO();
        qual18.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(14).getCourseUuid())
                .setTeacherUuid(teachers.get(15).getTeacherUuid());
        qualifications.add(qual18);

        // 额外添加一些关联以增加数据多样性
        // 数据结构 -> 刘教授
        CourseQualificationDO qual19 = new CourseQualificationDO();
        qual19.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(0).getCourseUuid())
                .setTeacherUuid(teachers.get(4).getTeacherUuid());
        qualifications.add(qual19);

        // 电路原理 -> 孙教授
        CourseQualificationDO qual20 = new CourseQualificationDO();
        qual20.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(3).getCourseUuid())
                .setTeacherUuid(teachers.get(7).getTeacherUuid());
        qualifications.add(qual20);

        courseQualificationDAO.saveBatch(qualifications);
        log.info("课程教师资格关联数据初始化完成，共 {} 条记录", qualifications.size());
    }

    /**
     * 初始化教学班数据
     * 扩充到30个教学班
     */
    private List<TeachingClassDO> initializeTeachingClasses(
            List<CourseDO> courses,
            List<TeacherDO> teachers,
            List<SemesterDO> semesters) {
        log.info("正在初始化教学班数据...");

        List<TeachingClassDO> teachingClasses = new ArrayList<>();

        String semester1 = semesters.get(0).getSemesterUuid();
        String semester2 = semesters.get(1).getSemesterUuid();

        // 课程和教师配对（基于教师资格关联）
        // 数据结构-张教授 (tc1-tc3)
        for (int i = 1; i <= 3; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(0).getCourseUuid()) // 数据结构
                    .setTeacherUuid(teachers.get(0).getTeacherUuid()) // 张教授
                    .setSemesterUuid(semester1)
                    .setTeachingClassName("数据结构-张教授-计科210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // 操作系统-张教授 (tc4-tc6)
        for (int i = 1; i <= 2; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(1).getCourseUuid()) // 操作系统
                    .setTeacherUuid(teachers.get(0).getTeacherUuid()) // 张教授
                    .setSemesterUuid(semester1)
                    .setTeachingClassName("操作系统-张教授-计科210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // 操作系统-王副教授 (tc7-tc9)
        for (int i = 1; i <= 3; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(1).getCourseUuid()) // 操作系统
                    .setTeacherUuid(teachers.get(1).getTeacherUuid()) // 王副教授
                    .setSemesterUuid(semester1)
                    .setTeachingClassName("操作系统-王副教授-软件210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // Java程序设计-李讲师 (tc10-tc13)
        for (int i = 1; i <= 4; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(2).getCourseUuid()) // Java程序设计
                    .setTeacherUuid(teachers.get(2).getTeacherUuid()) // 李讲师
                    .setSemesterUuid(semester1)
                    .setTeachingClassName("Java程序设计-李讲师-计科210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // 电路原理-王副教授 (tc14-tc16)
        for (int i = 1; i <= 3; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(3).getCourseUuid()) // 电路原理
                    .setTeacherUuid(teachers.get(1).getTeacherUuid()) // 王副教授
                    .setSemesterUuid(semester1)
                    .setTeachingClassName("电路原理-王副教授-电子210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // 大学体育-赵助教 (tc17-tc22)
        for (int i = 1; i <= 6; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(4).getCourseUuid()) // 大学体育
                    .setTeacherUuid(teachers.get(3).getTeacherUuid()) // 赵助教
                    .setSemesterUuid(i <= 3 ? semester1 : semester2)
                    .setTeachingClassName("大学体育-赵助教-计科210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // 数据库系统-李讲师 (tc23-tc25)
        for (int i = 1; i <= 3; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(5).getCourseUuid()) // 数据库系统
                    .setTeacherUuid(teachers.get(2).getTeacherUuid()) // 李讲师
                    .setSemesterUuid(semester1)
                    .setTeachingClassName("数据库系统-李讲师-软件210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // 计算机网络-刘教授 (tc26-tc28)
        for (int i = 1; i <= 3; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(6).getCourseUuid()) // 计算机网络
                    .setTeacherUuid(teachers.get(4).getTeacherUuid()) // 刘教授
                    .setSemesterUuid(semester1)
                    .setTeachingClassName("计算机网络-刘教授-网络210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // 编译原理-周讲师 (tc29)
        TeachingClassDO tc29 = new TeachingClassDO();
        tc29.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(7).getCourseUuid()) // 编译原理
                .setTeacherUuid(teachers.get(6).getTeacherUuid()) // 周讲师
                .setSemesterUuid(semester1)
                .setTeachingClassName("编译原理-周讲师-计科2104班")
                .setTeachingClassHours(0);
        teachingClasses.add(tc29);

        // 软件工程-刘教授 (tc30)
        TeachingClassDO tc30 = new TeachingClassDO();
        tc30.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(8).getCourseUuid()) // 软件工程
                .setTeacherUuid(teachers.get(4).getTeacherUuid()) // 刘教授
                .setSemesterUuid(semester1)
                .setTeachingClassName("软件工程-刘教授-软件2103班")
                .setTeachingClassHours(0);
        teachingClasses.add(tc30);

        // 通信原理-孙教授 (tc31) - 新增以补足30个教学班
        TeachingClassDO tc31 = new TeachingClassDO();
        tc31.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(9).getCourseUuid()) // 通信原理
                .setTeacherUuid(teachers.get(7).getTeacherUuid()) // 孙教授
                .setSemesterUuid(semester1)
                .setTeachingClassName("通信原理-孙教授-通信2101班")
                .setTeachingClassHours(0);
        teachingClasses.add(tc31);

        // 先保存教学班（学时暂时为0，待排课初始化完成后更新）
        teachingClassDAO.saveBatch(teachingClasses);
        log.info("教学班数据初始化完成，共 {} 条记录（待更新学时）", teachingClasses.size());
        return teachingClasses;
    }

    /**
     * 初始化教学班-行政班关联数据
     * 扩充到匹配30个教学班
     *
     * 教学班索引分布（共30个，索引0-29）：
     * 0-2: 数据结构-张教授（3个）
     * 3-4: 操作系统-张教授（2个）
     * 5-7: 操作系统-王副教授（3个）
     * 8-11: Java程序设计-李讲师（4个）
     * 12-14: 电路原理-王副教授（3个）
     * 15-20: 大学体育-赵助教（6个）
     * 21-23: 数据库系统-李讲师（3个）
     * 24-26: 计算机网络-刘教授（3个）
     * 27: 编译原理-周讲师（1个）
     * 28: 软件工程-刘教授（1个）
     * 29: 通信原理-孙教授（1个）
     */
    private void initializeTeachingClassClasses(
            List<TeachingClassDO> teachingClasses,
            List<ClassDO> classes) {
        log.info("正在初始化教学班-行政班关联数据...");

        List<TeachingClassClassDO> relations = new ArrayList<>();

        // 班级索引：0-4 计科2101-2105, 5-8 软件2101-2104, 9-11 电子2101-2103,
        // 12 机械2101, 13-14 网络2101-2102, 15-16 通信2101-2102,
        // 17-18 自动化2101-2102, 19 工商2101

        // 索引0-2: 数据结构-张教授 -> 计科2101-2103班
        for (int i = 0; i < 3; i++) {
            TeachingClassClassDO rel = new TeachingClassClassDO();
            rel.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                    .setTeachingClassUuid(teachingClasses.get(i).getTeachingClassUuid())
                    .setClassUuid(classes.get(i).getClassUuid());
            relations.add(rel);
        }

        // 索引3-4: 操作系统-张教授 -> 计科2101-2102班
        for (int i = 0; i < 2; i++) {
            TeachingClassClassDO rel = new TeachingClassClassDO();
            rel.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                    .setTeachingClassUuid(teachingClasses.get(3 + i).getTeachingClassUuid())
                    .setClassUuid(classes.get(i).getClassUuid()); // 计科2101-2102
            relations.add(rel);
        }

        // 索引5-7: 操作系统-王副教授 -> 软件2101-2103班
        for (int i = 0; i < 3; i++) {
            TeachingClassClassDO rel = new TeachingClassClassDO();
            rel.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                    .setTeachingClassUuid(teachingClasses.get(5 + i).getTeachingClassUuid())
                    .setClassUuid(classes.get(5 + i).getClassUuid());
            relations.add(rel);
        }

        // 索引8-11: Java程序设计-李讲师 -> 计科2101-2104班
        for (int i = 0; i < 4; i++) {
            TeachingClassClassDO rel = new TeachingClassClassDO();
            rel.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                    .setTeachingClassUuid(teachingClasses.get(8 + i).getTeachingClassUuid())
                    .setClassUuid(classes.get(i).getClassUuid());
            relations.add(rel);
        }

        // 索引12-14: 电路原理-王副教授 -> 电子2101-2103班
        for (int i = 0; i < 3; i++) {
            TeachingClassClassDO rel = new TeachingClassClassDO();
            rel.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                    .setTeachingClassUuid(teachingClasses.get(12 + i).getTeachingClassUuid())
                    .setClassUuid(classes.get(9 + i).getClassUuid());
            relations.add(rel);
        }

        // 索引15-20: 大学体育-赵助教 -> 计科2101-2105班 + 软件2101班
        for (int i = 0; i < 6; i++) {
            TeachingClassClassDO rel = new TeachingClassClassDO();
            rel.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                    .setTeachingClassUuid(teachingClasses.get(15 + i).getTeachingClassUuid())
                    .setClassUuid(classes.get(i < 5 ? i : 5).getClassUuid());
            relations.add(rel);
        }

        // 索引21-23: 数据库系统-李讲师 -> 软件2101-2103班
        for (int i = 0; i < 3; i++) {
            TeachingClassClassDO rel = new TeachingClassClassDO();
            rel.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                    .setTeachingClassUuid(teachingClasses.get(21 + i).getTeachingClassUuid())
                    .setClassUuid(classes.get(5 + i).getClassUuid());
            relations.add(rel);
        }

        // 索引24-26: 计算机网络-刘教授 -> 网络2101班 + 网络2102班 + 通信2101班
        TeachingClassClassDO rel24 = new TeachingClassClassDO();
        rel24.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                .setTeachingClassUuid(teachingClasses.get(24).getTeachingClassUuid())
                .setClassUuid(classes.get(13).getClassUuid()); // 网络2101
        relations.add(rel24);

        TeachingClassClassDO rel25 = new TeachingClassClassDO();
        rel25.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                .setTeachingClassUuid(teachingClasses.get(25).getTeachingClassUuid())
                .setClassUuid(classes.get(14).getClassUuid()); // 网络2102
        relations.add(rel25);

        TeachingClassClassDO rel26 = new TeachingClassClassDO();
        rel26.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                .setTeachingClassUuid(teachingClasses.get(26).getTeachingClassUuid())
                .setClassUuid(classes.get(15).getClassUuid()); // 通信2101
        relations.add(rel26);

        // 索引27: 编译原理-周讲师 -> 计科2104班
        TeachingClassClassDO rel27 = new TeachingClassClassDO();
        rel27.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                .setTeachingClassUuid(teachingClasses.get(27).getTeachingClassUuid())
                .setClassUuid(classes.get(3).getClassUuid()); // 计科2104
        relations.add(rel27);

        // 索引28: 软件工程-刘教授 -> 软件2103班
        TeachingClassClassDO rel28 = new TeachingClassClassDO();
        rel28.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                .setTeachingClassUuid(teachingClasses.get(28).getTeachingClassUuid())
                .setClassUuid(classes.get(7).getClassUuid()); // 软件2103
        relations.add(rel28);

        // 索引29: 通信原理-孙教授 -> 通信2101班
        TeachingClassClassDO rel29 = new TeachingClassClassDO();
        rel29.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                .setTeachingClassUuid(teachingClasses.get(29).getTeachingClassUuid())
                .setClassUuid(classes.get(15).getClassUuid()); // 通信2101
        relations.add(rel29);

        teachingClassClassDAO.saveBatch(relations);
        log.info("教学班-行政班关联数据初始化完成，共 {} 条记录", relations.size());
    }

    /**
     * 初始化排课记录数据
     * 扩充到50条排课记录
     */
    private void initializeSchedules(
            List<TeachingClassDO> teachingClasses,
            List<SemesterDO> semesters,
            List<ClassroomDO> classrooms,
            List<CourseDO> courses,
            List<TeacherDO> teachers) {
        log.info("正在初始化排课记录数据...");

        List<ScheduleDO> schedules = new ArrayList<>();

        // 生成周次JSON字符串
        String fullWeeks = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18]";
        String weeks1to8 = "[1,2,3,4,5,6,7,8]";
        String weeks1to16 = "[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16]";
        String weeks9to16 = "[9,10,11,12,13,14,15,16]";

        // ObjectMapper 用于解析周次JSON数组
        ObjectMapper objectMapper = new ObjectMapper();

        // 辅助方法：创建排课记录
        int[][] scheduleTemplates = {
                // 周日, 节次开始, 节次结束, 教室索引, 周次类型(0-full,1-1to8,2-1to16,3-9to16)
                {1, 1, 2, 0, 1}, {1, 3, 4, 1, 0}, {2, 1, 2, 2, 0}, {2, 3, 4, 3, 2},
                {3, 1, 2, 4, 0}, {3, 3, 4, 5, 1}, {3, 5, 6, 6, 0}, {4, 1, 2, 7, 0},
                {4, 3, 4, 8, 2}, {4, 5, 6, 9, 0}, {5, 1, 2, 10, 0}, {5, 3, 4, 11, 1},
                {1, 5, 6, 12, 0}, {2, 5, 6, 13, 2}, {3, 7, 8, 14, 0}, {4, 7, 8, 15, 0},
                {5, 5, 6, 16, 0}, {1, 7, 8, 17, 2}, {2, 7, 8, 18, 0}, {3, 1, 2, 19, 1},
                {4, 1, 2, 20, 0}, {5, 7, 8, 21, 0}, {1, 1, 2, 22, 0}, {2, 3, 4, 23, 2},
                {3, 5, 6, 24, 0}, {4, 3, 4, 25, 1}, {5, 1, 2, 26, 0}, {1, 3, 4, 27, 0},
                {2, 1, 2, 28, 2}, {3, 3, 4, 29, 0}, {4, 5, 6, 30, 0}, {5, 3, 4, 31, 1},
                {1, 5, 6, 32, 0}, {2, 5, 6, 33, 0}, {3, 7, 8, 34, 2}, {4, 7, 8, 35, 0},
                {5, 5, 6, 36, 0}, {1, 7, 8, 37, 1}, {2, 7, 8, 38, 0}, {3, 1, 2, 39, 0},
                {4, 1, 2, 40, 2}, {5, 3, 4, 41, 0}, {1, 3, 4, 42, 0}, {2, 5, 6, 43, 1},
                {3, 5, 6, 44, 0}, {4, 3, 4, 45, 0}, {5, 1, 2, 46, 2}, {1, 1, 2, 47, 0},
                {2, 3, 4, 48, 0}, {3, 7, 8, 49, 1}
        };

        String[] weeksArray = {fullWeeks, weeks1to8, weeks1to16, weeks9to16};

        // 为前30个教学班生成排课记录（每个教学班1-2条）
        for (int tcIndex = 0; tcIndex < Math.min(30, teachingClasses.size()); tcIndex++) {
            TeachingClassDO tc = teachingClasses.get(tcIndex);

            // 每个教学班生成1-2条排课记录
            int recordCount = (tcIndex % 2 == 0) ? 2 : 1;

            for (int r = 0; r < recordCount; r++) {
                int templateIndex = (tcIndex * 2 + r) % scheduleTemplates.length;
                int[] template = scheduleTemplates[templateIndex];

                // 确保教室索引不越界
                int classroomIndex = template[3] % classrooms.size();

                ScheduleDO sched = new ScheduleDO();
                sched.setScheduleUuid(UuidUtil.generateUuidNoDash())
                        .setSemesterUuid(tc.getSemesterUuid())
                        .setTeachingClassUuid(tc.getTeachingClassUuid())
                        .setCourseUuid(tc.getCourseUuid())
                        .setTeacherUuid(tc.getTeacherUuid())
                        .setClassroomUuid(classrooms.get(classroomIndex).getClassroomUuid())
                        .setDayOfWeek(template[0])
                        .setSectionStart(template[1])
                        .setSectionEnd(template[2])
                        .setWeeksJson(weeksArray[template[4]])
                        .setStatus(1);

                // 计算学时
                try {
                    JsonNode weeksNode = objectMapper.readTree(weeksArray[template[4]]);
                    int weekCount = weeksNode.size();
                    int creditHours = (sched.getSectionEnd() - sched.getSectionStart() + 1) * weekCount;
                    sched.setCreditHours(creditHours);
                } catch (Exception e) {
                    log.warn("解析周次JSON失败，使用默认值0", e);
                    sched.setCreditHours(0);
                }

                schedules.add(sched);
            }
        }

        // 为剩余的教学班（如果有）添加额外的排课记录以达到50条
        int currentIndex = schedules.size();
        for (int i = currentIndex; i < 50; i++) {
            int tcIndex = i % teachingClasses.size();
            TeachingClassDO tc = teachingClasses.get(tcIndex);
            int templateIndex = i % scheduleTemplates.length;
            int[] template = scheduleTemplates[templateIndex];
            int classroomIndex = (template[3] + i) % classrooms.size();

            ScheduleDO sched = new ScheduleDO();
            sched.setScheduleUuid(UuidUtil.generateUuidNoDash())
                    .setSemesterUuid(tc.getSemesterUuid())
                    .setTeachingClassUuid(tc.getTeachingClassUuid())
                    .setCourseUuid(tc.getCourseUuid())
                    .setTeacherUuid(tc.getTeacherUuid())
                    .setClassroomUuid(classrooms.get(classroomIndex).getClassroomUuid())
                    .setDayOfWeek(template[0])
                    .setSectionStart(template[1])
                    .setSectionEnd(template[2])
                    .setWeeksJson(weeksArray[template[4]])
                    .setStatus(1);

            // 计算学时
            try {
                JsonNode weeksNode = objectMapper.readTree(weeksArray[template[4]]);
                int weekCount = weeksNode.size();
                int creditHours = (sched.getSectionEnd() - sched.getSectionStart() + 1) * weekCount;
                sched.setCreditHours(creditHours);
            } catch (Exception e) {
                log.warn("解析周次JSON失败，使用默认值0", e);
                sched.setCreditHours(0);
            }

            schedules.add(sched);
        }

        // 保存排课记录
        scheduleDAO.saveBatch(schedules);
        log.info("排课记录数据初始化完成，共 {} 条记录", schedules.size());

        // 更新所有教学班的学时统计
        log.info("正在更新教学班学时统计...");
        for (TeachingClassDO tc : teachingClasses) {
            // 统计该教学班的所有排课学时
            int totalHours = schedules.stream()
                    .filter(s -> s.getTeachingClassUuid().equals(tc.getTeachingClassUuid()))
                    .mapToInt(s -> s.getCreditHours() != null ? s.getCreditHours() : 0)
                    .sum();
            tc.setTeachingClassHours(totalHours);
            log.info("教学班 {} 学时: {}", tc.getTeachingClassName(), totalHours);
        }

        // 更新教学班学时（已存在的记录）
        teachingClassDAO.updateBatchById(teachingClasses);
        log.info("教学班学时更新完成");
    }
}
