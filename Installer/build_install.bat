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

REM Version information
set "D2V=2024-12-30"
set "NUM_VER=15.1.15"
set "NAME_VER=Oberon"

REM Build paths
set "JDK_VER=21"
set "JDK_PATH=C:\Program Files\Eclipse Adoptium\jdk-21.0.5.11-hotspot"
set "BPATH=C:\Users\nritchie\repositories"
set "IZPACK_HOME=C:\izpack\bin"
set "L4J_PATH=C:\Program Files (x86)\Launch4j"
set "SED_PATH=C:\Program Files (x86)\GnuWin32\bin"
set "DTSA_ARCHIVE_PATH=V:\internal\643_violet\DTSA-II Latest"

REM Eliminate old versions of the .jar file so only fresh ones exist upon script completion
cd %BPATH%
del DTSA-II\Installer\dtsa2_*.jar

REM Update the application version numbers
echo %NAME_VER% %D2V% > "DTSA-II\src\gov\nist\microanalysis\dtsa2\revision"
echo %NAME_VER% %D2V% > "DTSA-II\target\classes\gov\nist\microanalysis\dtsa2\revision"
echo %NAME_VER% %D2V% > "epq\target\classes\gov\nist\microanalysis\EPQLibrary\revision"
echo %NAME_VER% %D2V% > "epq\src\gov\nist\microanalysis\EPQLibrary\revision"
echo %NAME_VER% %D2V% > "graf\src\gov\nist\microanalysis\Graf\revision"
echo %NAME_VER% %D2V% > "graf\target\classes\gov\nist\microanalysis\Graf\revision"


REM Build relocation application from the EPQ source
cd %BPATH%\epq
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.relocate.template > pom.xml
call mvn clean
call mvn package
call mvn install

REM Build EPQ library from the EPQ source
cd %BPATH%\epq
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

REM Build DTSA-II application from the DTSA-II source
cd %BPATH%\DTSA-II
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

REM Build FastQuant library
cd %BPATH%\FastQuant
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

REM Build the Graf application
cd %BPATH%\graf
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

REM Build the SEMantics library
cd %BPATH%\semantics
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

REM Build the NIST Glass Database application
cd %BPATH%\nist-glass-database
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

REM Build the Jython GUI application
cd %BPATH%\JythonGUI
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

REM Build a JRE to include with the install
call "%BPATH%\DTSA-II\Installer\buildjre.bat"

REM Delete old artifacts
del "%BPATH%\DTSA-II\Installer\DTSA-II Build\DTSA-II.exe"
del "%BPATH%\DTSA-II\Installer\DTSA-II Build\GlassDatabase.exe"
del "%BPATH%\DTSA-II\Installer\DTSA-II Build\Graf.exe"
del "%BPATH%\DTSA-II\Installer\DTSA-II Build\Relocate.exe"

REM Build "DTSA-II.exe" (Windows executable)
"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\DTSA-II\Installer\Launch4j_config.template" > "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"
call "%L4J_PATH%\\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"
del "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"

REM Build "Relocate.exe" (Windows executable)
"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\DTSA-II\Installer\Launch4j_relocate.template" > "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"
del "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"

REM Build "JythonGUI.exe" (Windows executable)
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.template" > "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.xml"
del "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.xml"

REM Build "Graf.exe" (Windows executable)
"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\graf\Launch4j config.template" > "%BPATH%\graf\Launch4j config.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\graf\Launch4j config.xml"
del "%BPATH%\graf\Launch4j config.xml"

REM Build "GlassDatabase.exe" (Windows executable)
"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\nist-glass-database\installer\launch4j.template" > "%BPATH%\nist-glass-database\installer\launch4j.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\nist-glass-database\installer\launch4j.xml"
del "%BPATH%\nist-glass-database\installer\launch4j.xml"

REM Build full installer (DTSA-II & SEMantics & Graf)
cd %BPATH%\DTSA-II\Installer
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/NAME_VERSION/%NAME_VER%/g" "%BPATH%\DTSA-II\Installer\izPack_install_full.template" > "%BPATH%\DTSA-II\Installer\izPack_install_full.xml"
call "%IZPACK_HOME%\compile.bat" -h ${IZPACK_HOME} izPack_install_full.xml -b . -o dtsa2_%NAME_VER%_full.jar -k standard
del "%BPATH%\DTSA-II\Installer\izPack_install_full.xml"

REM Build basic installer (DTSA-II)
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/NAME_VERSION/%NAME_VER%/g" "%BPATH%\DTSA-II\Installer\izPack_install.template" > "%BPATH%\DTSA-II\Installer\izPack_install.xml"
call "%IZPACK_HOME%\compile.bat" -h ${IZPACK_HOME} izPack_install.xml -b . -o dtsa2_%NAME_VER%.jar -k standard
del "%BPATH%\DTSA-II\Installer\izPack_install.xml"

REM Build basic installer (no JRE) (DTSA-II)
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/NAME_VERSION/%NAME_VER%/g" "%BPATH%\DTSA-II\Installer\izPack_install_nojre.template" > "%BPATH%\DTSA-II\Installer\izPack_install_nojre.xml"
call "%IZPACK_HOME%\compile.bat" -h ${IZPACK_HOME} izPack_install_nojre.xml -b . -o dtsa2_%NAME_VER%_nojre.jar -k standard
del "%BPATH%\DTSA-II\Installer\izPack_install_nojre.xml"

REM Copy the resulting installers to the archive
cd %BPATH%\DTSA-II\Installer
copy dtsa2_%NAME_VER%.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%.jar"
copy dtsa2_%NAME_VER%_nojre.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%_nojre.jar"
copy dtsa2_%NAME_VER%_full.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%_full.jar"
copy dtsa2_%NAME_VER%_full.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%_full_%NUM_VER%.jar"
copy dtsa2_%NAME_VER%.jar "%DTSA_ARCHIVE_PATH%\dtsa2_prerelease.jar"
copy dtsa2_%NAME_VER%_nojre.jar "%DTSA_ARCHIVE_PATH%\dtsa2_prerelease_nojre.jar"

cd %BPATH%\DTSA-II\Installer