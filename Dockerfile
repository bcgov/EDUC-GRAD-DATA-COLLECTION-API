FROM artifacts.developer.gov.bc.ca/docker-remote/maven:3.9.9-amazoncorretto-21-alpine AS maven

# Use the timezone-patched JDK for both compilation and the final runtime.
FROM artifacts.developer.gov.bc.ca/docker-remote/amazoncorretto:21.0.12-alpine3.23 AS build
COPY --from=maven /usr/share/maven /usr/share/maven
ENV PATH="/usr/share/maven/bin:${PATH}"
RUN apk add --no-cache fontconfig ttf-dejavu
WORKDIR /workspace/app

COPY api/pom.xml .
COPY api/src src
RUN mvn package -DskipTests \
&& mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM artifacts.developer.gov.bc.ca/docker-remote/amazoncorretto:21.0.12-alpine3.23

RUN apk add --no-cache fontconfig ttf-dejavu
RUN adduser -D -s /bin/sh spring \
&& mkdir -p /logs && mkdir -p /temp \
&& chown -R spring:spring /logs && chown -R spring:spring /temp \
&& chmod 755 /logs && chmod 755 /temp
USER spring
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-Duser.name=EDUC-GRAD-DATA-COLLECTION-API","-Xms600m","-Xmx800m","-XX:TieredStopAtLevel=1","-XX:+UseParallelGC","-XX:MinHeapFreeRatio=20","-XX:MaxHeapFreeRatio=40","-XX:GCTimeRatio=4","-XX:AdaptiveSizePolicyWeight=90","-XX:MaxMetaspaceSize=300m","-XX:ParallelGCThreads=2","-Djava.util.concurrent.ForkJoinPool.common.parallelism=8","-XX:CICompilerCount=2","-XX:+ExitOnOutOfMemoryError","-Dspring.profiles.active=openshift","-Djava.security.egd=file:/dev/./urandom","-cp","app:app/lib/*","ca.bc.gov.educ.graddatacollection.api.GradDataCollectionApiApplication"]
