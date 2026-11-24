@rem AI작성: Gradle Wrapper JAR을 실행하는 Windows용 진입점이다.
@echo off
setlocal
set APP_HOME=%~dp0

if defined JAVA_HOME (
  set "JAVACMD=%JAVA_HOME%\bin\java.exe"
  if not exist "%JAVACMD%" set "JAVACMD=%JAVA_HOME:~0,-1%\bin\java.exe"
) else (
  set JAVACMD=java.exe
)

"%JAVACMD%" -classpath "%APP_HOME%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
