package io.github.flashlack1314.smartschedulecorev2.config.database;

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
 * 课程资源初始化器
 * 负责初始化课程和教室相关数据
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseResourceInitializer {

    private final CourseTypeDAO courseTypeDAO;
    private final ClassroomTypeDAO classroomTypeDAO;
    private final CourseClassroomTypeDAO courseClassroomTypeDAO;
    private final CourseDAO courseDAO;
    private final ClassroomDAO classroomDAO;
    private final ClassDAO classDAO;

    /**
     * 初始化课程类型数据
     */
    public List<CourseTypeDO> initializeCourseTypes() {
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
    public List<ClassroomTypeDO> initializeClassroomTypes() {
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
     * 初始化课程类型-教室类型关联数据
     */
    public List<CourseClassroomTypeDO> initializeCourseClassroomTypes(List<CourseTypeDO> courseTypes, List<ClassroomTypeDO> classroomTypes) {
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
    public List<CourseDO> initializeCourses(List<CourseTypeDO> courseTypes) {
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
    public List<ClassroomDO> initializeClassrooms(List<BuildingDO> buildings, List<ClassroomTypeDO> classroomTypes) {
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

        // G栋（艺术楼）- 20个教室
        String buildingG = buildings.get(6).getBuildingUuid();
        // 音乐教室 G101-G105（5个，40人容量）
        for (int i = 1; i <= 5; i++) {
            ClassroomDO room = new ClassroomDO();
            room.setClassroomUuid(UuidUtil.generateUuidNoDash())
                    .setBuildingUuid(buildingG)
                    .setClassroomName("G" + String.format("%02d", 100 + i))
                    .setClassroomCapacity(40)
                    .setClassroomTypeUuid(normalRoomType);
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
                    .setClassroomTypeUuid(computerRoomType);
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
    public List<ClassDO> initializeClasses(List<MajorDO> majors) {
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
}
