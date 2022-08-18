FROM eclipse-temurin:17-alpine as builder
WORKDIR /build/DisChat

ADD . .

RUN ./gradlew --no-daemon build --stacktrace --info

FROM gcr.io/distroless/java17-debian11:nonroot

WORKDIR /runtime/
COPY --chown=nonroot:nonroot --from=builder /build/DisChat/build/libs/*-all.jar DisChat.jar

WORKDIR /run/

ADD --chown=nonroot:nonroot run/config.yml .

USER nonroot:nonroot

CMD ["/runtime/DisChat.jar"]