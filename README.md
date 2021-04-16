
# Corbeans Yo! Cordapp [![Build Status](https://travis-ci.com/manosbatsis/corbeans-yo-cordapp.svg?branch=master)](https://travis-ci.com/manosbatsis/corbeans-yo-cordapp)

A [Corbeans](https://manosbatsis.github.io/corbeans/) project sample and template 
originally based on Joel Dudley's [yo-cordapp](https://github.com/corda/samples/tree/release-V3/yo-cordapp) by R3.

This project demonstrates how to apply Corbeans in order to create fully testable webapp/cordapp modules with 
Spring Boot and Corda. It also demonstrates some approaches towards more 
maintainable code using the following projects:

- [Partiture](https://manosbatsis.github.io/partiture/) for component-based Flows
- [Vaultaire](https://manosbatsis.github.io/vaultaire/) for auto-generating data access services, DTOs and query DSL for your Corda states
- [Corda RPC PoolBoy](https://manosbatsis.github.io/corda-rpc-poolboy/) for RPC connection pooling
- [Corda Testacles](https://github.com/manosbatsis/corda-testacles) for testing with MockNetwork, Node Driver, or Docker

## Quick HowTo

	> For Windows, use `gradlew.bat` instead of `./gradlew`

1. Start by cloning the `corbeans-yo-cordapp` template

```bash
git clone https://github.com/manosbatsis/corbeans-yo-cordapp.git
```

2. Navigate to the project directory

```bash
cd corbeans-yo-cordapp
```

3. Build the project and run unit tests

```bash
./gradlew clean build 
```

Note that unit tests for the Spring Boot module use the node driver.
You can also use the `integrationTest` task to execute the same tests
against a Docker-based corda network:


```bash
./gradlew clean build integrationTest -x test
```

This will implicitly call `deployNodes` as well (see below) and uses
Docker via [Testcontainers](https://www.testcontainers.org/)
(see also [Corda Testacles](https://manosbatsis.github.io/corda-testacles/testcontainers/)).

Both approaches run the same tests by extending `mypackage.server.AbstractRootTest`.

4. Deploy Corda nodes

```bash
./gradlew deployNodes
```

5. Run nodes and webserver

Linux/Unix:

```bash
cordapp-workflow/build/nodes/runnodes
```
Windows:

```bash
call cordapp/build/nodes/runnodes.bat
```

You can also uncomment `webPort` and `webserverJar` in build.gradle
to have `runnodes` launch Spring Boot as well, then browse the API:

http://localhost:8080/swagger-ui.html

## Project Modules

- **cordapp-contract**: States , contracts and tests with MockServices.
- **cordapp-workflow**: Flows and tests with MockNetwork.
- **bootapp-webserver**: Spring Boot app with a number of approaches to [integration testing](starter-test.html).

## Customisation

### Application Properties

You can configure nodes, logging and other options for either runtime or testing by editing
__server/src/main/resources/application.properties__  
or __server/src/test/resources/application.properties__ respectively.

### Custom Package

If you refactor from `mypackage` to your actual base package, make sure to update main and test sources
throughout project modules, along with the `corbeans.cordappPackages` property in both __application.properties__
files in the __bootapp-webserver__ module.

### Multiple Webservers

By default `runnodes` will only create a single webserver instance.
If a webserver per node is desired, uncomment "PartyB" node's `webPort` and `webserverJar`
in __cordapp-workflow/build.gradle__'s `Cordform` task.  
