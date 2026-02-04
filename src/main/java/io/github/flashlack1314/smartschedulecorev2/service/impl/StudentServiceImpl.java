package io.github.flashlack1314.smartschedulecorev2.service.impl;

import com.xlf.utility.ErrorCode;
import com.xlf.utility.exception.BusinessException;
import com.xlf.utility.util.PasswordUtil;
import com.xlf.utility.util.UuidUtil;
import io.github.flashlack1314.smartschedulecorev2.dao.ClassDAO;
import io.github.flashlack1314.smartschedulecorev2.dao.StudentDAO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.ClassDO;
import io.github.flashlack1314.smartschedulecorev2.model.entity.StudentDO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddStudentVO;
import io.github.flashlack1314.smartschedulecorev2.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
