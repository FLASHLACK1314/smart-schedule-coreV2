package io.github.flashlack1314.smartschedulecorev2.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author flash
 */
@Data
@Accessors(chain = true)
public class PageDTO<T> {
    /**
     * 总数据
     */
    private int total;
    /**
     * 当前页码
     */
    private int page;
    /**
     * 每页数据
     */
    private int size;
    private List<T> records;
}
