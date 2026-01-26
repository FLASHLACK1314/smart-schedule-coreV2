-- 教学班-行政班级关联表
create table public.sc_teaching_class_class
(
    teaching_class_class_uuid varchar(32) not null
        constraint sc_teaching_class_class_pk
            primary key,
    teaching_class_uuid varchar(32) not null
        constraint sc_teaching_class_class_teaching_class_fk
            references public.sc_teaching_class,
    class_uuid varchar(32) not null
        constraint sc_teaching_class_class_class_fk
            references public.sc_class,
    constraint sc_teaching_class_class_teaching_class_unique
        unique (teaching_class_uuid, class_uuid)
);

-- 添加注释
comment on table public.sc_teaching_class_class is '教学班-行政班级关联表';
comment on column public.sc_teaching_class_class.teaching_class_class_uuid is '关联关系UUID';
comment on column public.sc_teaching_class_class.teaching_class_uuid is '教学班UUID';
comment on column public.sc_teaching_class_class.class_uuid is '行政班级UUID';

-- 权限归属
alter table public.sc_teaching_class_class
    owner to "smart-schedule-core";