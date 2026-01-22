-- 课程类型-教室类型关联表
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