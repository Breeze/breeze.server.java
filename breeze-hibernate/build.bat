cls
call mvn clean install -DskipTests

echo local build
echo -----------

call mvn assembly:single

copy src\test\java\northwind\model\*.*         ..\breeze-webtest\src\main\java\northwind\model\
copy src\test\resources\northwind\mapping\*.*  ..\breeze-webtest\src\main\resources\northwind\mapping\



