FROM 10.98.86.99:8085/library/openjdk:17-alpine

WORKDIR /app
RUN touch /app/app.log
COPY ./target/*.jar /app/app.jar

CMD java -jar /app/app.jar 