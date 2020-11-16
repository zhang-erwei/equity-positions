FROM hub.cesinstitute.com.cn/public/jdk:8u192-centos1804ces
VOLUME /tmp
ARG FINAL_NAME
ARG PROFILE_NAME

ADD ${FINAL_NAME}.jar /app.jar
ENV JAVA_OPTS="-Xmx256m -Duser.timezone=GMT+8"
ENV JPDA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=18868"
#ENV JPDA_OPTS=" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=18868"
ENV PROFILE="${PROFILE_NAME}"
ENV CONFIG_WKK=""
ENV CALLBACK_ENV=""
ENV JOB_ADDRES=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS $JPDA_OPTS $CONFIG_WKK -Djava.security.egd=file:/dev/./urandom -DcallbackUrl=$CALLBACK_ENV -Dxxl.job.executor.address=$JOB_ADDRES -Dspring.profiles.active=$PROFILE -jar /app.jar"]
