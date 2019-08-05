# Local verification of the Converter process

1. Start Solr and MySQL using `docker-compose`, in project root:

```bash
cd ./crawler-database
docker-compose up -d
```

Open Solr core [manufacturer product](http://localhost:8983/solr/#/manufacturer_product/core-overview), to see there are 0 documents.

2. Run Flyway migration to setup the MySQL schema, in project root:

```bash
./gradlew flywayMigrate
```

3. Import test data from `data/sample.sql`, in project root:

```bash
docker cp ./data/sample.sql crawler-database_mysql_1:/
docker exec -it crawler-database_mysql_1 bash
mysql -p -D website_crawler < /sample.sql 
```

4. Run converter to sync Solr, in project root:

```bash
./gradlew bootRun -Pargs=--proc=converter
```

Here's a sample output:

```
2019-07-31 23:38:29.479  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : Start converter on ALL-WEB-SITES with --only-data-cleanup=false
2019-07-31 23:38:29.522  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : Processing web site id=1, name=UNIQLO
2019-07-31 23:38:29.522  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : Running cleanup on website (id=1 name=UNIQLO)
2019-07-31 23:38:29.556  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : 1 expired pages marked as deleted.
2019-07-31 23:38:29.674  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : 1 pages to delete.
2019-07-31 23:38:29.766  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : SolrServer response to deleteById request: {responseHeader={status=0,QT
2019-07-31 23:38:29.847  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : Update WebSite (id=1, name=UNIQLO) last clean up time to Wed Jul 31 23:38:29 AEST 2019
2019-07-31 23:38:29.847  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : Running convert on website (id=1 name=UNIQLO)
2019-07-31 23:38:29.983  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : 3 pages to add/update.
2019-07-31 23:38:29.990  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : SolrServer response for solrClient.add: {NOTE=the request is processed in a background stream}
2019-07-31 23:38:30.421  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : Update WebSite (id=1, name=UNIQLO) last processed time to Wed Jul 31 23:38:30 AEST 2019
2019-07-31 23:38:30.422  INFO 15300 --- [           main] c.t.p.c.service.ConverterService         : WebSite (id=1, name=UNIQLO) stats: pages expired=1, pages deleted=1, pages added=2, pages updated=1.
```

5. Refresh the Solr Core Overview page to verify the documents were added. 