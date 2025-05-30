# Paperless Paging for Alma
## Description
Paperless Paging is an application to view, assign, and complete tasks in Alma.
<br />
The application has a Java Spring Boot application for the backend which reads tasks from Alma
using the API and tracks assignments and progress of tasks in a database.
<br />
The frontend is a ReactJS application.

## Requirements

- Java JDK >= 21
- maven 3.9+
- npm 10.5+
- node 21.7+
- Alma API key with the following rights: readonly access to configuration, user, and tasks-uses, bibs.

## Quickstart
For all Modes:
```bash
# download the backend code, the frontend is a submodule
git clone git@github.com:yalelibrary/paperless-paging.git
cd paperless-paging
```
### Quickstart Demo Mode
#### Description of Demo Mode
Run the application locally in demo mode with an in-memory database and sample users.
#### Demo Mode Configuration
Find the profiles section of `src/main/resources/application.yml`. (spring:profiles:active)
<br />
Set the active profiles to `demo,secrets`

Demo mode: Run the application locally in demo mode with an in-memory database and sample users.

Running in demo mode requires an API key for Alma and the pre-requisites and can be used to test the functionality
before setting up your system.

```bash
# copy the example secrets file and update the API token
cp src/main/resources/application-secrets-demo-example.yml src/main/resources/application-secrets.yml

# build the JAR file
mvn clean install

# run the application
java -jar target/*.jar
```

_Demo mode users are build in:_

Username/Password:
- user/password
- admin/password
- retriever/password

### Quickstart Dev Mode
#### Dev Mode Description
Dev mode: Run the application locally for development purposes using a Postgres database and AWS Cognito for
authentication and initial authentication.

#### Dev Mode Configuration
Find the profiles section of `src/main/resources/application.yml`. (spring:profiles:active)
<br />
Set the active profiles to `dev,secrets`


#### Building and Running
```bash

# copy the example secrets file and update the API token and Cognito information in src/main/resources/application-secrets.yml
cp src/main/resources/application-secrets-example.yml src/main/resources/application-secrets.yml

# start the database using docker
docker run -d --rm --name paperless_paging_dev_db -e POSTGRES_DB=paperless_paging -e POSTGRES_USER=paperless_paging -e POSTGRES_PASSWORD=paperless_paging_password -p 5432:5432 -v ${PWD}/postgresql/data:/var/lib/postgresql/data postgres:15-alpine

# build the JAR file
mvn clean install

# run the application
java -jar target/*.jar
```

#### Stopping the docker database
```
docker stop paperless_paging_dev_db
```

## Deployment

### Building

#### With `docker compose`
```bash
git clone git@github.com:yalelibrary/paperless-paging.git
cd paperless-paging
git checkout main # or other branch
mkdir output
docker compose -f docker-compose-build.yml build
docker compose -f docker-compose-build.yml up
```
The Jar will bin in output directory: `output/paperless-paging-alma.jar`


#### With `docker` (alternative to docker compose)
```bash
git clone git@github.com:yalelibrary/paperless-paging.git
cd paperless-paging
git checkout main # or other branch
mkdir output
docker build . -f ./Dockerfile.build -t yul-paperless-paging-build-$(git rev-parse --short HEAD)
docker run --rm -v ./:/app/ yul-paperless-paging-build-$(git rev-parse --short HEAD)
docker image rm yul-paperless-paging-build-$(git rev-parse --short HEAD)
```
The Jar will bin in output directory: `output/paperless-paging-alma.jar`


#### Build Deploy Docker Image and Push to Dockerhub
`Dockerfile.deploy` can be used for running the application.<br />
```bash
docker build . -f ./Dockerfile.deploy -t yul-paperless-paging-$(git rev-parse --short HEAD)
echo Push this image to docker hub: yul-paperless-paging-$(git rev-parse --short HEAD)
```

### Running In Production Mode
Environment variables can be used to set values in the YAML files:
<br />
- `ALMA_API_TOKEN` will override alma:api:token in the yaml file
- For the datasource: `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_URL`
- For Cognito: `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_COGNITO_CLIENTID`, `SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_COGNITO_CLIENTSECRET`
- For the Environment: `SPRING_PROFILES_ACTIVE` (`tst`, `uat`, or `prod`)

For values that are not secrets, `application.yml` has profile sections for tst, uat, and prod.<br />

### Setting up Cognito
Configure a User Pool in AWS Cognito and create a group called `paperless-paging-admin`.
<br />
You may have to update `edu.yale.library.paperless.config.CognitoFilter` to meet your needs.

In the AWS User Pool configuration, create an Application Client with a ClientID and Secret.<br />
Put those values in the `application-secrets.yml` file or the corresponding environment variables.<br />
Configure the application to have the appropriate response URLs. For your test environment, use localhost without https.
