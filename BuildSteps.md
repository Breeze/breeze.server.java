# Breeze WebTest steps

   1) - Deploy breeze-northwind jar to local maven repo
        - From: {breeze-northwind root} Execute: mvn install
      - Build breeze-northwind class files for local eclipse use
        -  From: {breeze-northwind root} Execute: mvn assembly:single
  
   2) - Deploy breeze-hibernate jar to local maven repo 
        - From: {breeze-hibernate root} Execute: mvn install
      - Build breeze-hibernate class files for local eclipse use
        -  From: {breeze-hibernate root} Execute: mvn assembly:single
       
   3) - Build breeze-webTest 
        - From: {breeze-webTest root} Execute: mvn package  
       
   4) -  Start Apache TomCat 
     - From {TomCat install dir}/bin  Execute: Startup.bat
         - or 'catalina jpda start' (if debugging). 															

   -  Hit the webServer:
     - From {browser} Execute: http://localhost:8080/breeze-webtest/
     

Steps (1-3) can be accomplished by running build.bat from breeze-webtest root  