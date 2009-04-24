@echo off

set FIZ_HOME=@@FIZ_HOME
set FIZ_REPO=@@FIZ_REPO

if "%1"=="" goto printUsage
if %1==help goto printUsage
if %1==create goto create
if %1==fetch goto fetch
if %1==upgrade goto upgrade
if %1==version goto version
goto printUsage

:create
call :numargs %*
if %COUNT% NEQ 2 goto printUsage
set DEST_DIR=%~f2
ant -buildfile %FIZ_HOME%\antscripts\create.xml -Dfizhome="%FIZ_HOME%" -Ddestdir="%DEST_DIR%"
goto end

:fetch
call :numargs %*
if %COUNT%==2 goto fetchToDefaultDir 
if %COUNT%==3 goto fetchToDestDir
goto printUsage

:fetchToDefaultDir
ant -buildfile %FIZ_HOME%\antscripts\fetch.xml -Dfizrepository="%FIZ_REPO%" -Dfizhome="%FIZ_HOME%" -Dversion="%~2"
goto end

:fetchToDestDir
set DEST_DIR=%~f3
ant -buildfile %FIZ_HOME%\antscripts\fetch.xml -Dfizrepository="%FIZ_REPO%" -Dversion="%~2" -Ddestdir="%DEST_DIR%"
goto end

:upgrade
call :numargs %*
if %COUNT%==1 goto synchWithInstaller
if %COUNT%==2 goto upgradeToVersion
goto printUsage

:synchWithInstaller
ant -buildfile antscripts\upgrade.xml -Dfizhome="%FIZ_HOME%"
goto end

:upgradeToVersion
ant -buildfile antscripts\upgrade.xml upgradetoversion -Dfizrepository="%FIZ_REPO%" -Dversion="%~2"
goto end

:version
call :numargs %*
if %COUNT%==1 goto fizVersion
if %COUNT%==2 goto appVersion
goto printUsage

:fizVersion
ant -buildfile %FIZ_HOME%/antscripts/version.xml -Dfiz.jar.path="%FIZ_HOME%/lib/fiz.jar"
goto end

:appVersion
set ABS_PATH=%~f2
ant -buildfile %FIZ_HOME%/antscripts/version.xml -Dfiz.jar.path="%ABS_PATH%/lib/fiz.jar"
goto end

:numargs
set COUNT=0
:numargsloop
if "%1"=="" goto :EOF
shift
set /a COUNT+=1
goto numargsloop

:printUsage
	echo Usage: fiz ^<subcommand^> [options] [args]
	echo.
	echo Subcommands:
	echo.
	echo    help     Print this message and exit.
	echo    version  Print the version of the Fiz platform or a Fiz application.
	echo             To print the version of a specific Fiz installation or a 
	echo             Fiz application, provide the path to the root directory of 
	echo             that intallation or application. Otherwise, the version of 
	echo             the default Fiz installation is printed.
	echo    create   Create a new Fiz application. Provide the path to 
	echo             application's directory.
	echo    fetch    Fetch and unpack a new version of the Fiz platform. Provide 
	echo             the version number of specific version you want fetch, and 
	echo             optionally a destination directory for the new installation.
	echo    upgrade  Upgrade the Fiz-specific files of an application to a 
	echo             specific version. Provide the version number of the specific 
	echo             version of Fiz and run this command at the root directory of 
	echo             the application.
	echo.

:end