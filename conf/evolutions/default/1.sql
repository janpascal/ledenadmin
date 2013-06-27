# --- !Ups

create table afschrift (
  id                        bigint auto_increment not null,
  datum                     datetime,
  bedrag                    decimal(38),
  afbij                     integer,
  tegenrekening             varchar(255),
  naam                      varchar(255),
  mededelingen              varchar(255),
  constraint ck_afschrift_afbij check (afbij in (0,1)),
  constraint pk_afschrift primary key (id))
;

create table bankrekening (
  id                        bigint auto_increment not null,
  lid_id                    bigint,
  nummer                    varchar(255),
  constraint pk_bankrekening primary key (id))
;

create table factuur (
  _type                     integer(31) not null,
  id                        bigint auto_increment not null,
  bedrag                    decimal(38),
  datum                     datetime,
  lid_id                    bigint,
  betaling_id               bigint,
  jaar                      integer,
  constraint pk_factuur primary key (id))
;

create table lid (
  id                        bigint auto_increment not null,
  lid_sinds                 datetime,
  lid_tot                   datetime,
  address                   varchar(255),
  constraint pk_lid primary key (id))
;

create table persoon (
  id                        bigint auto_increment not null,
  lid_id                    bigint,
  name                      varchar(255),
  email                     varchar(255),
  constraint pk_persoon primary key (id))
;

alter table bankrekening add constraint fk_bankrekening_lid_1 foreign key (lid_id) references lid (id) on delete restrict on update restrict;
create index ix_bankrekening_lid_1 on bankrekening (lid_id);
alter table factuur add constraint fk_factuur_lid_2 foreign key (lid_id) references lid (id) on delete restrict on update restrict;
create index ix_factuur_lid_2 on factuur (lid_id);
alter table factuur add constraint fk_factuur_betaling_3 foreign key (betaling_id) references afschrift (id) on delete restrict on update restrict;
create index ix_factuur_betaling_3 on factuur (betaling_id);
alter table persoon add constraint fk_persoon_lid_4 foreign key (lid_id) references lid (id) on delete restrict on update restrict;
create index ix_persoon_lid_4 on persoon (lid_id);



# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table afschrift;

drop table bankrekening;

drop table factuur;

drop table lid;

drop table persoon;

SET FOREIGN_KEY_CHECKS=1;

