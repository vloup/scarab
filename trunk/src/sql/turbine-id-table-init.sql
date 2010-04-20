delete from ID_TABLE where id_table_id >= 1;

insert into ID_TABLE (id_table_id, table_name, next_id, quantity) VALUES (1, 'TURBINE_PERMISSION', 10000, 10);
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) VALUES (2, 'TURBINE_ROLE', 10000, 10);
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) VALUES (3, 'TURBINE_GROUP', 10000, 10);
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) VALUES (4, 'TURBINE_ROLE_PERMISSION', 10000, 10);
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) VALUES (5, 'TURBINE_USER', 10000, 10);
insert into ID_TABLE (id_table_id, table_name, next_id, quantity) VALUES (6, 'TURBINE_USER_GROUP_ROLE', 10000, 10);
