call setenv.bat
cd ..\breeze-hibernate
call build.bat
cd ..\breeze-webtest
echo Building breeze-webtest
echo --------
call mvn package
call mvn assembly:single
echo Deploying to Tomcat
echo -----------
call deployToTomcat.bat