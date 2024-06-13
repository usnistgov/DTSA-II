_COMPANY_ = "Vallecitos AEM"
_INSTRUMENT_ = "TESCAN MIRA4 S/N 122-0310"

base = jl.System.getProperty("user.dir")
sys.path.append("%s\\graf.jar" % base)
sys.path.append("%s\\FastQuant.jar" % base)
sys.path.append("%s\\semantics.jar" % base)


defaultBounds = (-40.0, -30.0, 40.0, 30.0)
BLANKER_INDEX = 1
SAVE_FIELD_MASK = 0x0
DEFAULT_E0 = 25
availableDets = ( True, )*3
_edsResolution="MediumLE"
defLED = True
det_off=1
sedName = "SE"
bseName = "BSE"
_apaWrite = ( (epq.SpectrumProperties.MicroImage, 3), (epq.SpectrumProperties.MicroImage2, 2) )

rootPath = "C:\\Users\\Tescan\\Documents\\Data"
setDefaultPath("%s\\Daily\\%s" % (rootPath, jtext.SimpleDateFormat("dd-MMM-yyyy").format(ju.Date())))
defaultRulePath = "%s\\Standards\\Rule" % rootPath
defaultVecPath = "%s\\Standards\\Combined\\% keV" % ( rootPath, DEFAULT_E0 )
nullImagePath = "%s\\NullImages" % base
keyPath = nullImagePath
defaultArchivePath = None
IMAGE_MAGICK = "C:\\Program Files (x86)\\ImageMagick-6.9.12-Q8"