:: Copy the WAR to tomcat's webapps dir, and start tomcat
call setenv.bat
rmdir %CATALINA_HOME%\webapps\breeze-webtest /S /Q 
copy target\breeze-webtest.war %CATALINA_HOME%\webapps
pushd %CATALINA_HOME%\bin
call catalina jpda start
popd

