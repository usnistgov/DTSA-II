REM This script builds a minimal JRE enviroment for Windows 
cd "%BPATH%\DTSA-II\Installer\DTSA-II Build"
rmdir /S /Q java-runtime
"%JDK_PATH%\bin\jlink.exe" --module-path "%JDK_PATH%\jmods" --no-header-files --no-man-pages --strip-debug --add-modules java.base,java.compiler,java.desktop,java.management,java.naming,java.prefs,java.scripting,java.sql,jdk.unsupported,jdk.xml.dom --output java-runtime
