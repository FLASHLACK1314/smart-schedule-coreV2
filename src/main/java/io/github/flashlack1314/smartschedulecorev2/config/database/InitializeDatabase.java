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
        this.initializeDepartments();
        this.initializeBuildings();
        this.initializeCourseTypes();
        this.initializeClassroomTypes();
        this.initializeSemesters();

        // 第二层：依赖基础数据的表
        this.initializeMajors();
        this.initializeAcademicAdmins();

        // 第三层：核心业务数据
        this.initializeCourseClassroomTypes();
        this.initializeCourses();
        this.initializeClassrooms();
        this.initializeClasses();
        this.initializeTeachers();
        this.initializeStudents();

        // 第四层：关联数据
        this.initializeCourseQualifications();

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
    private void initializeDepartments() {
        log.info("正在初始化学院数据...");

        List<DepartmentDO> departments = new ArrayList<>();

        DepartmentDO dept1 = new DepartmentDO();
        dept1.setDepartmentUuid("dept_001")
                .setDepartmentName("计算机科学与技术学院");
        departments.add(dept1);

        DepartmentDO dept2 = new DepartmentDO();
        dept2.setDepartmentUuid("dept_002")
                .setDepartmentName("电子信息工程学院");
        departments.add(dept2);

        DepartmentDO dept3 = new DepartmentDO();
        dept3.setDepartmentUuid("dept_003")
                .setDepartmentName("机械工程学院");
        departments.add(dept3);

        DepartmentDO dept4 = new DepartmentDO();
        dept4.setDepartmentUuid("dept_004")
                .setDepartmentName("经济管理学院");
        departments.add(dept4);

        departmentDAO.saveBatch(departments);
        log.info("学院数据初始化完成，共 {} 条记录", departments.size());
    }

    /**
     * 初始化教学楼数据
     */
    private void initializeBuildings() {
        log.info("正在初始化教学楼数据...");

        List<BuildingDO> buildings = new ArrayList<>();

        BuildingDO building1 = new BuildingDO();
        building1.setBuildingUuid("bld_001")
                .setBuildingNum("A")
                .setBuildingName("第一教学楼");
        buildings.add(building1);

        BuildingDO building2 = new BuildingDO();
        building2.setBuildingUuid("bld_002")
                .setBuildingNum("B")
                .setBuildingName("第二教学楼");
        buildings.add(building2);

        BuildingDO building3 = new BuildingDO();
        building3.setBuildingUuid("bld_003")
                .setBuildingNum("C")
                .setBuildingName("实验楼");
        buildings.add(building3);

        BuildingDO building4 = new BuildingDO();
        building4.setBuildingUuid("bld_004")
                .setBuildingNum("D")
                .setBuildingName("信息楼");
        buildings.add(building4);

        buildingDAO.saveBatch(buildings);
        log.info("教学楼数据初始化完成，共 {} 条记录", buildings.size());
    }

    /**
     * 初始化课程类型数据
     */
    private void initializeCourseTypes() {
        log.info("正在初始化课程类型数据...");

        List<CourseTypeDO> courseTypes = new ArrayList<>();

        CourseTypeDO ct1 = new CourseTypeDO();
        ct1.setCourseTypeUuid("ct_001")
                .setTypeName("理论课")
                .setTypeDescription("以理论讲授为主的课程");
        courseTypes.add(ct1);

        CourseTypeDO ct2 = new CourseTypeDO();
        ct2.setCourseTypeUuid("ct_002")
                .setTypeName("实验课")
                .setTypeDescription("需要实验设备的课程");
        courseTypes.add(ct2);

        CourseTypeDO ct3 = new CourseTypeDO();
        ct3.setCourseTypeUuid("ct_003")
                .setTypeName("实践课")
                .setTypeDescription("实践操作类课程");
        courseTypes.add(ct3);

        CourseTypeDO ct4 = new CourseTypeDO();
        ct4.setCourseTypeUuid("ct_004")
                .setTypeName("体育课")
                .setTypeDescription("体育类课程");
        courseTypes.add(ct4);

        courseTypeDAO.saveBatch(courseTypes);
        log.info("课程类型数据初始化完成，共 {} 条记录", courseTypes.size());
    }

    /**
     * 初始化教室类型数据
     */
    private void initializeClassroomTypes() {
        log.info("正在初始化教室类型数据...");

        List<ClassroomTypeDO> classroomTypes = new ArrayList<>();

        ClassroomTypeDO crt1 = new ClassroomTypeDO();
        crt1.setClassroomTypeUuid("room_type_001")
                .setTypeName("普通教室")
                .setTypeDescription("标准多媒体教室");
        classroomTypes.add(crt1);

        ClassroomTypeDO crt2 = new ClassroomTypeDO();
        crt2.setClassroomTypeUuid("room_type_002")
                .setTypeName("机房")
                .setTypeDescription("计算机实验室");
        classroomTypes.add(crt2);

        ClassroomTypeDO crt3 = new ClassroomTypeDO();
        crt3.setClassroomTypeUuid("room_type_003")
                .setTypeName("实验室")
                .setTypeDescription("专业实验教室");
        classroomTypes.add(crt3);

        ClassroomTypeDO crt4 = new ClassroomTypeDO();
        crt4.setClassroomTypeUuid("room_type_004")
                .setTypeName("体育馆")
                .setTypeDescription("体育场馆");
        classroomTypes.add(crt4);

        ClassroomTypeDO crt5 = new ClassroomTypeDO();
        crt5.setClassroomTypeUuid("room_type_005")
                .setTypeName("阶梯教室")
                .setTypeDescription("大型阶梯教室");
        classroomTypes.add(crt5);

        classroomTypeDAO.saveBatch(classroomTypes);
        log.info("教室类型数据初始化完成，共 {} 条记录", classroomTypes.size());
    }

    /**
     * 初始化学期数据
     */
    private void initializeSemesters() {
        log.info("正在初始化学期数据...");

        List<SemesterDO> semesters = new ArrayList<>();

        SemesterDO sem1 = new SemesterDO();
        sem1.setSemesterUuid("sem_2024_01")
                .setSemesterName("2024-2025学年第一学期");
        semesters.add(sem1);

        SemesterDO sem2 = new SemesterDO();
        sem2.setSemesterUuid("sem_2024_02")
                .setSemesterName("2024-2025学年第二学期");
        semesters.add(sem2);

        semesterDAO.saveBatch(semesters);
        log.info("学期数据初始化完成，共 {} 条记录", semesters.size());
    }

    /**
     * 初始化专业数据
     */
    private void initializeMajors() {
        log.info("正在初始化专业数据...");

        List<MajorDO> majors = new ArrayList<>();

        MajorDO major1 = new MajorDO();
        major1.setMajorUuid("major_001")
                .setDepartmentUuid("dept_001")
                .setMajorNum("CS001")
                .setMajorName("计算机科学与技术");
        majors.add(major1);

        MajorDO major2 = new MajorDO();
        major2.setMajorUuid("major_002")
                .setDepartmentUuid("dept_001")
                .setMajorNum("SE001")
                .setMajorName("软件工程");
        majors.add(major2);

        MajorDO major3 = new MajorDO();
        major3.setMajorUuid("major_003")
                .setDepartmentUuid("dept_002")
                .setMajorNum("EE001")
                .setMajorName("电子信息工程");
        majors.add(major3);

        MajorDO major4 = new MajorDO();
        major4.setMajorUuid("major_004")
                .setDepartmentUuid("dept_003")
                .setMajorNum("ME001")
                .setMajorName("机械设计制造");
        majors.add(major4);

        majorDAO.saveBatch(majors);
        log.info("专业数据初始化完成，共 {} 条记录", majors.size());
    }

    /**
     * 初始化教务管理员数据
     */
    private void initializeAcademicAdmins() {
        log.info("正在初始化教务管理员数据...");

        List<AcademicAdminDO> academicAdmins = new ArrayList<>();

        AcademicAdminDO admin1 = new AcademicAdminDO();
        admin1.setAcademicUuid("academic_001")
                .setDepartmentUuid("dept_001")
                .setAcademicNum("A001")
                .setAcademicName("李教务")
                .setAcademicPassword(PasswordUtil.encrypt("qwer1234"));
        academicAdmins.add(admin1);

        AcademicAdminDO admin2 = new AcademicAdminDO();
        admin2.setAcademicUuid("academic_002")
                .setDepartmentUuid("dept_002")
                .setAcademicNum("A002")
                .setAcademicName("王教务")
                .setAcademicPassword(PasswordUtil.encrypt("qwer1234"));
        academicAdmins.add(admin2);

        academicAdminDAO.saveBatch(academicAdmins);
        log.info("教务管理员数据初始化完成，共 {} 条记录", academicAdmins.size());
    }

    /**
     * 初始化课程类型-教室类型关联数据
     */
    private void initializeCourseClassroomTypes() {
        log.info("正在初始化课程类型-教室类型关联数据...");

        List<CourseClassroomTypeDO> relations = new ArrayList<>();

        CourseClassroomTypeDO rel1 = new CourseClassroomTypeDO();
        rel1.setRelationUuid("rel_001")
                .setCourseTypeUuid("ct_001")
                .setClassroomTypeUuid("room_type_001");
        relations.add(rel1);

        CourseClassroomTypeDO rel2 = new CourseClassroomTypeDO();
        rel2.setRelationUuid("rel_002")
                .setCourseTypeUuid("ct_002")
                .setClassroomTypeUuid("room_type_003");
        relations.add(rel2);

        CourseClassroomTypeDO rel3 = new CourseClassroomTypeDO();
        rel3.setRelationUuid("rel_003")
                .setCourseTypeUuid("ct_003")
                .setClassroomTypeUuid("room_type_002");
        relations.add(rel3);

        CourseClassroomTypeDO rel4 = new CourseClassroomTypeDO();
        rel4.setRelationUuid("rel_004")
                .setCourseTypeUuid("ct_004")
                .setClassroomTypeUuid("room_type_004");
        relations.add(rel4);

        courseClassroomTypeDAO.saveBatch(relations);
        log.info("课程类型-教室类型关联数据初始化完成，共 {} 条记录", relations.size());
    }

    /**
     * 初始化课程数据
     */
    private void initializeCourses() {
        log.info("正在初始化课程数据...");

        List<CourseDO> courses = new ArrayList<>();

        CourseDO course1 = new CourseDO();
        course1.setCourseUuid("course_001")
                .setCourseNum("CS101")
                .setCourseName("数据结构")
                .setCourseTypeUuid("ct_001")
                .setCourseCredit(new BigDecimal("4.0"));
        courses.add(course1);

        CourseDO course2 = new CourseDO();
        course2.setCourseUuid("course_002")
                .setCourseNum("CS102")
                .setCourseName("操作系统")
                .setCourseTypeUuid("ct_001")
                .setCourseCredit(new BigDecimal("3.5"));
        courses.add(course2);

        CourseDO course3 = new CourseDO();
        course3.setCourseUuid("course_003")
                .setCourseNum("CS201")
                .setCourseName("Java程序设计")
                .setCourseTypeUuid("ct_002")
                .setCourseCredit(new BigDecimal("3.0"));
        courses.add(course3);

        CourseDO course4 = new CourseDO();
        course4.setCourseUuid("course_004")
                .setCourseNum("EE101")
                .setCourseName("电路原理")
                .setCourseTypeUuid("ct_001")
                .setCourseCredit(new BigDecimal("4.0"));
        courses.add(course4);

        CourseDO course5 = new CourseDO();
        course5.setCourseUuid("course_005")
                .setCourseNum("PE101")
                .setCourseName("大学体育")
                .setCourseTypeUuid("ct_004")
                .setCourseCredit(new BigDecimal("1.0"));
        courses.add(course5);

        CourseDO course6 = new CourseDO();
        course6.setCourseUuid("course_006")
                .setCourseNum("CS301")
                .setCourseName("数据库系统")
                .setCourseTypeUuid("ct_001")
                .setCourseCredit(new BigDecimal("3.0"));
        courses.add(course6);

        courseDAO.saveBatch(courses);
        log.info("课程数据初始化完成，共 {} 条记录", courses.size());
    }

    /**
     * 初始化教室数据
     */
    private void initializeClassrooms() {
        log.info("正在初始化教室数据...");

        List<ClassroomDO> classrooms = new ArrayList<>();

        ClassroomDO room1 = new ClassroomDO();
        room1.setClassroomUuid("room_001")
                .setBuildingUuid("bld_001")
                .setClassroomName("A101")
                .setClassroomCapacity(60)
                .setClassroomTypeUuid("room_type_001");
        classrooms.add(room1);

        ClassroomDO room2 = new ClassroomDO();
        room2.setClassroomUuid("room_002")
                .setBuildingUuid("bld_001")
                .setClassroomName("A102")
                .setClassroomCapacity(80)
                .setClassroomTypeUuid("room_type_001");
        classrooms.add(room2);

        ClassroomDO room3 = new ClassroomDO();
        room3.setClassroomUuid("room_003")
                .setBuildingUuid("bld_001")
                .setClassroomName("A201")
                .setClassroomCapacity(120)
                .setClassroomTypeUuid("room_type_005");
        classrooms.add(room3);

        ClassroomDO room4 = new ClassroomDO();
        room4.setClassroomUuid("room_004")
                .setBuildingUuid("bld_002")
                .setClassroomName("B101")
                .setClassroomCapacity(50)
                .setClassroomTypeUuid("room_type_001");
        classrooms.add(room4);

        ClassroomDO room5 = new ClassroomDO();
        room5.setClassroomUuid("room_005")
                .setBuildingUuid("bld_003")
                .setClassroomName("C101")
                .setClassroomCapacity(40)
                .setClassroomTypeUuid("room_type_002");
        classrooms.add(room5);

        ClassroomDO room6 = new ClassroomDO();
        room6.setClassroomUuid("room_006")
                .setBuildingUuid("bld_003")
                .setClassroomName("C201")
                .setClassroomCapacity(30)
                .setClassroomTypeUuid("room_type_003");
        classrooms.add(room6);

        ClassroomDO room7 = new ClassroomDO();
        room7.setClassroomUuid("room_007")
                .setBuildingUuid("bld_004")
                .setClassroomName("D101")
                .setClassroomCapacity(100)
                .setClassroomTypeUuid("room_type_004");
        classrooms.add(room7);

        classroomDAO.saveBatch(classrooms);
        log.info("教室数据初始化完成，共 {} 条记录", classrooms.size());
    }

    /**
     * 初始化行政班级数据
     */
    private void initializeClasses() {
        log.info("正在初始化行政班级数据...");

        List<ClassDO> classes = new ArrayList<>();

        ClassDO class1 = new ClassDO();
        class1.setClassUuid("class_001")
                .setMajorUuid("major_001")
                .setClassName("计科2101班");
        classes.add(class1);

        ClassDO class2 = new ClassDO();
        class2.setClassUuid("class_002")
                .setMajorUuid("major_001")
                .setClassName("计科2102班");
        classes.add(class2);

        ClassDO class3 = new ClassDO();
        class3.setClassUuid("class_003")
                .setMajorUuid("major_002")
                .setClassName("软件2101班");
        classes.add(class3);

        ClassDO class4 = new ClassDO();
        class4.setClassUuid("class_004")
                .setMajorUuid("major_003")
                .setClassName("电子2101班");
        classes.add(class4);

        classDAO.saveBatch(classes);
        log.info("行政班级数据初始化完成，共 {} 条记录", classes.size());
    }

    /**
     * 初始化教师数据
     */
    private void initializeTeachers() {
        log.info("正在初始化教师数据...");

        List<TeacherDO> teachers = new ArrayList<>();

        TeacherDO teacher1 = new TeacherDO();
        teacher1.setTeacherUuid("teacher_001")
                .setTeacherNum("T1001")
                .setTeacherName("张教授")
                .setTitle("教授")
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(20)
                .setLikeTime("周一、周二、周三的第1-2节和第3-4节")
                .setIsActive(true);
        teachers.add(teacher1);

        TeacherDO teacher2 = new TeacherDO();
        teacher2.setTeacherUuid("teacher_002")
                .setTeacherNum("T1002")
                .setTeacherName("王副教授")
                .setTitle("副教授")
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(18)
                .setLikeTime("周二、周四的第5-6节和第7-8节")
                .setIsActive(true);
        teachers.add(teacher2);

        TeacherDO teacher3 = new TeacherDO();
        teacher3.setTeacherUuid("teacher_003")
                .setTeacherNum("T1003")
                .setTeacherName("李讲师")
                .setTitle("讲师")
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("周三、周五的第1-2节")
                .setIsActive(true);
        teachers.add(teacher3);

        TeacherDO teacher4 = new TeacherDO();
        teacher4.setTeacherUuid("teacher_004")
                .setTeacherNum("T1004")
                .setTeacherName("赵老师")
                .setTitle("助教")
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(12)
                .setLikeTime("周一、周四的第3-4节")
                .setIsActive(true);
        teachers.add(teacher4);

        teacherDAO.saveBatch(teachers);
        log.info("教师数据初始化完成，共 {} 条记录", teachers.size());
    }

    /**
     * 初始化学生数据
     */
    private void initializeStudents() {
        log.info("正在初始化学生数据...");

        List<StudentDO> students = new ArrayList<>();

        StudentDO student1 = new StudentDO();
        student1.setStudentUuid("stu_001")
                .setStudentId("202101001")
                .setStudentName("张三")
                .setClassUuid("class_001")
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student1);

        StudentDO student2 = new StudentDO();
        student2.setStudentUuid("stu_002")
                .setStudentId("202101002")
                .setStudentName("李四")
                .setClassUuid("class_001")
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student2);

        StudentDO student3 = new StudentDO();
        student3.setStudentUuid("stu_003")
                .setStudentId("202101003")
                .setStudentName("王五")
                .setClassUuid("class_001")
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student3);

        StudentDO student4 = new StudentDO();
        student4.setStudentUuid("stu_004")
                .setStudentId("202102001")
                .setStudentName("赵六")
                .setClassUuid("class_002")
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student4);

        StudentDO student5 = new StudentDO();
        student5.setStudentUuid("stu_005")
                .setStudentId("202103001")
                .setStudentName("钱七")
                .setClassUuid("class_003")
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student5);

        StudentDO student6 = new StudentDO();
        student6.setStudentUuid("stu_006")
                .setStudentId("202104001")
                .setStudentName("孙八")
                .setClassUuid("class_004")
                .setStudentPassword(PasswordUtil.encrypt("qwer1234"));
        students.add(student6);

        studentDAO.saveBatch(students);
        log.info("学生数据初始化完成，共 {} 条记录", students.size());
    }

    /**
     * 初始化课程教师资格关联数据
     */
    private void initializeCourseQualifications() {
        log.info("正在初始化课程教师资格关联数据...");

        List<CourseQualificationDO> qualifications = new ArrayList<>();

        CourseQualificationDO qual1 = new CourseQualificationDO();
        qual1.setCourseQualificationUuid("qual_001")
                .setCourseUuid("course_001")
                .setTeacherUuid("teacher_001");
        qualifications.add(qual1);

        CourseQualificationDO qual2 = new CourseQualificationDO();
        qual2.setCourseQualificationUuid("qual_002")
                .setCourseUuid("course_002")
                .setTeacherUuid("teacher_001");
        qualifications.add(qual2);

        CourseQualificationDO qual3 = new CourseQualificationDO();
        qual3.setCourseQualificationUuid("qual_003")
                .setCourseUuid("course_002")
                .setTeacherUuid("teacher_002");
        qualifications.add(qual3);

        CourseQualificationDO qual4 = new CourseQualificationDO();
        qual4.setCourseQualificationUuid("qual_004")
                .setCourseUuid("course_003")
                .setTeacherUuid("teacher_003");
        qualifications.add(qual4);

        CourseQualificationDO qual5 = new CourseQualificationDO();
        qual5.setCourseQualificationUuid("qual_005")
                .setCourseUuid("course_004")
                .setTeacherUuid("teacher_002");
        qualifications.add(qual5);

        CourseQualificationDO qual6 = new CourseQualificationDO();
        qual6.setCourseQualificationUuid("qual_006")
                .setCourseUuid("course_005")
                .setTeacherUuid("teacher_004");
        qualifications.add(qual6);

        CourseQualificationDO qual7 = new CourseQualificationDO();
        qual7.setCourseQualificationUuid("qual_007")
                .setCourseUuid("course_006")
                .setTeacherUuid("teacher_003");
        qualifications.add(qual7);

        courseQualificationDAO.saveBatch(qualifications);
        log.info("课程教师资格关联数据初始化完成，共 {} 条记录", qualifications.size());
    }
}
