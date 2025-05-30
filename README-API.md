# Paperless Paging API
## API Endpoints
### Call Slips and Batches
#### GET `/api/users/{user-id}/task-batches/current`
Retrieve the users current batch.  The batch is prepared for filling by setting the incomingStatus to
the status of the Task and the status to New.  If the requesting user matches user-id the start time
of the batch is set.  This API call is used for the My Call Slips page and when a user Views a batch 
from Review Assignments.  If there were responses stored for the batch, they are applied to the batch
before it is returned.
#### GET `/api/users/{user-id}/task-batches/open`
Return a list of open batches for a user. Retrievers may only view their own.  The 
system only allows for one open batch at a time, so this is currently equivalent 
to current batch.
#### PUT `/api/users/{user-id}/task-batches`
Create a batch for a user. Only available to assigners.  The PUT data is an array of call slips. 
The server uses just the IDs of the tasks to generate the batch.
#### GET `/api/task-batches/{task-batch-id}/assigner-close`
Closes or cancels a batch for a retriever.  Only available to assigners.  If the batch has any 
responses, those are applied, and the batch is saved. Call slips are processed as if the user 
 submitted the batch. If the batch has no responses, it is deleted.
All call slips in the deleted batch return to the list of available call slips. 
#### GET `/api/task-batch-infos`
Returns a list of TaskBatchInfos which contain summaries of all open batches.
This is called by Review Assignments to get the list of open batches.  TaskBatchInfos are 
created by reading the entities from the DB and calculating the summary information.
#### GET `/api/tasks/unassigned`
Returns all call slips which can be assigned.  These are call slips without a current batch
and which are open.  Batches are open if they have a status of New or NOS.
#### PUT `/api/tasks/{task-id}/status`
Set the status of a single call slip.  This is not currently used.
#### GET `/api/task-batches/{batch-id}`
Return the call slip batch including all call slips.
#### PUT `/api/task-batches/{batch-id}`
Submits the call slip batch.  The body of the request is the list of tasks.  Only the 
call slip ID, the status, and the fill problem list are used to update the tasks.  This 
call will close the batch, set the status of the call slips.  Any New or NOS call slips will return 
to the list of unassigned call slips.
#### GET `/api/task-status-list`
Returns the list of call slip statuses.  Used by client to display options.
#### PUT `/api/task-batches/{batch-id}/task/{task-id}/batch-response`
Stores a batch response on the server.  As retrievers respond to batches, the client sends 
the responses to the server.   This is used for server side caching, and to allow 
assigners to monitor and close batches.
### Locations
#### GET `/api/task-location-list`
Return the list of Circulation Desks from Alma.
This list is used to assign to users, for dropdowns, for filtering, etc.
### Problems
#### GET `/api/task-fill-problems`
Returns the list of possible of fill problems for the client to display.  These are used in the Fill Problems dialog.
#### GET `/api/task-problems`
Returns the list of possible call slip problems.
### Users
#### GET `/api/users`
Returns all users. (Admin only)
#### PUT `/api/users`
Update or create a user. (Admin only).  If id is not positive, the user is created.
#### GET `/api/users/assignable`
Get the list of users who are assignable.  These are users without an active batch who 
have some overlapping Libraries with the calling user.
#### GET `/api/users/active-batch`
Get the list of users with active batches.  Only users with overlapping Libraries 
with the calling user will be included in the list.
## Notes:
The API is REST like but not 100% REST compliant.  There are some non-idempotent PUTs, 
and there are some calls which are actions rather than resource based.


[Main README](README.md)