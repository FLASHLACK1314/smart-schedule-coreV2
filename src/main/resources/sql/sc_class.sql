create table public.sc_class
(
    class_uuid varchar(32) not null
        constraint sc_class_pk
            primary key,
    major_uuid varchar(32) not null
        constraint sc_class_sc_major_major_uuid_fk
            references public.sc_major,
    class_name varchar     not null
);

comment on table public.sc_class is '行政班级表';

comment on column public.sc_class.class_uuid is '行政班级班级UUID';

comment on column public.sc_class.major_uuid is '专业UUID';

comment on column public.sc_class.class_name is '行政班级名称';

alter table public.sc_class
    owner to "smart-schedule-core";

