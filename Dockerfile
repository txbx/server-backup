FROM tanxiubiao/maven:0.2
COPY /target/backup-0.0.1.jar /app/app.jar
ENV TZ=Asia/Shanghai
ENV SERVER_NAME="oracle-arm"
ENV LANG C.UTF-8
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
CMD java -Dfile.encoding=UTF-8 -jar /app/app.jar