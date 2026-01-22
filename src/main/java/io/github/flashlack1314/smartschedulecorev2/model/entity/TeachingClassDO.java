package io.github.flashlack1314.smartschedulecorev2.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 教学班DO
 *
 * @author flash
 */
@Data
@Accessors(chain = true)
@TableName("sc_teaching_class")
public class TeachingClassDO {

    /**
     * 教学班UUID
     */
    @TableId
    private String teachingClassUuid;

    /**
     * 课程UUID
     */
    @TableField("course_uuid")
    private String courseUuid;

    /**
     * 教师UUID
     */
    @TableField("teacher_uuid")
    private String teacherUuid;

    /**
     * 学期UUID
     */
    @TableField("semester_uuid")
    private String semesterUuid;

    /**
     * 关联的行政班级UUID列表 (JSONB存储，如 ["class_uuid1", "class_uuid2"])
     */
    @TableField(value = "class_uuids")
    private String classUuids;

    /**
     * 教学班名称
     */
    @TableField("teaching_class_name")
    private String teachingClassName;
}
