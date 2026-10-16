# Build image
FROM maven:3.9.12-eclipse-temurin-21-alpine AS buildimage

ADD . /rmlmapper-java

WORKDIR /rmlmapper-java
RUN mvn -Pno-buildnumber clean package -DskipTests=true
RUN mv $(readlink -f target/rmlmapper-*-all.jar) /rmlmapper.jar

# "Runtime" image
FROM eclipse-temurin:25-jre
COPY --from=buildimage /rmlmapper.jar /rmlmapper.jar

WORKDIR /data

ENTRYPOINT ["java", "-jar", "/rmlmapper.jar"]
