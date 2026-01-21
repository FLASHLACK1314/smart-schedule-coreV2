create table public.sc_building
(
    building_uuid varchar(32) not null
        constraint sc_building_pk
            primary key,
    building_num  varchar(32) not null,
    building_name varchar(32) not null
);

comment on table public.sc_building is '教学楼';

comment on column public.sc_building.building_uuid is '教学楼UUID';

comment on column public.sc_building.building_num is '教学楼编号';

comment on column public.sc_building.building_name is '教学楼名称';

alter table public.sc_building
    owner to "smart-schedule-core";

