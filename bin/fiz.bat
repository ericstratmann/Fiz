@echo off

set SERVER_URL=@@SERVER_URL

if "%FIZ_HOME%"=="" set FIZ_HOME="@@FIZ_HOME"

java -DFIZ_HOME="%FIZ_HOME%" -DSERVER_URL="%SERVER_URL%" -classpath "%FIZ_HOME%"\lib\ant.jar;"%FIZ_HOME%"\lib\ant-launcher.jar;"%FIZ_HOME%"\lib\jyaml.jar;"%FIZ_HOME%"\lib\fiz.jar org.fiz.tools.Fiz %*
