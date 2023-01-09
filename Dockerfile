FROM tanxiubiao/jdk8:0.1
COPY /target/backup-0.0.1.jar /app/app.jar
ENV TZ=Asia/Shanghai
ENV SERVER_NAME="oracle-arm"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
CMD java -Dfile.encoding=GBK -jar /app/app.jar
