FROM maven:3.5.4-jdk-10-slim AS builder
COPY . /usr/src/microscopy-metadata-service
WORKDIR /usr/src/microscopy-metadata-service
RUN mvn -Pdocker package

FROM openjdk:10.0.2-jre-slim-sid
VOLUME /logs
RUN mkdir /temps
ENV SPRING_BOOT_APP leanda-microscopy-metadata-service.jar
ENV SPRING_BOOT_APP_JAVA_OPTS -Xmx256m -XX:NativeMemoryTracking=summary
WORKDIR /app
RUN apt-get update && apt-get install -y curl
RUN curl https://raw.githubusercontent.com/vishnubob/wait-for-it/master/wait-for-it.sh > /app/wait-for-it.sh && chmod 777 /app/wait-for-it.sh
COPY --from=builder /usr/src/microscopy-metadata-service/target/$SPRING_BOOT_APP ./
ENTRYPOINT java $SPRING_BOOT_APP_JAVA_OPTS -jar $SPRING_BOOT_APP

