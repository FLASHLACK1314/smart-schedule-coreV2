package io.github.flashlack1314.smartschedulecorev2.service;

import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeachingClassClassInfoDTO;

/**
 * 教学班-行政班关联服务接口
 *
 * @author flash
 */
public interface TeachingClassClassService {
    /**
     * 添加行政班到教学班
     *
     * @param teachingClassUuid 教学班UUID
     * @param classUuid         行政班级UUID
     */
    void addTeachingClassClass(String teachingClassUuid, String classUuid);

    /**
     * 分页查询教学班的行政班列表
     *
     * @param page              页码
     * @param size              每页数量
     * @param teachingClassUuid 教学班UUID（可选过滤）
     * @param classUuid         行政班级UUID（可选过滤）
     * @return 分页结果
     */
    PageDTO<TeachingClassClassInfoDTO> getTeachingClassClassPage(int page, int size,
            String teachingClassUuid, String classUuid);

    /**
     * 移除教学班的行政班
     *
     * @param teachingClassClassUuid 关联关系UUID
     */
    void deleteTeachingClassClass(String teachingClassClassUuid);
}
