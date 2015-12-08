@echo off
::java -jar -d32 EACodeGenerator.jar %*
tasklist /FI "IMAGENAME eq EA.exe" /FO CSV /NH > .ea1.temp
FOR /F "tokens=2 delims=, " %%i IN (.ea1.temp) DO (
	ECHO PID %%i
	FINDSTR /C:#%%i .ea1.temp
	ECHO %ERRORLEVEL%
)
	