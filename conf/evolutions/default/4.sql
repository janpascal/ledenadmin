# --- !Ups

create table email_check_probe (
  id                        bigint auto_increment not null,
  persoon_id                bigint,
  `type`                    char(20),
  `timestamp`               datetime,
  email                     varchar(255),
  token                     varchar(255),
  constraint pk_email_check_probe primary key (id))
;

alter table email_check_probe add constraint fk_email_check_probe_persoon_1 foreign key (persoon_id) references persoon (id) on delete restrict on update restrict;
create index ix_email_check_probe_persoon on email_check_probe (persoon_id);
create index ix_email_check_probe_token on email_check_probe (token);


# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

drop table email_check_probe;

SET FOREIGN_KEY_CHECKS=1;

