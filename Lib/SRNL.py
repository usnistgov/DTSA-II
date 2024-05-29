_COMPANY = "SRNL"
_INSTRUMENT_ = "TESCAN MIRA4"

base = jl.System.getProperty("user.dir")
sys.path.append("%s\\graf.jar" % base)
sys.path.append("%s\\FastQuant.jar" % base)
sys.path.append("%s\\semantics.jar" % base)

defaultBounds = (-40.0, -30.0, 40.0, 30.0)
BLANKER_INDEX = 1
SAVE_FIELD_MASK = 0x3
DEFAULT_E0 = 25
availableDets = ( True, )*3
_edsResolution="MediumLE"
defLED = True
det_off=0
sedName = "SE"
bseName = "BSE"
_apaWrite = ( (epq.SpectrumProperties.MicroImage, 3), (epq.SpectrumProperties.MicroImage2, 2) )

rootPath = "C:\\Users\\Tescan\\DTSA_DATA"
setDefaultPath("%s\\Daily\\%s" % (rootPath, jtext.SimpleDateFormat("dd-MMM-yyyy").format(ju.Date())))
defaultRulePath = "%s\\Standards\\Combined" % rootPath
defaultVecPath = "%s\\Standards\\Combined\\%d keV" % (rootPath, DEFAULT_E0)
nullImagePath = "%s\\Standards\\Null images" % rootPath
keyPath = "%s\\Standards\\Null images" % rootPath
defaultArchivePath = None
IMAGE_MAGICK = "C:\\Program Files\\ImageMagick-6.9.12-Q16"