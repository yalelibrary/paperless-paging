FROM amazoncorretto:21-alpine-jdk
RUN mkdir -p /app/paperless-paging/jars
WORKDIR /app/paperless-paging/jars
COPY target/paperless-paging-alma.jar /app/paperless-paging/jars
EXPOSE 8080/tcp
CMD ["java", "-Xmx1536m", "-jar", "paperless-paging-alma.jar"]