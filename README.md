## REST API Service

### Run tests

The DB is simulated by code.
The service should be launched and since we simulate a database and it's not reinitialised, the tests pass on the first try.
I ~could~ should reinitialise them as it stupid easy to do.

### Run through docker (recommended)

Run `docker-compose up service`.

Please have a look on `docker-compose.yml` file for more details on the configuration.

#### Build and publish image in local

`sbt service/assembly && sbt service/docker`