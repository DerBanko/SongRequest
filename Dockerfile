FROM gradle:8.2.1-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/
WORKDIR /home/gradle/
RUN gradle installDist --no-daemon

FROM openjdk:17.0.2-oracle
COPY --from=build /home/gradle/build/libs/*.jar /usr/app/songrequest.jar
COPY --from=build /home/gradle/build/install/SongRequest/lib/ /usr/app/lib/
WORKDIR /usr/app/
ENTRYPOINT ["java", "-jar", "songrequest.jar"]