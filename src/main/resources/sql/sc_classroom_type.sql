-- 教室类型表
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