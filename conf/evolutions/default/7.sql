# --- !Ups

alter table persoon add index uk_persoon_account (account_name);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table persoon drop index uk_persoon_account;

SET FOREIGN_KEY_CHECKS=1;

