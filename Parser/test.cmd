@echo off
echo Executin parser in test mode...
SET CLASSPATH=%CLASSPATH%;C:\Utils\ANTLR\antlr-4.5.1-complete.jar;
SET JAVAPATH="C:\Utils\Java\jdk1.7.0_79\bin"
%JAVAPATH%\java org.antlr.v4.gui.TestRig EACodeTemplate file %*
