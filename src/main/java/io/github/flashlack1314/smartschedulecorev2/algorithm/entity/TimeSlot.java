package io.github.flashlack1314.smartschedulecorev2.algorithm.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 时间槽：全局唯一的时间索引
 * 固定2节连上的时间表示
 *
 * @author flash
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlot {
    /**
     * 星期几 (1-7)
     */
    private Integer dayOfWeek;

    /**
     * 起始节次 (固定为单数: 1,3,5,7,9,11)
     */
    private Integer sectionStart;

    /**
     * 结束节次 (固定为sectionStart+1)
     */
    private Integer sectionEnd;

    /**
     * 上课周次列表 (如 [1,2,3,4,5])
     */
    private List<Integer> weeks;

    /**
     * 单次学时 (固定为2节)
     */
    public static final int HOURS_PER_SESSION = 2;

    /**
     * 时间槽唯一标识
     */
    public String getUniqueId() {
        return dayOfWeek + "-" + sectionStart + "-" + sectionEnd + "-" + weeks.toString();
    }

    /**
     * 检查与另一个时间槽是否存在时间重叠
     *
     * @param other 另一个时间槽
     * @return 是否重叠
     */
    public boolean isOverlap(TimeSlot other) {
        if (!this.dayOfWeek.equals(other.dayOfWeek)) {
            return false;
        }
        // 检查节次重叠
        boolean sectionOverlap = !(this.sectionEnd < other.sectionStart || this.sectionStart > other.sectionEnd);
        if (!sectionOverlap) {
            return false;
        }
        // 检查周次重叠
        return this.weeks.stream().anyMatch(other.weeks::contains);
    }

    /**
     * 计算本时间槽的总学时 (周次数 × 2)
     */
    public int getTotalHours() {
        return weeks.size() * HOURS_PER_SESSION;
    }
}
