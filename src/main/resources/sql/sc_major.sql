create table public.sc_major
(
    major_uuid      varchar(32) not null
        constraint sc_major_pk
            primary key,
    department_uuid varchar(32) not null
        constraint sc_major_sc_department_department_uuid_fk
            references public.sc_department,
    major_num       varchar     not null,
    major_name      varchar     not null
);

comment on table public.sc_major is '专业表';

comment on column public.sc_major.major_uuid is '专业UUID';

comment on column public.sc_major.department_uuid is '学院UUID';

comment on column public.sc_major.major_num is '专业编号';

comment on column public.sc_major.major_name is '专业名称';

alter table public.sc_major
    owner to "smart-schedule-core";

