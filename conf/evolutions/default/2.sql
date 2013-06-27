# --- !Ups


alter table factuur add column naam varchar(255);
alter table factuur add column omschrijving varchar(255);

# --- !Downs

SET FOREIGN_KEY_CHECKS=0;

alter table factuur drop column naam;
alter table factuur drop column omschrijving;

SET FOREIGN_KEY_CHECKS=1;

