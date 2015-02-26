cd ..\breezejs-hibernate
call build.bat
cd ..\breezejs-intest

call mvn package

call mvn assembly:single

call deployToTomcat.bat