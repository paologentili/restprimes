# Rest Primes - Coding Exercise  - Requirements
## Project Outline
Write a RESTful service that calculates and returns all the prime numbers up to an including a number provided.
### Example
The REST call should look like http://your.host.com/primes/10 and should return JSON content:
```
{
  “Initial”:  “10
  “Priimes”: [2,3,5,7]
}
```
### Requirements
- The project must be written in Java 7 / 8.
- The project must use Maven OR Gradle to build, test and run.
- The project must have unit and integration tests.
- The project must run, in that the service should be hosted in a container e.g. Tomcat, Jetty, Spring Boot etc.
- You may use any frameworks or libraries for support e.g. Spring MVC, Apache CXF, Jackson etc.
- The project must be accessible from Github.

### Optional Extensions
- Consider supporting varying return content type such as XML based on requested media type.
- Consider ways to improve performance e.g. caching, threading
- Consider supporting multiple algorithms based on optional parameters


# ====== Solution Documentation =======
- The implementation uses Play! Framework which uses
    - Netty as container
    - SBT as build tool
    - is written using Java8
    - Uses Guice for DI
    - Uses AKKA and ACtor model to implement a parallel algorithm based on the Segmented Sieve of Erathostenes https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes#Segmented_sieve

## Important Note!!!
The following instructions ask you to use a command line tool called `./activator`.
The first time 'activator' is executed the it'll download all the Play framework and app dependencies so it will take few minutes!

From the project folder:
1) `./activator test` to run unit and integration tests
2) `./activator run` to run in dev mode
3) `./activator start` to run in prod mode
2) `./activator start -J-Xmx4096m -J-server` to run in prod mode with increased heap space

## Swagger Api Documentation
- Swagger console available at http://localhost:9000/docs/index.html
- it contains documentation on the REST api like parameters, conten-types etc. and it allow to call the api 

## Code Coverage
- run `./activator jacoco:cover` form the project folder
- the html report will be at ./target/scala-2.11/jacoco/html/index.html
