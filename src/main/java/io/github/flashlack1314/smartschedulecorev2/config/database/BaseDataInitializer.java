package io.github.flashlack1314.smartschedulecorev2.config.database;

import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 基础数据初始化器
 * 负责初始化基础组织架构数据
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaseDataInitializer {

    private final DepartmentDAO departmentDAO;
    private final BuildingDAO buildingDAO;
    private final MajorDAO majorDAO;
    private final SemesterDAO semesterDAO;

    /**
     * 初始化学院数据
     */
    public List<DepartmentDO> initializeDepartments() {
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
    public List<BuildingDO> initializeBuildings() {
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
     * 初始化学期数据
     */
    public List<SemesterDO> initializeSemesters() {
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
    public List<MajorDO> initializeMajors(List<DepartmentDO> departments) {
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
}
