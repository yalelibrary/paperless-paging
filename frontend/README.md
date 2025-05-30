# Paperless Paging Client

Paperless Paging Client is a React UI for Paperless Paging.

## Overview
Paperless Paging is an application used to retrieve material for users based on tasks in Alma.

This client is a React application, using Material UI for most components.

## Important Screens and Functionality
The Paperless Paging Client has the following primary sections:
- Admin Users/Roles: Add, Remove, and set properties for users. (Desktop)
- Assign Tasks: Assign tasks to retrievers and review assignments. (Desktop)
- My Tasks: Used for retrieving material and setting the status of a task batch. (Tablet)

## Roles:
#### Admin
Admins have access to all the screens, including User administration.

#### Retrieve
Users with the Retrieve role may complete tasks.

#### Assign Task
Users with Assign tasks privilege can assign tasks to users.

## Associated Circulation Desks
All users are assigned to a set of circulation desks from Alma in user management. Users will only see tasks which are associated with one of their circulation desks.

## Authentication
The api server will authenticate the user and redirect to the react app.  Session cookies are used to maintain the login session. The backend authenticates using AWS Cognito.

### React + Vite
This application is build using Vite.