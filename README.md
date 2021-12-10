# T30-Bicloin

Distributed Systems 2020-2021, 2nd semester project


## Authors

**Group T30**

93709 [Francisca Almeida](mailto:francisca.almeida@tecnico.ulisboa.pt)

93716 [Gonçalo Antunes](mailto:goncalo.nuno.antunes@tecnico.ulisboa.pt)

93753 [Rodrigo Pedro](mailto:rodrigorpedro@tecnico.ulisboa.pt)

### Module leaders

T1 - Rodrigo Pedro 93753

T2 - Francisca Almeida 93709

T3 - Gonçalo Antunes 93716

For each module, the README file must identify the lead developer and the contributors.
The leads should be evenly divided among the group members.

### Code identification

In all the source files (including POMs), please replace __CXX__ with your group identifier.  
The group identifier is composed by Campus - A (Alameda) or T (Tagus) - and number - always with two digits.

This change is important for code dependency management, to make sure that your code runs using the correct components and not someone else's.


## Getting Started

The overall system is composed of multiple modules.

See the project statement for a full description of the domain and the system.

### Prerequisites

Java Developer Kit 11 is required running on Linux, Windows or Mac.
Maven 3 is also required.

To confirm that you have them installed, open a terminal and type:

```sh
javac -version

mvn -version
```

### Installing

To compile and install all modules:

```sh
mvn clean install -DskipTests
```

The integration tests are skipped because they require theservers to be running.

First, start the _Rec_ server:
```sh
cd rec/
mvn compile exec:java
```

Then, do the same for _Hub_.
```sh
cd hub/
mvn compile exec:java
```

Then to run tests, execute the _verify_  maven plugin in the source folder:
```sh
mvn verify
```

To run the _App_ with the maven _exec_ plugin:
```sh
cd app/
mvn compile exec:java
```

To run the _App_ with an input file:
```sh
cd app/
mvn compile exec:java < inputfile
```

## Built With

* [Maven](https://maven.apache.org/) - Build Tool and Dependency Management
* [gRPC](https://grpc.io/) - RPC framework  


## Versioning

We use [SemVer](http://semver.org/) for versioning. 
