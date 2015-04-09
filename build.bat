call setenv.bat

::initialize error flag to undefined
set "mvnErr="

::run your Maven command and define the error flag if there was an error
call mvn package -DskipTests || set mvnErr=1

if defined mvnErr (
	echo Maven error - not deploying to tomcat
) else (
	call deployToTomcat.bat
)

:done
