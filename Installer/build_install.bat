REM This script rebuilds DTSA-II (including EPQ), SEMantics, Graf, FastQuant, and the NIST K-Glass Database
REM It requires:  
REM   * A Java Development Kit (version 21 or above)
REM   * IZ-Pack (version 5 or above) [https://izpack.org/] (Don't install in "Program Files" as the path 
REM     becomes too long and won't build.)
REM   * Gnu SED [https://www.gnu.org/software/sed/]
REM   * Launch4J [https://launch4j.sourceforge.net/]
REM   * Maven [https://maven.apache.org/]
REM  Update the paths below to reflect the location of these executables on your system.
REM Output:
REM   * An installation JAR containing all the applications (_full.jar)
REM   * An installation JAR containing the core DTSA-II applications plus a Windows JRE (.jar)  
REM   * An installation JAR containing the core DTSA-II applications (_nojre.jar)  

ECHO OFF
REM Setting "FULL_BUILD=True" only works if NIST-only internal projects graf/SEMantics/FastQuant/Glass Database 
REM are available. (Unless you work at NIST, the answer is likely 'no.')
SET "FULL_BUILD=False"

if [%FULL_BUILD%] == [True] (
ECHO ***** Performing Full Build *****
) else (
ECHO ***** Performing DTSA-II and Relocate-only Build *****
)

REM Version information
SET "D2V=2025-3-5"
SET "NUM_VER=15.1.17"
SET "NAME_VER=Oberon"

REM Build paths
SET "JDK_VER=21"
SET "JDK_PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
SET "BPATH=C:\Users\nritchie\repositories"
SET "IZPACK_HOME=C:\izpack\bin"
SET "L4J_PATH=C:\Program Files (x86)\Launch4j"
SET "SED_PATH=C:\Program Files (x86)\GnuWin32\bin"
SET "DTSA_ARCHIVE_PATH=V:\internal\643_violet\DTSA-II Latest"


REM Eliminate old versions of the .jar and .exe files so only fresh ones exist upon script completion
CD %BPATH%
REM Delete old artifacts
DEL "%BPATH%\DTSA-II\Installer\dtsa2_*.jar"
DEL "%BPATH%\DTSA-II\Installer\DTSA-II Build\*.exe"
DEL /Q "%BPATH%\DTSA-II\Installer\DTSA-II Build\java-runtime\*.*"

REM Update the application version numbers
echo %NAME_VER% %D2V% > "DTSA-II\src\gov\nist\microanalysis\dtsa2\revision"
echo %NAME_VER% %D2V% > "epq\src\gov\nist\microanalysis\EPQLibrary\revision"
if [%FULL_BUILD%] == [True] (
echo %NAME_VER% %D2V% > "graf\src\gov\nist\microanalysis\Graf\revision"
)

REM Build relocation application from the EPQ source
CD %BPATH%\epq
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.relocate.template > pom.xml
call mvn clean
call mvn package
call mvn install

REM Build EPQ library from the EPQ source
CD %BPATH%\epq
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

REM Build DTSA-II application from the DTSA-II source
CD %BPATH%\DTSA-II
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

if [%FULL_BUILD%] == [True] (
REM Build FastQuant library
CD %BPATH%\FastQuant
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install
)

if [%FULL_BUILD%] == [True] (
REM Build the Graf application
CD %BPATH%\graf
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install
)

if [%FULL_BUILD%] == [True] (
REM Build the SEMantics library
CD %BPATH%\semantics
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install
)

if [%FULL_BUILD%] == [True] (
REM Build the NIST Glass Database application
CD %BPATH%\nist-glass-database
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install
)

if [%FULL_BUILD%] == [True] (
REM Build the Jython GUI application
CD %BPATH%\JythonGUI
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install
)

REM Build a JRE to include with the install
call "%BPATH%\DTSA-II\Installer\buildjre.bat"

REM Build "DTSA-II.exe" (Windows executable)
"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\DTSA-II\Installer\Launch4j_config.template" > "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"
call "%L4J_PATH%\\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"
DEL "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"

REM Build "Relocate.exe" (Windows executable)
"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\DTSA-II\Installer\Launch4j_relocate.template" > "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"
DEL "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"

if [%FULL_BUILD%] == [True] (
REM Build "JythonGUI.exe" (Windows executable)
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.template" > "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.xml"
DEL "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.xml"
)

if [%FULL_BUILD%] == [True] (
REM Build "Graf.exe" (Windows executable)
"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\graf\Launch4j config.template" > "%BPATH%\graf\Launch4j config.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\graf\Launch4j config.xml"
DEL "%BPATH%\graf\Launch4j config.xml"
)

if [%FULL_BUILD%] == [True] (
REM Build "GlassDatabase.exe" (Windows executable)
"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\nist-glass-database\installer\launch4j.template" > "%BPATH%\nist-glass-database\installer\launch4j.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\nist-glass-database\installer\launch4j.xml"
DEL "%BPATH%\nist-glass-database\installer\launch4j.xml"
)

CD %BPATH%\DTSA-II\Installer
REM Build basic installer (DTSA-II)
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/NAME_VERSION/%NAME_VER%/g" "%BPATH%\DTSA-II\Installer\izPack_install.template" > "%BPATH%\DTSA-II\Installer\izPack_install.xml"
call "%IZPACK_HOME%\compile.bat" -h ${IZPACK_HOME} izPack_install.xml -b . -o dtsa2_%NAME_VER%.jar -k standard
DEL "%BPATH%\DTSA-II\Installer\izPack_install.xml"

REM Build basic installer (no JRE) (DTSA-II)
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/NAME_VERSION/%NAME_VER%/g" "%BPATH%\DTSA-II\Installer\izPack_install_nojre.template" > "%BPATH%\DTSA-II\Installer\izPack_install_nojre.xml"
call "%IZPACK_HOME%\compile.bat" -h ${IZPACK_HOME} izPack_install_nojre.xml -b . -o dtsa2_%NAME_VER%_nojre.jar -k standard
DEL "%BPATH%\DTSA-II\Installer\izPack_install_nojre.xml"

if [%FULL_BUILD%] == [True] (
REM Build full installer (DTSA-II & SEMantics & Graf)
CD %BPATH%\DTSA-II\Installer
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/NAME_VERSION/%NAME_VER%/g" "%BPATH%\DTSA-II\Installer\izPack_install_full.template" > "%BPATH%\DTSA-II\Installer\izPack_install_full.xml"
call "%IZPACK_HOME%\compile.bat" -h ${IZPACK_HOME} izPack_install_full.xml -b . -o dtsa2_%NAME_VER%_full.jar -k standard
DEL "%BPATH%\DTSA-II\Installer\izPack_install_full.xml"
)

REM Copy the resulting installers to the archive path
CD %BPATH%\DTSA-II\Installer
COPY dtsa2_%NAME_VER%.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%.jar"
COPY dtsa2_%NAME_VER%_nojre.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%_nojre.jar"
COPY dtsa2_%NAME_VER%.jar "%DTSA_ARCHIVE_PATH%\dtsa2_prerelease.jar"
COPY dtsa2_%NAME_VER%_nojre.jar "%DTSA_ARCHIVE_PATH%\dtsa2_prerelease_nojre.jar"
if [%FULL_BUILD%] == [True] (
COPY dtsa2_%NAME_VER%_full.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%_full.jar"
COPY dtsa2_%NAME_VER%_full.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%_full_%NUM_VER%.jar"
)

CD %BPATH%\DTSA-II\Installer