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

