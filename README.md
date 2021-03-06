# Rest Prime Sieve - Coding Exercise

## Outline
- A RESTful service that calculates and returns all the prime numbers up to an including a number provided.
It uses Akka and the Actor model to implement a parallel version on the Segmented Sieve of Eratostenes https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes#Segmented_sieve.
- It also has and endpoint that streams prime numbers using chunked http response when the input number is very big, in order to avoid out of memory errors.

### Example
The REST call should look like http://localhost:9000/primes/10 and should return JSON content:
```
{
  "initial": 10
  "primes": [2,3,5,7]
}
```

- The service supports buth JSON and XML and the consumer can choose the format with the `accept` header
- For detailed API documentation, launch the service and point your browser to the swagger ui http://localhost:9000/docs/index.html
  where you can find explanation about the enpoonts (urls, header, request params, data format, errors, etc)

## Implementation
The service is implemented in Java8 with [Play! Framework](http://www.playframework.com).
Uses [Akka](http://www.akka.io) and Actor model to implement a parallel algorithm based on the [Segmented Sieve of Erathostenes](https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes#Segmented_sieve)

### How to
- `./activator test` to run unit and integration tests
- `./activator run` to run in dev mode (app will be available at http://localhost:9000)
- `./activator start` to run in prod mode (app will be available at http://localhost:9000)
- `./activator start -J-Xmx4096m -J-server` to run in prod mode with increased heap space

#### Important Note!!!
The first time 'activator' is executed the it'll download all the Play framework and app dependencies so it will take few minutes!

## Code Coverage
- run `./activator jacoco:cover` form the project folder
- The html report will be at `./target/scala-2.11/jacoco/html/index.html`

### Few interesting links i found about parallelisation of prime number geerators:
- [Sieve of Erathostenes](https://en.wikipedia.org/wiki/Sieve_of_Eratosthenes)
- [Generating Primes](https://en.wikipedia.org/wiki/Generating_primes)
- https://www.quora.com/What-is-the-segmented-sieve-of-Eratosthenes
- http://stackoverflow.com/questions/9307180/parallelizing-sieve-of-eratosthenes-algorithm-for-finding-prime-number
- http://primesieve.org/segmented_sieve.html
- http://programmingpraxis.com/2010/02/05/segmented-sieve-of-eratosthenes/
- http://www.massey.ac.nz/~mjjohnso/notes/59735/seminars1/01077635.pdf
- http://www.uio.no/studier/emner/matnat/ifi/INF3380/v12/undervisningsmateriale/inf3380-sieve.pdf
- http://rosettacode.org/wiki/Sieve_of_Eratosthenes#Java
- http://compoasso.free.fr/primelistweb/page/prime/eratosthene_en.php
- http://cs-alb-pc3.massey.ac.nz/notes/59735/seminars/01077635.pdf
- http://stackoverflow.com/questions/4700107/how-do-i-implement-the-sieve-of-eratosthenes-using-multithreaded-c


