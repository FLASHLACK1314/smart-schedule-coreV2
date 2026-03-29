package io.github.flashlack1314.smartschedulecorev2.controller;

import com.xlf.utility.BaseResponse;
import com.xlf.utility.ResultUtil;
import io.github.flashlack1314.smartschedulecorev2.annotation.RequireRole;
import io.github.flashlack1314.smartschedulecorev2.enums.UserType;
import io.github.flashlack1314.smartschedulecorev2.model.dto.PageDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ScoreInfoDTO;
import io.github.flashlack1314.smartschedulecorev2.model.dto.base.ScoreStatisticsDTO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.AddScoreVO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.BatchAddScoreVO;
import io.github.flashlack1314.smartschedulecorev2.model.vo.UpdateScoreVO;
import io.github.flashlack1314.smartschedulecorev2.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 成绩控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/score")
public class ScoreController {
    private final ScoreService scoreService;

    /**
     * 添加成绩
     *
     * @param token   Token
     * @param getData 添加成绩信息
     * @return 添加结果
     */
    @PostMapping("/add")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER})
    public ResponseEntity<BaseResponse<String>> addScore(
            @RequestHeader("Authorization") String token,
            @RequestBody AddScoreVO getData
    ) {
        String scoreUuid = scoreService.addScore(getData);
        return ResultUtil.success("添加成绩成功", scoreUuid);
    }

    /**
     * 批量添加成绩
     *
     * @param token   Token
     * @param getData 批量添加成绩信息
     * @return 添加结果
     */
    @PostMapping("/batchAdd")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER})
    public ResponseEntity<BaseResponse<Integer>> batchAddScore(
            @RequestHeader("Authorization") String token,
            @RequestBody BatchAddScoreVO getData
    ) {
        int successCount = scoreService.batchAddScore(getData);
        return ResultUtil.success("批量添加成绩成功", successCount);
    }

    /**
     * 更新成绩
     *
     * @param token   Token
     * @param getData 更新成绩信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER})
    public ResponseEntity<BaseResponse<Void>> updateScore(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateScoreVO getData
    ) {
        scoreService.updateScore(getData);
        return ResultUtil.success("更新成绩成功");
    }

    /**
     * 删除成绩
     *
     * @param token     Token
     * @param scoreUuid 成绩UUID
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN})
    public ResponseEntity<BaseResponse<Void>> deleteScore(
            @RequestHeader("Authorization") String token,
            @RequestParam("score_uuid") String scoreUuid
    ) {
        scoreService.deleteScore(scoreUuid);
        return ResultUtil.success("删除成绩成功");
    }

    /**
     * 获取成绩信息
     *
     * @param token     Token
     * @param scoreUuid 成绩UUID
     * @return 成绩信息
     */
    @GetMapping("/get")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<ScoreInfoDTO>> getScore(
            @RequestHeader("Authorization") String token,
            @RequestParam("score_uuid") String scoreUuid
    ) {
        ScoreInfoDTO result = scoreService.getScore(scoreUuid);
        return ResultUtil.success("获取成绩信息成功", result);
    }

    /**
     * 分页查询成绩
     *
     * @param token             Token
     * @param page              页码
     * @param size              每页数量
     * @param studentUuid       学生UUID（可选）
     * @param teachingClassUuid 教学班UUID（可选）
     * @param semesterUuid      学期UUID（可选）
     * @return 分页结果
     */
    @GetMapping("/getPage")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER})
    public ResponseEntity<BaseResponse<PageDTO<ScoreInfoDTO>>> getScorePage(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(value = "student_uuid", required = false) String studentUuid,
            @RequestParam(value = "teaching_class_uuid", required = false) String teachingClassUuid,
            @RequestParam(value = "semester_uuid", required = false) String semesterUuid
    ) {
        PageDTO<ScoreInfoDTO> result = scoreService.getScorePage(page, size, studentUuid, teachingClassUuid, semesterUuid);
        return ResultUtil.success("获取成绩分页信息成功", result);
    }

    /**
     * 查询学生成绩列表
     *
     * @param token        Token
     * @param studentUuid  学生UUID
     * @param semesterUuid 学期UUID（可选）
     * @return 成绩列表
     */
    @GetMapping("/student/list")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<List<ScoreInfoDTO>>> getStudentScores(
            @RequestHeader("Authorization") String token,
            @RequestParam("student_uuid") String studentUuid,
            @RequestParam(value = "semester_uuid", required = false) String semesterUuid
    ) {
        List<ScoreInfoDTO> result = scoreService.getStudentScores(studentUuid, semesterUuid);
        return ResultUtil.success("获取学生成绩列表成功", result);
    }

    /**
     * 查询教学班成绩列表
     *
     * @param token             Token
     * @param teachingClassUuid 教学班UUID
     * @return 成绩列表
     */
    @GetMapping("/teachingClass/list")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER})
    public ResponseEntity<BaseResponse<List<ScoreInfoDTO>>> getTeachingClassScores(
            @RequestHeader("Authorization") String token,
            @RequestParam("teaching_class_uuid") String teachingClassUuid
    ) {
        List<ScoreInfoDTO> result = scoreService.getTeachingClassScores(teachingClassUuid);
        return ResultUtil.success("获取教学班成绩列表成功", result);
    }

    /**
     * 获取成绩统计信息
     *
     * @param token             Token
     * @param teachingClassUuid 教学班UUID
     * @return 统计信息
     */
    @GetMapping("/statistics")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER})
    public ResponseEntity<BaseResponse<ScoreStatisticsDTO>> getScoreStatistics(
            @RequestHeader("Authorization") String token,
            @RequestParam("teaching_class_uuid") String teachingClassUuid
    ) {
        ScoreStatisticsDTO result = scoreService.getScoreStatistics(teachingClassUuid);
        return ResultUtil.success("获取成绩统计信息成功", result);
    }

    /**
     * 计算学生绩点
     *
     * @param token        Token
     * @param studentUuid  学生UUID
     * @param semesterUuid 学期UUID（可选）
     * @return 绩点
     */
    @GetMapping("/gpa")
    @RequireRole({UserType.SYSTEM_ADMIN, UserType.ACADEMIC_ADMIN, UserType.TEACHER, UserType.STUDENT})
    public ResponseEntity<BaseResponse<BigDecimal>> calculateGPA(
            @RequestHeader("Authorization") String token,
            @RequestParam("student_uuid") String studentUuid,
            @RequestParam(value = "semester_uuid", required = false) String semesterUuid
    ) {
        BigDecimal gpa = scoreService.calculateGPA(studentUuid, semesterUuid);
        return ResultUtil.success("计算绩点成功", gpa);
    }
}
