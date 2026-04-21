package io.github.flashlack1314.smartschedulecorev2.config.database;

import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 教学班初始化器
 * 负责初始化教学班及其关联关系
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TeachingClassInitializer {

    private final TeachingClassDAO teachingClassDAO;
    private final TeachingClassClassDAO teachingClassClassDAO;

    /**
     * 初始化教学班数据
     * 扩充到30个教学班
     */
    public List<TeachingClassDO> initializeTeachingClasses(
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

        // 编译原理-张教授 (tc29-tc31) - 新增3个教学班
        for (int i = 1; i <= 3; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(7).getCourseUuid()) // 编译原理
                    .setTeacherUuid(teachers.get(0).getTeacherUuid()) // 张教授
                    .setSemesterUuid(semester1)
                    .setTeachingClassName("编译原理-张教授-计科210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // 软件工程-张教授 (tc32-tc34) - 新增3个教学班
        for (int i = 1; i <= 3; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(8).getCourseUuid()) // 软件工程
                    .setTeacherUuid(teachers.get(0).getTeacherUuid()) // 张教授
                    .setSemesterUuid(semester1)
                    .setTeachingClassName("软件工程-张教授-软件210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // 数据库系统-张教授 (tc35-tc37) - 新增3个教学班
        for (int i = 1; i <= 3; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(5).getCourseUuid()) // 数据库系统
                    .setTeacherUuid(teachers.get(0).getTeacherUuid()) // 张教授
                    .setSemesterUuid(semester1)
                    .setTeachingClassName("数据库系统-张教授-计科210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // 计算机网络-张教授 (tc38-tc40) - 新增3个教学班
        for (int i = 1; i <= 3; i++) {
            TeachingClassDO tc = new TeachingClassDO();
            tc.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                    .setCourseUuid(courses.get(6).getCourseUuid()) // 计算机网络
                    .setTeacherUuid(teachers.get(0).getTeacherUuid()) // 张教授
                    .setSemesterUuid(semester1)
                    .setTeachingClassName("计算机网络-张教授-网络210" + i + "班")
                    .setTeachingClassHours(0);
            teachingClasses.add(tc);
        }

        // 通信原理-孙教授 (tc41) - 新增以补足40个教学班
        TeachingClassDO tc41 = new TeachingClassDO();
        tc41.setTeachingClassUuid(UuidUtil.generateUuidNoDash())
                .setCourseUuid(courses.get(9).getCourseUuid()) // 通信原理
                .setTeacherUuid(teachers.get(7).getTeacherUuid()) // 孙教授
                .setSemesterUuid(semester1)
                .setTeachingClassName("通信原理-孙教授-通信2101班")
                .setTeachingClassHours(0);
        teachingClasses.add(tc41);

        // 先保存教学班（学时暂时为0，待排课初始化完成后更新）
        teachingClassDAO.saveBatch(teachingClasses);
        log.info("教学班数据初始化完成，共 {} 条记录（待更新学时）", teachingClasses.size());
        return teachingClasses;
    }

    /**
     * 初始化教学班-行政班关联数据
     * 扩充到匹配40个教学班
     *
     * 教学班索引分布（共40个，索引0-39）：
     * 0-2: 数据结构-张教授（3个）
     * 3-4: 操作系统-张教授（2个）
     * 5-7: 操作系统-王副教授（3个）
     * 8-11: Java程序设计-李讲师（4个）
     * 12-14: 电路原理-王副教授（3个）
     * 15-20: 大学体育-赵助教（6个）
     * 21-23: 数据库系统-李讲师（3个）
     * 24-26: 计算机网络-刘教授（3个）
     * 27-29: 编译原理-张教授（3个）
     * 30-32: 软件工程-张教授（3个）
     * 33-35: 数据库系统-张教授（3个）
     * 36-38: 计算机网络-张教授（3个）
     * 39: 通信原理-孙教授（1个）
     */
    public void initializeTeachingClassClasses(
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

        // 索引27-29: 编译原理-张教授 -> 计科2101-2103班
        for (int i = 0; i < 3; i++) {
            TeachingClassClassDO rel = new TeachingClassClassDO();
            rel.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                    .setTeachingClassUuid(teachingClasses.get(27 + i).getTeachingClassUuid())
                    .setClassUuid(classes.get(i).getClassUuid()); // 计科2101-2103
            relations.add(rel);
        }

        // 索引30-32: 软件工程-张教授 -> 软件2101-2103班
        for (int i = 0; i < 3; i++) {
            TeachingClassClassDO rel = new TeachingClassClassDO();
            rel.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                    .setTeachingClassUuid(teachingClasses.get(30 + i).getTeachingClassUuid())
                    .setClassUuid(classes.get(5 + i).getClassUuid()); // 软件2101-2103
            relations.add(rel);
        }

        // 索引33-35: 数据库系统-张教授 -> 计科2101-2103班
        for (int i = 0; i < 3; i++) {
            TeachingClassClassDO rel = new TeachingClassClassDO();
            rel.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                    .setTeachingClassUuid(teachingClasses.get(33 + i).getTeachingClassUuid())
                    .setClassUuid(classes.get(i).getClassUuid()); // 计科2101-2103
            relations.add(rel);
        }

        // 索引36-38: 计算机网络-张教授 -> 网络2101-2103班
        for (int i = 0; i < 3; i++) {
            TeachingClassClassDO rel = new TeachingClassClassDO();
            rel.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                    .setTeachingClassUuid(teachingClasses.get(36 + i).getTeachingClassUuid())
                    .setClassUuid(classes.get(13 + i).getClassUuid()); // 网络2101-2103
            relations.add(rel);
        }

        // 索引39: 通信原理-孙教授 -> 通信2101班
        TeachingClassClassDO rel39 = new TeachingClassClassDO();
        rel39.setTeachingClassClassUuid(UuidUtil.generateUuidNoDash())
                .setTeachingClassUuid(teachingClasses.get(39).getTeachingClassUuid())
                .setClassUuid(classes.get(15).getClassUuid()); // 通信2101
        relations.add(rel39);

        teachingClassClassDAO.saveBatch(relations);
        log.info("教学班-行政班关联数据初始化完成，共 {} 条记录", relations.size());
    }
}
