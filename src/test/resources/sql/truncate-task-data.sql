SET REFERENTIAL_INTEGRITY = false;
truncate table user_account;
truncate table task;
truncate table task_log;
truncate table task_status_update;
truncate table user_task_batch;
truncate table user_task_batch_tasks;
SET REFERENTIAL_INTEGRITY = true;