
## Building the Library

The project uses [Maven](http://maven.apache.org/) as its build tool and dependency manager, so you'll need to install Maven.

The dependencies are defined in `pom.xml` files in each directory discussed below.  Maven reads this file to determine what other libraries it needs to download
before it can start the build.  Hibernate has its own dependencies, so expect Maven to install many jars before 
actually compiling.

The current breeze.server.java GitHub repository consists of 4 directories containing java code.  Two of these comprise the primary breeze libraries

- breeze-hibernate ( core breeze/hibernate functionality)
- breeze-webserver ( additions to make it easier to build java servlet app)

and two of these are test directories for building and running a breeze test harness web application.

- breeze-northwind ( hibernate test domain model)
- breeze-webtest   ( test web application) - has dependencies on the breeze.js GitHub repo for the actual client side tests.

## Build and testing
 
There is also a 'build' directory with a parent `pom.xml` file and a build.bat file that attempts to build the entire project including tests, and then loads the web app onto an Apache Tomcat server. Before running this batch file, please understand the dependencies listed below. 

#### Test dependencies.

- You will need to install Apache Tomcat to run the tests.
- You will also need to download the 'breeze.js' GitHub repo. 
- You will need to edit the local 'setEnv.bat' file in the build.bat directory to point to your local Tomcat installation and the location of the 'breezeJs' github repo on your machine.  
- You can then run build.bat which will build the test project and launch Tomcat in debug mode with the war file created during the build.
- On a browser, go to [http://localhost:8080/breeze-webtest/](http://localhost:8080/breeze-webtest/) 


## Using Eclipse or Other IDE

In order to edit and build using an IDE, you will first need to use Maven to get the jars of all the dependencies, as described under "Dependencies" above.  Then you 
can include the jars in the classpath of the project in your IDE.

For Eclipse, the `.classpath` and `.project` files are provided in each of the directories mentioned above.  After performing the 'build' command described above, you can open Eclipse and use "import project" to use the existing project definition.

