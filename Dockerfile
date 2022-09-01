FROM maven:3.5-jdk-8-alpine as builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn package -DskipTests

CMD ["java","-jar","/app/target/suibe_mma.jar","--spring.profiles.active=prod","-Ddruid.mysql.usePingMethod=false"]