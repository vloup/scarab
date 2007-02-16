drop database @DB_NAME@;
drop user @DATABASE_USERNAME@;
create user @DATABASE_USERNAME@ with encrypted password '@DATABASE_PASSWORD@';
create database @DB_NAME@ with owner @DATABASE_USERNAME@;
 
