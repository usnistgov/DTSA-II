_COMPANY = "PNNL"
_INSTRUMENT_="TESCAN MIRA3 LMU S/N 116-0153"

base = jl.System.getProperty("user.dir")
sys.path.append("%s\\graf.jar" % base)
sys.path.append("%s\\FastQuant.jar" % base)
sys.path.append("%s\\semantics.jar" % base)

defaultBounds = (-40.0, -30.0, 40.0, 30.0)
BLANKER_INDEX = 0
SAVE_FIELD_MASK = 0x3
DEFAULT_E0 = 25
availableDets = ( True, )*3 # False, False )
_edsResolution="MediumLE"
defLED = True
det_off=1
sedName = "SE"
bseName = "BSE"
_apaWrite = ( (epq.SpectrumProperties.MicroImage, 1), (epq.SpectrumProperties.MicroImage2, 0) )

rootPath = "C:\\Users\\Tescan\\My Documents\\Data"
setDefaultPath("%s\\Daily\\%s" % (rootPath, jtext.SimpleDateFormat("dd-MMM-yyyy").format(ju.Date())))
defaultRulePath = "%s\\Standards\\Rule" % rootPath
defaultVecPath = "%s\\Standards\\Combined\\% keV" % ( rootPath, DEFAULT_E0 )
nullImagePath = "%s\\NullImages" % base
keyPath = nullImagePath
defaultArchivePath = None
IMAGE_MAGICK = "C:\\Program Files\\ImageMagick-6.9.6-Q16"