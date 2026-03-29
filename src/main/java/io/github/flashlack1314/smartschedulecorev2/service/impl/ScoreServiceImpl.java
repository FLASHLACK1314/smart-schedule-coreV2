package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.*;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.*;
import io.github.flashlack1314.smartschedulecorev2.model.entity.*;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddScoreVO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.BatchAddScoreVO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.UpdateScoreVO;
import io.github.flashlack1314.smartschedulecorev2.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 成绩服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreServiceImpl implements ScoreService {

    private final ScoreDAO scoreDAO;
    private final StudentDAO studentDAO;
    private final TeachingClassDAO teachingClassDAO;
    private final SemesterDAO semesterDAO;
    private final CourseDAO courseDAO;
    private final TeacherDAO teacherDAO;
    private final ClassDAO classDAO;
    private final MajorDAO majorDAO;
    private final DepartmentDAO departmentDAO;
    private final TeachingClassClassDAO teachingClassClassDAO;

    // 成绩权重配置（可根据需要调整）
    private static final BigDecimal USUAL_WEIGHT = new BigDecimal("0.30");      // 平时成绩权重 30%
    private static final BigDecimal MIDTERM_WEIGHT = new BigDecimal("0.20");    // 期中成绩权重 20%
    private static final BigDecimal FINAL_WEIGHT = new BigDecimal("0.50");      // 期末成绩权重 50%

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addScore(AddScoreVO getData) {
        log.info("添加成绩 - 学生UUID: {}, 教学班UUID: {}", getData.getStudentUuid(), getData.getTeachingClassUuid());

        // 验证学生是否存在
        StudentDO student = studentDAO.getById(getData.getStudentUuid());
        if (student == null) {
            throw new BusinessException("学生不存在: " + getData.getStudentUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证教学班是否存在
        TeachingClassDO teachingClass = teachingClassDAO.getById(getData.getTeachingClassUuid());
        if (teachingClass == null) {
            throw new BusinessException("教学班不存在: " + getData.getTeachingClassUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证学期是否存在
        SemesterDO semester = semesterDAO.getById(getData.getSemesterUuid());
        if (semester == null) {
            throw new BusinessException("学期不存在: " + getData.getSemesterUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 检查成绩是否已存在
        if (scoreDAO.existsByStudentAndTeachingClass(getData.getStudentUuid(), getData.getTeachingClassUuid())) {
            throw new BusinessException("该学生的成绩已存在", ErrorCode.OPERATION_FAILED);
        }

        // 验证成绩范围
        validateScoreRange(getData.getUsualScore(), getData.getMidtermScore(), getData.getFinalScore());

        // 计算总评成绩和绩点
        BigDecimal totalScore = calculateTotalScore(getData.getUsualScore(), getData.getMidtermScore(), getData.getFinalScore());
        BigDecimal gradePoint = calculateGradePoint(totalScore);

        // 创建成绩对象
        ScoreDO scoreDO = new ScoreDO();
        scoreDO.setScoreUuid(UuidUtil.generateUuidNoDash());
        scoreDO.setStudentUuid(getData.getStudentUuid());
        scoreDO.setTeachingClassUuid(getData.getTeachingClassUuid());
        scoreDO.setSemesterUuid(getData.getSemesterUuid());
        scoreDO.setUsualScore(getData.getUsualScore());
        scoreDO.setMidtermScore(getData.getMidtermScore());
        scoreDO.setFinalScore(getData.getFinalScore());
        scoreDO.setTotalScore(totalScore);
        scoreDO.setGradePoint(gradePoint);
        scoreDO.setRemark(getData.getRemark());
        scoreDO.setCreateTime(LocalDateTime.now());
        scoreDO.setUpdateTime(LocalDateTime.now());

        // 保存到数据库
        boolean saved = scoreDAO.save(scoreDO);
        if (!saved) {
            throw new BusinessException("保存成绩失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("成绩添加成功 - UUID: {}, 总评: {}, 绩点: {}", scoreDO.getScoreUuid(), totalScore, gradePoint);
        return scoreDO.getScoreUuid();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAddScore(BatchAddScoreVO getData) {
        log.info("批量添加成绩 - 教学班UUID: {}, 成绩数量: {}", getData.getTeachingClassUuid(), getData.getScoreItems().size());

        // 验证教学班是否存在
        TeachingClassDO teachingClass = teachingClassDAO.getById(getData.getTeachingClassUuid());
        if (teachingClass == null) {
            throw new BusinessException("教学班不存在: " + getData.getTeachingClassUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证学期是否存在
        SemesterDO semester = semesterDAO.getById(getData.getSemesterUuid());
        if (semester == null) {
            throw new BusinessException("学期不存在: " + getData.getSemesterUuid(), ErrorCode.OPERATION_FAILED);
        }

        int successCount = 0;
        List<String> failedStudents = new ArrayList<>();

        for (BatchAddScoreVO.ScoreItem item : getData.getScoreItems()) {
            try {
                // 验证学生是否存在
                StudentDO student = studentDAO.getById(item.getStudentUuid());
                if (student == null) {
                    failedStudents.add(item.getStudentUuid());
                    continue;
                }

                // 检查成绩是否已存在
                if (scoreDAO.existsByStudentAndTeachingClass(item.getStudentUuid(), getData.getTeachingClassUuid())) {
                    failedStudents.add(item.getStudentUuid());
                    continue;
                }

                // 验证成绩范围
                validateScoreRange(item.getUsualScore(), item.getMidtermScore(), item.getFinalScore());

                // 计算总评成绩和绩点
                BigDecimal totalScore = calculateTotalScore(item.getUsualScore(), item.getMidtermScore(), item.getFinalScore());
                BigDecimal gradePoint = calculateGradePoint(totalScore);

                // 创建成绩对象
                ScoreDO scoreDO = new ScoreDO();
                scoreDO.setScoreUuid(UuidUtil.generateUuidNoDash());
                scoreDO.setStudentUuid(item.getStudentUuid());
                scoreDO.setTeachingClassUuid(getData.getTeachingClassUuid());
                scoreDO.setSemesterUuid(getData.getSemesterUuid());
                scoreDO.setUsualScore(item.getUsualScore());
                scoreDO.setMidtermScore(item.getMidtermScore());
                scoreDO.setFinalScore(item.getFinalScore());
                scoreDO.setTotalScore(totalScore);
                scoreDO.setGradePoint(gradePoint);
                scoreDO.setRemark(item.getRemark());
                scoreDO.setCreateTime(LocalDateTime.now());
                scoreDO.setUpdateTime(LocalDateTime.now());

                scoreDAO.save(scoreDO);
                successCount++;
            } catch (Exception e) {
                log.warn("批量添加成绩失败 - 学生UUID: {}, 原因: {}", item.getStudentUuid(), e.getMessage());
                failedStudents.add(item.getStudentUuid());
            }
        }

        if (!failedStudents.isEmpty()) {
            log.warn("批量添加成绩部分失败 - 失败的学生UUID: {}", failedStudents);
        }

        log.info("批量添加成绩完成 - 成功: {}, 失败: {}", successCount, failedStudents.size());
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateScore(UpdateScoreVO getData) {
        log.info("更新成绩 - UUID: {}", getData.getScoreUuid());

        // 查询成绩是否存在
        ScoreDO score = scoreDAO.getById(getData.getScoreUuid());
        if (score == null) {
            throw new BusinessException("成绩不存在: " + getData.getScoreUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 验证成绩范围
        validateScoreRange(getData.getUsualScore(), getData.getMidtermScore(), getData.getFinalScore());

        // 计算总评成绩和绩点
        BigDecimal totalScore = calculateTotalScore(getData.getUsualScore(), getData.getMidtermScore(), getData.getFinalScore());
        BigDecimal gradePoint = calculateGradePoint(totalScore);

        // 更新成绩
        boolean updated = scoreDAO.updateScore(
                getData.getScoreUuid(),
                getData.getUsualScore(),
                getData.getMidtermScore(),
                getData.getFinalScore(),
                totalScore,
                gradePoint,
                getData.getRemark()
        );

        if (!updated) {
            throw new BusinessException("更新成绩失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("成绩更新成功 - UUID: {}, 总评: {}, 绩点: {}", getData.getScoreUuid(), totalScore, gradePoint);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteScore(String scoreUuid) {
        log.info("删除成绩 - UUID: {}", scoreUuid);

        // 查询成绩是否存在
        ScoreDO score = scoreDAO.getById(scoreUuid);
        if (score == null) {
            throw new BusinessException("成绩不存在: " + scoreUuid, ErrorCode.OPERATION_FAILED);
        }

        // 执行删除
        boolean deleted = scoreDAO.removeById(scoreUuid);
        if (!deleted) {
            throw new BusinessException("删除成绩失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("成绩删除成功 - UUID: {}", scoreUuid);
    }

    @Override
    public ScoreInfoDTO getScore(String scoreUuid) {
        log.info("获取成绩信息 - UUID: {}", scoreUuid);

        ScoreDO score = scoreDAO.getById(scoreUuid);
        if (score == null) {
            throw new BusinessException("成绩不存在: " + scoreUuid, ErrorCode.OPERATION_FAILED);
        }

        return convertToScoreInfoDTO(score);
    }

    @Override
    public PageDTO<ScoreInfoDTO> getScorePage(int page, int size, String studentUuid,
                                               String teachingClassUuid, String semesterUuid) {
        log.info("分页查询成绩 - page: {}, size: {}, studentUuid: {}, teachingClassUuid: {}, semesterUuid: {}",
                page, size, studentUuid, teachingClassUuid, semesterUuid);

        IPage<ScoreDO> pageResult = scoreDAO.getScorePage(page, size, studentUuid, teachingClassUuid, semesterUuid);

        List<ScoreInfoDTO> scoreInfoList = pageResult.getRecords().stream()
                .map(this::convertToScoreInfoDTO)
                .collect(Collectors.toList());

        return buildPageDTO(page, size, (int) pageResult.getTotal(), scoreInfoList);
    }

    @Override
    public List<ScoreInfoDTO> getStudentScores(String studentUuid, String semesterUuid) {
        log.info("查询学生成绩列表 - studentUuid: {}, semesterUuid: {}", studentUuid, semesterUuid);

        // 验证学生是否存在
        StudentDO student = studentDAO.getById(studentUuid);
        if (student == null) {
            throw new BusinessException("学生不存在: " + studentUuid, ErrorCode.OPERATION_FAILED);
        }

        LambdaQueryWrapper<ScoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreDO::getStudentUuid, studentUuid);
        if (StringUtils.hasText(semesterUuid)) {
            queryWrapper.eq(ScoreDO::getSemesterUuid, semesterUuid);
        }
        queryWrapper.orderByDesc(ScoreDO::getCreateTime);

        List<ScoreDO> scores = scoreDAO.list(queryWrapper);
        return scores.stream()
                .map(this::convertToScoreInfoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScoreInfoDTO> getTeachingClassScores(String teachingClassUuid) {
        log.info("查询教学班成绩列表 - teachingClassUuid: {}", teachingClassUuid);

        // 验证教学班是否存在
        TeachingClassDO teachingClass = teachingClassDAO.getById(teachingClassUuid);
        if (teachingClass == null) {
            throw new BusinessException("教学班不存在: " + teachingClassUuid, ErrorCode.OPERATION_FAILED);
        }

        List<ScoreDO> scores = scoreDAO.listByTeachingClassUuid(teachingClassUuid);
        return scores.stream()
                .map(this::convertToScoreInfoDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ScoreStatisticsDTO getScoreStatistics(String teachingClassUuid) {
        log.info("获取成绩统计 - teachingClassUuid: {}", teachingClassUuid);

        // 验证教学班是否存在
        TeachingClassDO teachingClass = teachingClassDAO.getById(teachingClassUuid);
        if (teachingClass == null) {
            throw new BusinessException("教学班不存在: " + teachingClassUuid, ErrorCode.OPERATION_FAILED);
        }

        // 获取教学班的成绩列表
        List<ScoreDO> scores = scoreDAO.listByTeachingClassUuid(teachingClassUuid);

        // 获取教学班关联的行政班级
        LambdaQueryWrapper<TeachingClassClassDO> tccQueryWrapper = new LambdaQueryWrapper<>();
        tccQueryWrapper.eq(TeachingClassClassDO::getTeachingClassUuid, teachingClassUuid);
        List<TeachingClassClassDO> classRelations = teachingClassClassDAO.list(tccQueryWrapper);

        // 计算学生总数
        int totalStudents = 0;
        for (TeachingClassClassDO relation : classRelations) {
            totalStudents += (int) studentDAO.countByClassUuid(relation.getClassUuid());
        }

        // 构建统计DTO
        ScoreStatisticsDTO statisticsDTO = new ScoreStatisticsDTO();
        statisticsDTO.setTeachingClassUuid(teachingClassUuid);

        // 获取课程信息
        CourseDO course = courseDAO.getById(teachingClass.getCourseUuid());
        TeacherDO teacher = teacherDAO.getById(teachingClass.getTeacherUuid());
        statisticsDTO.setCourseName(course != null ? course.getCourseName() : null);
        statisticsDTO.setTeacherName(teacher != null ? teacher.getTeacherName() : null);

        statisticsDTO.setTotalStudents(totalStudents);
        statisticsDTO.setEnteredCount(scores.size());

        if (scores.isEmpty()) {
            statisticsDTO.setAverageScore(BigDecimal.ZERO);
            statisticsDTO.setMaxScore(BigDecimal.ZERO);
            statisticsDTO.setMinScore(BigDecimal.ZERO);
            statisticsDTO.setPassCount(0);
            statisticsDTO.setPassRate(BigDecimal.ZERO);
            statisticsDTO.setExcellentCount(0);
            statisticsDTO.setExcellentRate(BigDecimal.ZERO);
            statisticsDTO.setAverageGradePoint(BigDecimal.ZERO);
            return statisticsDTO;
        }

        // 计算统计数据
        List<BigDecimal> totalScores = scores.stream()
                .map(ScoreDO::getTotalScore)
                .filter(s -> s != null)
                .collect(Collectors.toList());

        if (!totalScores.isEmpty()) {
            BigDecimal sum = totalScores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avg = sum.divide(new BigDecimal(totalScores.size()), 2, RoundingMode.HALF_UP);
            BigDecimal max = totalScores.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal min = totalScores.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

            statisticsDTO.setAverageScore(avg);
            statisticsDTO.setMaxScore(max);
            statisticsDTO.setMinScore(min);

            // 计算及格人数和优秀人数
            int passCount = (int) totalScores.stream().filter(s -> s.compareTo(new BigDecimal("60")) >= 0).count();
            int excellentCount = (int) totalScores.stream().filter(s -> s.compareTo(new BigDecimal("90")) >= 0).count();

            statisticsDTO.setPassCount(passCount);
            statisticsDTO.setExcellentCount(excellentCount);

            // 计算及格率和优秀率
            BigDecimal passRate = new BigDecimal(passCount)
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(totalScores.size()), 2, RoundingMode.HALF_UP);
            BigDecimal excellentRate = new BigDecimal(excellentCount)
                    .multiply(new BigDecimal("100"))
                    .divide(new BigDecimal(totalScores.size()), 2, RoundingMode.HALF_UP);

            statisticsDTO.setPassRate(passRate);
            statisticsDTO.setExcellentRate(excellentRate);

            // 计算平均绩点
            List<BigDecimal> gradePoints = scores.stream()
                    .map(ScoreDO::getGradePoint)
                    .filter(g -> g != null)
                    .collect(Collectors.toList());
            if (!gradePoints.isEmpty()) {
                BigDecimal gpaSum = gradePoints.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal gpaAvg = gpaSum.divide(new BigDecimal(gradePoints.size()), 2, RoundingMode.HALF_UP);
                statisticsDTO.setAverageGradePoint(gpaAvg);
            }
        }

        return statisticsDTO;
    }

    @Override
    public BigDecimal calculateGPA(String studentUuid, String semesterUuid) {
        log.info("计算学生绩点 - studentUuid: {}, semesterUuid: {}", studentUuid, semesterUuid);

        LambdaQueryWrapper<ScoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreDO::getStudentUuid, studentUuid);
        if (StringUtils.hasText(semesterUuid)) {
            queryWrapper.eq(ScoreDO::getSemesterUuid, semesterUuid);
        }

        List<ScoreDO> scores = scoreDAO.list(queryWrapper);

        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 计算加权平均绩点（按课程学分加权）
        BigDecimal totalWeightedGP = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;

        for (ScoreDO score : scores) {
            if (score.getGradePoint() != null) {
                TeachingClassDO teachingClass = teachingClassDAO.getById(score.getTeachingClassUuid());
                if (teachingClass != null) {
                    CourseDO course = courseDAO.getById(teachingClass.getCourseUuid());
                    if (course != null && course.getCourseCredit() != null) {
                        BigDecimal credit = course.getCourseCredit();
                        totalWeightedGP = totalWeightedGP.add(score.getGradePoint().multiply(credit));
                        totalCredits = totalCredits.add(credit);
                    }
                }
            }
        }

        if (totalCredits.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalWeightedGP.divide(totalCredits, 2, RoundingMode.HALF_UP);
    }

    /**
     * 验证成绩范围
     */
    private void validateScoreRange(BigDecimal usualScore, BigDecimal midtermScore, BigDecimal finalScore) {
        BigDecimal min = BigDecimal.ZERO;
        BigDecimal max = new BigDecimal("100");

        if (usualScore != null && (usualScore.compareTo(min) < 0 || usualScore.compareTo(max) > 0)) {
            throw new BusinessException("平时成绩必须在0-100之间", ErrorCode.OPERATION_FAILED);
        }
        if (midtermScore != null && (midtermScore.compareTo(min) < 0 || midtermScore.compareTo(max) > 0)) {
            throw new BusinessException("期中成绩必须在0-100之间", ErrorCode.OPERATION_FAILED);
        }
        if (finalScore != null && (finalScore.compareTo(min) < 0 || finalScore.compareTo(max) > 0)) {
            throw new BusinessException("期末成绩必须在0-100之间", ErrorCode.OPERATION_FAILED);
        }
    }

    /**
     * 计算总评成绩
     */
    private BigDecimal calculateTotalScore(BigDecimal usualScore, BigDecimal midtermScore, BigDecimal finalScore) {
        BigDecimal total = BigDecimal.ZERO;

        if (usualScore != null) {
            total = total.add(usualScore.multiply(USUAL_WEIGHT));
        }
        if (midtermScore != null) {
            total = total.add(midtermScore.multiply(MIDTERM_WEIGHT));
        }
        if (finalScore != null) {
            total = total.add(finalScore.multiply(FINAL_WEIGHT));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算绩点（标准4.0制）
     * 90-100: 4.0
     * 85-89:  3.7
     * 82-84:  3.3
     * 78-81:  3.0
     * 75-77:  2.7
     * 72-74:  2.3
     * 68-71:  2.0
     * 64-67:  1.5
     * 60-63:  1.0
     * 0-59:   0
     */
    private BigDecimal calculateGradePoint(BigDecimal totalScore) {
        if (totalScore == null) {
            return BigDecimal.ZERO;
        }

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

    /**
     * 转换 ScoreDO 为 ScoreInfoDTO
     */
    private ScoreInfoDTO convertToScoreInfoDTO(ScoreDO scoreDO) {
        ScoreInfoDTO dto = new ScoreInfoDTO();
        dto.setScoreUuid(scoreDO.getScoreUuid());
        dto.setUsualScore(scoreDO.getUsualScore());
        dto.setMidtermScore(scoreDO.getMidtermScore());
        dto.setFinalScore(scoreDO.getFinalScore());
        dto.setTotalScore(scoreDO.getTotalScore());
        dto.setGradePoint(scoreDO.getGradePoint());
        dto.setRemark(scoreDO.getRemark());
        dto.setCreateTime(scoreDO.getCreateTime());
        dto.setUpdateTime(scoreDO.getUpdateTime());

        // 获取学生信息
        StudentDO student = studentDAO.getById(scoreDO.getStudentUuid());
        if (student != null) {
            StudentInfoDTO studentInfo = new StudentInfoDTO();
            studentInfo.setStudentUuid(student.getStudentUuid());
            studentInfo.setStudentId(student.getStudentId());
            studentInfo.setStudentName(student.getStudentName());

            // 获取班级信息
            ClassDO classDO = classDAO.getById(student.getClassUuid());
            if (classDO != null) {
                ClassInfoDTO classInfo = new ClassInfoDTO();
                classInfo.setClassUuid(classDO.getClassUuid());
                classInfo.setClassName(classDO.getClassName());

                // 获取专业信息
                MajorDO major = majorDAO.getById(classDO.getMajorUuid());
                if (major != null) {
                    MajorInfoDTO majorInfo = new MajorInfoDTO();
                    majorInfo.setMajorUuid(major.getMajorUuid());
                    majorInfo.setMajorNum(major.getMajorNum());
                    majorInfo.setMajorName(major.getMajorName());
                    majorInfo.setDepartmentUuid(major.getDepartmentUuid());

                    // 获取学院信息
                    DepartmentDO department = departmentDAO.getById(major.getDepartmentUuid());
                    if (department != null) {
                        majorInfo.setDepartmentName(department.getDepartmentName());
                    }

                    classInfo.setMajorInfo(majorInfo);
                }

                studentInfo.setClassInfo(classInfo);
            }

            dto.setStudentInfo(studentInfo);
        }

        // 获取教学班信息
        TeachingClassDO teachingClass = teachingClassDAO.getById(scoreDO.getTeachingClassUuid());
        if (teachingClass != null) {
            TeachingClassInfoDTO teachingClassInfo = new TeachingClassInfoDTO();
            teachingClassInfo.setTeachingClassUuid(teachingClass.getTeachingClassUuid());
            teachingClassInfo.setTeachingClassName(teachingClass.getTeachingClassName());
            teachingClassInfo.setWeeklySessions(teachingClass.getWeeklySessions());
            teachingClassInfo.setSectionsPerSession(teachingClass.getSectionsPerSession());

            // 获取课程信息
            CourseDO course = courseDAO.getById(teachingClass.getCourseUuid());
            if (course != null) {
                teachingClassInfo.setCourseName(course.getCourseName());
            }

            // 获取教师信息
            TeacherDO teacher = teacherDAO.getById(teachingClass.getTeacherUuid());
            if (teacher != null) {
                teachingClassInfo.setTeacherName(teacher.getTeacherName());
            }

            // 获取学期信息
            SemesterDO semester = semesterDAO.getById(teachingClass.getSemesterUuid());
            if (semester != null) {
                teachingClassInfo.setSemesterName(semester.getSemesterName());
            }

            dto.setTeachingClassInfo(teachingClassInfo);
        }

        // 获取学期信息
        SemesterDO semester = semesterDAO.getById(scoreDO.getSemesterUuid());
        if (semester != null) {
            SemesterInfoDTO semesterInfo = new SemesterInfoDTO();
            semesterInfo.setSemesterUuid(semester.getSemesterUuid());
            semesterInfo.setSemesterName(semester.getSemesterName());
            semesterInfo.setSemesterWeeks(semester.getSemesterWeeks());
            semesterInfo.setStartDate(semester.getStartDate());
            semesterInfo.setEndDate(semester.getEndDate());
            dto.setSemesterInfo(semesterInfo);
        }

        return dto;
    }

    /**
     * 构建分页DTO
     */
    private PageDTO<ScoreInfoDTO> buildPageDTO(int page, int size, int total, List<ScoreInfoDTO> records) {
        PageDTO<ScoreInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
