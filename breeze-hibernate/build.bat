cd ..\breeze-northwind
call build.bat
cd ..\breeze-hibernate
echo Building breeze-hibernate
echo -----------
call mvn clean install -DskipTests
call mvn assembly:single

rem copy src\test\java\northwind\model\*.*         ..\breeze-webtest\src\main\java\northwind\model\
rem copy src\test\resources\northwind\mapping\*.*  ..\breeze-webtest\src\main\resources\northwind\mapping\



