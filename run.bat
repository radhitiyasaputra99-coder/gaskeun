@echo off
rem Compile dan jalankan Gaskeun. Butuh JDK 17 atau lebih baru.
setlocal
cd /d "%~dp0"
if not exist out\classes mkdir out\classes
dir /s /b src\*.java > out\sources.txt
javac --release 17 -encoding UTF-8 -d out\classes @out\sources.txt || exit /b 1
java -cp out\classes travel.Main
