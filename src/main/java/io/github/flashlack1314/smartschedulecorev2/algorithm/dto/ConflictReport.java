package io.github.flashlack1314.smartschedulecorev2.algorithm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 冲突报告
 *
 * @author flash
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConflictReport {
    /**
     * 硬约束冲突列表
     */
    private List<Conflict> hardConflicts = new ArrayList<>();

    /**
     * 软约束冲突列表
     */
    private List<Conflict> softConflicts = new ArrayList<>();

    /**
     * 添加硬约束冲突
     */
    public void addHardConflict(Conflict conflict) {
        hardConflicts.add(conflict);
    }

    /**
     * 添加软约束冲突
     */
    public void addSoftConflict(Conflict conflict) {
        softConflicts.add(conflict);
    }

    /**
     * 获取硬约束冲突数量
     */
    public int getHardConflictCount() {
        return hardConflicts != null ? hardConflicts.size() : 0;
    }

    /**
     * 获取软约束冲突数量
     */
    public int getSoftConflictCount() {
        return softConflicts != null ? softConflicts.size() : 0;
    }

    /**
     * 是否有冲突
     */
    public boolean hasConflicts() {
        return getHardConflictCount() > 0 || getSoftConflictCount() > 0;
    }
}
