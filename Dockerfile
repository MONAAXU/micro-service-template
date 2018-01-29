FROM centos

# Set the working directory to /micro-service-template
WORKDIR /micro-service-template

# Copy the current directory contents into the container at /micro-service-template
ADD . /micro-service-template

RUN curl https://bintray.com/sbt/rpm/rpm | tee /etc/yum.repos.d/bintray-sbt-rpm.repo

RUN yum -y install sbt

RUN yum install -y \
       java-1.8.0-openjdk \
       java-1.8.0-openjdk-devel

ENV JAVA_HOME /etc/alternatives/jre

RUN sbt compile

# Make port 8080 available to the world outside this container
EXPOSE 8080