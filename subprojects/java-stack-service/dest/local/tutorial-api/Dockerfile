FROM adoptopenjdk:11-jdk-hotspot
RUN mkdir /app

COPY ./build/libs/tutorial-api*.jar /app/api.jar

COPY ./entrypoint.sh /app/entrypoint.sh
ENTRYPOINT ["/app/entrypoint.sh"]