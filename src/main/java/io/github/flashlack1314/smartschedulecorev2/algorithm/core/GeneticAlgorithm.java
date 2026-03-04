package io.github.flashlack1314.smartschedulecorev2.algorithm.core;

import io.github.flashlack1314.smartschedulecorev2.algorithm.dto.ScheduleContext;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.Chromosome;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.CourseAppointment;
import io.github.flashlack1314.smartschedulecorev2.algorithm.entity.TimeSlot;
import io.github.flashlack1314.smartschedulecorev2.algorithm.util.TimeSlotGenerator;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 遗传算法主类：执行自动排课
 *
 * @author flash
 */
@Slf4j
@Data
@RequiredArgsConstructor
public class GeneticAlgorithm {

    private final ScheduleContext context;
    private final FitnessCalculator fitnessCalculator;
    private final ConflictDetector conflictDetector;
    private final TimeSlotGenerator timeSlotGenerator;
    private final HoursCalculator hoursCalculator;

    // 算法参数
    private int populationSize = 100;
    private int maxGenerations = 500;
    private double crossoverRate = 0.8;
    private double mutationRate = 0.2;
    private int eliteSize = 10;
    private double geneProtectionRate = 0.3;

    // 可用时间槽缓存
    private List<TimeSlot> allTimeSlots;

    /**
     * 执行自动排课
     *
     * @return 最佳排课方案
     */
    public Chromosome schedule() {
        // 生成所有可能的时间槽
        allTimeSlots = timeSlotGenerator.generateAllTimeSlots(
                context.getDaysPerWeek(),
                context.getSectionsPerDay(),
                generateWeeksList()
        );

        // 阶段1：初始化种群
        List<Chromosome> population = initializePopulation();

        Chromosome globalBest = null;
        int stagnantGenerations = 0;

        // 阶段2：进化迭代
        for (int generation = 0; generation < maxGenerations; generation++) {
            // 评估适应度
            evaluatePopulation(population);

            // 排序并记录当前代最优解
            population.sort(Collections.reverseOrder());
            Chromosome currentBest = population.get(0).copy();

            // 更新全局最优
            if (globalBest == null || currentBest.getFitness() > globalBest.getFitness()) {
                globalBest = currentBest;
                stagnantGenerations = 0;
            } else {
                stagnantGenerations++;
            }

            // 早停策略：如果连续50代无改进，提前终止
            if (stagnantGenerations >= 50) {
                log.info("连续{}代无改进，提前终止于第{}代", stagnantGenerations, generation);
                break;
            }

            // 检查是否达到终止条件
            if (isTerminationConditionMet(globalBest)) {
                log.info("找到满足条件的解，提前终止于第{}代", generation);
                break;
            }

            // 【精英保留策略】保留精英个体
            List<Chromosome> elites = preserveElites(population, eliteSize);

            // 【突变基因保留】识别并保护优质基因片段
            Map<String, List<GeneFragment>> protectedGenes = identifyValuableGenes(population);

            // 选择
            List<Chromosome> selected = selection(population);

            // 交叉（保护优质基因）
            List<Chromosome> offspring = crossover(selected, protectedGenes);

            // 变异
            mutate(offspring);

            // 构建新一代：精英 + 后代
            population = new ArrayList<>();
            population.addAll(elites);
            population.addAll(offspring);

            if (generation % 50 == 0) {
                log.info("第 {} 代，最优适应度: {}, 硬冲突数: {}, 未完成数: {}",
                        generation, globalBest.getFitness(),
                        globalBest.getHardConstraintViolations(),
                        globalBest.getUnscheduledTeachingClasses().size());
            }
        }

        log.info("算法结束，最优适应度: {}, 硬冲突数: {}, 未完成数: {}",
                globalBest.getFitness(),
                globalBest.getHardConstraintViolations(),
                globalBest.getUnscheduledTeachingClasses().size());

        return globalBest;
    }

    /**
     * 初始化种群：生成合法的初始解
     */
    private List<Chromosome> initializePopulation() {
        List<Chromosome> population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            Chromosome chromosome = new Chromosome();

            // 为每个教学班生成排课安排
            for (ScheduleContext.TeachingClassInfo tc : context.getTeachingClassList()) {
                // 计算需要的上课次数
                int requiredSessions = tc.getRequiredSessions();

                // 尝试为该教学班安排requiredSessions次上课
                int scheduledSessions = 0;
                int attempts = 0;
                int maxAttempts = requiredSessions * 5;

                while (scheduledSessions < requiredSessions && attempts < maxAttempts) {
                    attempts++;

                    // 随机选择时间槽
                    TimeSlot timeSlot = selectRandomTimeSlot();

                    // 随机选择合适教室
                    String classroomUuid = selectSuitableClassroom(tc, timeSlot);

                    if (classroomUuid == null) {
                        continue;
                    }

                    // 创建课程安排
                    CourseAppointment appointment = buildAppointment(tc, classroomUuid, timeSlot);

                    // 检查是否与已有安排冲突
                    if (!conflictDetector.hasConflict(chromosome, appointment, context)) {
                        // 添加到染色体
                        chromosome.getGenes()
                                .computeIfAbsent(timeSlot, k -> new ArrayList<>())
                                .add(appointment);

                        scheduledSessions++;
                    }
                }

                // 如果未能完成所有排课，记录到未完成列表
                if (scheduledSessions < requiredSessions) {
                    chromosome.getUnscheduledTeachingClasses().add(tc.getTeachingClassUuid());
                }
            }

            population.add(chromosome);
        }

        log.info("初始化种群完成，种群大小: {}", population.size());
        return population;
    }

    /**
     * 评估种群中所有个体的适应度
     */
    private void evaluatePopulation(List<Chromosome> population) {
        for (Chromosome chromosome : population) {
            fitnessCalculator.calculateFitness(chromosome, context);
        }
    }

    /**
     * 精英保留策略：保留适应度最高的N个个体
     */
    private List<Chromosome> preserveElites(List<Chromosome> population, int eliteSize) {
        List<Chromosome> elites = new ArrayList<>();
        for (int i = 0; i < Math.min(eliteSize, population.size()); i++) {
            elites.add(population.get(i).copy());
        }
        return elites;
    }

    /**
     * 检查是否满足终止条件
     */
    private boolean isTerminationConditionMet(Chromosome solution) {
        return solution.getHardConstraintViolations() == 0 &&
                solution.getUnscheduledTeachingClasses().isEmpty() &&
                solution.getFitness() >= 0;
    }

    /**
     * 选择算子：轮盘赌选择
     */
    private List<Chromosome> selection(List<Chromosome> population) {
        List<Chromosome> selected = new ArrayList<>();

        // 计算总适应度（处理负值）
        double minFitness = population.stream()
                .mapToDouble(Chromosome::getFitness)
                .min()
                .orElse(0);

        double totalFitness = population.stream()
                .mapToDouble(c -> c.getFitness() - minFitness + 1)
                .sum();

        Random random = new Random();

        // 选择 populationSize - eliteSize 个个体
        int selectCount = populationSize - eliteSize;

        for (int i = 0; i < selectCount; i++) {
            double rand = random.nextDouble() * totalFitness;
            double cumulative = 0;

            for (Chromosome chromosome : population) {
                cumulative += (chromosome.getFitness() - minFitness + 1);
                if (cumulative >= rand) {
                    selected.add(chromosome.copy());
                    break;
                }
            }
        }

        return selected;
    }

    /**
     * 交叉算子：时间点交叉
     */
    private List<Chromosome> crossover(List<Chromosome> parents,
                                       Map<String, List<GeneFragment>> protectedGenes) {
        List<Chromosome> offspring = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < parents.size(); i += 2) {
            if (i + 1 >= parents.size()) break;

            Chromosome parent1 = parents.get(i);
            Chromosome parent2 = parents.get(i + 1);

            if (random.nextDouble() < crossoverRate) {
                Chromosome child1 = parent1.copy();
                Chromosome child2 = parent2.copy();

                // 选择交叉点
                List<TimeSlot> allSlots = new ArrayList<>(child1.getGenes().keySet());
                if (!allSlots.isEmpty()) {
                    int crossoverPoint = random.nextInt(allSlots.size());

                    // 执行交叉
                    for (int j = crossoverPoint; j < allSlots.size(); j++) {
                        TimeSlot slot = allSlots.get(j);
                        List<CourseAppointment> temp = child1.getGenes().get(slot);
                        child1.getGenes().put(slot, child2.getGenes().getOrDefault(slot, new ArrayList<>()));
                        child2.getGenes().put(slot, temp != null ? temp : new ArrayList<>());
                    }
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
     * 变异算子：三态变异
     */
    private void mutate(List<Chromosome> population) {
        Random random = new Random();

        for (Chromosome chromosome : population) {
            if (random.nextDouble() > mutationRate) continue;

            List<TimeSlot> timeSlots = new ArrayList<>(chromosome.getGenes().keySet());
            if (timeSlots.isEmpty()) continue;

            // 随机选择一个时间槽
            TimeSlot targetSlot = timeSlots.get(random.nextInt(timeSlots.size()));
            List<CourseAppointment> appointments = chromosome.getGenes().get(targetSlot);

            if (appointments == null || appointments.isEmpty()) continue;

            CourseAppointment target = appointments.get(random.nextInt(appointments.size()));

            // 三态变异
            double mutationType = random.nextDouble();

            if (mutationType < 0.5) {
                // 时间槽变异
                TimeSlot newSlot = selectRandomTimeSlot();
                if (newSlot != null) {
                    appointments.remove(target);
                    CourseAppointment newAppointment = target.copy();
                    newAppointment.setTimeSlot(newSlot);
                    chromosome.getGenes()
                            .computeIfAbsent(newSlot, k -> new ArrayList<>())
                            .add(newAppointment);
                }
            } else if (mutationType < 0.8) {
                // 教室变异
                ScheduleContext.TeachingClassInfo tcInfo = findTeachingClassInfo(target.getTeachingClassUuid());
                if (tcInfo != null) {
                    String newClassroom = selectSuitableClassroom(tcInfo, target.getTimeSlot());
                    if (newClassroom != null) {
                        target.setClassroomUuid(newClassroom);
                    }
                }
            }
            // 教师变异暂时不实现（教师通常是固定的）
        }
    }

    /**
     * 基因片段：用于保护优质基因
     */
    @Data
    private static class GeneFragment {
        private String teachingClassUuid;
        private TimeSlot timeSlot;
        private String classroomUuid;
        private double qualityScore;

        public GeneFragment(String teachingClassUuid, TimeSlot timeSlot, String classroomUuid, double qualityScore) {
            this.teachingClassUuid = teachingClassUuid;
            this.timeSlot = timeSlot;
            this.classroomUuid = classroomUuid;
            this.qualityScore = qualityScore;
        }
    }

    /**
     * 识别优质基因片段
     */
    private Map<String, List<GeneFragment>> identifyValuableGenes(List<Chromosome> population) {
        Map<String, List<GeneFragment>> valuableGenes = new HashMap<>();

        // 统计每个教学班的基因片段出现频率
        Map<String, Map<String, GeneStats>> geneStatistics = new HashMap<>();

        // 遍历种群前50%的优秀个体
        int topCount = (int) (population.size() * 0.5);
        for (int i = 0; i < topCount && i < population.size(); i++) {
            Chromosome chromosome = population.get(i);

            for (Map.Entry<TimeSlot, List<CourseAppointment>> entry : chromosome.getGenes().entrySet()) {
                TimeSlot slot = entry.getKey();

                for (CourseAppointment appointment : entry.getValue()) {
                    String tcUuid = appointment.getTeachingClassUuid();
                    String geneKey = tcUuid + "|" + slot.getUniqueId() + "|" + appointment.getClassroomUuid();

                    geneStatistics
                            .computeIfAbsent(tcUuid, k -> new HashMap<>())
                            .computeIfAbsent(geneKey, k -> new GeneStats())
                            .increment(chromosome.getFitness());
                }
            }
        }

        // 筛选优质基因
        for (Map.Entry<String, Map<String, GeneStats>> entry : geneStatistics.entrySet()) {
            String tcUuid = entry.getKey();
            List<GeneFragment> fragments = new ArrayList<>();

            for (Map.Entry<String, GeneStats> statsEntry : entry.getValue().entrySet()) {
                GeneStats stats = statsEntry.getValue();
                double qualityScore = (double) stats.count / topCount * (stats.fitnessSum / stats.count);

                if (qualityScore > geneProtectionRate) {
                    String[] parts = statsEntry.getKey().split("\\|");
                    if (parts.length >= 3) {
                        String slotId = parts[1];
                        String classroomUuid = parts[2];
                        TimeSlot slot = parseTimeSlotFromId(slotId);
                        if (slot != null) {
                            fragments.add(new GeneFragment(tcUuid, slot, classroomUuid, qualityScore));
                        }
                    }
                }
            }

            if (!fragments.isEmpty()) {
                valuableGenes.put(tcUuid, fragments);
            }
        }

        return valuableGenes;
    }

    /**
     * 基因统计信息
     */
    private static class GeneStats {
        int count = 0;
        double fitnessSum = 0;

        void increment(double fitness) {
            this.count++;
            this.fitnessSum += fitness;
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成周次列表
     */
    private List<Integer> generateWeeksList() {
        int semesterWeeks = context.getSemesterWeeks() != null ? context.getSemesterWeeks() : 16;
        return IntStream.rangeClosed(1, semesterWeeks).boxed().collect(Collectors.toList());
    }

    /**
     * 随机选择时间槽
     */
    private TimeSlot selectRandomTimeSlot() {
        if (allTimeSlots == null || allTimeSlots.isEmpty()) {
            return null;
        }
        Random random = new Random();
        TimeSlot baseSlot = allTimeSlots.get(random.nextInt(allTimeSlots.size()));

        // 复制并设置随机周次
        TimeSlot slot = new TimeSlot();
        slot.setDayOfWeek(baseSlot.getDayOfWeek());
        slot.setSectionStart(baseSlot.getSectionStart());
        slot.setSectionEnd(baseSlot.getSectionEnd());

        // 随机选择1-5个连续周次
        int semesterWeeks = context.getSemesterWeeks() != null ? context.getSemesterWeeks() : 16;
        int startWeek = random.nextInt(semesterWeeks) + 1;
        int weekCount = random.nextInt(Math.min(5, semesterWeeks - startWeek + 1)) + 1;
        slot.setWeeks(IntStream.rangeClosed(startWeek, startWeek + weekCount - 1).boxed().collect(Collectors.toList()));

        return slot;
    }

    /**
     * 选择合适的教室
     * 根据课程类型-教室类型映射选择合适类型的教室
     */
    private String selectSuitableClassroom(ScheduleContext.TeachingClassInfo tc, TimeSlot timeSlot) {
        if (context.getAvailableClassrooms() == null || context.getAvailableClassrooms().isEmpty()) {
            log.warn("没有可用的教室数据");
            return null;
        }

        // 1. 获取该课程类型对应的教室类型列表
        List<String> allowedClassroomTypes = context.getCourseTypeToClassroomTypes() != null
                ? context.getCourseTypeToClassroomTypes().get(tc.getCourseTypeUuid())
                : null;

        // 2. 收集这些教室类型下的所有教室
        List<ScheduleContext.ClassroomInfo> classrooms = new ArrayList<>();
        if (allowedClassroomTypes != null && !allowedClassroomTypes.isEmpty()) {
            for (String classroomTypeUuid : allowedClassroomTypes) {
                List<ScheduleContext.ClassroomInfo> typeRooms = context.getAvailableClassrooms()
                        .get(classroomTypeUuid);
                if (typeRooms != null) {
                    classrooms.addAll(typeRooms);
                }
            }
            log.debug("课程类型 {} 找到 {} 个匹配类型的教室",
                    tc.getCourseTypeUuid(), classrooms.size());
        } else {
            log.debug("课程类型 {} 没有配置教室类型映射，使用所有可用教室",
                    tc.getCourseTypeUuid());
            // 向后兼容：只有当没有配置映射时才使用所有教室
            classrooms = context.getAvailableClassrooms().values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }

        // 3. 如果仍然没有教室，记录警告并返回null
        if (classrooms.isEmpty()) {
            log.warn("课程类型 {} 找不到任何可用教室", tc.getCourseTypeUuid());
            return null;
        }

        // 4. 过滤容量足够的教室
        List<ScheduleContext.ClassroomInfo> suitableClassrooms = classrooms.stream()
                .filter(c -> c.getCapacity() >= tc.getTotalStudents())
                .collect(Collectors.toList());

        if (suitableClassrooms.isEmpty()) {
            log.debug("课程 {} 需要 {} 个座位，但没有足够容量的教室，使用所有可用教室",
                    tc.getCourseName(), tc.getTotalStudents());
            suitableClassrooms = classrooms; // 如果没有足够容量的，使用所有教室
        }

        if (suitableClassrooms.isEmpty()) {
            log.warn("课程 {} 无法找到任何合适的教室", tc.getCourseName());
            return null;
        }

        // 5. 随机选择一个
        Random random = new Random();
        ScheduleContext.ClassroomInfo selected = suitableClassrooms.get(random.nextInt(suitableClassrooms.size()));
        log.trace("为课程 {} 选择教室: {}", tc.getCourseName(), selected.getClassroomName());
        return selected.getClassroomUuid();
    }

    /**
     * 构建课程安排对象
     */
    private CourseAppointment buildAppointment(ScheduleContext.TeachingClassInfo tc, String classroomUuid, TimeSlot timeSlot) {
        CourseAppointment appointment = new CourseAppointment();
        appointment.setTeachingClassUuid(tc.getTeachingClassUuid());
        appointment.setTeachingClassName(tc.getTeachingClassName());
        appointment.setCourseUuid(tc.getCourseUuid());
        appointment.setCourseName(tc.getCourseName());
        appointment.setCourseTotalHours(tc.getCourseTotalHours());
        appointment.setTeacherUuid(tc.getTeacherUuid());
        appointment.setTeacherName(tc.getTeacherName());
        appointment.setClassroomUuid(classroomUuid);
        appointment.setCourseTypeUuid(tc.getCourseTypeUuid());
        appointment.setClassUuids(tc.getClassUuids());
        appointment.setTotalStudents(tc.getTotalStudents());
        appointment.setTimeSlot(timeSlot);

        // 设置教室名称和容量
        if (context.getAvailableClassrooms() != null) {
            for (List<ScheduleContext.ClassroomInfo> classrooms : context.getAvailableClassrooms().values()) {
                for (ScheduleContext.ClassroomInfo info : classrooms) {
                    if (info.getClassroomUuid().equals(classroomUuid)) {
                        appointment.setClassroomName(info.getClassroomName());
                        appointment.setClassroomCapacity(info.getCapacity());
                        appointment.setClassroomTypeUuid(info.getClassroomTypeUuid());
                        break;
                    }
                }
            }
        }

        return appointment;
    }

    /**
     * 查找教学班信息
     */
    private ScheduleContext.TeachingClassInfo findTeachingClassInfo(String teachingClassUuid) {
        if (context.getTeachingClassList() == null) {
            return null;
        }
        return context.getTeachingClassList().stream()
                .filter(tc -> tc.getTeachingClassUuid().equals(teachingClassUuid))
                .findFirst()
                .orElse(null);
    }

    /**
     * 从ID解析时间槽
     */
    private TimeSlot parseTimeSlotFromId(String slotId) {
        try {
            // 格式: day-sectionStart-sectionEnd-[weeks]
            String[] parts = slotId.split("-");
            if (parts.length >= 3) {
                TimeSlot slot = new TimeSlot();
                slot.setDayOfWeek(Integer.parseInt(parts[0]));
                slot.setSectionStart(Integer.parseInt(parts[1]));
                slot.setSectionEnd(Integer.parseInt(parts[2]));
                // 周次暂时设为空列表
                slot.setWeeks(new ArrayList<>());
                return slot;
            }
        } catch (Exception e) {
            log.warn("解析时间槽ID失败: {}", slotId);
        }
        return null;
    }
}
