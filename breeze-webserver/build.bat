call setenv.bat
cd ..\breeze-hibernate
call build.bat
cd ..\breeze-webserver
echo Building breeze-webserver
echo --------
call mvn clean install -DskipTests
