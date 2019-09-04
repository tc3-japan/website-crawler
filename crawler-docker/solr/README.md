# Solr Docker Image for Product Search Service

## Build and Run

1. Build image
   ```bash
   docker build -t tc3jp/productsearch-solr:1.0 .
   ```

1. Run image
   ```bash
   docker run -t --name solr -p 8983:8983 tc3jp/productsearch-solr:1.0
   ```
