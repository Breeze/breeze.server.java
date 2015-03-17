echo Building breeze-northwind
echo -----------
call mvn clean install -DskipTests
call mvn assembly:single



