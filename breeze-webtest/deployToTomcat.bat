set TCDIR=C:\Java\apache-tomcat-7.0.59-windows-x64\apache-tomcat-7.0.59
rmdir %TCDIR%\webapps\breeze-webtest /S /Q 
copy target\breeze-webtest.war %TCDIR%\webapps
pushd %TCDIR%\bin
call catalina jpda start
popd

