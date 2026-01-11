FROM openjdk:8-jre-alpine

LABEL maintainer="lilishop"

# 设置时区
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone

WORKDIR /app

# 复制 jar 文件
COPY target/*.jar app.jar

# JVM 参数可通过环境变量配置
ENV JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8085

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
