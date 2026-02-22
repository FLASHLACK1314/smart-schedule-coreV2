-- 2. 排课表 (核心表：存最终生成的课表数据)
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
    weeks_json          varchar     not null, -- 上课周次 JSON数组字符串 如"[1,2,3]"

    -- 学时信息
    credit_hours        integer     not null default 0, -- 累计学时 (单次学时 × 周次数)

    -- 状态：0-预览方案, 1-正式执行
    status              integer     not null default 0,
    updated_at          timestamp   default current_timestamp
);

comment on table public.sc_schedule is '排课表 (基于教学班进行排课)';
comment on column public.sc_schedule.teaching_class_uuid is '教学班UUID (代替原逻辑分组ID)';
comment on column public.sc_schedule.credit_hours is '累计学时 (单次学时 × 周次数)';