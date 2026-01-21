-- 创建教务管理表
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