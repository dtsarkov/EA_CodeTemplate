@echo off
echo Compile Grammar file...
call antlrj EACodeTemplate.g4

echo Compile Java files
call javac *.java
