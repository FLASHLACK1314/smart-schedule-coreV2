package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.flashlack1314.smartschedulecorev2.dao.AcademicAdminDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.StudentDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.SystemAdminDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.TeacherDAO;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.UserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.AcademicAdminUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.StudentUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.SystemAdminUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.TeacherUserInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.AcademicAdminDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.StudentDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.SystemAdminDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.TeacherDO;
import io.github.flashlack1314.smartschedulecorev2.service.SystemAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统管理员服务实现
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemAdminImpl implements SystemAdminService {

    private final AcademicAdminDAO academicAdminDAO;
    private final StudentDAO studentDAO;
    private final TeacherDAO teacherDAO;
    private final SystemAdminDAO systemAdminDAO;

    @Override
    public PageDTO<AcademicAdminUserInfoDTO> getAcademicAdmin(int page, int size, String userName) {
        // 构建查询条件
        LambdaQueryWrapper<AcademicAdminDO> queryWrapper = new LambdaQueryWrapper<>();
        if (userName != null && !userName.trim().isEmpty()) {
            queryWrapper.like(AcademicAdminDO::getAcademicName, userName);
        }

        // 分页查询
        IPage<AcademicAdminDO> pageResult = academicAdminDAO.page(
                new Page<>(page, size),
                queryWrapper
        );

        // 转换为 DTO
        List<AcademicAdminUserInfoDTO> dtoList = pageResult.getRecords().stream()
                .map(this::convertToAcademicAdminDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<AcademicAdminUserInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal((int) pageResult.getTotal());
        pageDTO.setRecords(dtoList);

        return pageDTO;
    }

    @Override
    public PageDTO<UserInfoDTO> getUserInfoPageByType(int page, int size, String userType, String userName) {
        log.info("接收到的参数 - page: {}, size: {}, userType: {}, userName: {}", page, size, userType, userName);

        // 将字符串转换为枚举
        UserType type;
        try {
            type = UserType.fromString(userType);
            log.info("解析后的用户类型: {}", type);
        } catch (Exception e) {
            log.error("解析用户类型失败: {}, 错误: {}", userType, e.getMessage());
            // 如果解析失败，默认使用 STUDENT
            type = UserType.STUDENT;
        }

        // 根据用户类型查询数据
        PageDTO<UserInfoDTO> result = switch (type) {
            case STUDENT -> this.getStudentPage(page, size, userName);
            case TEACHER -> getTeacherPage(page, size, userName);
            case ACADEMIC_ADMIN -> getAcademicAdminPage(page, size, userName);
            case SYSTEM_ADMIN -> getSystemAdminPage(page, size, userName);
            default -> createEmptyPage(page, size);
        };

        log.info("查询结果 - 总数: {}", result.getTotal());
        return result;
    }

    /**
     * 获取学生分页数据
     */
    private @NotNull PageDTO<UserInfoDTO> getStudentPage(int page, int size, String userName) {
        LambdaQueryWrapper<StudentDO> queryWrapper = new LambdaQueryWrapper<>();
        if (userName != null && !userName.trim().isEmpty()) {
            queryWrapper.like(StudentDO::getStudentName, userName);
        }

        IPage<StudentDO> pageResult = studentDAO.page(new Page<>(page, size), queryWrapper);

        // 转换为 UserInfoDTO
        List<UserInfoDTO> userInfoList = pageResult.getRecords().stream()
                .map(studentDO -> {
                    UserInfoDTO userInfo = new UserInfoDTO();
                    StudentUserInfoDTO studentDTO = convertToStudentDTO(studentDO);
                    userInfo.setStudentInfo(studentDTO);
                    return userInfo;
                })
                .collect(Collectors.toList());

        return buildPageDTO(page, size, (int) pageResult.getTotal(), userInfoList);
    }

    /**
     * 获取教师分页数据
     */
    private PageDTO<UserInfoDTO> getTeacherPage(int page, int size, String userName) {
        LambdaQueryWrapper<TeacherDO> queryWrapper = new LambdaQueryWrapper<>();
        if (userName != null && !userName.trim().isEmpty()) {
            queryWrapper.like(TeacherDO::getTeacherName, userName);
        }

        IPage<TeacherDO> pageResult = teacherDAO.page(new Page<>(page, size), queryWrapper);

        // 转换为 UserInfoDTO
        List<UserInfoDTO> userInfoList = pageResult.getRecords().stream()
                .map(teacherDO -> {
                    UserInfoDTO userInfo = new UserInfoDTO();
                    TeacherUserInfoDTO teacherDTO = convertToTeacherDTO(teacherDO);
                    userInfo.setTeacherInfo(teacherDTO);
                    return userInfo;
                })
                .collect(Collectors.toList());

        return buildPageDTO(page, size, (int) pageResult.getTotal(), userInfoList);
    }

    /**
     * 获取教务管理员分页数据
     */
    private PageDTO<UserInfoDTO> getAcademicAdminPage(int page, int size, String userName) {
        LambdaQueryWrapper<AcademicAdminDO> queryWrapper = new LambdaQueryWrapper<>();
        if (userName != null && !userName.trim().isEmpty()) {
            queryWrapper.like(AcademicAdminDO::getAcademicName, userName);
        }

        IPage<AcademicAdminDO> pageResult = academicAdminDAO.page(new Page<>(page, size), queryWrapper);

        // 转换为 UserInfoDTO
        List<UserInfoDTO> userInfoList = pageResult.getRecords().stream()
                .map(academicAdminDO -> {
                    UserInfoDTO userInfo = new UserInfoDTO();
                    AcademicAdminUserInfoDTO academicAdminDTO = convertToAcademicAdminDTO(academicAdminDO);
                    userInfo.setAcademicAdminInfo(academicAdminDTO);
                    return userInfo;
                })
                .collect(Collectors.toList());

        return buildPageDTO(page, size, (int) pageResult.getTotal(), userInfoList);
    }

    /**
     * 获取系统管理员分页数据
     */
    private PageDTO<UserInfoDTO> getSystemAdminPage(int page, int size, String userName) {
        LambdaQueryWrapper<SystemAdminDO> queryWrapper = new LambdaQueryWrapper<>();
        if (userName != null && !userName.trim().isEmpty()) {
            queryWrapper.like(SystemAdminDO::getAdminUsername, userName);
        }

        IPage<SystemAdminDO> pageResult = systemAdminDAO.page(new Page<>(page, size), queryWrapper);

        // 转换为 UserInfoDTO
        List<UserInfoDTO> userInfoList = pageResult.getRecords().stream()
                .map(systemAdminDO -> {
                    UserInfoDTO userInfo = new UserInfoDTO();
                    SystemAdminUserInfoDTO systemAdminDTO = convertToSystemAdminDTO(systemAdminDO);
                    userInfo.setSystemAdminInfo(systemAdminDTO);
                    return userInfo;
                })
                .collect(Collectors.toList());

        return buildPageDTO(page, size, (int) pageResult.getTotal(), userInfoList);
    }

    /**
     * 构建分页DTO
     */
    private PageDTO<UserInfoDTO> buildPageDTO(int page, int size, int total, List<UserInfoDTO> records) {
        PageDTO<UserInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }

    /**
     * 创建空分页数据
     */
    private PageDTO<UserInfoDTO> createEmptyPage(int page, int size) {
        PageDTO<UserInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(0);
        pageDTO.setRecords(new ArrayList<>());
        return pageDTO;
    }

    /**
     * 转换 StudentDO 为 StudentUserInfoDTO
     */
    private StudentUserInfoDTO convertToStudentDTO(StudentDO DO) {
        StudentUserInfoDTO dto = new StudentUserInfoDTO();
        BeanUtils.copyProperties(DO, dto);
        return dto;
    }

    /**
     * 转换 TeacherDO 为 TeacherUserInfoDTO
     */
    private TeacherUserInfoDTO convertToTeacherDTO(TeacherDO DO) {
        TeacherUserInfoDTO dto = new TeacherUserInfoDTO();
        BeanUtils.copyProperties(DO, dto);
        return dto;
    }

    /**
     * 转换 AcademicAdminDO 为 AcademicAdminUserInfoDTO
     */
    private AcademicAdminUserInfoDTO convertToAcademicAdminDTO(AcademicAdminDO DO) {
        AcademicAdminUserInfoDTO dto = new AcademicAdminUserInfoDTO();
        BeanUtils.copyProperties(DO, dto);
        return dto;
    }

    /**
     * 转换 SystemAdminDO 为 SystemAdminUserInfoDTO
     */
    private SystemAdminUserInfoDTO convertToSystemAdminDTO(SystemAdminDO DO) {
        SystemAdminUserInfoDTO dto = new SystemAdminUserInfoDTO();
        BeanUtils.copyProperties(DO, dto);
        return dto;
    }
}
