FROM tanxiubiao/jdk8:0.1
COPY /backup-0.0.1.jar /app/app.jar
ENV TZ=Asia/Shanghai
ENV SERVER_NAME="oracle-arm"
ENV PARENT="1tnwC9TV8ZdbnGxZQhFwyTwuBjVdFJVz4"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
CMD java -Dfile.encoding=UTF-8 -jar /app/app.jar