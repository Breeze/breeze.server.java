cd ..\breeze-hibernate
call build.bat
cd ..\breeze-webtest

call mvn package

call mvn assembly:single

call deployToTomcat.bat