# for base we will use ubuntu 18.04 LTS
FROM ubuntu:18.04

# Install JRE 8, lsof wget curl as these are used to install and run Solr
RUN apt update && \
  apt -y install openjdk-8-jre-headless lsof wget curl && \
  rm -rf /var/lib/apt/lists/*

# Set environment variables for SOLR

ENV SOLR_VERSION="8.1.1" \
    SOLR_URL="${SOLR_DOWNLOAD_SERVER:-https://archive.apache.org/dist/lucene/solr}/8.1.1/solr-8.1.1.tgz"



# Download and install Solr version 8.1.1 also remove the downloaded tar file and the solr documentation
RUN set -e; \
echo "downloading $SOLR_URL" && \
  wget -nv "$SOLR_URL" -O "/opt/solr-$SOLR_VERSION.tgz" && \
  tar -C /opt --extract --file "/opt/solr-$SOLR_VERSION.tgz" && \
  rm "/opt/solr-$SOLR_VERSION.tgz"* && \
  rm -Rf /opt/solr/docs/ 

# Expose ports 8983
EXPOSE 8983

# Run Solr inside container -force to run as root and -f to run in foreground , this won't exit container. 
CMD  cd /opt/solr-${SOLR_VERSION} && \
  bin/solr start -force -p 8983 -f && \
# Create Core for TC Challenge#2
 bin/solr create -c manufacturer_product -force && \
# Create Schema
 curl -X POST -H 'Content-type:application/json' --data-binary '{"add-field": {"name":"manufacturer_name", "type":"string", "required":true, "indexed":true, "stored":true}}' http://localhost:8983/solr/manufacturer_product/schema

# build Dockerfile: docker build -t testsolr .
# run docker container: docker run -t --name testcont-solr -p 8983:8983 testsolr:latest


