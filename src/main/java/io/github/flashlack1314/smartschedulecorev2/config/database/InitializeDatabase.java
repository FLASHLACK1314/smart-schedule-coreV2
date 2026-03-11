package io.github.flashlack1314.smartschedulecorev2.config.database;

import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据库数据初始化协调器
 * 协调各初始化器完成数据库初始化
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitializeDatabase {

    private final BaseDataInitializer baseDataInitializer;
    private final CourseResourceInitializer courseResourceInitializer;
    private final PersonnelInitializer personnelInitializer;
    private final TeachingClassInitializer teachingClassInitializer;
    private final ScheduleInitializer scheduleInitializer;
    private final ScheduleConflictInitializer scheduleConflictInitializer;

    /**
     * 初始化数据库数据
     * 在表结构创建完成后自动调用
     *
     * @param mode 数据初始化模式
     */
    public void initializeDatabase(DatabaseInitProperties.InitMode mode) {
        log.info("开始初始化数据库基础数据，模式: {}", mode);

        // 第一层：基础数据
        List<DepartmentDO> departments = baseDataInitializer.initializeDepartments();
        List<BuildingDO> buildings = baseDataInitializer.initializeBuildings();
        List<SemesterDO> semesters = baseDataInitializer.initializeSemesters();

        // 第二层：依赖基础数据的表
        List<MajorDO> majors = baseDataInitializer.initializeMajors(departments);
        List<AcademicAdminDO> academicAdmins = personnelInitializer.initializeAcademicAdmins(departments);

        // 第三层：核心业务数据
        List<CourseTypeDO> courseTypes = courseResourceInitializer.initializeCourseTypes();
        List<ClassroomTypeDO> classroomTypes = courseResourceInitializer.initializeClassroomTypes();
        List<CourseClassroomTypeDO> courseClassroomTypes = courseResourceInitializer.initializeCourseClassroomTypes(courseTypes, classroomTypes);
        List<CourseDO> courses = courseResourceInitializer.initializeCourses(courseTypes);
        List<ClassroomDO> classrooms = courseResourceInitializer.initializeClassrooms(buildings, classroomTypes);
        List<ClassDO> classes = courseResourceInitializer.initializeClasses(majors);
        List<TeacherDO> teachers = personnelInitializer.initializeTeachers(departments);

        // 第四层：关联数据
        personnelInitializer.initializeCourseQualifications(courses, teachers);

        // 根据模式决定是否继续
        if (mode == DatabaseInitProperties.InitMode.MINIMAL) {
            personnelInitializer.createSystemAdmin();
            log.info("最小化数据初始化完成");
            return;
        }

        // 第五层：学生数据
        List<StudentDO> students = personnelInitializer.initializeStudents(classes);

        // 演示模式到此为止
        if (mode == DatabaseInitProperties.InitMode.DEMO) {
            personnelInitializer.createSystemAdmin();
            log.info("演示数据初始化完成（不含教学班和排课）");
            return;
        }

        // 第六层：教学班和排课记录
        List<TeachingClassDO> teachingClasses = teachingClassInitializer.initializeTeachingClasses(courses, teachers, semesters);
        teachingClassInitializer.initializeTeachingClassClasses(teachingClasses, classes);
        scheduleInitializer.initializeSchedules(teachingClasses, semesters, classrooms, courses, teachers);

        // 第七层：排课冲突记录（仅FULL模式）
        scheduleConflictInitializer.initializeScheduleConflicts(null, semesters);

        // 系统管理员
        personnelInitializer.createSystemAdmin();

        log.info("满数据初始化完成");
    }
}
