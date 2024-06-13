set "D2V=2024-06-11"
set "NUM_VER=15.1.1.0"
set "JDK_VER=21"
set "BPATH=C:\Users\nritchie\repositories"
set "NAME_VER=Oberon"
cd %BPATH%

del dtsa2_%NAME_VER%.jar
del dtsa2_%NAME_VER%_nojre.jar
del dtsa2_%NAME_VER%_full.jar

echo %NAME_VER% %D2V% > "DTSA-II\src\gov\nist\microanalysis\dtsa2\revision"
echo %NAME_VER% %D2V% > "DTSA-II\target\classes\gov\nist\microanalysis\dtsa2\revision"
echo %NAME_VER% %D2V% > "epq\target\classes\gov\nist\microanalysis\EPQLibrary\revision"
echo %NAME_VER% %D2V% > "epq\src\gov\nist\microanalysis\EPQLibrary\revision"
echo %NAME_VER% %D2V% > "graf\src\gov\nist\microanalysis\Graf\revision"
echo %NAME_VER% %D2V% > "graf\target\classes\gov\nist\microanalysis\Graf\revision"

cd %BPATH%\DTSA-II
jar cfm "%BPATH%\DTSA-II\Installer\DTSA-II Build\dtsa2.jar" MANIFEST.MF LicenseFile.txt -C "target\classes" . 

cd %BPATH%\epq
jar cfm "%BPATH%\DTSA-II\Installer\DTSA-II Build\epq.jar" "MANIFEST.txt" "revision" -C "target\classes" "gov\nist\microanalysis\Utility" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\Henke93" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\SalvatXion" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\Detector" -C "target\classes" "gov\nist\microanalysis\EPQTools\ClipArt" "epq.ico" -C "target\classes" "flanagan\math" -C "target\classes" "gov\nist\microanalysis\EPQDatabase" -C "target\classes" "gov\nist\microanalysis\EPQTests\TestData" -C "src" "RelocationUtilitySplash.png" -C "target\classes" "gov\nist\microanalysis\NISTMonte\Gen3" "EPQ.html" -C "target\classes" "flanagan\interpolation" -C "target\classes" "gov\nist\microanalysis\EPQTests" -C "target\classes" "gov\nist\microanalysis\EPQTools" -C "target\classes" "gov\nist\microanalysis\EPQLibrary" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\NistXSec" -C "target\classes" "gov\nist\microanalysis\EPQImage" -C "target\classes" "gov\nist\microanalysis\NISTMonte" -C "target\classes" "gov\nist\microanalysis\EPQTests\TestData\STEM" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\CzyzewskiXSec" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\BergerSeltzerBrem" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\CitMAC"  

cd %BPATH%\graf
jar cfm "%BPATH%\DTSA-II\Installer\DTSA-II Build\graf.jar" "manifest.mf" "license.txt" -C "target\classes" .

cd %BPATH%\semantics
jar cfm "%BPATH%\DTSA-II\Installer\DTSA-II Build\semantics.jar" "manifest.mf" "license.txt" -C "target\classes" . 

cd %BPATH%\nist-glass-database
jar cfm "%BPATH%\DTSA-II\Installer\DTSA-II Build\NISTGlassDB.jar" "manifest.mf" "license.txt" -C "target\classes" . 

call "%BPATH%\DTSA-II\Installer\DTSA-II Build\buildjre.bat"

"C:\Program Files (x86)\GnuWin32\bin\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/" -e "s/DATE_VERSION/%D2V%/" -e "s/JDK_VERSION/%JDK_VER%/" "%BPATH%\DTSA-II\Installer\Launch4j_config.template" > "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"
call "C:\Program Files (x86)\Launch4j\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"
del "%BPATH%\DTSA-II\Installer\Launch4j_config.xml"

"C:\Program Files (x86)\GnuWin32\bin\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/" -e "s/DATE_VERSION/%D2V%/" -e "s/JDK_VERSION/%JDK_VER%/" "%BPATH%\DTSA-II\Installer\Launch4j_config_g1.template" > "%BPATH%\DTSA-II\Installer\Launch4j_config_g1.xml"
call "C:\Program Files (x86)\Launch4j\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_config_g1.xml"
del "%BPATH%\DTSA-II\Installer\Launch4j_config_g1.xml"

"C:\Program Files (x86)\GnuWin32\bin\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/" -e "s/DATE_VERSION/%D2V%/" -e "s/JDK_VERSION/%JDK_VER%/" "%BPATH%\DTSA-II\Installer\Launch4j_relocate.template" > "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"
call "C:\Program Files (x86)\Launch4j\launch4jc.exe" "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"
del "%BPATH%\DTSA-II\Installer\Launch4j_relocate.xml"

"C:\Program Files (x86)\GnuWin32\bin\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/" -e "s/DATE_VERSION/%D2V%/" -e "s/JDK_VERSION/%JDK_VER%/" "%BPATH%\graf\Launch4j config.template" > "%BPATH%\graf\Launch4j config.xml"
call "C:\Program Files (x86)\Launch4j\launch4jc.exe" "%BPATH%\graf\Launch4j config.xml"
del "%BPATH%\graf\Launch4j config.xml"

"C:\Program Files (x86)\GnuWin32\bin\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/" -e "s/DATE_VERSION/%D2V%/" -e "s/JDK_VERSION/%JDK_VER%/" "%BPATH%\nist-glass-database\installer\launch4j.template" > "%BPATH%\nist-glass-database\installer\launch4j.xml"
call "C:\Program Files (x86)\Launch4j\launch4jc.exe" "%BPATH%\nist-glass-database\installer\launch4j.xml"
del "%BPATH%\nist-glass-database\installer\launch4j.xml"

cd %BPATH%\DTSA-II\Installer
"C:\Program Files (x86)\GnuWin32\bin\sed.exe" "s/DATE_VERSION/%D2V%/" "%BPATH%\DTSA-II\Installer\izPack_install.template" > "%BPATH%\DTSA-II\Installer\izPack_install.xml"
call "C:\Program Files\IzPack\bin\compile.bat" -h ${IZPACK_HOME} izPack_install.xml -b . -o dtsa2_%NAME_VER%.jar -k standard
del "%BPATH%\DTSA-II\Installer\izPack_install.xml"

"C:\Program Files (x86)\GnuWin32\bin\sed.exe" "s/DATE_VERSION/%D2V%/" "%BPATH%\DTSA-II\Installer\izPack_install_full.template" > "%BPATH%\DTSA-II\Installer\izPack_install_full.xml"
call "C:\Program Files\IzPack\bin\compile.bat" -h ${IZPACK_HOME} izPack_install_full.xml -b . -o dtsa2_%NAME_VER%_full.jar -k standard
del "%BPATH%\DTSA-II\Installer\izPack_install_full.xml"

"C:\Program Files (x86)\GnuWin32\bin\sed.exe" "s/DATE_VERSION/%D2V%/" "%BPATH%\DTSA-II\Installer\izPack_install_nojre.template" > "%BPATH%\DTSA-II\Installer\izPack_install_nojre.xml"
call "C:\Program Files\IzPack\bin\compile.bat" -h ${IZPACK_HOME} izPack_install_nojre.xml -b . -o dtsa2_%NAME_VER%_nojre.jar -k standard
del "%BPATH%\DTSA-II\Installer\izPack_install_nojre.xml"

copy dtsa2_%NAME_VER%.jar "V:\internal\643_violet\DTSA-II Latest"
copy dtsa2_%NAME_VER%_nojre.jar "V:\internal\643_violet\DTSA-II Latest"
copy dtsa2_%NAME_VER%_full.jar "V:\internal\643_violet\DTSA-II Latest"
copy dtsa2_%NAME_VER%_full.jar "V:\internal\643_violet\DTSA-II Latest\dtsa2_%NAME_VER%_full_%NUM_VER%.jar"
copy dtsa2_%NAME_VER%.jar "V:\internal\643_violet\DTSA-II Latest\dtsa2_prerelease.jar"
copy dtsa2_%NAME_VER%_nojre.jar "V:\internal\643_violet\DTSA-II Latest\dtsa2_prerelease_nojre.jar"

cd %BPATH%\DTSA-II\Installer