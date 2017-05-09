![](stargraph_logo2_black.png)

# StarGraph

An Open Source graph database to query large Knowledge Graphs. StarGraph support natural language queries facilitating the interaction between domain experts and data.

# Features

* Natural language interface
* Easy integration of structured and unstructured data
* Built-in semantic approximations
* Multi-lingual
* REST-like interface

# Demo: Querying The Wikipedia Knowledge Graph

To give you a more hands-on experience we've prepared a docker based deployment ready to consume the public dumps of __DBpedia 2016__. Please follow this instructions:

## Software Requirements

These are our recommendations and does not mean that Stargraph will not work on other tools and platforms. We appreciate feedbacks as well.

* Docker and Docker Compose: Our test environments are using version 1.17 and 1.11 respectively.
* Linux OS: We adopted Ubuntu LTS Server. Probably from ubuntu trusty onwards you're safe.

## Hardware Requirements

These are our recommendations.  

* Around 60GB of free disk space (SSD is a big plus)
* 16GB of RAM but we strongly recommend 32GB
* +4 CPUs
* Internet connection to download all resources.

## Installation

Stargraph uses two majors Open Source software components:

* [Indra](https://github.com/Lambda-3/Indra)
* [Elasticsearch](https://github.com/elastic/elasticsearch)

For this demonstration we use a public hosted version of Indra. More information [here](https://github.com/Lambda-3/Indra#public-endpoint) hence you need internet connection. If this is not possible follow [this](https://github.com/Lambda-3/IndraComposed) instruction to have Indra in your infrastructure.

### Starting Up

_Docker Compose_ will assemble all software components and ease the setup of this Demo. Just copy and paste this file as __docker-compose.yml__ in your filesystem.

```yml
version: '2'

services:
 
  elastic:
    image: elasticsearch:5.1.1
    container_name: elastic
    command: "-Ecluster.name=StarGraphV2Cluster -Ebootstrap.memory_lock=true -Ediscovery.zen.minimum_master_nodes=1"
    expose:
      - 9300
    volumes:
      - ./esdata:/usr/share/elasticsearch/data
    ulimits:
      memlock:
        soft: -1
        hard: -1
      nofile:
        soft: 65536
        hard: 65536
    mem_limit: 8g
    cap_add:
      - IPC_LOCK
    environment:
      - "ES_JAVA_OPTS=-Xms7g -Xmx7g"

  stargraph:
    image: lambdacube/stargraph:latest
    container_name: stargraphv2
    ports:
      - 8917:8917
    volumes:
      - ./logs:/usr/share/stargraph/logs
      - ./data:/usr/share/stargraph/data
    mem_limit: 8g
    environment:
      - "STARGRAPH_JAVA_OPTS=-Xms7g -Xmx7g"
 ```
      
.. now run the following command in your favorite terminal:

```shell
$ docker-compose up
```

This will download the docker image for [Stargraph](https://hub.docker.com/r/lambdacube/stargraph/) and also setup a local instance of [Elastic Search Server](https://hub.docker.com/_/elasticsearch/) from the officials repositories. On the first run this can take some minutes to finish.

After a while you should notice that Stargraph is up and listening on port 8917. 

### Load DBpedia-2016 data

Stargraph needs to build a set of indices to be able to answer your queries. Ensure that Stargraph is listening on port 8917 and issue the following command using _cURL_ or any other HTTP client you like. At this point Stargraph will use internet connection to fetch the DBpedia dumps.

First test. List the pre-configure datasets for DBpedia.

```shell
$ curl -XGET http://localhost:8917/_kb
["dbpedia-2016/facts","dbpedia-2016/entities","dbpedia-2016/relations"] 
```
It's all good. Let's populate those indices.

```shell
$ curl -XPOST http://localhost:8917/_kb/dbpedia-2016/_load
{"ack": true}
```

Stargraph acknowledges your command and will start building the indices. Actually this can take few hours depending on your hardware.

## Querying DBpedia

Use your favorite HTTP client to ask something to Stagraph: _Who is the wife of Barack Obama?_ 

```shell
$ curl -v -XGET "http://localhost:8917/_kb/dbpedia-2016/query?q=Who%20is%20the%20wife%20of%20Barack%20Obama?"
```
Note that the question is URL encoded, this is a requirement to use _cURL_ properly.

And the output is a JSON response.

```json
{
  "query": "Who is the wife of Barack Obama?",
  "sparqlQuery": "SELECT * WHERE {\n{<http://dbpedia.org/resource/Barack_Obama> <http://dbpedia.org/ontology/child> ?VAR_1} UNION \n{<http://dbpedia.org/resource/Barack_Obama> <http://dbpedia.org/property/spouse> ?VAR_1} UNION \n{<http://dbpedia.org/resource/Barack_Obama> <http://dbpedia.org/property/children> ?VAR_1}\n}",
  "interactionMode": "NLI",
  "answers": [
    {
      "id": "dbr:Michelle_Obama",
      "value": "Michelle Obama",
      "score": 1
    },
    {
      "id": "\"1992-10-03\"^^<http://www.w3.org/2001/XMLSchema#date>",
      "value": "1992-10-03",
      "score": 1
    }
  ],
  "mappings": {
    "Barack Obama": [
      {
        "id": "http://dbpedia.org/resource/Barack_Obama",
        "value": "Barack Obama",
        "score": 1
      }
    ],
    "wife": [
      {
        "id": "http://dbpedia.org/ontology/child",
        "value": "child",
        "score": 0.560512271511479
      },
      {
        "id": "http://dbpedia.org/property/spouse",
        "value": "spouse",
        "score": 0.5078599088550189
      },
      {
        "id": "http://dbpedia.org/property/children",
        "value": "children",
        "score": 0.5803166605881719
      }
    ]
  }
}
```
