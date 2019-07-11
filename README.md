To build in project root run:
$gradle build

To run test case in project root run:
$gradle test

To run the website-crawler in project root run:
$gradle run

To build the docker image of website-crawler:
$cd crawler-service
$docker build -t website-crawler .

To run the website-crawler app inside the docker image:
$docker run website-crawler
