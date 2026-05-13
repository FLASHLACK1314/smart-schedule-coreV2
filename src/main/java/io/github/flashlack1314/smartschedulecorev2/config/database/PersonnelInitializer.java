package io.github.flashlack1314.smartschedulecorev2.config.database;

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
 * 人员数据初始化器
 * 负责初始化人员相关数据
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PersonnelInitializer {

    private final SystemAdminDAO systemAdminDAO;
    private final AcademicAdminDAO academicAdminDAO;
    private final TeacherDAO teacherDAO;
    private final StudentDAO studentDAO;
    private final CourseQualificationDAO courseQualificationDAO;

    /**
     * 初始化教务管理员数据
     */
    public List<AcademicAdminDO> initializeAcademicAdmins(List<DepartmentDO> departments) {
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
     * 初始化教师数据
     * 扩充到20位教师
     */
    public List<TeacherDO> initializeTeachers(List<DepartmentDO> departments) {
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

        // ===== 新增教师（21-30）=====

        // 计算机科学与技术学院（+4人：教授1、副教授1、讲师2）
        TeacherDO t21 = new TeacherDO();
        t21.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1008")
                .setTeacherName("孙教授")
                .setTitle("教授")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(20)
                .setLikeTime("{\"1\":[5,6],\"3\":[5,6]}")
                .setIsActive(true);
        teachers.add(t21);

        TeacherDO t22 = new TeacherDO();
        t22.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1009")
                .setTeacherName("吴副教授")
                .setTitle("副教授")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(18)
                .setLikeTime("{\"2\":[1,2],\"4\":[1,2]}")
                .setIsActive(true);
        teachers.add(t22);

        TeacherDO t23 = new TeacherDO();
        t23.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1010")
                .setTeacherName("郑讲师")
                .setTitle("讲师")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"1\":[3,4],\"5\":[3,4]}")
                .setIsActive(true);
        teachers.add(t23);

        TeacherDO t24 = new TeacherDO();
        t24.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T1011")
                .setTeacherName("冯讲师")
                .setTitle("讲师")
                .setDepartmentUuid(csDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"3\":[1,2],\"4\":[1,2]}")
                .setIsActive(true);
        teachers.add(t24);

        // 电子信息工程学院（+3人：教授1、副教授1、讲师1）
        TeacherDO t25 = new TeacherDO();
        t25.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T2006")
                .setTeacherName("李教授")
                .setTitle("教授")
                .setDepartmentUuid(eeDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(20)
                .setLikeTime("{\"1\":[1,2],\"3\":[1,2]}")
                .setIsActive(true);
        teachers.add(t25);

        TeacherDO t26 = new TeacherDO();
        t26.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T2007")
                .setTeacherName("周副教授")
                .setTitle("副教授")
                .setDepartmentUuid(eeDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(18)
                .setLikeTime("{\"2\":[3,4],\"4\":[3,4]}")
                .setIsActive(true);
        teachers.add(t26);

        TeacherDO t27 = new TeacherDO();
        t27.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T2008")
                .setTeacherName("梁讲师")
                .setTitle("讲师")
                .setDepartmentUuid(eeDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"1\":[7,8],\"5\":[7,8]}")
                .setIsActive(true);
        teachers.add(t27);

        // 机械工程学院（+2人：讲师1、助教1）
        TeacherDO t28 = new TeacherDO();
        t28.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T3004")
                .setTeacherName("姜讲师")
                .setTitle("讲师")
                .setDepartmentUuid(meDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(16)
                .setLikeTime("{\"2\":[5,6],\"3\":[5,6]}")
                .setIsActive(true);
        teachers.add(t28);

        TeacherDO t29 = new TeacherDO();
        t29.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T3005")
                .setTeacherName("沈助教")
                .setTitle("助教")
                .setDepartmentUuid(meDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(12)
                .setLikeTime("{\"4\":[7,8],\"5\":[7,8]}")
                .setIsActive(true);
        teachers.add(t29);

        // 经济管理学院（+1人：教授1）
        TeacherDO t30 = new TeacherDO();
        t30.setTeacherUuid(UuidUtil.generateUuidNoDash())
                .setTeacherNum("T4003")
                .setTeacherName("唐教授")
                .setTitle("教授")
                .setDepartmentUuid(baDeptUuid)
                .setTeacherPassword(PasswordUtil.encrypt("qwer1234"))
                .setMaxHoursPerWeek(20)
                .setLikeTime("{\"2\":[1,2],\"4\":[1,2]}")
                .setIsActive(true);
        teachers.add(t30);

        teacherDAO.saveBatch(teachers);
        log.info("教师数据初始化完成，共 {} 条记录", teachers.size());
        return teachers;
    }

    /**
     * 初始化学生数据
     * 扩充到100个学生，均匀分布在20个班级（每班5人）
     */
    public List<StudentDO> initializeStudents(List<ClassDO> classes) {
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
     * 创建默认系统管理员
     */
    public void createSystemAdmin() {
        log.info("正在创建系统管理员...");
        SystemAdminDO systemAdminDO = new SystemAdminDO();
        systemAdminDO.setAdminUuid(UuidUtil.generateUuidNoDash())
                .setAdminUsername("admin")
                .setAdminPassword(PasswordUtil.encrypt("qwer1234"));
        systemAdminDAO.save(systemAdminDO);
        log.info("系统管理员创建成功 - 用户名: admin, 密码: qwer1234");
    }

    /**
     * 初始化课程教师资格关联数据
     * 扩充到更多关联记录
     */
    public void initializeCourseQualifications(List<CourseDO> courses, List<TeacherDO> teachers) {
        log.info("正在初始化课程教师资格关联数据...");

        List<CourseQualificationDO> qualifications = new ArrayList<>();

        // 教师索引分组
        // 计算机学院教师(0-6): 张教授、王副教授、李讲师、赵助教、刘教授、陈副教授、周讲师
        // 电子信息学院教师(7-11): 孙教授、马副教授、郑副教授、冯讲师、袁助教
        // 机械学院教师(12-14): 吴讲师、钱助教、徐讲师
        // 经管学院教师(15-16): 林教授、曹副教授
        // 体艺部教师(17-19): 何老师、许助教、高老师
        // 新增教师(20-23): 孙教授、吴副教授、郑讲师、冯讲师(计算机学院)
        // 新增教师(24-26): 李教授、周副教授、梁讲师(电子信息学院)
        // 新增教师(27-28): 姜讲师、沈助教(机械学院)
        // 新增教师(29): 唐教授(经济管理学院)

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

        // ===== 张教授新增资格（让他能教更多课程）=====
        // 数据库系统 -> 张教授（新增，之前李讲师在教）
        CourseQualificationDO qual22 = new CourseQualificationDO();
        qual22.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(5).getCourseUuid())
                .setTeacherUuid(teachers.get(0).getTeacherUuid());
        qualifications.add(qual22);

        // 计算机网络 -> 张教授（新增，之前刘教授在教）
        CourseQualificationDO qual23 = new CourseQualificationDO();
        qual23.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(6).getCourseUuid())
                .setTeacherUuid(teachers.get(0).getTeacherUuid());
        qualifications.add(qual23);

        // 软件工程 -> 张教授（新增，之前刘教授在教）
        CourseQualificationDO qual24 = new CourseQualificationDO();
        qual24.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(8).getCourseUuid())
                .setTeacherUuid(teachers.get(0).getTeacherUuid());
        qualifications.add(qual24);

        // 编译原理 -> 张教授（新增，之前周讲师在教）
        CourseQualificationDO qual25 = new CourseQualificationDO();
        qual25.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(7).getCourseUuid())
                .setTeacherUuid(teachers.get(0).getTeacherUuid());
        qualifications.add(qual25);

        // ===== 新增课程资格关联（课程16-20）=====
        // 课程16(线性代数) -> 吴副教授(T1009, 索引21)
        CourseQualificationDO qual26 = new CourseQualificationDO();
        qual26.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(15).getCourseUuid())
                .setTeacherUuid(teachers.get(21).getTeacherUuid());
        qualifications.add(qual26);

        // 课程16(线性代数) -> 郑讲师(T1010, 索引22)
        CourseQualificationDO qual27 = new CourseQualificationDO();
        qual27.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(15).getCourseUuid())
                .setTeacherUuid(teachers.get(22).getTeacherUuid());
        qualifications.add(qual27);

        // 课程17(离散数学) -> 孙教授(T1008, 索引20)
        CourseQualificationDO qual28 = new CourseQualificationDO();
        qual28.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(16).getCourseUuid())
                .setTeacherUuid(teachers.get(20).getTeacherUuid());
        qualifications.add(qual28);

        // 课程17(离散数学) -> 张教授(T1001, 索引0)
        CourseQualificationDO qual29 = new CourseQualificationDO();
        qual29.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(16).getCourseUuid())
                .setTeacherUuid(teachers.get(0).getTeacherUuid());
        qualifications.add(qual29);

        // 课程18(概率论与数理统计) -> 吴副教授(T1009, 索引21)
        CourseQualificationDO qual30 = new CourseQualificationDO();
        qual30.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(17).getCourseUuid())
                .setTeacherUuid(teachers.get(21).getTeacherUuid());
        qualifications.add(qual30);

        // 课程18(概率论与数理统计) -> 李教授(T2006, 索引24)
        CourseQualificationDO qual31 = new CourseQualificationDO();
        qual31.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(17).getCourseUuid())
                .setTeacherUuid(teachers.get(24).getTeacherUuid());
        qualifications.add(qual31);

        // 课程19(数字电路) -> 马副教授(T2002, 索引8)
        CourseQualificationDO qual32 = new CourseQualificationDO();
        qual32.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(18).getCourseUuid())
                .setTeacherUuid(teachers.get(8).getTeacherUuid());
        qualifications.add(qual32);

        // 课程19(数字电路) -> 周副教授(T2007, 索引25)
        CourseQualificationDO qual33 = new CourseQualificationDO();
        qual33.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(18).getCourseUuid())
                .setTeacherUuid(teachers.get(25).getTeacherUuid());
        qualifications.add(qual33);

        // 课程20(大学物理) -> 李教授(T2006, 索引24)
        CourseQualificationDO qual34 = new CourseQualificationDO();
        qual34.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(19).getCourseUuid())
                .setTeacherUuid(teachers.get(24).getTeacherUuid());
        qualifications.add(qual34);

        // 课程20(大学物理) -> 唐教授(T4003, 索引29)
        CourseQualificationDO qual35 = new CourseQualificationDO();
        qual35.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(19).getCourseUuid())
                .setTeacherUuid(teachers.get(29).getTeacherUuid());
        qualifications.add(qual35);

        // ===== 为热门课程添加更多授课教师，分担负载 =====
        // 数据结构 -> 孙教授(T1008, 索引20)
        CourseQualificationDO qual36 = new CourseQualificationDO();
        qual36.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(0).getCourseUuid())
                .setTeacherUuid(teachers.get(20).getTeacherUuid());
        qualifications.add(qual36);

        // 操作系统 -> 吴副教授(T1009, 索引21)
        CourseQualificationDO qual37 = new CourseQualificationDO();
        qual37.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(1).getCourseUuid())
                .setTeacherUuid(teachers.get(21).getTeacherUuid());
        qualifications.add(qual37);

        // 数据库系统 -> 吴副教授(T1009, 索引21)
        CourseQualificationDO qual38 = new CourseQualificationDO();
        qual38.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(5).getCourseUuid())
                .setTeacherUuid(teachers.get(21).getTeacherUuid());
        qualifications.add(qual38);

        // 计算机网络 -> 孙教授(T1008, 索引20)
        CourseQualificationDO qual39 = new CourseQualificationDO();
        qual39.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(6).getCourseUuid())
                .setTeacherUuid(teachers.get(20).getTeacherUuid());
        qualifications.add(qual39);

        // 软件工程 -> 孙教授(T1008, 索引20)
        CourseQualificationDO qual40 = new CourseQualificationDO();
        qual40.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(8).getCourseUuid())
                .setTeacherUuid(teachers.get(20).getTeacherUuid());
        qualifications.add(qual40);

        // 编译原理 -> 郑讲师(T1010, 索引22)
        CourseQualificationDO qual41 = new CourseQualificationDO();
        qual41.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(7).getCourseUuid())
                .setTeacherUuid(teachers.get(22).getTeacherUuid());
        qualifications.add(qual41);

        // 通信原理 -> 李教授(T2006, 索引24)
        CourseQualificationDO qual42 = new CourseQualificationDO();
        qual42.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(9).getCourseUuid())
                .setTeacherUuid(teachers.get(24).getTeacherUuid());
        qualifications.add(qual42);

        // 信号与系统 -> 周副教授(T2007, 索引25)
        CourseQualificationDO qual43 = new CourseQualificationDO();
        qual43.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(10).getCourseUuid())
                .setTeacherUuid(teachers.get(25).getTeacherUuid());
        qualifications.add(qual43);

        // 管理学 -> 曹副教授(T4002, 索引16)
        CourseQualificationDO qual44 = new CourseQualificationDO();
        qual44.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(14).getCourseUuid())
                .setTeacherUuid(teachers.get(16).getTeacherUuid());
        qualifications.add(qual44);

        // 管理学 -> 唐教授(T4003, 索引29)
        CourseQualificationDO qual45 = new CourseQualificationDO();
        qual45.setCourseQualificationUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(14).getCourseUuid())
                .setTeacherUuid(teachers.get(29).getTeacherUuid());
        qualifications.add(qual45);

        courseQualificationDAO.saveBatch(qualifications);
        log.info("课程教师资格关联数据初始化完成，共 {} 条记录", qualifications.size());
    }
}
