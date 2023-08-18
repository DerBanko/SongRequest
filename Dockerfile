FROM gradle:8.2.1-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/
WORKDIR /home/gradle/
RUN gradle installDist --no-daemon

FROM openjdk:17.0.2-oracle
COPY --from=build /home/gradle/build/libs/*.jar /usr/app/butils-bot.jar
COPY --from=build /home/gradle/build/install/gradle/lib/ /usr/app/lib/
WORKDIR /usr/app/
ENTRYPOINT ["java", "-jar", "SpotifySR-1.0-SNAPSHOT.jar"]