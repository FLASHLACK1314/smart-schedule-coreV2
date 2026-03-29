package io.github.flashlack1314.smartschedulecorev2.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.flashlack1314.smartschedulecorev2.mapper.ScoreMapper;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ScoreDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 成绩DAO
 *
 * @author flash
 */
@Slf4j
@Repository
public class ScoreDAO extends ServiceImpl<ScoreMapper, ScoreDO>
        implements IService<ScoreDO> {

    /**
     * 根据学生UUID和教学班UUID查询成绩
     *
     * @param studentUuid      学生UUID
     * @param teachingClassUuid 教学班UUID
     * @return 成绩实体，如果不存在则返回null
     */
    public ScoreDO getByStudentAndTeachingClass(String studentUuid, String teachingClassUuid) {
        LambdaQueryWrapper<ScoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreDO::getStudentUuid, studentUuid)
                .eq(ScoreDO::getTeachingClassUuid, teachingClassUuid);
        return this.getOne(queryWrapper);
    }

    /**
     * 检查成绩是否已存在
     *
     * @param studentUuid      学生UUID
     * @param teachingClassUuid 教学班UUID
     * @return 如果已存在返回true，否则返回false
     */
    public boolean existsByStudentAndTeachingClass(String studentUuid, String teachingClassUuid) {
        return getByStudentAndTeachingClass(studentUuid, teachingClassUuid) != null;
    }

    /**
     * 根据学生UUID查询成绩列表
     *
     * @param studentUuid 学生UUID
     * @return 成绩列表
     */
    public List<ScoreDO> listByStudentUuid(String studentUuid) {
        LambdaQueryWrapper<ScoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreDO::getStudentUuid, studentUuid)
                .orderByDesc(ScoreDO::getCreateTime);
        return this.list(queryWrapper);
    }

    /**
     * 根据教学班UUID查询成绩列表
     *
     * @param teachingClassUuid 教学班UUID
     * @return 成绩列表
     */
    public List<ScoreDO> listByTeachingClassUuid(String teachingClassUuid) {
        LambdaQueryWrapper<ScoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreDO::getTeachingClassUuid, teachingClassUuid)
                .orderByDesc(ScoreDO::getCreateTime);
        return this.list(queryWrapper);
    }

    /**
     * 根据学期UUID查询成绩列表
     *
     * @param semesterUuid 学期UUID
     * @return 成绩列表
     */
    public List<ScoreDO> listBySemesterUuid(String semesterUuid) {
        LambdaQueryWrapper<ScoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreDO::getSemesterUuid, semesterUuid)
                .orderByDesc(ScoreDO::getCreateTime);
        return this.list(queryWrapper);
    }

    /**
     * 分页查询成绩（支持多条件筛选）
     *
     * @param page             页码
     * @param size             每页数量
     * @param studentUuid      学生UUID（可选）
     * @param teachingClassUuid 教学班UUID（可选）
     * @param semesterUuid     学期UUID（可选）
     * @return 分页结果
     */
    public IPage<ScoreDO> getScorePage(int page, int size, String studentUuid,
                                        String teachingClassUuid, String semesterUuid) {
        Page<ScoreDO> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ScoreDO> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(studentUuid)) {
            queryWrapper.eq(ScoreDO::getStudentUuid, studentUuid);
        }
        if (StringUtils.hasText(teachingClassUuid)) {
            queryWrapper.eq(ScoreDO::getTeachingClassUuid, teachingClassUuid);
        }
        if (StringUtils.hasText(semesterUuid)) {
            queryWrapper.eq(ScoreDO::getSemesterUuid, semesterUuid);
        }

        queryWrapper.orderByDesc(ScoreDO::getCreateTime);
        return this.page(pageParam, queryWrapper);
    }

    /**
     * 更新成绩信息
     *
     * @param scoreUuid    成绩UUID
     * @param usualScore   平时成绩
     * @param midtermScore 期中成绩
     * @param finalScore   期末成绩
     * @param totalScore   总评成绩
     * @param gradePoint   绩点
     * @param remark       备注
     * @return 是否更新成功
     */
    public boolean updateScore(String scoreUuid, BigDecimal usualScore, BigDecimal midtermScore,
                               BigDecimal finalScore, BigDecimal totalScore, BigDecimal gradePoint, String remark) {
        LambdaUpdateWrapper<ScoreDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ScoreDO::getScoreUuid, scoreUuid)
                .set(ScoreDO::getUsualScore, usualScore)
                .set(ScoreDO::getMidtermScore, midtermScore)
                .set(ScoreDO::getFinalScore, finalScore)
                .set(ScoreDO::getTotalScore, totalScore)
                .set(ScoreDO::getGradePoint, gradePoint)
                .set(ScoreDO::getRemark, remark)
                .set(ScoreDO::getUpdateTime, LocalDateTime.now());
        return this.update(updateWrapper);
    }

    /**
     * 统计教学班的成绩数量
     *
     * @param teachingClassUuid 教学班UUID
     * @return 成绩数量
     */
    public long countByTeachingClassUuid(String teachingClassUuid) {
        LambdaQueryWrapper<ScoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreDO::getTeachingClassUuid, teachingClassUuid);
        return this.count(queryWrapper);
    }

    /**
     * 检查成绩是否存在（排除指定UUID）
     *
     * @param studentUuid      学生UUID
     * @param teachingClassUuid 教学班UUID
     * @param excludeUuid      要排除的成绩UUID
     * @return 如果存在返回true，否则返回false
     */
    public boolean existsByStudentAndTeachingClassExcludeUuid(String studentUuid,
                                                               String teachingClassUuid, String excludeUuid) {
        LambdaQueryWrapper<ScoreDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScoreDO::getStudentUuid, studentUuid)
                .eq(ScoreDO::getTeachingClassUuid, teachingClassUuid)
                .ne(ScoreDO::getScoreUuid, excludeUuid);
        return this.count(queryWrapper) > 0;
    }
}
