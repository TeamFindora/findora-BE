# 멀티 스테이지 빌드를 사용하여 최적화된 이미지 생성
FROM --platform=linux/amd64 gradle:8.5-jdk21 AS build

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 파일들을 먼저 복사 (캐시 최적화)
COPY build.gradle settings.gradle ./
COPY gradle gradle
COPY gradlew gradlew.bat ./

# 의존성 다운로드 (캐시 최적화)
RUN gradle dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN gradle build --no-daemon

# 실행 스테이지
FROM --platform=linux/amd64 eclipse-temurin:21-jre

# 메타데이터 설정
LABEL maintainer="findora-team"
LABEL version="1.0"
LABEL description="Findora Spring Boot Application"

# 작업 디렉토리 설정
WORKDIR /app

# 보안을 위해 root가 아닌 사용자 생성
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 빌드된 JAR 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 사용자 변경
USER appuser

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"] 