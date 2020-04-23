# Building OpenKit Java
## Prerequisites for building
### Building the Source/Generating the JavaDoc
* Java Development Kit (JDK) 6, 7, 8 or 11
  * Environment Variable JAVA_HOME set to JDK install directory

### (Unit-)Testing the OpenKit
* Java Runtime Environment (JRE) 6, 7, 8 or 11  
  Dependencies for testing (JUnit, Hamcrest, Mockito) are managed by Gradle.

## Building the Source
Navigate to OpenKit's top level directory and run the following command in your shell.

* Windows command prompt 
  ```shell
  gradlew jar
  ```
* Linux/UNIX shell
  ```shell
  ./gradlew jar
  ```

The built jar file(s) `openkit-<version>.jar` will be located in the `build/libs` directory.

## Running all checks
Navigate to OpenKit's top level directory and run the following command in your shell.

* Windows command prompt 
  ```shell
  gradlew test
  ```
* Linux/UNIX shell
  ```shell
  ./gradlew test
  ```

## Generating the JavaDoc
Navigate to OpenKit's top level directory and run the following command in your shell.

* Windows command prompt 
  ```shell
  gradlew javadoc
  ```
* Linux/UNIX shell
  ```shell
  ./gradlew javadoc
  ```

The generated javadoc will be located in the `build/docs/javadoc` directory.
