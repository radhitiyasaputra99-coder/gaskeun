@echo off
rem Compile dan jalankan seluruh test suite (tanpa JUnit/Maven). Exit code 1 jika ada yang gagal.
setlocal
cd /d "%~dp0"
if not exist out\classes mkdir out\classes
if not exist out\test-classes mkdir out\test-classes
dir /s /b src\*.java > out\sources.txt
javac --release 17 -encoding UTF-8 -d out\classes @out\sources.txt || exit /b 1
javac --release 17 -encoding UTF-8 -cp out\classes -d out\test-classes test\travel\TravelAppTests.java || exit /b 1
java -cp out\classes;out\test-classes travel.TravelAppTests
