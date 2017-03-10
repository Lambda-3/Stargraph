FROM openjdk:8-jdk

RUN apt-get update \
    && apt-get install -y --no-install-recommends wordnet wordnet-sense-index \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/share/stargraph
ADD stargraph-server-${project.version}-dist.tar.gz .
WORKDIR /usr/share/stargraph/bin

EXPOSE 8917
CMD ["./stargraph"]