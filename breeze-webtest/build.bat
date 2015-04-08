rem don't need these because breeze-webserver builds breeze-hibernate
rem    and breeze-hibernate builds breeze-northwind for its unit tests.
rem call setenv.bat
rem cd ..\breeze-hibernate
rem call build.bat
rem cd ..\breeze-northwind
rem call build.bat

cd ..\breeze-webserver
call build.bat

cd ..\breeze-webtest
echo Building webtest
echo --------
call mvn clean
call mvn package

echo Deploying to Tomcat
echo -----------
call deployToTomcat.bat

