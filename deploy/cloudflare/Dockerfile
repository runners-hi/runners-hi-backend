# 배포 워크플로가 ./gradlew bootJar 결과를 app.jar로 복사한 뒤 빌드한다
FROM --platform=linux/amd64 eclipse-temurin:17-jre
COPY app.jar /app/app.jar
WORKDIR /app
EXPOSE 8080
# 적은 CPU에서 빨리 뜨도록 가벼운 GC와 1단계 JIT를 쓴다
ENTRYPOINT ["java","-XX:+UseSerialGC","-XX:TieredStopAtLevel=1","-XX:MaxRAMPercentage=70","-Xshare:auto","-jar","/app/app.jar"]
