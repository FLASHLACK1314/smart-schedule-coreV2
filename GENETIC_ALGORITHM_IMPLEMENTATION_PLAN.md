# 智能排课系统 - 遗传算法自动排课实现计划

## 一、项目背景

智能排课系统需要实现基于遗传算法的自动排课功能。当前系统已完成基础数据模型和 CRUD 功能，但缺少自动排课算法实现。

### 已有基础
- **数据库表完整**：sc_schedule、sc_teaching_class、sc_teacher、sc_classroom、sc_course_qualification 等
- **基础服务完善**：ScheduleService、TeachingClassService、TeacherService 等
- **约束数据齐全**：教师时间偏好、教室容量、教师资格、教室类型等

### 待实现目标
根据遗传算法设计思路实现自动排课功能：
1. 改进型染色体编码：`Map<TimeSlot, List<CourseAppointment>>`
2. 多层级权重适应度函数（硬约束惩罚 + 软约束奖励）
3. 精英保留策略
4. 三态变异（时间槽、教室、教师变异）

---

## 二、算法核心数据结构设计

### 2.1 时间槽（TimeSlot）
```java
package io.github.flashlack1314.smartschedulecorev2.algorithm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 时间槽：全局唯一的时间索引
 * 包含星期几、节次范围、周次
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {
    /** 星期几 (1-7) */
    private Integer dayOfWeek;

    /** 起始节次 */
    private Integer sectionStart;

    /** 结束节次 */
    private Integer sectionEnd;

    /** 上课周次列表 (如 [1,2,3,4,5]) */
    private List<Integer> weeks;

    /** 时间槽唯一标识 (格式: "1-2-3-1,2,3" 表示 周一第2-3节第1,2,3周) */
    public String getUniqueId() {
        return dayOfWeek + "-" + sectionStart + "-" + sectionEnd + "-" + weeks.toString();
    }

    /** 检查与另一个时间槽是否存在时间重叠 */
    public boolean isOverlap(TimeSlot other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        // 检查节次是否重叠
        boolean sectionOverlap = !(this.sectionEnd < other.sectionStart || this.sectionStart > other.sectionEnd);
        if (!sectionOverlap) {
            return false;
        }
        // 检查周次是否重叠
        return this.weeks.stream().anyMatch(other.weeks::contains);
    }
}
```

### 2.2 课程安排（CourseAppointment）
```java
package io.github.flashlack1314.smartschedulecorev2.algorithm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 课程安排实体：单个教学班的排课信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseAppointment {
    /** 教学班UUID */
    private String teachingClassUuid;

    /** 教学班名称 */
    private String teachingClassName;

    /** 课程UUID */
    private String courseUuid;

    /** 课程名称 */
    private String courseName;

    /** 教师UUID */
    private String teacherUuid;

    /** 教师名称 */
    private String teacherName;

    /** 教室UUID */
    private String classroomUuid;

    /** 教室名称 */
    private String classroomName;

    /** 教室容量 */
    private Integer classroomCapacity;

    /** 课程类型UUID (用于教室类型匹配) */
    private String courseTypeUuid;

    /** 关联的行政班UUID列表 */
    private List<String> classUuids;

    /** 学生总人数 */
    private Integer totalStudents;
}
```

### 2.3 染色体（Chromosome）
```java
package io.github.flashlack1314.smartschedulecorev2.algorithm.entity;

import lombok.Data;
import java.util.HashMap;
import java.util.Map;

/**
 * 染色体：表示一个完整的排课方案
 * 使用 Map<TimeSlot, List<CourseAppointment>> 编码
 */
@Data
public class Chromosome implements Comparable<Chromosome> {
    /** 排课方案：时间槽 -> 该时间段的所有课程安排 */
    private Map<TimeSlot, List<CourseAppointment>> genes;

    /** 适应度分数 (越大越好) */
    private Double fitness;

    /** 硬约束违反次数 */
    private int hardConstraintViolations;

    /** 软约束违反次数 */
    private int softConstraintViolations;

    public Chromosome() {
        this.genes = new HashMap<>();
        this.fitness = 0.0;
        this.hardConstraintViolations = 0;
        this.softConstraintViolations = 0;
    }

    /** 深度复制染色体 */
    public Chromosome copy() {
        Chromosome copy = new Chromosome();
        Map<TimeSlot, List<CourseAppointment>> copiedGenes = new HashMap<>();
        for (Map.Entry<TimeSlot, List<CourseAppointment>> entry : genes.entrySet()) {
            List<CourseAppointment> appointments = new ArrayList<>(entry.getValue());
            copiedGenes.put(entry.getKey(), appointments);
        }
        copy.setGenes(copiedGenes);
        copy.setFitness(this.fitness);
        copy.setHardConstraintViolations(this.hardConstraintViolations);
        copy.setSoftConstraintViolations(this.softConstraintViolations);
        return copy;
    }

    @Override
    public int compareTo(Chromosome other) {
        return Double.compare(other.fitness, this.fitness); // 降序排列
    }
}
```

---

## 三、排课上下文（输入数据封装）

### 3.1 ScheduleContext
```java
package io.github.flashlack1314.smartschedulecorev2.algorithm.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 自动排课上下文：封装算法运行所需的所有输入数据
 */
@Data
public class ScheduleContext {
    /** 学期UUID */
    private String semesterUuid;

    /** 学期周数 */
    private Integer semesterWeeks;

    /** 待排课的教学班列表 */
    private List<TeachingClassInfo> teachingClassList;

    /** 可用教室列表 (按教室类型分组) */
    private Map<String, List<ClassroomInfo>> availableClassrooms;

    /** 教师时间偏好 (teacherUuid -> List<TimeSlot>) */
    private Map<String, List<TimeSlot>> teacherTimePreferences;

    /** 教师每周最大课时 (teacherUuid -> maxHours) */
    private Map<String, Integer> teacherMaxHours;

    /** 已锁定的排课记录 (算法跳过这些) */
    private List<LockedSchedule> lockedSchedules;

    /** 每周节次数 (如每天12节) */
    private Integer sectionsPerDay;

    /** 每周上课天数 (通常5或7) */
    private Integer daysPerWeek;

    @Data
    public static class TeachingClassInfo {
        private String teachingClassUuid;
        private String teachingClassName;
        private String courseUuid;
        private String courseName;
        private String courseTypeUuid;
        private String teacherUuid;
        private String teacherName;
        private List<String> classUuids; // 关联的行政班
        private Integer totalStudents; // 总学生数
        private Integer requiredHours; // 需要排课的课时数
    }

    @Data
    public static class ClassroomInfo {
        private String classroomUuid;
        private String classroomName;
        private Integer capacity;
        private String classroomTypeUuid;
    }

    @Data
    public static class LockedSchedule {
        private String teachingClassUuid;
        private TimeSlot timeSlot;
        private String classroomUuid;
    }
}
```

---

## 四、核心组件实现

### 4.1 冲突检测器（ConflictDetector）

**文件路径**: `src/main/java/io/github/flashlack1314/smartschedulecorev2/algorithm/core/ConflictDetector.java`

**功能**:
```java
/**
 * 冲突检测器：检测排课方案中的硬约束冲突
 */
public class ConflictDetector {
    /**
     * 检测单个染色体的所有冲突
     * @return ConflictReport (包含冲突列表和严重程度)
     */
    public ConflictReport detectConflicts(Chromosome chromosome, ScheduleContext context) {
        List<Conflict> conflicts = new ArrayList<>();

        // 1. 教师时间冲突检测
        conflicts.addAll(detectTeacherConflicts(chromosome));

        // 2. 教室时间冲突检测
        conflicts.addAll(detectClassroomConflicts(chromosome));

        // 3. 班级时间冲突检测
        conflicts.addAll(detectClassConflicts(chromosome, context));

        // 4. 教室容量约束检测
        conflicts.addAll(detectCapacityConflicts(chromosome, context));

        // 5. 教室类型匹配检测
        conflicts.addAll(detectClassroomTypeConflicts(chromosome, context));

        // 6. 教师资格约束检测
        conflicts.addAll(detectQualificationConflicts(chromosome, context));

        return new ConflictReport(conflicts);
    }

    private List<Conflict> detectTeacherConflicts(Chromosome chromosome) {
        // 实现：同一教师同一时间只能上一门课
    }

    private List<Conflict> detectClassroomConflicts(Chromosome chromosome) {
        // 实现：同一教室同一时间只能有一门课
    }

    private List<Conflict> detectClassConflicts(Chromosome chromosome, ScheduleContext context) {
        // 实现：同一行政班级学生同一时间只能上一门课
        // 需要查询 teaching_class_class 关联表
    }

    // ... 其他检测方法
}
```

### 4.2 适应度计算器（FitnessCalculator）

**文件路径**: `src/main/java/io/github/flashlack1314/smartschedulecorev2/algorithm/core/FitnessCalculator.java`

**功能**:
```java
/**
 * 适应度计算器：计算排课方案的适应度
 * 公式：F = Σ(W_h,i * C_h,i) + Σ(W_s,j * C_s,j)
 */
public class FitnessCalculator {
    // 硬约束权重配置
    private static final int TEACHER_CONFLICT_PENALTY = 1_000_000;
    private static final int CLASSROOM_CONFLICT_PENALTY = 1_000_000;
    private static final int CLASS_CONFLICT_PENALTY = 1_000_000;
    private static final int CAPACITY_PENALTY = 500_000;
    private static final int TYPE_MISMATCH_PENALTY = 500_000;
    private static final int QUALIFICATION_PENALTY = 1_000_000;

    // 软约束权重配置
    private static final int TEACHER_PREFERENCE_REWARD = 1000;
    private static final int WORKLOAD_BALANCE_REWARD = 500;
    private static final int COURSE_DISTRIBUTION_REWARD = 300;

    /**
     * 计算染色体适应度
     */
    public void calculateFitness(Chromosome chromosome, ScheduleContext context) {
        ConflictReport report = conflictDetector.detectConflicts(chromosome, context);

        // 硬约束惩罚（负分）
        double hardPenalty = 0;
        for (Conflict conflict : report.getHardConflicts()) {
            hardPenalty += getPenalty(conflict.getType());
        }

        // 软约束奖励（正分）
        double softReward = 0;
        softReward += calculateTeacherPreferenceScore(chromosome, context);
        softReward += calculateWorkloadBalanceScore(chromosome, context);
        softReward += calculateCourseDistributionScore(chromosome, context);

        // 总适应度 = 奖励 - 惩罚
        double fitness = softReward - hardPenalty;

        chromosome.setFitness(fitness);
        chromosome.setHardConstraintViolations(report.getHardConflicts().size());
        chromosome.setSoftConstraintViolations(report.getSoftConflicts().size());
    }

    private double calculateTeacherPreferenceScore(Chromosome chromosome, ScheduleContext context) {
        // 实现：检查课程时间是否在教师偏好时间内
    }

    private double calculateWorkloadBalanceScore(Chromosome chromosome, ScheduleContext context) {
        // 实现：检查教师工作量是否均衡（不超过maxHoursPerWeek）
    }

    private double calculateCourseDistributionScore(Chromosome chromosome, ScheduleContext context) {
        // 实现：检查课程分布是否均匀（避免过度集中）
    }
}
```

### 4.3 遗传算法主类（GeneticAlgorithm）

**文件路径**: `src/main/java/io/github/flashlack1314/smartschedulecorev2/algorithm/core/GeneticAlgorithm.java`

**功能**:
```java
/**
 * 遗传算法主类
 */
public class GeneticAlgorithm {
    private final ScheduleContext context;
    private final FitnessCalculator fitnessCalculator;
    private final ConflictDetector conflictDetector;

    // 算法参数
    private int populationSize = 100;      // 种群大小
    private int maxGenerations = 500;       // 最大迭代次数
    private double crossoverRate = 0.8;    // 交叉概率
    private double mutationRate = 0.2;      // 变异概率
    private int eliteSize = 10;             // 精英保留数量

    /**
     * 执行自动排课
     * @return 最佳排课方案
     */
    public Chromosome schedule() {
        // 阶段1：初始化种群
        List<Chromosome> population = initializePopulation();

        Chromosome bestSolution = null;

        // 阶段2：进化迭代
        for (int generation = 0; generation < maxGenerations; generation++) {
            // 评估适应度
            evaluatePopulation(population);

            // 排序并记录最优解
            population.sort(Collections.reverseOrder());
            bestSolution = population.get(0).copy();

            // 检查是否达到终止条件
            if (isTerminationConditionMet(bestSolution)) {
                break;
            }

            // 精英保留策略
            List<Chromosome> elites = population.stream()
                    .limit(eliteSize)
                    .map(Chromosome::copy)
                    .collect(Collectors.toList());

            // 选择
            List<Chromosome> selected = selection(population);

            // 交叉
            List<Chromosome> offspring = crossover(selected);

            // 变异（三态变异）
            mutate(offspring);

            // 构建新一代：精英 + 后代
            population = new ArrayList<>();
            population.addAll(elites);
            population.addAll(offspring);

            log.info("第 {} 代，最优适应度: {}, 硬冲突数: {}",
                    generation, bestSolution.getFitness(), bestSolution.getHardConstraintViolations());
        }

        return bestSolution;
    }

    /**
     * 初始化种群：生成合法的初始解
     */
    private List<Chromosome> initializePopulation() {
        List<Chromosome> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            Chromosome chromosome = new Chromosome();

            // 为每个教学班随机分配时间和教室
            for (TeachingClassInfo tc : context.getTeachingClassList()) {
                // 随机选择时间槽
                TimeSlot timeSlot = generateRandomTimeSlot();

                // 随机选择合适教室（容量满足、类型匹配）
                String classroomUuid = selectSuitableClassroom(tc, timeSlot);

                // 创建课程安排
                CourseAppointment appointment = buildAppointment(tc, classroomUuid);

                // 添加到染色体
                chromosome.getGenes()
                    .computeIfAbsent(timeSlot, k -> new ArrayList<>())
                    .add(appointment);
            }

            population.add(chromosome);
        }

        return population;
    }

    /**
     * 交叉算子：时间点交叉（TimePoint Crossover）
     */
    private List<Chromosome> crossover(List<Chromosome> parents) {
        List<Chromosome> offspring = new ArrayList<>();

        for (int i = 0; i < parents.size(); i += 2) {
            if (i + 1 >= parents.size()) break;

            Chromosome parent1 = parents.get(i);
            Chromosome parent2 = parents.get(i + 1);

            if (Math.random() < crossoverRate) {
                // 单点交叉：随机选择一个时间点，交换该时间点之后的所有基因
                Chromosome child1 = parent1.copy();
                Chromosome child2 = parent2.copy();

                List<TimeSlot> allTimeSlots = new ArrayList<>(parent1.getGenes().keySet());
                int crossoverPoint = new Random().nextInt(allTimeSlots.size());

                // 交换时间点后的基因
                for (int j = crossoverPoint; j < allTimeSlots.size(); j++) {
                    TimeSlot slot = allTimeSlots.get(j);
                    // 交换两个父代在该时间槽的课程安排
                    List<CourseAppointment> temp = child1.getGenes().get(slot);
                    child1.getGenes().put(slot, child2.getGenes().get(slot));
                    child2.getGenes().put(slot, temp);
                }

                offspring.add(child1);
                offspring.add(child2);
            } else {
                offspring.add(parent1.copy());
                offspring.add(parent2.copy());
            }
        }

        return offspring;
    }

    /**
     * 变异算子：三态变异（时间槽、教室、教师）
     */
    private void mutate(List<Chromosome> population) {
        Random random = new Random();

        for (Chromosome chromosome : population) {
            if (Math.random() > mutationRate) continue;

            // 随机选择一个课程安排进行变异
            List<TimeSlot> timeSlots = new ArrayList<>(chromosome.getGenes().keySet());
            TimeSlot targetSlot = timeSlots.get(random.nextInt(timeSlots.size()));
            List<CourseAppointment> appointments = chromosome.getGenes().get(targetSlot);
            if (appointments.isEmpty()) continue;

            CourseAppointment target = appointments.get(random.nextInt(appointments.size()));

            // 三态变异之一：时间槽变异
            if (random.nextDouble() < 0.4) {
                TimeSlot newSlot = generateRandomTimeSlot();
                // 从原时间槽移除
                appointments.remove(target);
                // 添加到新时间槽
                chromosome.getGenes()
                    .computeIfAbsent(newSlot, k -> new ArrayList<>())
                    .add(target);
            }
            // 三态变异之二：教室变异
            else if (random.nextDouble() < 0.7) {
                TeachingClassInfo tc = findTeachingClassInfo(target.getTeachingClassUuid());
                String newClassroom = selectSuitableClassroom(tc, targetSlot);
                target.setClassroomUuid(newClassroom);
            }
            // 三态变异之三：教师变异（如果有多个资格教师）
            else {
                String newTeacher = selectAlternativeTeacher(target.getCourseUuid(), target.getTeacherUuid());
                if (newTeacher != null) {
                    target.setTeacherUuid(newTeacher);
                }
            }
        }
    }

    /**
     * 选择算子：轮盘赌选择
     */
    private List<Chromosome> selection(List<Chromosome> population) {
        // 实现轮盘赌选择
    }
}
```

---

## 五、服务层实现

### 5.1 AutoScheduleService 接口

**文件路径**: `src/main/java/io/github/flashlack1314/smartschedulecorev2/service/AutoScheduleService.java`

```java
package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.ScheduleResult;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AutoScheduleVO;

/**
 * 自动排课服务接口
 */
public interface AutoScheduleService {
    /**
     * 执行自动排课
     * @param request 排课请求参数
     * @return 排课结果
     */
    ScheduleResult autoSchedule(AutoScheduleVO request);

    /**
     * 保存排课方案到数据库
     * @param result 排课结果
     */
    void saveScheduleResult(ScheduleResult result);
}
```

### 5.2 AutoScheduleVO

**文件路径**: `src/main/java/io/github/flashlack1314/smartschedulecorev2/model/vo/AutoScheduleVO.java`

```java
package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import java.util.List;

/**
 * 自动排课请求参数
 */
@Data
public class AutoScheduleVO {
    /** 学期UUID（必填） */
    private String semesterUuid;

    /** 待排课的教学班UUID列表（必填） */
    private List<String> teachingClassUuids;

    /** 可用教室UUID列表（可选，不传则查询所有） */
    private List<String> classroomUuids;

    /** 算法参数配置（可选，使用默认值） */
    private Integer populationSize;      // 种群大小，默认100
    private Integer maxGenerations;      // 最大迭代次数，默认500
    private Double crossoverRate;        // 交叉概率，默认0.8
    private Double mutationRate;         // 变异概率，默认0.2
    private Integer eliteSize;           // 精英数量，默认10
}
```

### 5.3 AutoScheduleServiceImpl

**文件路径**: `src/main/java/io/github/flashlack1314/smartschedulecorev2/service/impl/AutoScheduleServiceImpl.java`

```java
package io.github.flashlack1314.smartschedulecorev2.service.impl;

import io.github.flashlack1314.smartschedulecorev2.service.AutoScheduleService;
import io.github.flashlack1314.smartschedulecorev2.algorithm.core.GeneticAlgorithm;
import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoScheduleServiceImpl implements AutoScheduleService {
    private final SemesterDAO semesterDAO;
    private final TeachingClassDAO teachingClassDAO;
    private final TeachingClassClassDAO teachingClassClassDAO;
    private final TeacherDAO teacherDAO;
    private final ClassroomDAO classroomDAO;
    private final CourseDAO courseDAO;
    private final CourseQualificationDAO courseQualificationDAO;
    private final ScheduleDAO scheduleDAO;
    private final ScheduleConflictDAO scheduleConflictDAO;

    @Override
    public ScheduleResult autoSchedule(AutoScheduleVO request) {
        log.info("开始自动排课 - 学期: {}, 教学班数: {}",
                request.getSemesterUuid(), request.getTeachingClassUuids().size());

        // 1. 构建排课上下文
        ScheduleContext context = buildScheduleContext(request);

        // 2. 初始化遗传算法
        GeneticAlgorithm ga = new GeneticAlgorithm(
            context,
            configureAlgorithm(request)
        );

        // 3. 执行算法
        Chromosome bestSolution = ga.schedule();

        // 4. 构建结果
        ScheduleResult result = buildResult(bestSolution, context);

        log.info("自动排课完成 - 适应度: {}, 硬冲突: {}",
                result.getFitness(), result.getHardConflicts().size());

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveScheduleResult(ScheduleResult result) {
        log.info("保存排课方案到数据库 - 方案UUID: {}", result.getSolutionUuid());

        // 1. 删除该学期预览状态的旧排课
        scheduleDAO.remove(
            scheduleDAO.lambdaQuery()
                .eq(ScheduleDO::getSemesterUuid, result.getSemesterUuid())
                .eq(ScheduleDO::getStatus, 0) // 只删除预览状态的
                .getWrapper()
        );

        // 2. 批量插入新的排课记录
        List<ScheduleDO> schedules = convertToScheduleDOList(result);
        schedules.forEach(scheduleDAO::save);

        // 3. 保存冲突记录
        result.getHardConflicts().forEach(conflict -> saveConflict(conflict, 1));
        result.getSoftConflicts().forEach(conflict -> saveConflict(conflict, 0));

        log.info("排课方案保存完成 - 新增 {} 条排课记录", schedules.size());
    }

    /**
     * 构建排课上下文
     */
    private ScheduleContext buildScheduleContext(AutoScheduleVO request) {
        ScheduleContext context = new ScheduleContext();

        // 查询学期信息
        SemesterDO semester = semesterDAO.getById(request.getSemesterUuid());
        context.setSemesterUuid(semester.getSemesterUuid());
        context.setSemesterWeeks(semester.getSemesterWeeks());
        context.setSectionsPerDay(12); // 可配置
        context.setDaysPerWeek(5);    // 可配置

        // 查询教学班信息
        List<TeachingClassDO> teachingClasses = teachingClassDAO.listByIds(request.getTeachingClassUuids());
        List<ScheduleContext.TeachingClassInfo> tcInfoList = new ArrayList<>();

        for (TeachingClassDO tc : teachingClasses) {
            ScheduleContext.TeachingClassInfo info = new ScheduleContext.TeachingClassInfo();
            info.setTeachingClassUuid(tc.getTeachingClassUuid());
            info.setTeachingClassName(tc.getTeachingClassName());
            info.setCourseUuid(tc.getCourseUuid());
            info.setTeacherUuid(tc.getTeacherUuid());

            // 查询课程名称
            CourseDO course = courseDAO.getById(tc.getCourseUuid());
            info.setCourseName(course.getCourseName());
            info.setCourseTypeUuid(course.getCourseTypeUuid());

            // 查询教师名称
            TeacherDO teacher = teacherDAO.getById(tc.getTeacherUuid());
            info.setTeacherName(teacher.getTeacherName());

            // 查询关联的行政班和学生总数
            List<TeachingClassClassDO> relations = teachingClassClassDAO.list(
                teachingClassClassDAO.lambdaQuery()
                    .eq(TeachingClassClassDO::getTeachingClassUuid, tc.getTeachingClassUuid())
                    .getWrapper()
            );

            List<String> classUuids = relations.stream()
                .map(TeachingClassClassDO::getClassUuid)
                .collect(Collectors.toList());
            info.setClassUuids(classUuids);

            // 计算学生总数
            int totalStudents = calculateTotalStudents(classUuids);
            info.setTotalStudents(totalStudents);

            // 设置所需课时（可从课程配置或学期周数推算）
            info.setRequiredHours(calculateRequiredHours(semester.getSemesterWeeks()));

            tcInfoList.add(info);
        }
        context.setTeachingClassList(tcInfoList);

        // 查询可用教室
        List<String> classroomUuids = request.getClassroomUuids();
        if (classroomUuids == null || classroomUuids.isEmpty()) {
            // 查询所有教室
            classroomUuids = classroomDAO.list()
                .stream()
                .map(ClassroomDO::getClassroomUuid)
                .collect(Collectors.toList());
        }

        List<ClassroomDO> classrooms = classroomDAO.listByIds(classroomUuids);
        Map<String, List<ScheduleContext.ClassroomInfo>> classroomMap = classrooms.stream()
            .collect(Collectors.groupingBy(
                ClassroomDO::getClassroomTypeUuid,
                Collectors.mapping(this::convertToClassroomInfo, Collectors.toList())
            ));
        context.setAvailableClassrooms(classroomMap);

        // 查询教师时间偏好和工作量限制
        Map<String, List<TimeSlot>> preferences = new HashMap<>();
        Map<String, Integer> maxHours = new HashMap<>();

        for (ScheduleContext.TeachingClassInfo tc : tcInfoList) {
            TeacherDO teacher = teacherDAO.getById(tc.getTeacherUuid());

            // 解析时间偏好
            List<TimeSlot> teacherPrefs = parseLikeTime(teacher.getLikeTime());
            preferences.put(tc.getTeacherUuid(), teacherPrefs);

            // 工作量限制
            maxHours.put(tc.getTeacherUuid(), teacher.getMaxHoursPerWeek());
        }
        context.setTeacherTimePreferences(preferences);
        context.setTeacherMaxHours(maxHours);

        // 查询已锁定的排课记录
        List<ScheduleDO> lockedSchedules = scheduleDAO.list(
            scheduleDAO.lambdaQuery()
                .eq(ScheduleDO::getSemesterUuid, request.getSemesterUuid())
                .eq(ScheduleDO::getIsLocked, true)
                .getWrapper()
        );

        List<ScheduleContext.LockedSchedule> lockedList = lockedSchedules.stream()
            .map(this::convertToLockedSchedule)
            .collect(Collectors.toList());
        context.setLockedSchedules(lockedList);

        return context;
    }

    // 辅助方法...
}
```

---

## 六、实现阶段划分

### Stage 1: 基础数据结构搭建
**Goal**: 建立遗传算法所需的核心数据模型
**Success Criteria**:
- TimeSlot、CourseAppointment、Chromosome 类编译通过
- ConflictReport、ScheduleResult DTO 类创建完成
- 基本的单元测试通过

**Tasks**:
1. 创建 `algorithm/entity` 包，实现 TimeSlot、CourseAppointment、Chromosome 类
2. 创建 `algorithm/dto` 包，实现 ScheduleContext、ScheduleResult 类
3. 创建 `algorithm/dto/ConflictReport.java` 和 `Conflict.java`
4. 编写 TimeSlot 的重叠检测单元测试
5. 编写 Chromosome 的复制和比较单元测试

**Status**: Not Started

---

### Stage 2: 冲突检测器实现
**Goal**: 实现所有硬约束冲突检测逻辑
**Success Criteria**:
- ConflictDetector 类实现完成
- 6种硬约束检测逻辑全部实现
- 冲突检测单元测试通过
- 能正确识别教师、教室、班级时间冲突

**Tasks**:
1. 创建 `algorithm/core/ConflictDetector.java`
2. 实现教师时间冲突检测 `detectTeacherConflicts()`
3. 实现教室时间冲突检测 `detectClassroomConflicts()`
4. 实现班级时间冲突检测 `detectClassConflicts()`（需查询关联表）
5. 实现教室容量约束检测 `detectCapacityConflicts()`
6. 实现教室类型匹配检测 `detectClassroomTypeConflicts()`
7. 实现教师资格约束检测 `detectQualificationConflicts()`
8. 编写冲突检测的集成测试

**Status**: Not Started

---

### Stage 3: 适应度计算器实现
**Goal**: 实现多层级权重适应度评价函数
**Success Criteria**:
- FitnessCalculator 类实现完成
- 硬约束惩罚计算正确
- 软约束奖励计算正确
- 适应度函数单元测试通过

**Tasks**:
1. 创建 `algorithm/core/FitnessCalculator.java`
2. 定义硬约束和软约束的权重常量
3. 实现教师偏好匹配评分 `calculateTeacherPreferenceScore()`
4. 实现工作量均衡评分 `calculateWorkloadBalanceScore()`
5. 实现课程分布均匀评分 `calculateCourseDistributionScore()`
6. 实现 `calculateFitness()` 总方法
7. 编写适应度计算单元测试（验证不同场景的分数）

**Status**: Not Started

---

### Stage 4: 遗传算法核心逻辑
**Goal**: 实现遗传算法的主循环和遗传算子
**Success Criteria**:
- GeneticAlgorithm 类实现完成
- 种群初始化方法可行
- 选择、交叉、变异算子实现
- 算法能运行并产生结果

**Tasks**:
1. 创建 `algorithm/core/GeneticAlgorithm.java`
2. 实现 `initializePopulation()` 初始化种群
3. 实现轮盘赌选择 `selection()`
4. 实现时间点交叉 `crossover()`
5. 实现三态变异 `mutate()`（时间槽、教室、教师）
6. 实现 `schedule()` 主方法
7. 添加进化过程日志记录
8. 编写算法运行测试（验证收敛性）

**Status**: Not Started

---

### Stage 5: 服务层集成
**Goal**: 将算法集成到现有服务体系
**Success Criteria**:
- AutoScheduleService 接口和实现完成
- 能从数据库加载排课上下文
- 算法结果能保存到数据库
- API 接口可调用

**Tasks**:
1. 创建 `service/AutoScheduleService.java` 接口
2. 创建 `model/vo/AutoScheduleVO.java` 请求参数
3. 创建 `service/impl/AutoScheduleServiceImpl.java`
4. 实现 `buildScheduleContext()` 方法（数据加载）
5. 实现 `autoSchedule()` 方法（调用算法）
6. 实现 `saveScheduleResult()` 方法（保存结果）
7. 实现 `parseLikeTime()` 方法（解析教师时间偏好）
8. 创建 Controller 层接口
9. 编写服务层集成测试

**Status**: Not Started

---

### Stage 6: 冲突记录与结果展示
**Goal**: 完善冲突记录机制和结果展示
**Success Criteria**:
- 冲突能正确保存到 `sc_schedule_conflict` 表
- ScheduleResult 包含完整的冲突列表和统计信息
- 前端能展示排课结果和冲突报告

**Tasks**:
1. 实现 `saveConflict()` 方法（保存冲突记录）
2. 完善 `ScheduleResult` 类的构建逻辑
3. 添加冲突统计功能（按类型分组统计）
4. 创建排课结果查询接口
5. 更新 API 文档

**Status**: Not Started

---

### Stage 7: 优化与测试
**Goal**: 优化算法性能并完善测试覆盖
**Success Criteria**:
- 算法性能满足要求（100个教学班5分钟内完成）
- 单元测试覆盖率 > 80%
- 集成测试通过
- 性能测试报告完成

**Tasks**:
1. 性能分析：识别瓶颈并优化
2. 优化数据加载逻辑（批量查询）
3. 优化冲突检测算法（使用索引加速）
4. 编写完整的单元测试套件
5. 编写集成测试（真实数据库环境）
6. 进行性能基准测试
7. 代码审查和重构
8. 更新文档和注释

**Status**: Not Started

---

## 七、关键文件路径

### 新增文件列表
```
algorithm/
├── entity/
│   ├── TimeSlot.java                          # 时间槽
│   ├── CourseAppointment.java                  # 课程安排
│   └── Chromosome.java                       # 染色体
├── dto/
│   ├── ScheduleContext.java                   # 排课上下文
│   ├── ScheduleResult.java                    # 排课结果
│   └── ConflictReport.java                   # 冲突报告
├── core/
│   ├── ConflictDetector.java                  # 冲突检测器
│   ├── FitnessCalculator.java                 # 适应度计算器
│   └── GeneticAlgorithm.java                 # 遗传算法主类
└── util/
    ├── TimeSlotGenerator.java                 # 时间槽生成器
    └── ScheduleConverter.java                 # 排课方案转换器

service/
├── AutoScheduleService.java                   # 自动排课服务接口
└── impl/
    └── AutoScheduleServiceImpl.java           # 自动排课服务实现

model/vo/
└── AutoScheduleVO.java                      # 自动排课请求参数

controller/
└── AutoScheduleController.java               # 自动排课API接口
```

### 依赖的现有文件
```
dao/
├── ScheduleDAO.java                         # 排课数据访问
├── TeachingClassDAO.java                    # 教学班数据访问
├── TeachingClassClassDAO.java               # 教学班-行政班关联
├── TeacherDAO.java                         # 教师数据访问
├── ClassroomDAO.java                       # 教室数据访问
├── CourseDAO.java                          # 课程数据访问
├── CourseQualificationDAO.java             # 课程资格数据访问
└── ScheduleConflictDAO.java                # 冲突记录数据访问

model/entity/
├── ScheduleDO.java                         # 排课记录实体
├── TeachingClassDO.java                    # 教学班实体
├── TeacherDO.java                          # 教师实体
├── ClassroomDO.java                        # 教室实体
├── CourseDO.java                           # 课程实体
└── TeachingClassClassDO.java               # 教学班-行政班关联实体
```

---

## 八、数据库设计补充

### sc_schedule_conflict 表（已存在，需完善使用）

```sql
-- 冲突类型枚举建议
-- TEACHER_TIME_CONFLICT: 教师时间冲突
-- CLASSROOM_TIME_CONFLICT: 教室时间冲突
-- CLASS_TIME_CONFLICT: 班级时间冲突
-- CAPACITY_OVERFLOW: 教室容量不足
-- CLASSROOM_TYPE_MISMATCH: 教室类型不匹配
-- QUALIFICATION_MISMATCH: 教师资格不足
```

---

## 九、验证测试计划

### 单元测试
1. **TimeSlotTest**: 验证时间槽重叠检测逻辑
2. **ChromosomeTest**: 验证染色体复制和比较
3. **ConflictDetectorTest**: 验证各类冲突检测
4. **FitnessCalculatorTest**: 验证适应度计算
5. **GeneticAlgorithmTest**: 验证算法收敛性

### 集成测试
1. **SmallScaleTest**: 10个教学班的小规模排课
2. **MediumScaleTest**: 50个教学班的中规模排课
3. **LargeScaleTest**: 200个教学班的大规模排课
4. **ConflictHandlingTest**: 验证冲突检测和记录

### 性能测试
1. 测量不同规模数据下的运行时间
2. 验证内存占用情况
3. 测试算法参数对性能的影响

---

## 十、注意事项

1. **教师时间偏好格式**：JSON对象格式 `{"星期几":[节次列表]}`
   - 示例：`{"1":[1,2,3,4],"2":[1,2,3,4],"3":[1,2,3,4]}` 表示周一/周二/周三的第1-4节为偏好时间
   - 解析方法：使用 Jackson 的 ObjectMapper 将 JSON 字符串解析为 `Map<String, List<Integer>>`
2. **周次处理**：初始化时需要为每个课程生成合理的周次列表
3. **课时计算**：需要确定教学班的课时需求（可从课程学分推算或手动指定）
4. **已锁定记录**：算法必须跳过 `is_locked=true` 的排课记录
5. **事务处理**：保存排课方案时使用事务，确保数据一致性
6. **性能优化**：批量查询代替循环查询，使用 Map 缓存减少数据库访问

---

## 十一、后续优化方向

1. **并行化**：适应度计算和冲突检测可以并行处理
2. **本地搜索**：算法结束后使用爬山法进行局部优化
3. **约束调整**：允许用户动态调整约束权重
4. **增量排课**：支持在现有方案基础上调整部分课程
5. **多目标优化**：引入帕累托最优概念
6. **GPU加速**：大规模数据时考虑使用并行计算
