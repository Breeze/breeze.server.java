cls
call mvn clean install -DskipTests

echo local build
echo -----------

call mvn assembly:single

copy src\test\java\northwind\model\*.*         ..\breezejs-intest\src\main\java\northwind\model\
copy src\test\resources\northwind\mapping\*.*  ..\breezejs-intest\src\main\resources\northwind\mapping\



