# 배포 워크플로가 ./gradlew bootJar 결과를 app.jar로 복사한 뒤 빌드한다
FROM --platform=linux/amd64 eclipse-temurin:17-jre AS base
RUN java -XX:+UseSerialGC -Xshare:dump

# 쉬던 서버가 깨어날 때 첫 요청이 오래 기다리지 않도록 클래스 적재 결과를 미리 저장한다.
# 학습용 값은 이 단계에서만 쓰고 최종 이미지에 들어가지 않는다.
FROM base AS train
RUN apt-get update && apt-get install -y --no-install-recommends postgresql
COPY app.jar /build/app.jar
RUN java -Djarmode=tools -jar /build/app.jar extract --destination /app \
  && service postgresql start \
  && until su postgres -c pg_isready; do sleep 1; done \
  && su postgres -c "psql -c \"alter user postgres password 'train'\"" \
  && SPRING_PROFILES_ACTIVE=prod \
     DB_URL=jdbc:postgresql://localhost:5432/postgres DB_USERNAME=postgres DB_PASSWORD=train \
     JWT_SECRET=train-only-secret-train-only-secret-train-only-secret-train-only-secret-train-only-secret \
     java -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -XX:ArchiveClassesAtExit=/app/app.jsa \
       -Dspring.context.exit=onRefresh -jar /app/app.jar \
  && test -s /app/app.jsa

FROM base
COPY --from=train /app /app
WORKDIR /app
EXPOSE 8080
# 적은 CPU에서 빨리 뜨도록 가벼운 GC와 1단계 JIT를 쓴다
ENTRYPOINT ["java","-XX:+UseSerialGC","-XX:TieredStopAtLevel=1","-XX:MaxRAMPercentage=70","-XX:SharedArchiveFile=/app/app.jsa","-Xshare:auto","-jar","/app/app.jar"]
