create table public.sc_classroom
(
    classroom_uuid       varchar(32) not null
        constraint sc_classroom_pk
            primary key,
    building_uuid        varchar(32) not null
        constraint sc_classroom_sc_building_building_uuid_fk
            references public.sc_building,
    classroom_name       varchar     not null,
    classroom_capacity   integer     not null,
    classroom_type_uuid  varchar(32) not null
        constraint sc_classroom_classroom_type_fk
            references public.sc_classroom_type
);

comment on table public.sc_classroom is '教室表';

comment on column public.sc_classroom.classroom_uuid is '教室UUID';

comment on column public.sc_classroom.building_uuid is '教学楼UUID';

comment on column public.sc_classroom.classroom_name is '教室名称';

comment on column public.sc_classroom.classroom_capacity is '教室容量';

comment on column public.sc_classroom.classroom_type_uuid is '教室类型UUID';

alter table public.sc_classroom
    owner to "smart-schedule-core";

