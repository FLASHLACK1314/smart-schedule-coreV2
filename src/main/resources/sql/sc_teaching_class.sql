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
    -- 包含的行政班级列表 (JSONB 存储，如 ["class_uuid1", "class_uuid2"])
    class_uuids         jsonb       not null default '[]'::jsonb,
    teaching_class_name varchar(64)
);

comment on table public.sc_teaching_class is '教学班表 (排课的业务主体)';
comment on column public.sc_teaching_class.class_uuids is '关联的行政班级UUID列表';