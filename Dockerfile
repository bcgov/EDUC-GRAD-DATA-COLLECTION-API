FROM artifacts.developer.gov.bc.ca/docker-remote/maven:3.9.9-amazoncorretto-21-alpine AS build
WORKDIR /workspace/app

COPY api/pom.xml .
COPY api/src src
RUN mvn package -DskipTests \
&& mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

FROM artifacts.developer.gov.bc.ca/docker-remote/openjdk:21-jdk-oracle
RUN useradd -ms /bin/bash spring \
&& mkdir -p /logs && mkdir -p /temp \
&& chown -R spring:spring /logs && chown -R spring:spring /temp \
&& chmod 755 /logs && chmod 755 /temp
USER spring
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-Duser.name=EDUC-GRAD-DATA-COLLECTION-API","-Xms600m","-Xmx800m","-noverify","-XX:TieredStopAtLevel=1","-XX:+UseParallelGC","-XX:MinHeapFreeRatio=20","-XX:MaxHeapFreeRatio=40","-XX:GCTimeRatio=4","-XX:AdaptiveSizePolicyWeight=90","-XX:MaxMetaspaceSize=300m","-XX:ParallelGCThreads=2","-Djava.util.concurrent.ForkJoinPool.common.parallelism=8","-XX:CICompilerCount=2","-XX:+ExitOnOutOfMemoryError","-Dspring.profiles.active=openshift","-Djava.security.egd=file:/dev/./urandom","-cp","app:app/lib/*","ca.bc.gov.educ.graddatacollection.api.GradDataCollectionApiApplication"]
