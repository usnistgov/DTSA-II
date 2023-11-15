_COMPANY = "SRNL"
_INSTRUMENT = "TESCAN MIRA4"

base = jl.System.getProperty("user.dir")
print "JAR paths based on user.dir = %s." % base
sys.path.append("%s\\graf.jar" % base)
sys.path.append("%s\\FastQuant.jar" % base)
sys.path.append("%s\\semantics.jar" % base)

defaultBounds = (-40.0, -30.0, 40.0, 30.0)

defaultArchivePath = None

rootPath = "C:\\Users\\Tescan\\SEMantics Data"

BLANKER_INDEX = 1

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
det_off=0

# Location of the Image Magick executables 'convert' and 'montage'
IMAGE_MAGICK = "C:\\Program Files\\ImageMagick-6.9.6-Q16"

sedName = "SE"
bseName = "BSE"

_apaWrite = ( (epq.SpectrumProperties.MicroImage, 3), (epq.SpectrumProperties.MicroImage2, 2) )