# Base image with Java 1.8
FROM openjdk:8-jdk-alpine

# Set up the necessary directories
WORKDIR /app

# Install required packages and tools
RUN apk update && \
    apk add bash wget curl tar && \
    wget https://github.com/paulp/sbt-extras/raw/master/sbt -O /usr/local/bin/sbt && \
    chmod +x /usr/local/bin/sbt

# Install Scala
RUN wget https://downloads.lightbend.com/scala/2.12.10/scala-2.12.10.tgz && \
    tar -xzvf scala-2.12.10.tgz && \
    mv scala-2.12.10 /usr/local/scala && \
    ln -s /usr/local/scala/bin/scala /usr/local/bin/scala && \
    ln -s /usr/local/scala/bin/scalac /usr/local/bin/scalac

# Install Spark
RUN curl -O https://archive.apache.org/dist/spark/spark-3.1.3/spark-3.1.3-bin-hadoop3.2.tgz && \
    tar -xzvf spark-3.1.3-bin-hadoop3.2.tgz && \
    mv spark-3.1.3-bin-hadoop3.2 /usr/local/spark

# Set environment variables
ENV SPARK_HOME=/usr/local/spark
ENV PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin

# Copy project files
COPY . .

# Compile and package the project
RUN sbt package && ls target/scala-2.12/

# Run tests
RUN sbt test

# Specify the command to run the main application
CMD ["spark-submit", "--class", "MemberEligibilityReport", "target/scala-2.12/memberreportgenerator_2.12-0.1.0-SNAPSHOT.jar"]