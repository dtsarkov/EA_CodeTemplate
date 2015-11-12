@echo off
echo Compile Grammar file...
antlrj EACodeTemplate.g4

echo Compile Java files
javac *.java
