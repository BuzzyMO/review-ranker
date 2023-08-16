FROM adoptopenjdk:11-jre-hotspot

ENV RANKER_FILE review-ranker-assembly-0.1.0-SNAPSHOT.jar

COPY target/scala-2.13/$RANKER_FILE /app/$RANKER_FILE

EXPOSE 8080

WORKDIR /app
ENTRYPOINT ["sh", "-c"]
CMD ["exec java -jar $RANKER_FILE"]