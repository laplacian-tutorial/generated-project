FROM adoptopenjdk:11-jdk-hotspot
RUN mkdir /app

COPY ./build/libs/db-migrate*.jar /app/db-migrate.jar

COPY ./entrypoint.sh /app/entrypoint.sh
ENTRYPOINT ["/app/entrypoint.sh"]