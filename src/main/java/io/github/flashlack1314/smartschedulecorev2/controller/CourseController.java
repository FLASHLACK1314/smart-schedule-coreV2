package io.github.flashlack1314.smartschedulecorev2.controller;

import io.github.flashlack1314.smartschedulecorev2.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 课程控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/course")
public class CourseController {
    private final CourseService courseService;

}
