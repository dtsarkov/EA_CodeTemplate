@echo off
echo Compile Grammar file...
call C:\Utils\ANTLR\antlrj.cmd EACodeTemplate.g4

echo Compile Java files
SET JAVAPATH="C:\Utils\Java\jdk1.7.0_79\bin"
SET CLASSPATH=%CLASSPATH%;C:\Utils\ANTLR\antlr-4.5.1-complete.jar;
call %JAVAPATH%\javac *.java
