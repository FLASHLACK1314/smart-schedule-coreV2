package io.github.flashlack1314.smartschedulecorev2.model.vo;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 自动排课请求参数
 *
 * @author flash
 */
@Data
public class AutoScheduleVO {
    /**
     * 学期UUID（必填）
     */
    private String semesterUuid;

    /**
     * 待排课的教学班UUID列表（必填）
     */
    private List<String> teachingClassUuids;

    /**
     * 每周上课次数配置（可选，默认使用教学班表中的配置）
     * 格式：Map<teachingClassUuid, weeklySessions>
     */
    private Map<String, Integer> weeklySessionsConfig;

    /**
     * 可用教室UUID列表（可选，不传则查询所有）
     */
    private List<String> classroomUuids;

    /**
     * 算法参数配置（可选）
     */
    private Integer populationSize = 100;
    private Integer maxGenerations = 500;
    private Double crossoverRate = 0.8;
    private Double mutationRate = 0.2;
    private Integer eliteSize = 10;
}
