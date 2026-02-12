package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.PasswordUtil;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.ClassDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.DepartmentDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.MajorDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.StudentDAO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ClassInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.MajorInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.StudentInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.DepartmentDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.MajorDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.StudentDO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddStudentVO;
import io.github.flashlack1314.smartschedulecorev2.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学生服务实现类
 *
 * @author flash
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {
    private final StudentDAO studentDAO;
    private final ClassDAO classDAO;
    private final MajorDAO majorDAO;
    private final DepartmentDAO departmentDAO;

    @Override
    public void addStudent(AddStudentVO getData) {
        log.info("添加学生 - 学号: {}, 姓名: {}, 班级UUID: {}",
                getData.getStudentId(), getData.getStudentName(), getData.getClassUuid());

        // 检查班级是否存在
        ClassDO classDO = classDAO.getById(getData.getClassUuid());
        if (classDO == null) {
            throw new BusinessException("班级不存在: " + getData.getClassUuid(), ErrorCode.OPERATION_FAILED);
        }

        // 检查学号是否已存在
        if (studentDAO.existsByStudentId(getData.getStudentId())) {
            throw new BusinessException("学号已存在: " + getData.getStudentId(), ErrorCode.OPERATION_FAILED);
        }

        // 创建学生对象
        StudentDO studentDO = new StudentDO();
        studentDO.setStudentUuid(UuidUtil.generateUuidNoDash());
        studentDO.setStudentId(getData.getStudentId());
        studentDO.setStudentName(getData.getStudentName());
        studentDO.setClassUuid(getData.getClassUuid());
        studentDO.setStudentPassword(PasswordUtil.encrypt(getData.getStudentPassword()));

        // 保存到数据库
        boolean saved = studentDAO.save(studentDO);

        if (!saved) {
            throw new BusinessException("保存学生失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("学生添加成功 - UUID: {}, 学号: {}, 姓名: {}",
                studentDO.getStudentUuid(), getData.getStudentId(), getData.getStudentName());
    }

    @Override
    public PageDTO<StudentInfoDTO> getStudentPage(int page, int size, String studentName, String studentId,
                                                    String classUuid, String majorUuid, String departmentUuid) {
        log.info("查询学生分页信息 - page: {}, size: {}, studentName: {}, studentId: {}, classUuid: {}, majorUuid: {}, departmentUuid: {}",
                page, size, studentName, studentId, classUuid, majorUuid, departmentUuid);

        // 如果指定了专业或学院，先查询符合条件的班级UUID列表
        List<String> classUuidList = null;
        if (!StringUtils.hasText(classUuid)) {
            if (StringUtils.hasText(majorUuid) || StringUtils.hasText(departmentUuid)) {
                classUuidList = getClassUuidListByMajorOrDepartment(majorUuid, departmentUuid);
            }
        }

        // 调用DAO层进行分页查询
        IPage<StudentDO> pageResult = studentDAO.getStudentPage(page, size, studentName, studentId, classUuid, classUuidList);

        // 转换为 StudentInfoDTO
        List<StudentInfoDTO> studentInfoList = pageResult.getRecords().stream()
                .map(this::convertToStudentInfoDTO)
                .collect(Collectors.toList());

        // 构建返回结果
        PageDTO<StudentInfoDTO> result = buildPageDTO(page, size, (int) pageResult.getTotal(), studentInfoList);

        return result;
    }

    @Override
    public StudentInfoDTO getStudent(String studentUuid) {
        log.info("获取学生信息 - UUID: {}", studentUuid);

        StudentDO student = studentDAO.getById(studentUuid);
        if (student == null) {
            throw new BusinessException("学生不存在: " + studentUuid, ErrorCode.OPERATION_FAILED);
        }

        return convertToStudentInfoDTO(student);
    }

    @Override
    public void updateStudent(String studentUuid, String studentId, String studentName, String classUuid, String studentPassword) {
        log.info("更新学生信息 - UUID: {}, 学号: {}, 姓名: {}", studentUuid, studentId, studentName);

        // 查询学生是否存在
        StudentDO student = studentDAO.getById(studentUuid);
        if (student == null) {
            throw new BusinessException("学生不存在: " + studentUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查班级是否存在
        ClassDO classDO = classDAO.getById(classUuid);
        if (classDO == null) {
            throw new BusinessException("班级不存在: " + classUuid, ErrorCode.OPERATION_FAILED);
        }

        // 检查学号是否被其他学生使用
        if (studentDAO.existsByStudentIdExcludeUuid(studentId, studentUuid)) {
            throw new BusinessException("学号已被其他学生使用: " + studentId, ErrorCode.OPERATION_FAILED);
        }

        // 更新学生信息
        student.setStudentId(studentId);
        student.setStudentName(studentName);
        student.setClassUuid(classUuid);

        // 如果提供了新密码，则更新密码
        if (StringUtils.hasText(studentPassword)) {
            student.setStudentPassword(PasswordUtil.encrypt(studentPassword));
        }

        // 保存更新
        boolean updated = studentDAO.updateById(student);
        if (!updated) {
            throw new BusinessException("更新学生失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("学生更新成功 - UUID: {}, 学号: {}, 姓名: {}", studentUuid, studentId, studentName);
    }

    @Override
    public void deleteStudent(String studentUuid) {
        log.info("删除学生 - UUID: {}", studentUuid);

        // 查询学生是否存在
        StudentDO student = studentDAO.getById(studentUuid);
        if (student == null) {
            throw new BusinessException("学生不存在: " + studentUuid, ErrorCode.OPERATION_FAILED);
        }

        // TODO: 检查学生是否被排课记录引用
        // 这部分可以后续完善，等排课功能实现后再补充

        // 执行删除
        boolean deleted = studentDAO.removeById(studentUuid);
        if (!deleted) {
            throw new BusinessException("删除学生失败", ErrorCode.OPERATION_FAILED);
        }

        log.info("学生删除成功 - UUID: {}, 学号: {}, 姓名: {}",
                studentUuid, student.getStudentId(), student.getStudentName());
    }

    /**
     * 根据专业或学院获取班级UUID列表
     */
    private List<String> getClassUuidListByMajorOrDepartment(String majorUuid, String departmentUuid) {
        LambdaQueryWrapper<ClassDO> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(majorUuid)) {
            queryWrapper.eq(ClassDO::getMajorUuid, majorUuid);
        } else if (StringUtils.hasText(departmentUuid)) {
            // 先查询该学院下的所有专业UUID
            List<String> majorUuids = majorDAO.listUuidByDepartmentUuid(departmentUuid);
            if (majorUuids.isEmpty()) {
                return Collections.emptyList();
            }
            queryWrapper.in(ClassDO::getMajorUuid, majorUuids);
        }

        return classDAO.list(queryWrapper).stream()
                .map(ClassDO::getClassUuid)
                .collect(Collectors.toList());
    }

    /**
     * 转换 StudentDO 为 StudentInfoDTO
     */
    private StudentInfoDTO convertToStudentInfoDTO(StudentDO studentDO) {
        StudentInfoDTO dto = new StudentInfoDTO();
        dto.setStudentUuid(studentDO.getStudentUuid());
        dto.setStudentId(studentDO.getStudentId());
        dto.setStudentName(studentDO.getStudentName());

        // 获取班级信息
        ClassDO classDO = classDAO.getById(studentDO.getClassUuid());
        if (classDO != null) {
            ClassInfoDTO classInfo = new ClassInfoDTO();
            classInfo.setClassUuid(classDO.getClassUuid());
            classInfo.setClassName(classDO.getClassName());

            // 获取专业信息
            MajorDO major = majorDAO.getById(classDO.getMajorUuid());
            if (major != null) {
                MajorInfoDTO majorInfo = new MajorInfoDTO();
                majorInfo.setMajorUuid(major.getMajorUuid());
                majorInfo.setMajorNum(major.getMajorNum());
                majorInfo.setMajorName(major.getMajorName());
                majorInfo.setDepartmentUuid(major.getDepartmentUuid());

                // 获取学院信息
                DepartmentDO department = departmentDAO.getById(major.getDepartmentUuid());
                if (department != null) {
                    majorInfo.setDepartmentName(department.getDepartmentName());
                }

                classInfo.setMajorInfo(majorInfo);
            }

            dto.setClassInfo(classInfo);
        }

        return dto;
    }

    /**
     * 构建分页DTO
     */
    private PageDTO<StudentInfoDTO> buildPageDTO(int page, int size, int total, List<StudentInfoDTO> records) {
        PageDTO<StudentInfoDTO> pageDTO = new PageDTO<>();
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotal(total);
        pageDTO.setRecords(records);
        return pageDTO;
    }
}
