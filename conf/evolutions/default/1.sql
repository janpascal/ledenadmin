# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table afschrift (
  id                        bigint not null,
  bedrag                    bigint,
  afbij                     integer,
  tegenrekening             varchar(255),
  mededelingen              varchar(255),
  constraint ck_afschrift_afbij check (afbij in (0,1)),
  constraint pk_afschrift primary key (id))
;

create table factuur (
  id                        bigint not null,
  bedrag                    bigint,
  constraint pk_factuur primary key (id))
;

create table factuur_contributie (
  id                        bigint not null,
  bedrag                    bigint,
  jaar                      integer,
  constraint pk_factuur_contributie primary key (id))
;

create table lid (
  id                        bigint not null,
  lid_sinds                 timestamp,
  lid_tot                   timestamp,
  address                   varchar(255),
  constraint pk_lid primary key (id))
;

create table persoon (
  id                        bigint not null,
  lid_id                    bigint not null,
  name                      varchar(255),
  email                     varchar(255),
  constraint pk_persoon primary key (id))
;

create sequence afschrift_seq;

create sequence factuur_seq;

create sequence factuur_contributie_seq;

create sequence lid_seq;

create sequence persoon_seq;

alter table persoon add constraint fk_persoon_lid_1 foreign key (lid_id) references lid (id) on delete restrict on update restrict;
create index ix_persoon_lid_1 on persoon (lid_id);



# --- !Downs

SET REFERENTIAL_INTEGRITY FALSE;

drop table if exists afschrift;

drop table if exists factuur;

drop table if exists factuur_contributie;

drop table if exists lid;

drop table if exists persoon;

SET REFERENTIAL_INTEGRITY TRUE;

drop sequence if exists afschrift_seq;

drop sequence if exists factuur_seq;

drop sequence if exists factuur_contributie_seq;

drop sequence if exists lid_seq;

drop sequence if exists persoon_seq;

