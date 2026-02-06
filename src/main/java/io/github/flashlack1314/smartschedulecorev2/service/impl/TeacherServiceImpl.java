package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.PasswordUtil;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.DepartmentDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.TeacherDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.DepartmentInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeacherInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.DepartmentDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeacherDO;
import io.github.flashlack1314.smartschedulecorev2.service.TeacherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 教师服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {
    private final TeacherDAO teacherDAO;
    private final DepartmentDAO departmentDAO;

    @Override
    public void addTeacher(String teacherNum, String teacherName, String title, String departmentUuid,
                           String teacherPassword, Integer maxHoursPerWeek, String likeTime, Boolean isActive) {
        log.info("添加教师 - 工号: {}, 姓名: {}, 学院UUID: {}", teacherNum, teacherName, departmentUuid);

        // 检查学院是否存在
        DepartmentDO department = departmentDAO.getById(departmentUuid);
        if (department == null) {
            throw new BusinessException("学院不存在: " + departmentUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查教师工号是否已存在
        if (teacherDAO.existsByTeacherNum(teacherNum)) {
            throw new BusinessException("教师工号已存在: " + teacherNum, ErrorCode.OPERATION_FAILED);
        }

        // 创建教师对象
        TeacherDO teacherDO = new TeacherDO();
        teacherDO.setTeacherUuid(UuidUtil.generateUuidNoDash());
        teacherDO.setTeacherNum(teacherNum);
        teacherDO.setTeacherName(teacherName);
        teacherDO.setTitle(title);
        teacherDO.setDepartmentUuid(departmentUuid);
        teacherDO.setTeacherPassword(PasswordUtil.encrypt(teacherPassword));
        teacherDO.setMaxHoursPerWeek(maxHoursPerWeek);
        teacherDO.setLikeTime(likeTime);
        teacherDO.setIsActive(isActive != null ? isActive : true);

        // 保存到数据库
        boolean saved = teacherDAO.save(teacherDO);

        if (!saved) {
            throw new BusinessException("保存教师失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教师添加成功 - UUID: {}, 工号: {}, 姓名: {}",
                teacherDO.getTeacherUuid(), teacherNum, teacherName);
    }

    @Override
    public PageDTO<TeacherInfoDTO> getTeacherPage(int page, int size, String teacherName, String teacherNum, String departmentUuid) {
        log.info("查询教师分页信息 - page: {}, size: {}, teacherName: {}, teacherNum: {}, departmentUuid: {}",
                page, size, teacherName, teacherNum, departmentUuid);

        // 调用DAO层进行分页查询
        IPage<TeacherDO> pageResult = teacherDAO.getTeacherPage(page, size, teacherName, teacherNum, departmentUuid);

        // 转换为 TeacherInfoDTO
        List<TeacherInfoDTO> teacherInfoList = pageResult.getRecords().stream()
                .map(this::convertToTeacherInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<TeacherInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), teacherInfoList);

        log.info("查询教师分页信息成功 - 总数: {}", result.getTotal());

        return result;
    }

    @Override
    public void updateTeacher(String teacherUuid, String teacherNum, String teacherName, String title,
                              String departmentUuid, String teacherPassword, Integer maxHoursPerWeek,
                              String likeTime, Boolean isActive) {
        log.info("更新教师信息 - UUID: {}, 工号: {}, 姓名: {}",
                teacherUuid, teacherNum, teacherName);

        // 查询教师是否存在
        TeacherDO teacher = teacherDAO.getById(teacherUuid);
        if (teacher == null) {
            throw new BusinessException("教师不存在: " + teacherUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查学院是否存在
        DepartmentDO department = departmentDAO.getById(departmentUuid);
        if (department == null) {
            throw new BusinessException("学院不存在: " + departmentUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查教师工号是否被其他教师使用
        if (teacherDAO.existsByTeacherNumExcludeUuid(teacherNum, teacherUuid)) {
            throw new BusinessException("教师工号已被其他教师使用: " + teacherNum, ErrorCode.OPERATION_FAILED);
        }

        // 更新教师信息
        teacher.setTeacherNum(teacherNum);
        teacher.setTeacherName(teacherName);
        teacher.setTitle(title);
        teacher.setDepartmentUuid(departmentUuid);
        teacher.setMaxHoursPerWeek(maxHoursPerWeek);
        teacher.setLikeTime(likeTime);
        teacher.setIsActive(isActive);

        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(teacherPassword)) {
            teacher.setTeacherPassword(PasswordUtil.encrypt(teacherPassword));
        }

        // 保存更新
        boolean updated = teacherDAO.updateById(teacher);

        if (!updated) {
            throw new BusinessException("更新教师失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教师更新成功 - UUID: {}, 工号: {}, 姓名: {}", teacherUuid, teacherNum, teacherName);
    }

    @Override
    public TeacherInfoDTO getTeacher(String teacherUuid) {
        log.info("获取教师信息 - UUID: {}", teacherUuid);

        // 查询教师
        TeacherDO teacher = teacherDAO.getById(teacherUuid);

        if (teacher == null) {
            throw new BusinessException("教师不存在: " + teacherUuid, ErrorCode.OPERATION_FAILED);
        }

        // 转换为 DTO 并返回
        TeacherInfoDTO teacherInfoDTO = convertToTeacherInfoDTO(teacher);

        log.info("获取教师信息成功 - UUID: {}, 工号: {}, 姓名: {}",
                teacherUuid, teacher.getTeacherNum(), teacher.getTeacherName());

        return teacherInfoDTO;
    }

    @Override
    public void deleteTeacher(String teacherUuid) {
        log.info("删除教师 - UUID: {}", teacherUuid);

        // 查询教师是否存在
        TeacherDO teacher = teacherDAO.getById(teacherUuid);
        if (teacher == null) {
            throw new BusinessException("教师不存在: " + teacherUuid, ErrorCode.OPERATION_FAILED);
        }

        // TODO: 检查教师是否被 teaching_class、course_qualification、schedule 引用
        // 这部分可以后续完善

        // 执行删除
        boolean deleted = teacherDAO.removeById(teacherUuid);

        if (!deleted) {
            throw new BusinessException("删除教师失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("教师删除成功 - UUID: {}, 工号: {}, 姓名: {}",
                teacherUuid, teacher.getTeacherNum(), teacher.getTeacherName());
    }

    /**
     * 转换 TeacherDO 为 TeacherInfoDTO
     *
     * @param teacherDO 教师实体
     * @return 教师信息DTO
     */
    private TeacherInfoDTO convertToTeacherInfoDTO(TeacherDO teacherDO) {
        TeacherInfoDTO dto = new TeacherInfoDTO();
        dto.setTeacherUuid(teacherDO.getTeacherUuid());
        dto.setTeacherNum(teacherDO.getTeacherNum());
        dto.setTeacherName(teacherDO.getTeacherName());
        dto.setTitle(teacherDO.getTitle());
        dto.setMaxHoursPerWeek(teacherDO.getMaxHoursPerWeek());
        dto.setLikeTime(teacherDO.getLikeTime());
        dto.setIsActive(teacherDO.getIsActive());

        // 获取学院信息
        DepartmentDO department = departmentDAO.getById(teacherDO.getDepartmentUuid());
        if (department != null) {
            DepartmentInfoDTO departmentInfo = new DepartmentInfoDTO();
            departmentInfo.setDepartmentUuid(department.getDepartmentUuid());
            departmentInfo.setDepartmentName(department.getDepartmentName());
            dto.setDepartmentInfo(departmentInfo);
        }

        return dto;
    }

    /**
     * 构建分页DTO
     *
     * @param page    页码
     * @param size    每页数量
     * @param total   总数
     * @param records 记录列表
     * @return 分页DTO
     */
    private PageDTO<TeacherInfoDTO> buildPageDTO(int page, int size, int total, List<TeacherInfoDTO> records) {
        PageDTO<TeacherInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
