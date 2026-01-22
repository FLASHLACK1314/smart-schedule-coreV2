-- 课程类型表
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