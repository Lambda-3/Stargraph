FROM openjdk:8-jdk

RUN apt-get update \
    && apt-get install -y --no-install-recommends wordnet wordnet-sense-index \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /usr/share/stargraph

# OpenNLP models for DE, EN and PT
ADD http://opennlp.sourceforge.net/models-1.5/de-token.bin opennlp/
ADD http://opennlp.sourceforge.net/models-1.5/en-token.bin opennlp/
ADD http://opennlp.sourceforge.net/models-1.5/pt-token.bin opennlp/
ADD http://opennlp.sourceforge.net/models-1.5/de-pos-maxent.bin opennlp/
ADD http://opennlp.sourceforge.net/models-1.5/en-pos-maxent.bin opennlp/
ADD http://opennlp.sourceforge.net/models-1.5/pt-pos-maxent.bin opennlp/


ADD stargraph-server-${project.version}-dist.tar.gz .

WORKDIR /usr/share/stargraph/bin

EXPOSE 8917
CMD ["./stargraph"]