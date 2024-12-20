REM This script rebuilds DTSA-II (including EPQ), SEMantics, Graf, FastQuant, and the NIST K-Glass Database
REM It requires:  
REM   * A Java Development Kit (version 21 or above)
REM   * IZ-Pack (version 5 or above) [https://izpack.org/]
REM   * Gnu SED [https://www.gnu.org/software/sed/]
REM   * Launch4J [https://launch4j.sourceforge.net/]
REM   * Maven [https://maven.apache.org/]
REM Output:
REM   * An installation JAR containing all the applications (_full.jar)
REM   * An installation JAR containing the core DTSA-II applications plus a Windows JRE (.jar)  
REM   * An installation JAR containing the core DTSA-II applications (_nojre.jar)  

set "D2V=2024-12-20"
set "NUM_VER=15.1.13"
set "JDK_VER=21"
set "BPATH=C:\Users\nritchie\repositories"
set "NAME_VER=Oberon"
set "IZPACK_HOME=C:\izpack\bin"
set "L4J_PATH=C:\Program Files (x86)\Launch4j"
set "SED_PATH=C:\Program Files (x86)\GnuWin32\bin"
set "DTSA_ARCHIVE_PATH=V:\internal\643_violet\DTSA-II Latest"

cd %BPATH%
del DTSA-II\Installer\dtsa2_%NAME_VER%.jar
del DTSA-II\Installer\dtsa2_%NAME_VER%_nojre.jar
del DTSA-II\Installer\dtsa2_%NAME_VER%_full.jar

echo %NAME_VER% %D2V% > "DTSA-II\src\gov\nist\microanalysis\dtsa2\revision"
echo %NAME_VER% %D2V% > "DTSA-II\target\classes\gov\nist\microanalysis\dtsa2\revision"
echo %NAME_VER% %D2V% > "epq\target\classes\gov\nist\microanalysis\EPQLibrary\revision"
echo %NAME_VER% %D2V% > "epq\src\gov\nist\microanalysis\EPQLibrary\revision"
echo %NAME_VER% %D2V% > "graf\src\gov\nist\microanalysis\Graf\revision"
echo %NAME_VER% %D2V% > "graf\target\classes\gov\nist\microanalysis\Graf\revision"


cd %BPATH%\epq
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.relocate.template > pom.xml
call mvn clean
call mvn package
call mvn install

cd %BPATH%\epq
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

cd %BPATH%\DTSA-II
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

cd %BPATH%\FastQuant
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

cd %BPATH%\graf
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

cd %BPATH%\semantics
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

cd %BPATH%\nist-glass-database
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

cd %BPATH%\JythonGUI
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" pom.template > pom.xml
call mvn clean
call mvn package
call mvn install

call "%BPATH%\DTSA-II\Installer\DTSA-II Build\buildjre.bat"

del "%BPATH%\DTSA-II\Installer\DTSA-II Build\DTSA-II.exe"
del "%BPATH%\DTSA-II\Installer\DTSA-II Build\GlassDatabase.exe"
del "%BPATH%\DTSA-II\Installer\DTSA-II Build\Graf.exe"
del "%BPATH%\DTSA-II\Installer\DTSA-II Build\Relocate.exe"

"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\DTSA-II\Installer\Launch4j_config.template" > "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"
call "%L4J_PATH%\\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"
del "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"

"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\DTSA-II\Installer\Launch4j_relocate.template" > "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"
del "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"

"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.template" > "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.xml"
del "%BPATH%\DTSA-II\Installer\Launch4j_JythonGUI.xml"

"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\graf\Launch4j config.template" > "%BPATH%\graf\Launch4j config.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\graf\Launch4j config.xml"
del "%BPATH%\graf\Launch4j config.xml"

"%SED_PATH%\sed.exe" -e "s/BASE_PATH/%BPATH%/g" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/NAME_VERSION/%NAME_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/JDK_VERSION/%JDK_VER%/g" "%BPATH%\nist-glass-database\installer\launch4j.template" > "%BPATH%\nist-glass-database\installer\launch4j.xml"
call "%L4J_PATH%\launch4jc.exe" "%BPATH%\nist-glass-database\installer\launch4j.xml"
del "%BPATH%\nist-glass-database\installer\launch4j.xml"

cd %BPATH%\DTSA-II\Installer
"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/NAME_VERSION/%NAME_VER%/g" "%BPATH%\DTSA-II\Installer\izPack_install_full.template" > "%BPATH%\DTSA-II\Installer\izPack_install_full.xml"
call "%IZPACK_HOME%\compile.bat" -h ${IZPACK_HOME} izPack_install_full.xml -b . -o dtsa2_%NAME_VER%_full.jar -k standard
del "%BPATH%\DTSA-II\Installer\izPack_install_full.xml"

"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/NAME_VERSION/%NAME_VER%/g" "%BPATH%\DTSA-II\Installer\izPack_install.template" > "%BPATH%\DTSA-II\Installer\izPack_install.xml"
call "%IZPACK_HOME%\compile.bat" -h ${IZPACK_HOME} izPack_install.xml -b . -o dtsa2_%NAME_VER%.jar -k standard
del "%BPATH%\DTSA-II\Installer\izPack_install.xml"

"%SED_PATH%\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/g" -e "s/DATE_VERSION/%D2V%/g" -e "s/NAME_VERSION/%NAME_VER%/g" "%BPATH%\DTSA-II\Installer\izPack_install_nojre.template" > "%BPATH%\DTSA-II\Installer\izPack_install_nojre.xml"
call "%IZPACK_HOME%\compile.bat" -h ${IZPACK_HOME} izPack_install_nojre.xml -b . -o dtsa2_%NAME_VER%_nojre.jar -k standard
del "%BPATH%\DTSA-II\Installer\izPack_install_nojre.xml"

cd %BPATH%\DTSA-II\Installer
copy dtsa2_%NAME_VER%.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%.jar"
copy dtsa2_%NAME_VER%_nojre.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%_nojre.jar"
copy dtsa2_%NAME_VER%_full.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%_full.jar"
copy dtsa2_%NAME_VER%_full.jar "%DTSA_ARCHIVE_PATH%\dtsa2_%NAME_VER%_full_%NUM_VER%.jar"
copy dtsa2_%NAME_VER%.jar "%DTSA_ARCHIVE_PATH%\dtsa2_prerelease.jar"
copy dtsa2_%NAME_VER%_nojre.jar "%DTSA_ARCHIVE_PATH%\dtsa2_prerelease_nojre.jar"

cd %BPATH%\DTSA-II\Installer