# Breeze WebTest steps

   - Deploy breeze-hibernate jar to local maven repo 
     - From: {breeze-hibernate root} Execute: mvn install
   - Build assembly for local eclipse use
     -  From: {breeze-hibernate root} Execute: mvn assembly:single
   - Build breeze-webTest 
     - From: {breeze-webTest root} Execute: mvn package   
   -  Start apache TomCat 
     - From {TomCat install dir}/bin  Execute: Startup.bat
   -  Hit the webServer:
     - From {browser} Execute: http://localhost:8080/breeze-intest/ 