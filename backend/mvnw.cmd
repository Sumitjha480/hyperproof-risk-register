@echo off
setlocal
set MAVEN_VERSION=3.9.16
if "%MAVEN_USER_HOME%"=="" set MAVEN_USER_HOME=%USERPROFILE%\.m2
set INSTALL_DIR=%MAVEN_USER_HOME%\wrapper\dists\apache-maven-%MAVEN_VERSION%
set MAVEN_BIN=%INSTALL_DIR%\bin\mvn.cmd

if not exist "%MAVEN_BIN%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $version='%MAVEN_VERSION%'; $base='%MAVEN_USER_HOME%\wrapper\dists'; $zip=Join-Path $env:TEMP ('apache-maven-' + $version + '-bin.zip'); $primary='https://dlcdn.apache.org/maven/maven-3/' + $version + '/binaries/apache-maven-' + $version + '-bin.zip'; $fallback='https://archive.apache.org/dist/maven/maven-3/' + $version + '/binaries/apache-maven-' + $version + '-bin.zip'; New-Item -ItemType Directory -Force -Path $base | Out-Null; try { Invoke-WebRequest -Uri $primary -OutFile $zip } catch { Invoke-WebRequest -Uri $fallback -OutFile $zip }; Expand-Archive -Force -Path $zip -DestinationPath $base; Remove-Item $zip"
)

call "%MAVEN_BIN%" %*
exit /b %ERRORLEVEL%
