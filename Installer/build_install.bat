cd C:\Users\nritchie\git
set "D2V=2022-11-1"
set "NUM_VER=14.0.1.0"
echo Neptune %D2V% > "nist-dtsa-ii\src\gov\nist\microanalysis\dtsa2\revision"
echo Neptune %D2V% > "epq\src\gov\nist\microanalysis\EPQLibrary\revision"
echo Neptune %D2V% > "graf\src\gov\nist\microanalysis\Graf\revision"

cd C:\Users\nritchie\git\nist-dtsa-ii
jar cfm "C:\Users\nritchie\git\nist-dtsa-ii\Installer\DTSA-II Build\dtsa2.jar" MANIFEST.MF LicenseFile.txt -C "target\classes" . 
cd C:\Users\nritchie\git\epq
jar cfm "C:\Users\nritchie\git\nist-dtsa-ii\Installer\DTSA-II Build\epq.jar" "MANIFEST.txt" "revision" -C "target\classes" "gov\nist\microanalysis\Utility" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\Henke93" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\SalvatXion" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\Detector" -C "target\classes" "gov\nist\microanalysis\EPQTools\ClipArt" "epq.ico" -C "target\classes" "flanagan\math" -C "target\classes" "gov\nist\microanalysis\EPQDatabase" -C "target\classes" "gov\nist\microanalysis\EPQTests\TestData" -C "src" "RelocationUtilitySplash.png" -C "target\classes" "gov\nist\microanalysis\NISTMonte\Gen3" "EPQ.html" -C "target\classes" "flanagan\interpolation" -C "target\classes" "gov\nist\microanalysis\EPQTests" -C "target\classes" "gov\nist\microanalysis\EPQTools" -C "target\classes" "gov\nist\microanalysis\EPQLibrary" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\NistXSec" -C "target\classes" "gov\nist\microanalysis\EPQImage" -C "target\classes" "gov\nist\microanalysis\NISTMonte" -C "target\classes" "gov\nist\microanalysis\EPQTests\TestData\STEM" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\CzyzewskiXSec" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\BergerSeltzerBrem" -C "target\classes" "gov\nist\microanalysis\EPQLibrary\CitMAC"  
cd C:\Users\nritchie\git\graf
jar cfm "C:\Users\nritchie\git\nist-dtsa-ii\Installer\DTSA-II Build\graf.jar" "manifest.mf" "license.txt" -C "target\classes" . 

call "C:\Users\nritchie\git\nist-dtsa-ii\Installer\DTSA-II Build\buildjre.bat"

"C:\Program Files (x86)\GnuWin32\bin\sed.exe" "s/DATE_VERSION/%D2V%/" "C:\Users\nritchie\git\nist-dtsa-ii\Installer\izPack_install.template" > "C:\Users\nritchie\git\nist-dtsa-ii\Installer\izPack_install.xml"
"C:\Program Files (x86)\GnuWin32\bin\sed.exe" "s/DATE_VERSION/%D2V%/" "C:\Users\nritchie\git\nist-dtsa-ii\Installer\izPack_install_full.template" > "C:\Users\nritchie\git\nist-dtsa-ii\Installer\izPack_install_full.xml"
"C:\Program Files (x86)\GnuWin32\bin\sed.exe" "s/DATE_VERSION/%D2V%/" "C:\Users\nritchie\git\nist-dtsa-ii\Installer\izPack_install_nojre.template" > "C:\Users\nritchie\git\nist-dtsa-ii\Installer\izPack_install_nojre.xml"

"C:\Program Files (x86)\GnuWin32\bin\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/" -e "s/DATE_VERSION/%D2V%/" "C:\Users\nritchie\git\nist-dtsa-ii\Installer\Launch4j_config.template" > "C:\Users\nritchie\git\nist-dtsa-ii\Installer\Launch4j_config.xml"
"C:\Program Files (x86)\GnuWin32\bin\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/" -e "s/DATE_VERSION/%D2V%/" "C:\Users\nritchie\git\nist-dtsa-ii\Installer\Launch4j_relocate.template" > "C:\Users\nritchie\git\nist-dtsa-ii\Installer\Launch4j_relocate.xml"
"C:\Program Files (x86)\GnuWin32\bin\sed.exe" -e "s/NUMBER_VERSION/%NUM_VER%/" -e "s/DATE_VERSION/%D2V%/" "C:\Users\nritchie\git\graf\Launch4j config.template" > "C:\Users\nritchie\git\graf\Launch4j config.xml"

call "C:\Program Files (x86)\Launch4j\launch4j_2.bat" "C:\Users\nritchie\git\nist-dtsa-ii\Installer\Launch4j_config.xml"
call "C:\Program Files (x86)\Launch4j\launch4j_2.bat" "C:\Users\nritchie\git\nist-dtsa-ii\Installer\Launch4j_relocate.xml"
call "C:\Program Files (x86)\Launch4j\launch4j_2.bat" "C:\Users\nritchie\git\graf\Launch4j config.xml"

cd C:\Users\nritchie\git\nist-dtsa-ii\Installer
call "C:\Program Files (x86)\IzPack\bin\compile.bat" -h ${IZPACK_HOME} izPack_install.xml -b . -o dtsa2_neptune.jar -k standard
call "C:\Program Files (x86)\IzPack\bin\compile.bat" -h ${IZPACK_HOME} izPack_install_nojre.xml -b . -o dtsa2_neptune_nojre.jar -k standard
call "C:\Program Files (x86)\IzPack\bin\compile.bat" -h ${IZPACK_HOME} izPack_install_full.xml -b . -o dtsa2_neptune_full.jar -k standard