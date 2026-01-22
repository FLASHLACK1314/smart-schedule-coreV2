-- 创建系统管理员表
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
