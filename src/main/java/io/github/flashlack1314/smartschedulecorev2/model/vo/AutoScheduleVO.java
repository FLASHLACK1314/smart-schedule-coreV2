package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 自动排课请求参数
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
public class AutoScheduleVO {

    // ==================== 基础参数（必填）====================

    /**
     * 学期UUID（必填）
     */
    private String semesterUuid;

    /**
     * 课程-行政班映射（必填）
     * key: 课程UUID
     * value: 该课程对应的行政班UUID列表（支持合班上课）
     *
     * 后端自动：
     * 1. 根据课程-行政班映射智能匹配或创建教学班
     * 2. 从 sc_course_qualification 表查询教师资格并自动分配教师
     * 3. 保证合班上课时，同一课程对应的多个行政班在同一时间上课
     */
    private Map<String, List<String>> courseClassMapping;

    /**
     * 指定教师映射（可选）
     * key: 课程UUID
     * value: 教师UUID（强制使用指定教师）
     *
     * 如果课程在此映射中指定了教师，将强制使用该教师（需验证教师资格）
     * 未指定的课程将根据 teacherSelectionStrategy 自动选择教师
     */
    private Map<String, String> teacherAssignment;

    /**
     * 教师选择策略（默认 "balanced"）
     * - "balanced": 均衡分配教师工作量（选择当前工作量最少的教师）
     * - "random": 随机选择有资格的教师
     * - "first": 选择第一个有资格的教师
     *
     * 仅对未在 teacherAssignment 中指定教师的课程生效
     */
    private String teacherSelectionStrategy = "balanced";

    // ==================== 资源范围配置（可选）====================

    /**
     * 可用教学楼UUID列表（可选，不传则查询所有）
     */
    private List<String> buildingUuids;

    // ==================== 排课模式配置（可选）====================

    /**
     * 排课模式：0-预览模式, 1-正式模式（默认0）
     * 预览模式：生成的排课记录 status=0，不影响正式课表，用于预览效果
     * 正式模式：生成的排课记录 status=1，直接写入正式课表
     */
    private Integer scheduleMode = 0;

    /**
     * 是否覆盖已有排课记录（默认false，即增量排课）
     * true - 删除该学期这些教学班的已有排课记录后重新排课
     * false - 保留已有排课记录，只排未排课的教学班
     */
    private Boolean overwrite = false;

    // ==================== 算法参数配置（可选）====================

    /**
     * 种群大小（默认100）
     */
    private Integer populationSize = 100;

    /**
     * 最大迭代代数（默认500）
     */
    private Integer maxGenerations = 500;

    /**
     * 交叉概率（默认0.8）
     */
    private Double crossoverRate = 0.8;

    /**
     * 变异概率（默认0.2）
     */
    private Double mutationRate = 0.2;

    /**
     * 精英保留数量（默认10）
     */
    private Integer eliteSize = 10;

    /**
     * 验证参数有效性
     *
     * @throws IllegalArgumentException 参数无效时抛出
     */
    public void validate() {
        // 学期UUID必填
        if (semesterUuid == null || semesterUuid.trim().isEmpty()) {
            throw new IllegalArgumentException("学期UUID不能为空");
        }

        // 课程-行政班映射必填
        if (courseClassMapping == null || courseClassMapping.isEmpty()) {
            throw new IllegalArgumentException("必须提供 courseClassMapping 课程-行政班映射");
        }
        validateCourseClassMapping();

        // 验证教师选择策略
        if (teacherSelectionStrategy != null && !isValidTeacherSelectionStrategy()) {
            throw new IllegalArgumentException("无效的教师选择策略，必须是 'balanced', 'random' 或 'first'");
        }

        // 验证算法参数
        if (populationSize != null && populationSize <= 0) {
            throw new IllegalArgumentException("种群大小必须大于0");
        }
        if (maxGenerations != null && maxGenerations <= 0) {
            throw new IllegalArgumentException("最大迭代代数必须大于0");
        }
        if (crossoverRate != null && (crossoverRate < 0 || crossoverRate > 1)) {
            throw new IllegalArgumentException("交叉概率必须在0-1之间");
        }
        if (mutationRate != null && (mutationRate < 0 || mutationRate > 1)) {
            throw new IllegalArgumentException("变异概率必须在0-1之间");
        }
        if (eliteSize != null && eliteSize < 0) {
            throw new IllegalArgumentException("精英保留数量不能小于0");
        }
    }

    /**
     * 验证课程-行政班映射
     */
    private void validateCourseClassMapping() {
        for (Map.Entry<String, List<String>> entry : courseClassMapping.entrySet()) {
            String courseUuid = entry.getKey();
            List<String> classUuids = entry.getValue();

            if (courseUuid == null || courseUuid.trim().isEmpty()) {
                throw new IllegalArgumentException("课程UUID不能为空");
            }

            if (classUuids == null || classUuids.isEmpty()) {
                throw new IllegalArgumentException("课程 " + courseUuid + " 必须关联至少一个行政班");
            }

            // 检查行政班UUID是否为空
            for (String classUuid : classUuids) {
                if (classUuid == null || classUuid.trim().isEmpty()) {
                    throw new IllegalArgumentException("行政班UUID不能为空");
                }
            }
        }
    }

    /**
     * 验证教师选择策略是否有效
     */
    private boolean isValidTeacherSelectionStrategy() {
        return "balanced".equals(teacherSelectionStrategy) ||
               "random".equals(teacherSelectionStrategy) ||
               "first".equals(teacherSelectionStrategy);
    }
}
