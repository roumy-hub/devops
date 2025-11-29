FROM alpine:latest

RUN apk add openjdk21

EXPOSE 80

CMD [ "java", "-version"]