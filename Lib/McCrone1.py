_COMPANY_ = "McCrone Associates"
_INSTRUMENT_ = "TESCAN MIRA3 LMU S/N ?"

base = jl.System.getProperty("user.dir")
print "JAR paths based on user.dir = %s." % base
sys.path.append("%s\\graf.jar" % base)
sys.path.append("%s\\FastQuant.jar" % base)
sys.path.append("%s\\semantics.jar" % base)

defaultBounds = (-40.0, -30.0, 40.0, 30.0)

defaultArchivePath = None

rootPath = "C:\\Data"

BLANKER_INDEX = 0

SAVE_FIELD_MASK = 0x3
DEFAULT_E0 = 25

setDefaultPath("%s\\Daily\\%s" % (rootPath, jtext.SimpleDateFormat("dd-MMM-yyyy").format(ju.Date())))
defaultRulePath = "%s\\Standards\\Combined" % rootPath
defaultVecPath = "%s\\Standards\\Combined\\%d keV" % (rootPath, DEFAULT_E0)
nullImagePath = "%s\\Standards\\Null images" % rootPath
keyPath = "%s\\Standards\\Null images" % rootPath

availableDets = ( True, )*3
_edsResolution="MediumLE"

defLED = True
det_off=1

IMAGE_MAGICK = "C:\\Program Files\\ImageMagick-6.9.6-Q16"

( sedName, bseName) = ( "SE", "BSED" )

_apaWrite = ( (epq.SpectrumProperties.MicroImage, 1), (epq.SpectrumProperties.MicroImage2, 0) )
