FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY *.jar /home/
ENTRYPOINT [ "sh", "-c", "java -Xmx512m -Djava.security.egd=file:/dev/./urandom -jar /home/resource-quota.jar" ]