@echo off
 
::Get 1st list of EA instances
TASKLIST /FI "IMAGENAME eq EA.exe" /FO CSV /NH > ea1.temp
 
::Execute EACodeGenerator
"C:\Utils\Java\jdk1.7.0_79\bin\java.exe" -d32 -jar "%~p0\"EACodeGenerator.jar %*
::;eaapi.jar;antlr-4.5.1-complete.jar;" com.github.dtsarkov.ea.tools.codegenerator.Generator 
 
::Get 2nd list of EA instances
TASKLIST /FI "IMAGENAME eq EA.exe" /FO CSV /NH > ea2.temp
 
::Kill instances presented in the 2nd list but on presented in the 1st
FOR /F "tokens=2 delims=, " %%i IN (ea2.temp) DO (
        ECHO TASKKILL /F /PID %%i > ea3.temp.cmd
        FOR /F %%j IN ('findstr /M /C:%%i ea1.temp') DO (
                ::ECHO FOUND PID='%%i'. DELETE CMD FILE
                DEL /F /Q ea3.temp.cmd
        ) 
        IF EXIST ea3.temp.cmd (
                ECHO KILL INSTANCE %%i
                CALL ea3.temp.cmd
        )
)
 
::Delete temporary files
DEL /F /Q ea*.temp*