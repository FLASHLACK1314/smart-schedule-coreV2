-- 教学班表
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