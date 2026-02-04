package io.github.flashlack1314.smartschedulecorev2.controller;

import io.github.flashlack1314.smartschedulecorev2.service.TeachingClassService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 教学班控制器
 *
 * @author flash
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/teachingClass")
public class TeachingClassController {
    private final TeachingClassService teachingClassService;

}
