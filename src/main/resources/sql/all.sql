-- ================================================================
-- Smart Schedule Core V2 - 智能排课系统数据库脚本
-- 数据库: PostgreSQL
-- Schema: public
-- 所有者: "smart-schedule-core"
-- ================================================================

-- 1. 学院表
create table public.sc_department
(
    department_uuid varchar(32) not null
        constraint sc_department_pk
            primary key,
    department_name varchar(32) not null
);

comment on table public.sc_department is '学院表';

comment on column public.sc_department.department_uuid is '学院UUID';

comment on column public.sc_department.department_name is '学院名称';

alter table public.sc_department
    owner to "smart-schedule-core";

-- 2. 教学楼
create table public.sc_building
(
    building_uuid varchar(32) not null
        constraint sc_building_pk
            primary key,
    building_num  varchar(32) not null,
    building_name varchar(32) not null
);

comment on table public.sc_building is '教学楼';

comment on column public.sc_building.building_uuid is '教学楼UUID';

comment on column public.sc_building.building_num is '教学楼编号';

comment on column public.sc_building.building_name is '教学楼名称';

alter table public.sc_building
    owner to "smart-schedule-core";

-- 3. 学期表
create table public.sc_semester
(
    semester_uuid varchar(32) not null
        constraint sc_semester_pk
            primary key,
    semester_name varchar(32) not null
);

comment on table public.sc_semester is '学期表';

comment on column public.sc_semester.semester_uuid is '学期UUID';

comment on column public.sc_semester.semester_name is '学期名称';

alter table public.sc_semester
    owner to "smart-schedule-core";

-- 4. 课程类型表
create table public.sc_course_type
(
    course_type_uuid varchar(32)  not null
        constraint sc_course_type_pk
            primary key,
    type_name         varchar(32) not null,
    type_description  varchar(255)
);

-- 添加表和字段注释
comment on table public.sc_course_type is '课程类型表';

comment on column public.sc_course_type.course_type_uuid is '课程类型UUID';

comment on column public.sc_course_type.type_name is '类型名称';

comment on column public.sc_course_type.type_description is '类型描述';

-- 权限归属
alter table public.sc_course_type
    owner to "smart-schedule-core";

-- 5. 教室类型表
create table public.sc_classroom_type
(
    classroom_type_uuid varchar(32)  not null
        constraint sc_classroom_type_pk
            primary key,
    type_name            varchar(32) not null,
    type_description     varchar(255)
);

-- 添加表和字段注释
comment on table public.sc_classroom_type is '教室类型表';

comment on column public.sc_classroom_type.classroom_type_uuid is '教室类型UUID';

comment on column public.sc_classroom_type.type_name is '类型名称';

comment on column public.sc_classroom_type.type_description is '类型描述';

-- 权限归属
alter table public.sc_classroom_type
    owner to "smart-schedule-core";

-- 6. 教师表
create table public.sc_teacher
(
    teacher_uuid       varchar(32)          not null
        constraint sc_teacher_pk
            primary key,
    teacher_num        varchar(32)          not null
        constraint sc_teacher_num_unique
            unique,
    teacher_name       varchar(32)          not null,
    title              varchar(32)          not null,
    teacher_password   varchar              not null,
    max_hours_per_week integer              not null,
    like_time          jsonb                not null,
    is_active          boolean default true not null
);

comment on table public.sc_teacher is '教师表';

comment on column public.sc_teacher.teacher_uuid is '教师UUID';

comment on column public.sc_teacher.teacher_num is '教师编号（唯一工号）';

comment on column public.sc_teacher.teacher_name is '教师名称';

comment on column public.sc_teacher.title is '职称';

comment on column public.sc_teacher.teacher_password is '密码';

comment on column public.sc_teacher.max_hours_per_week is '每周最高授课时长';

comment on column public.sc_teacher.like_time is '喜欢时间 (JSONB 格式)';

comment on column public.sc_teacher.is_active is '是否启用';

alter table public.sc_teacher
    owner to "smart-schedule-core";

-- 7. 系统管理员表
create table public.sc_system_admin
(
    admin_uuid     varchar(32)  not null
        constraint sc_system_admin_pk
            primary key,
    admin_username varchar(32)  not null
        constraint sc_system_admin_username_unique
            unique,
    admin_password varchar(128) not null
);

-- 添加表和字段注释
comment on table public.sc_system_admin is '系统管理员表';

comment on column public.sc_system_admin.admin_uuid is '管理员UUID';

comment on column public.sc_system_admin.admin_username is '管理员用户名 (唯一登录账号)';

comment on column public.sc_system_admin.admin_password is '管理员密码 (建议使用BCrypt等加密存储)';

-- 权限归属
alter table public.sc_system_admin
    owner to "smart-schedule-core";

-- 8. 专业表
create table public.sc_major
(
    major_uuid      varchar(32) not null
        constraint sc_major_pk
            primary key,
    department_uuid varchar(32) not null
        constraint sc_major_sc_department_department_uuid_fk
            references public.sc_department,
    major_num       varchar     not null,
    major_name      varchar     not null
);

comment on table public.sc_major is '专业表';

comment on column public.sc_major.major_uuid is '专业UUID';

comment on column public.sc_major.department_uuid is '学院UUID';

comment on column public.sc_major.major_num is '专业编号';

comment on column public.sc_major.major_name is '专业名称';

alter table public.sc_major
    owner to "smart-schedule-core";

-- 9. 教务管理表
create table public.sc_academic_admin
(
    academic_uuid     varchar(32)  not null
        constraint sc_academic_admin_pk
            primary key,
    department_uuid   varchar(32)  not null
        constraint sc_academic_admin_sc_department_department_uuid_fk
            references public.sc_department,
    academic_num      varchar(32)  not null
        constraint sc_academic_admin_num_unique
            unique,
    academic_name     varchar(32)  not null,
    academic_password varchar(128) not null
);

-- 添加表和字段注释
comment on table public.sc_academic_admin is '教务管理表';

comment on column public.sc_academic_admin.academic_uuid is '教务人员UUID';

comment on column public.sc_academic_admin.department_uuid is '所属学院UUID (外键)';

comment on column public.sc_academic_admin.academic_num is '教务工号 (唯一编码)';

comment on column public.sc_academic_admin.academic_name is '教务名称';

comment on column public.sc_academic_admin.academic_password is '教务密码 (建议加密存储)';

-- 权限归属
alter table public.sc_academic_admin
    owner to "smart-schedule-core";

-- 10. 课程类型-教室类型关联表
create table public.sc_course_classroom_type
(
    relation_uuid        varchar(32) not null
        constraint sc_course_classroom_type_pk
            primary key,
    course_type_uuid     varchar(32) not null
        constraint sc_course_classroom_type_course_type_fk
            references public.sc_course_type,
    classroom_type_uuid  varchar(32) not null
        constraint sc_course_classroom_type_classroom_type_fk
            references public.sc_classroom_type,
    constraint sc_course_classroom_type_course_classroom_uuid_unique
        unique (course_type_uuid, classroom_type_uuid)
);

-- 添加表和字段注释
comment on table public.sc_course_classroom_type is '课程类型-教室类型关联表';

comment on column public.sc_course_classroom_type.relation_uuid is '关联关系UUID';

comment on column public.sc_course_classroom_type.course_type_uuid is '课程类型UUID';

comment on column public.sc_course_classroom_type.classroom_type_uuid is '教室类型UUID';

-- 权限归属
alter table public.sc_course_classroom_type
    owner to "smart-schedule-core";

-- 11. 课程主表
create table public.sc_course
(
    course_uuid             varchar(32) not null
        constraint sc_course_pk
            primary key,
    course_num              varchar(32) not null
        constraint sc_course_num_unique
            unique,
    course_name             varchar(64) not null,
    course_type_uuid        varchar(32) not null
        constraint sc_course_course_type_fk
            references public.sc_course_type,
    course_credit           numeric     not null
);

-- 添加注释
comment on table public.sc_course is '课程主表';
comment on column public.sc_course.course_uuid is '课程UUID';
comment on column public.sc_course.course_num is '课程编号 (唯一编码)';
comment on column public.sc_course.course_name is '课程名称';
comment on column public.sc_course.course_type_uuid is '课程类型UUID';
comment on column public.sc_course.course_credit is '课程学分 (支持半分)';

-- 权限归属
alter table public.sc_course owner to "smart-schedule-core";

-- 12. 课程教师资格关联表
create table public.sc_course_qualification
(
    course_qualification_uuid varchar(32) not null
        constraint sc_course_qualification_pk
            primary key,
    course_uuid varchar(32) not null
        constraint sc_course_qualification_course_fk
            references public.sc_course,
    teacher_uuid varchar(32) not null
        constraint sc_course_qualification_teacher_fk
            references public.sc_teacher,
    constraint sc_course_qualification_course_teacher_unique
        unique (course_uuid, teacher_uuid)
);

-- 添加注释
comment on table public.sc_course_qualification is '课程教师资格关联表';
comment on column public.sc_course_qualification.course_qualification_uuid is '关联关系UUID';
comment on column public.sc_course_qualification.course_uuid is '课程UUID';
comment on column public.sc_course_qualification.teacher_uuid is '教师UUID';

-- 权限归属
alter table public.sc_course_qualification
    owner to "smart-schedule-core";

-- 13. 教室表
create table public.sc_classroom
(
    classroom_uuid       varchar(32) not null
        constraint sc_classroom_pk
            primary key,
    building_uuid        varchar(32) not null
        constraint sc_classroom_sc_building_building_uuid_fk
            references public.sc_building,
    classroom_name       varchar     not null,
    classroom_capacity   integer     not null,
    classroom_type_uuid  varchar(32) not null
        constraint sc_classroom_classroom_type_fk
            references public.sc_classroom_type
);

comment on table public.sc_classroom is '教室表';

comment on column public.sc_classroom.classroom_uuid is '教室UUID';

comment on column public.sc_classroom.building_uuid is '教学楼UUID';

comment on column public.sc_classroom.classroom_name is '教室名称';

comment on column public.sc_classroom.classroom_capacity is '教室容量';

comment on column public.sc_classroom.classroom_type_uuid is '教室类型UUID';

alter table public.sc_classroom
    owner to "smart-schedule-core";

-- 14. 行政班级表
create table public.sc_class
(
    class_uuid varchar(32) not null
        constraint sc_class_pk
            primary key,
    major_uuid varchar(32) not null
        constraint sc_class_sc_major_major_uuid_fk
            references public.sc_major,
    class_name varchar     not null
);

comment on table public.sc_class is '行政班级表';

comment on column public.sc_class.class_uuid is '行政班级班级UUID';

comment on column public.sc_class.major_uuid is '专业UUID';

comment on column public.sc_class.class_name is '行政班级名称';

alter table public.sc_class
    owner to "smart-schedule-core";

-- 15. 学生表
create table public.sc_student
(
    student_uuid     varchar(32)  not null
        constraint sc_student_pk
            primary key,
    student_id       varchar(32)  not null
        constraint sc_student_id_key
            unique,
    student_name     varchar(32)  not null,
    class_uuid       varchar(32)  not null
        constraint sc_student_sc_class_class_uuid_fk
            references public.sc_class(class_uuid),
    student_password varchar(128) not null
);

-- 注释部分
comment on table public.sc_student is '学生表';
comment on column public.sc_student.student_uuid is '学生UUID';
comment on column public.sc_student.student_id is '学号 (唯一编码)';
comment on column public.sc_student.student_name is '学生姓名';
comment on column public.sc_student.class_uuid is '行政班级UUID (外键)';
comment on column public.sc_student.student_password is '学生密码 (建议加密存储)';

-- 权限归属
alter table public.sc_student owner to "smart-schedule-core";

-- 16. 教学班-行政班级关联表
create table public.sc_teaching_class_class
(
    teaching_class_class_uuid varchar(32) not null
        constraint sc_teaching_class_class_pk
            primary key,
    teaching_class_uuid varchar(32) not null
        constraint sc_teaching_class_class_teaching_class_fk
            references public.sc_teaching_class,
    class_uuid varchar(32) not null
        constraint sc_teaching_class_class_class_fk
            references public.sc_class,
    constraint sc_teaching_class_class_teaching_class_unique
        unique (teaching_class_uuid, class_uuid)
);

-- 添加注释
comment on table public.sc_teaching_class_class is '教学班-行政班级关联表';
comment on column public.sc_teaching_class_class.teaching_class_class_uuid is '关联关系UUID';
comment on column public.sc_teaching_class_class.teaching_class_uuid is '教学班UUID';
comment on column public.sc_teaching_class_class.class_uuid is '行政班级UUID';

-- 权限归属
alter table public.sc_teaching_class_class
    owner to "smart-schedule-core";

-- 17. 教学班表
create table public.sc_teaching_class
(
    teaching_class_uuid varchar(32) not null
        constraint sc_teaching_class_pk
            primary key,
    course_uuid         varchar(32) not null
        constraint sc_teaching_course_fk references public.sc_course,
    teacher_uuid        varchar(32) not null
        constraint sc_teaching_teacher_fk references public.sc_teacher,
    semester_uuid       varchar(32) not null
        constraint sc_teaching_semester_fk references public.sc_semester,
    teaching_class_name varchar(64)
);

-- 添加注释
comment on table public.sc_teaching_class is '教学班表 (排课的业务主体)';
comment on column public.sc_teaching_class.teaching_class_uuid is '教学班UUID';
comment on column public.sc_teaching_class.course_uuid is '课程UUID';
comment on column public.sc_teaching_class.teacher_uuid is '教师UUID';
comment on column public.sc_teaching_class.semester_uuid is '学期UUID';
comment on column public.sc_teaching_class.teaching_class_name is '教学班名称';

-- 18. 排课表 (核心表：存最终生成的课表数据)
create table public.sc_schedule
(
    schedule_uuid       varchar(32) not null
        constraint sc_schedule_pk
            primary key,
    semester_uuid       varchar(32) not null
        constraint sc_sch_semester_fk references public.sc_semester,
    -- 关联教学班，通过教学班可以找到课程、老师和参与的学生群体
    teaching_class_uuid varchar(32) not null
        constraint sc_sch_teaching_class_fk references public.sc_teaching_class,
    -- 冗余字段，方便直接查询，减少 JOIN
    course_uuid         varchar(32) not null
        constraint sc_sch_course_fk references public.sc_course,
    teacher_uuid        varchar(32) not null
        constraint sc_sch_teacher_fk references public.sc_teacher,
    classroom_uuid      varchar(32) not null
        constraint sc_sch_room_fk references public.sc_classroom,

    -- 时间属性
    day_of_week         integer     not null, -- 1-7
    section_start       integer     not null, -- 起始节次
    section_end         integer     not null, -- 结束节次
    weeks_json          jsonb       not null default '[]'::jsonb, -- 上课周次 [1,2,3...]

    -- 锁定标识：如果老师手动调整并确认了这一节，可以锁定
    is_locked           boolean     not null default false,

    -- 状态：0-预览方案, 1-正式执行
    status              integer     not null default 0,
    updated_at          timestamp   default current_timestamp
);

comment on table public.sc_schedule is '排课表 (基于教学班进行排课)';
comment on column public.sc_schedule.teaching_class_uuid is '教学班UUID (代替原逻辑分组ID)';

-- 19. 排课冲突记录表
create table public.sc_schedule_conflict
(
    conflict_uuid   varchar(32) not null
        constraint sc_schedule_conflict_pk
            primary key,
    semester_uuid   varchar(32) not null
        constraint sc_conf_semester_fk
            references public.sc_semester,
    schedule_uuid_a varchar(32) not null
        constraint sc_conf_sch_a_fk
            references public.sc_schedule,
    schedule_uuid_b varchar(32)
        constraint sc_conf_sch_b_fk
            references public.sc_schedule,
    conflict_type   varchar(32) not null,
    severity        integer     not null,
    description     text        not null
);

-- 添加注释
comment on table public.sc_schedule_conflict is '排课冲突记录表';
comment on column public.sc_schedule_conflict.conflict_uuid is '冲突记录UUID';
comment on column public.sc_schedule_conflict.semester_uuid is '学期UUID';
comment on column public.sc_schedule_conflict.schedule_uuid_a is '排课记录UUID A';
comment on column public.sc_schedule_conflict.schedule_uuid_b is '排课记录UUID B (可为空，单条记录冲突)';
comment on column public.sc_schedule_conflict.conflict_type is '冲突类型';
comment on column public.sc_schedule_conflict.severity is '严重程度：1-硬冲突, 0-软冲突';
comment on column public.sc_schedule_conflict.description is '冲突描述';

-- 权限归属
alter table public.sc_schedule_conflict
    owner to "smart-schedule-core";

-- ================================================================
-- 表创建完成
-- ================================================================