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
     * 待排课的教学班UUID列表（必填）
     */
    private List<String> teachingClassUuids;

    // ==================== 资源范围配置（可选）====================

    /**
     * 可用教学楼UUID列表（可选，不传则查询所有）
     */
    private List<String> buildingUuids;

    /**
     * 可用教室类型UUID列表（可选，不传则使用所有类型）
     */
    private List<String> classroomTypeUuids;

    // ==================== 教学班配置覆盖（可选）====================

    /**
     * 每周上课次数配置（可选，默认使用教学班表中的配置）
     * 格式：Map<teachingClassUuid, weeklySessions>
     */
    private Map<String, Integer> weeklySessionsConfig;

    /**
     * 每次上课节次数配置（可选，默认使用教学班表中的配置）
     * 格式：Map<teachingClassUuid, sectionsPerSession>
     */
    private Map<String, Integer> sectionsPerSessionConfig;

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
}
