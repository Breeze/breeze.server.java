
## Building the Library

The project uses [Maven](http://maven.apache.org/) as its build tool and dependency manager, so you'll need to install Maven.

The dependencies are defined in the `pom.xml` file.  Maven reads this file to determine what other libraries it needs to download
before it can start the build.  Hibernate and Jersey each have their own dependencies, so expect Maven to install many jars before 
actually compiling.  Type 

	mvn package

to compile the sources and build `breezejs-hibernate-{version}.jar`.  

##### Dependency Jars

When you deploy the jar, you will need to deploy all the of the 
Hibernate and Jersey dependencies along with it.  To get all the jars in one handy directory, use

	mvn assembly:single

This will create the directory `breezejs-hibernate/target/breezejs-hibernate-0.1a-lib/lib` which contains all the necessary jars.  You might want to do this so that you can include the jars in the build path for your IDE.

##### JAR With Dependencies

If needed, you can create a single jar that has all of the dependencies included.  To do this, edit the pom.xml file.  **Comment out** the line

	<descriptor>src/main/assembly/lib.xml</descriptor>
	
and **uncomment** the line

	<descriptorRef>jar-with-dependencies</descriptorRef>
	
Then use 

	mvn assembly:single
	
and it will create `breezejs-hibernate/target/breezejs-hibernate-0.1a-jar-with-dependencies.jar` which has all the classes from all the 
jars discussed above.

## Using Eclipse or Other IDE

In order to edit and build using an IDE, you will first need to use Maven to get the jars of all the dependencies, as described under "Dependencies" above.  Then you 
can include the jars in the classpath of the project in your IDE.

For Eclipse, the `.classpath` and `.project` files are provided in the breezejs-hibernate 
directory.  After performing the `mvn assembly:single` command described above, you can open Eclipse and use "import project" to use the existing project definition.

