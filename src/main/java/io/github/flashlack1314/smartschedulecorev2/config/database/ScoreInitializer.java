package io.github.flashlack1314.smartschedulecorev2.config.database;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.ScoreDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.StudentDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.TeachingClassClassDAO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 成绩数据初始化器
 * 负责初始化成绩相关数据
 *
 * @author flash
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreInitializer {

    private final ScoreDAO scoreDAO;
    private final StudentDAO studentDAO;
    private final TeachingClassClassDAO teachingClassClassDAO;

    // 成绩权重配置
    private static final BigDecimal USUAL_WEIGHT = new BigDecimal("0.30");
    private static final BigDecimal MIDTERM_WEIGHT = new BigDecimal("0.20");
    private static final BigDecimal FINAL_WEIGHT = new BigDecimal("0.50");

    /**
     * 初始化成绩数据
     * 根据教学班-行政班关联关系，为每个教学班的学生生成成绩
     *
     * @param students        全部学生列表
     * @param teachingClasses 教学班列表
     * @param semesters       学期列表
     * @return 成绩列表
     */
    public List<ScoreDO> initializeScores(List<StudentDO> students,
                                          List<TeachingClassDO> teachingClasses,
                                          List<SemesterDO> semesters) {
        log.info("正在初始化成绩数据...");

        List<ScoreDO> scores = new ArrayList<>();
        Random random = new Random(42); // 固定种子保证可重复

        // 1. 构建学生UUID -> 学生的映射
        Map<String, StudentDO> studentMap = students.stream()
                .collect(Collectors.toMap(StudentDO::getStudentUuid, s -> s));

        // 2. 按行政班级分组学生
        Map<String, List<StudentDO>> studentsByClass = students.stream()
                .collect(Collectors.groupingBy(StudentDO::getClassUuid));

        // 3. 查询所有教学班-行政班关联
        LambdaQueryWrapper<TeachingClassClassDO> queryWrapper = new LambdaQueryWrapper<>();
        List<TeachingClassClassDO> relations = teachingClassClassDAO.list(queryWrapper);

        // 4. 按教学班UUID分组关联关系
        Map<String, List<TeachingClassClassDO>> relationsByTeachingClass = relations.stream()
                .collect(Collectors.groupingBy(TeachingClassClassDO::getTeachingClassUuid));

        // 5. 为每个教学班的学生生成成绩
        for (TeachingClassDO teachingClass : teachingClasses) {
            String teachingClassUuid = teachingClass.getTeachingClassUuid();
            String semesterUuid = teachingClass.getSemesterUuid();

            // 获取该教学班关联的行政班级
            List<TeachingClassClassDO> classRelations = relationsByTeachingClass.get(teachingClassUuid);
            if (classRelations == null || classRelations.isEmpty()) {
                log.warn("教学班 {} 没有关联任何行政班级，跳过成绩初始化", teachingClass.getTeachingClassName());
                continue;
            }

            // 获取该教学班的所有学生
            List<StudentDO> classStudents = new ArrayList<>();
            for (TeachingClassClassDO relation : classRelations) {
                List<StudentDO> classStudentList = studentsByClass.get(relation.getClassUuid());
                if (classStudentList != null) {
                    classStudents.addAll(classStudentList);
                }
            }

            if (classStudents.isEmpty()) {
                log.warn("教学班 {} 关联的行政班级中没有学生，跳过成绩初始化", teachingClass.getTeachingClassName());
                continue;
            }

            // 为大部分学生生成成绩（90%的学生有成绩，模拟真实情况）
            int scoreCount = (int) (classStudents.size() * 0.9);

            for (int i = 0; i < scoreCount && i < classStudents.size(); i++) {
                StudentDO student = classStudents.get(i);

                // 检查是否已经存在成绩（避免重复）
                LambdaQueryWrapper<ScoreDO> existQuery = new LambdaQueryWrapper<>();
                existQuery.eq(ScoreDO::getStudentUuid, student.getStudentUuid())
                        .eq(ScoreDO::getTeachingClassUuid, teachingClassUuid);
                if (scoreDAO.count(existQuery) > 0) {
                    continue;
                }

                // 生成随机成绩
                BigDecimal usualScore = generateRandomScore(random, 60, 100);
                BigDecimal midtermScore = generateRandomScore(random, 50, 100);
                BigDecimal finalScore = generateRandomScore(random, 40, 100);

                // 计算总评成绩
                BigDecimal totalScore = usualScore.multiply(USUAL_WEIGHT)
                        .add(midtermScore.multiply(MIDTERM_WEIGHT))
                        .add(finalScore.multiply(FINAL_WEIGHT))
                        .setScale(2, RoundingMode.HALF_UP);

                // 计算绩点
                BigDecimal gradePoint = calculateGradePoint(totalScore);

                ScoreDO score = new ScoreDO();
                score.setScoreUuid(UuidUtil.generateUuidNoDash())
                        .setStudentUuid(student.getStudentUuid())
                        .setTeachingClassUuid(teachingClassUuid)
                        .setSemesterUuid(semesterUuid)
                        .setUsualScore(usualScore)
                        .setMidtermScore(midtermScore)
                        .setFinalScore(finalScore)
                        .setTotalScore(totalScore)
                        .setGradePoint(gradePoint)
                        .setCreateTime(LocalDateTime.now())
                        .setUpdateTime(LocalDateTime.now());

                scores.add(score);
            }
        }

        // 批量保存成绩
        if (!scores.isEmpty()) {
            scoreDAO.saveBatch(scores);
        }

        log.info("成绩数据初始化完成，共 {} 条记录", scores.size());
        return scores;
    }

    /**
     * 生成随机成绩
     */
    private BigDecimal generateRandomScore(Random random, int min, int max) {
        int score = random.nextInt(max - min + 1) + min;
        // 添加一位小数
        int decimal = random.nextInt(10);
        return new BigDecimal(score + "." + decimal).setScale(1, RoundingMode.HALF_UP);
    }

    /**
     * 计算绩点（标准4.0制）
     */
    private BigDecimal calculateGradePoint(BigDecimal totalScore) {
        double score = totalScore.doubleValue();
        double gp;

        if (score >= 90) {
            gp = 4.0;
        } else if (score >= 85) {
            gp = 3.7;
        } else if (score >= 82) {
            gp = 3.3;
        } else if (score >= 78) {
            gp = 3.0;
        } else if (score >= 75) {
            gp = 2.7;
        } else if (score >= 72) {
            gp = 2.3;
        } else if (score >= 68) {
            gp = 2.0;
        } else if (score >= 64) {
            gp = 1.5;
        } else if (score >= 60) {
            gp = 1.0;
        } else {
            gp = 0;
        }

        return new BigDecimal(gp).setScale(1, RoundingMode.HALF_UP);
    }
}
