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