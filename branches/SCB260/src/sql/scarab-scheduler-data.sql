/*
 *  Turbine Scheduler
 */

/* Run the email sender every 5 minutes (300 seconds). */

/* insert into TURBINE_SCHEDULED_JOB (JOB_ID, JOB_SECOND, JOB_MINUTE, JOB_HOUR, WEEK_DAY, DAY_OF_MONTH, TASK, EMAIL, JOB_PROPERTY)
values (1, 300, -1, -1, -1, -1, 'task', 'thierry.lach@bbdodetroit.com', 'job_property');
*/

/* NOTE -  The following values are just for testing */
/* NOTE -  I still need to figure out what goes in the task column */
/* NOTE -  I still need to figure out what goes in the job_property column */

insert into TURBINE_SCHEDULED_JOB (JOB_ID, JOB_SECOND, JOB_MINUTE, JOB_HOUR, WEEK_DAY, DAY_OF_MONTH, TASK, EMAIL, JOB_PROPERTY)
values (1, 5, -1, -1, -1, -1, 'task', 'thierry.lach@bbdodetroit.com', 'job_property');

