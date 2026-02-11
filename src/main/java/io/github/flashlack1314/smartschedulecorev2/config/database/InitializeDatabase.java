package io.github.flashlack1314.smartschedulecorev2.config.database;

import com.xlf.utility.util.PasswordUtil;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
                .setSemesterName("2024-2025学年第一学期");
        semesters.add(sem1);

        SemesterDO sem2 = new SemesterDO();
        sem2.setSemesterUuid(UuidUtil.generateUuidNoDash())
                .setSemesterName("2024-2025学年第二学期");
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
     */
    private List<CourseDO> initializeCourses(List<CourseTypeDO> courseTypes) {
        log.info("正在初始化课程数据...");

        List<CourseDO> courses = new ArrayList<>();

        CourseDO course1 = new CourseDO();
        course1.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("CS101")
                .setCourseName("数据结构")
                .setCourseTypeUuid(courseTypes.get(0).getCourseTypeUuid())
                .setCourseCredit(new BigDecimal("4.0"));
        courses.add(course1);

        CourseDO course2 = new CourseDO();
        course2.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("CS102")
                .setCourseName("操作系统")
                .setCourseTypeUuid(courseTypes.get(0).getCourseTypeUuid())
                .setCourseCredit(new BigDecimal("3.5"));
        courses.add(course2);

        CourseDO course3 = new CourseDO();
        course3.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("CS201")
                .setCourseName("Java程序设计")
                .setCourseTypeUuid(courseTypes.get(1).getCourseTypeUuid())
                .setCourseCredit(new BigDecimal("3.0"));
        courses.add(course3);

        CourseDO course4 = new CourseDO();
        course4.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("EE101")
                .setCourseName("电路原理")
                .setCourseTypeUuid(courseTypes.get(0).getCourseTypeUuid())
                .setCourseCredit(new BigDecimal("4.0"));
        courses.add(course4);

        CourseDO course5 = new CourseDO();
        course5.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("PE101")
                .setCourseName("大学体育")
                .setCourseTypeUuid(courseTypes.get(3).getCourseTypeUuid())
                .setCourseCredit(new BigDecimal("1.0"));
        courses.add(course5);

        CourseDO course6 = new CourseDO();
        course6.setCourseUuid(UuidUtil.generateUuidNoDash())
                .setCourseNum("CS301")
                .setCourseName("数据库系统")
                .setCourseTypeUuid(courseTypes.get(0).getCourseTypeUuid())
                .setCourseCredit(new BigDecimal("3.0"));
        courses.add(course6);

        courseDAO.saveBatch(courses);
        log.info("课程数据初始化完成，共 {} 条记录", courses.size());
        return courses;
    }

    /**
     * 初始化教室数据
     */
    private List<ClassroomDO> initializeClassrooms(List<BuildingDO> buildings, List<ClassroomTypeDO> classroomTypes) {
        log.info("正在初始化教室数据...");

        List<ClassroomDO> classrooms = new ArrayList<>();

        ClassroomDO room1 = new ClassroomDO();
        room1.setClassroomUuid(UuidUtil.generateUuidNoDash())
                .setBuildingUuid(buildings.get(0).getBuildingUuid())
                .setClassroomName("A101")
                .setClassroomCapacity(60)
                .setClassroomTypeUuid(classroomTypes.get(0).getClassroomTypeUuid());
        classrooms.add(room1);

        ClassroomDO room2 = new ClassroomDO();
        room2.setClassroomUuid(UuidUtil.generateUuidNoDash())
                .setBuildingUuid(buildings.get(0).getBuildingUuid())
                .setClassroomName("A102")
                .setClassroomCapacity(80)
                .setClassroomTypeUuid(classroomTypes.get(0).getClassroomTypeUuid());
        classrooms.add(room2);

        ClassroomDO room3 = new ClassroomDO();
        room3.setClassroomUuid(UuidUtil.generateUuidNoDash())
                .setBuildingUuid(buildings.get(0).getBuildingUuid())
                .setClassroomName("A201")
                .setClassroomCapacity(120)
                .setClassroomTypeUuid(classroomTypes.get(4).getClassroomTypeUuid());
        classrooms.add(room3);

        ClassroomDO room4 = new ClassroomDO();
        room4.setClassroomUuid(UuidUtil.generateUuidNoDash())
                .setBuildingUuid(buildings.get(1).getBuildingUuid())
                .setClassroomName("B101")
                .setClassroomCapacity(50)
                .setClassroomTypeUuid(classroomTypes.get(0).getClassroomTypeUuid());
        classrooms.add(room4);

        ClassroomDO room5 = new ClassroomDO();
        room5.setClassroomUuid(UuidUtil.generateUuidNoDash())
                .setBuildingUuid(buildings.get(2).getBuildingUuid())
                .setClassroomName("C101")
                .setClassroomCapacity(40)
                .setClassroomTypeUuid(classroomTypes.get(1).getClassroomTypeUuid());
        classrooms.add(room5);

        ClassroomDO room6 = new ClassroomDO();
        room6.setClassroomUuid(UuidUtil.generateUuidNoDash())
                .setBuildingUuid(buildings.get(2).getBuildingUuid())
                .setClassroomName("C201")
                .setClassroomCapacity(30)
                .setClassroomTypeUuid(classroomTypes.get(2).getClassroomTypeUuid());
        classrooms.add(room6);

        ClassroomDO room7 = new ClassroomDO();
        room7.setClassroomUuid(UuidUtil.generateUuidNoDash())
                .setBuildingUuid(buildings.get(3).getBuildingUuid())
                .setClassroomName("D101")
                .setClassroomCapacity(100)
                .setClassroomTypeUuid(classroomTypes.get(3).getClassroomTypeUuid());
        classrooms.add(room7);

        classroomDAO.saveBatch(classrooms);
        log.info("教室数据初始化完成，共 {} 条记录", classrooms.size());
        return classrooms;
    }

    /**
     * 初始化行政班级数据
     */
    private List<ClassDO> initializeClasses(List<MajorDO> majors) {
        log.info("正在初始化行政班级数据...");

        List<ClassDO> classes = new ArrayList<>();

        ClassDO class1 = new ClassDO();
        class1.setClassUuid(UuidUtil.generateUuidNoDash())
                .setMajorUuid(majors.get(0).getMajorUuid())
                .setClassName("计科2101班");
        classes.add(class1);

        ClassDO class2 = new ClassDO();
        class2.setClassUuid(UuidUtil.generateUuidNoDash())
                .setMajorUuid(majors.get(0).getMajorUuid())
                .setClassName("计科2102班");
        classes.add(class2);

        ClassDO class3 = new ClassDO();
        class3.setClassUuid(UuidUtil.generateUuidNoDash())
                .setMajorUuid(majors.get(1).getMajorUuid())
                .setClassName("软件2101班");
        classes.add(class3);

        ClassDO class4 = new ClassDO();
        class4.setClassUuid(UuidUtil.generateUuidNoDash())
                .setMajorUuid(majors.get(2).getMajorUuid())
                .setClassName("电子2101班");
        classes.add(class4);

        classDAO.saveBatch(classes);
        log.info("行政班级数据初始化完成，共 {} 条记录", classes.size());
        return classes;
    }

    /**
     * 初始化教师数据
     */
    private List<TeacherDO> initializeTeachers(List<DepartmentDO> departments) {
        log.info("正在初始化教师数据...");

        List<TeacherDO> teachers = new ArrayList<>();

        // 获取计算机科学与技术学院的 UUID (第一个学院)
        String csDepartmentUuid = departments.get(0).getDepartmentUuid();

        TeacherDO teacher1 = new TeacherDO();
        teacher1.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1001")
                .setTeacherName("张教授")
                .setTitle("教授")
                .setDepartmentUuid(csDepartmentUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(20)
                .setLikeTime("{\"1\":[1,2,3,4],\"2\":[1,2,3,4],\"3\":[1,2,3,4]}")
                .setIsActive(true);
        teachers.add(teacher1);

        TeacherDO teacher2 = new TeacherDO();
        teacher2.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1002")
                .setTeacherName("王副教授")
                .setTitle("副教授")
                .setDepartmentUuid(csDepartmentUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(18)
                .setLikeTime("{\"2\":[5,6,7,8],\"4\":[5,6,7,8]}")
                .setIsActive(true);
        teachers.add(teacher2);

        TeacherDO teacher3 = new TeacherDO();
        teacher3.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1003")
                .setTeacherName("李讲师")
                .setTitle("讲师")
                .setDepartmentUuid(csDepartmentUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"3\":[1,2],\"5\":[1,2]}")
                .setIsActive(true);
        teachers.add(teacher3);

        TeacherDO teacher4 = new TeacherDO();
        teacher4.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1004")
                .setTeacherName("赵老师")
                .setTitle("助教")
                .setDepartmentUuid(csDepartmentUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(12)
                .setLikeTime("{\"1\":[3,4],\"4\":[3,4]}")
                .setIsActive(true);
        teachers.add(teacher4);

        teacherDAO.saveBatch(teachers);
        log.info("教师数据初始化完成，共 {} 条记录", teachers.size());
        return teachers;
    }

    /**
     * 初始化学生数据
     */
    private List<StudentDO> initializeStudents(List<ClassDO> classes) {
        log.info("正在初始化学生数据...");

        List<StudentDO> students = new ArrayList<>();

        StudentDO student1 = new StudentDO();
        student1.setStudentUuid(UuidUtil.generateUuidNoDash())
                .setStudentId("202101001")
                .setStudentName("张三")
                .setClassUuid(classes.get(0).getClassUuid())
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student1);

        StudentDO student2 = new StudentDO();
        student2.setStudentUuid(UuidUtil.generateUuidNoDash())
                .setStudentId("202101002")
                .setStudentName("李四")
                .setClassUuid(classes.get(0).getClassUuid())
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student2);

        StudentDO student3 = new StudentDO();
        student3.setStudentUuid(UuidUtil.generateUuidNoDash())
                .setStudentId("202101003")
                .setStudentName("王五")
                .setClassUuid(classes.get(0).getClassUuid())
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student3);

        StudentDO student4 = new StudentDO();
        student4.setStudentUuid(UuidUtil.generateUuidNoDash())
                .setStudentId("202102001")
                .setStudentName("赵六")
                .setClassUuid(classes.get(1).getClassUuid())
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student4);

        StudentDO student5 = new StudentDO();
        student5.setStudentUuid(UuidUtil.generateUuidNoDash())
                .setStudentId("202103001")
                .setStudentName("钱七")
                .setClassUuid(classes.get(2).getClassUuid())
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student5);

        StudentDO student6 = new StudentDO();
        student6.setStudentUuid(UuidUtil.generateUuidNoDash())
                .setStudentId("202104001")
                .setStudentName("孙八")
                .setClassUuid(classes.get(3).getClassUuid())
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student6);

        studentDAO.saveBatch(students);
        log.info("学生数据初始化完成，共 {} 条记录", students.size());
        return students;
    }

    /**
     * 初始化课程教师资格关联数据
     */
    private void initializeCourseQualifications(List<CourseDO> courses, List<TeacherDO> teachers) {
        log.info("正在初始化课程教师资格关联数据...");

        List<CourseQualificationDO> qualifications = new ArrayList<>();

        CourseQualificationDO qual1 = new CourseQualificationDO();
        qual1.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(0).getCourseUuid())
                .setTeacherUuid(teachers.get(0).getTeacherUuid());
        qualifications.add(qual1);

        CourseQualificationDO qual2 = new CourseQualificationDO();
        qual2.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(1).getCourseUuid())
                .setTeacherUuid(teachers.get(0).getTeacherUuid());
        qualifications.add(qual2);

        CourseQualificationDO qual3 = new CourseQualificationDO();
        qual3.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(1).getCourseUuid())
                .setTeacherUuid(teachers.get(1).getTeacherUuid());
        qualifications.add(qual3);

        CourseQualificationDO qual4 = new CourseQualificationDO();
        qual4.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(2).getCourseUuid())
                .setTeacherUuid(teachers.get(2).getTeacherUuid());
        qualifications.add(qual4);

        CourseQualificationDO qual5 = new CourseQualificationDO();
        qual5.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(3).getCourseUuid())
                .setTeacherUuid(teachers.get(1).getTeacherUuid());
        qualifications.add(qual5);

        CourseQualificationDO qual6 = new CourseQualificationDO();
        qual6.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(4).getCourseUuid())
                .setTeacherUuid(teachers.get(3).getTeacherUuid());
        qualifications.add(qual6);

        CourseQualificationDO qual7 = new CourseQualificationDO();
        qual7.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(5).getCourseUuid())
                .setTeacherUuid(teachers.get(2).getTeacherUuid());
        qualifications.add(qual7);

        courseQualificationDAO.saveBatch(qualifications);
        log.info("课程教师资格关联数据初始化完成，共 {} 条记录", qualifications.size());
    }
}
