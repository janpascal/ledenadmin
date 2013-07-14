# --- !Ups

alter table persoon add column account_name varchar(255);
alter table persoon add column crypted_password varchar(255);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table persoon drop column account_name;
alter table persoon drop column crypted_password;

SET FOREIGN_KEY_CHECKS=1;

