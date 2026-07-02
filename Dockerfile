# Wakfu PdR Scraper — Forum Scraping Project

FROM eclipse-temurin:25-jre AS runtime

LABEL maintainer="Jérémy Laurent <contact@jeremy-laurent.fr>"
LABEL org.opencontainers.image.title="Wakfu PdR Scraper"
LABEL org.opencontainers.image.description="Wakfu PdR Scraper is a forum scraping project."
LABEL org.opencontainers.image.url="https://github.com/poulpy2k/wakfu-pdr-scraper"
LABEL org.opencontainers.image.source="https://github.com/poulpy2k/wakfu-pdr-scraper"
LABEL org.opencontainers.image.documentation="https://github.com/poulpy2k/wakfu-pdr-scraper#readme"
LABEL org.opencontainers.image.vendor="POULPY2K"
LABEL org.opencontainers.image.licenses="MIT"
LABEL org.opencontainers.image.authors="poulpyy"

ENV TZ="Europe/Paris"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl tzdata \
    && ln -snf /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo "${TZ}" > /etc/timezone \
    && dpkg-reconfigure -f noninteractive tzdata \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system --gid 674 wakfu \
    && useradd --system --uid 674 --gid wakfu \
       --shell /usr/sbin/nologin --no-create-home wakfu

WORKDIR /app

COPY --chmod=555 wakfu-pdr-scraper-*.jar wakfu-pdr-scraper.jar

USER wakfu:wakfu

EXPOSE 8081

ENV JAVA_OPTS="-Xmx2G -XX:+UseZGC"

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl --fail --silent http://localhost:8081/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "exec java ${JAVA_OPTS} -jar wakfu-pdr-scraper.jar"]
