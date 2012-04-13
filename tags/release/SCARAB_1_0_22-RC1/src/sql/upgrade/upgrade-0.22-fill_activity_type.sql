create table  activity_temp as select activity_id from SCARAB_ACTIVITY a
where activity_type is null and depend_id is null and attachment_id is null and attribute_id = 0
and exists( select * from SCARAB_TRANSACTION t where t.transaction_id = a.transaction_id and t.type_id = 3)
and (select count(*) from SCARAB_ISSUE i, SCARAB_ACTIVITY a2 where a2.transaction_id = a.transaction_id and i.issue_id = a2.issue_id ) = 1;
update SCARAB_ACTIVITY a set activity_type = 'issue_moved' where activity_id in ( select activity_id from activity_temp );
drop table activity_temp;

create table  activity_temp as select activity_id from SCARAB_ACTIVITY a
where activity_type is null and depend_id is null and attachment_id is null and attribute_id = 0
and exists( select * from SCARAB_TRANSACTION t where t.transaction_id = a.transaction_id and t.type_id = 3)
and (select count(*) from SCARAB_ISSUE i, SCARAB_ACTIVITY a2 where a2.transaction_id = a.transaction_id and i.issue_id = a2.issue_id ) = 2;
update SCARAB_ACTIVITY a set activity_type = 'issue_copied' where activity_id in ( select activity_id from activity_temp );
drop table activity_temp;

update SCARAB_ACTIVITY a set activity_type = 'issue_created'
where activity_type is null and depend_id is null and attachment_id is null and attribute_id = 0
and exists( select * from SCARAB_TRANSACTION t where t.transaction_id = a.transaction_id and t.type_id = 1);

update SCARAB_ACTIVITY a set activity_type = 'url_deleted'
where activity_type is null and length(a.old_value) > 0
and exists( select * from SCARAB_ATTACHMENT t where t.attachment_id = a.attachment_id and t.attachment_type_id = 3);

update SCARAB_ACTIVITY a set activity_type = 'url_added'
where activity_type is null and (a.old_value is null or length(a.old_value)= 0 )
and exists( select * from SCARAB_ATTACHMENT t where t.attachment_id = a.attachment_id and t.attachment_type_id = 3);

update SCARAB_ACTIVITY a set activity_type = 'attachment_removed'
where activity_type is null and length(a.old_value) > 0
and exists( select * from SCARAB_ATTACHMENT t where t.attachment_id = a.attachment_id and t.attachment_type_id = 1);

update SCARAB_ACTIVITY a set activity_type = 'attachment_created'
where activity_type is null and (a.old_value is null or length(a.old_value)= 0 )
and exists( select * from SCARAB_ATTACHMENT t where t.attachment_id = a.attachment_id and t.attachment_type_id = 1);

update SCARAB_ACTIVITY a set a.activity_type = 'comment_changed'
where a.activity_type is null and length(a.old_value) > 0
and exists( select * from SCARAB_ATTACHMENT t where t.attachment_id = a.attachment_id and t.attachment_type_id = 2);

update SCARAB_ACTIVITY a set a.activity_type = 'comment_added'
where a.activity_type is null and (a.old_value is null or length(a.old_value) = 0)
and exists( select * from SCARAB_ATTACHMENT t where t.attachment_id = a.attachment_id and t.attachment_type_id = 2);

update SCARAB_ACTIVITY set activity_type = 'dependency_changed'
where activity_type is null and depend_id is not null and new_value is not null and old_value is not null;

update SCARAB_ACTIVITY set activity_type = 'dependency_deleted'
where activity_type is null and depend_id is not null and new_value is null and old_value is not null;

update SCARAB_ACTIVITY set activity_type = 'dependency_created'
where activity_type is null and depend_id is not null and old_value is null;

update SCARAB_ACTIVITY set activity_type = 'user_attribute_changed'
where activity_type is null and attribute_id <> 0 and (
old_user_id is not null or new_user_id is not null
);

update SCARAB_ACTIVITY set activity_type = 'attribute_changed'
where activity_type is null and attribute_id <> 0 and old_user_id is null and new_user_id is null and (
    ( old_numeric_value is not null or new_numeric_value is not null )
 or ( old_option_id is not null or new_option_id is not null )
 or ( old_value is not null or new_value is not null )
);

update SCARAB_ACTIVITY set activity_type = 'other' where activity_type is null;

update SCARAB_ACTIVITY set activity_type = 'other' where activity_type = 'attribute imported';

update SCARAB_ACTIVITY set description = null where activity_type <> 'other';

alter table SCARAB_ACTIVITY modify column
   activity_type varchar(30) not null
;
