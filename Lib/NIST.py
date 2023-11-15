_COMPANY_ = "DOC NIST MML - 637.02"
_INSTRUMENT_ = "TESCAN MIRA3 LMU S/N MI 0791077US"

if jl.System.getProperty('sun.java.command') == u'gov.nist.microanalysis.dtsa2.DTSA2':
	print "JAR paths based on workspace."
	sys.path.append("C:\\Users\\nritchie\\Documents\\workspace\\SEMantics\\bin")
	sys.path.append("C:\\Users\\nritchie\\Documents\\workspace\\Graf\\classes")
	sys.path.append("C:\\Users\\nritchie\\Documents\\workspace\\FastQuant\\bin")
else:
	base = jl.System.getProperty("user.dir")
	print "JAR paths based on user.dir = %s." % base
	sys.path.append("%s\\graf.jar" % base)
	sys.path.append("%s\\FastQuant.jar" % base)
	sys.path.append("%s\\semantics.jar" % base)

defaultBounds = (-40.0, -30.0, 40.0, 30.0)

rootPath = "D:"
defaultArchivePath = "P:"

BLANKER_INDEX = 0
SAVE_FIELD_MASK = 0x3
DEFAULT_E0 = 20

availableDets = ( True, )*4 # False, False, False )
_edsResolution="MediumLE"
defLED = True
det_off=1

IMAGE_MAGICK = "C:\\Program Files\\ImageMagick-6.9.12-Q8"

(sedName, bseName) = ("SE", "BSE")
_apaWrite = ( (epq.SpectrumProperties.MicroImage, 1), (epq.SpectrumProperties.MicroImage2, 0) )

setDefaultPath("%s\\Daily\\%s" % (rootPath, jtext.SimpleDateFormat("dd-MMM-yyyy").format(ju.Date())))
defaultRulePath = "%s\\Standards\\Combined" % rootPath
defaultVecPath = "%s\\Standards\\Combined\\%d keV" % (rootPath, DEFAULT_E0)
nullImagePath = "%s\\Standards\\Null images" % rootPath
keyPath = "%s\\Standards\\Null images" % rootPath

print "Instrument: %s" % _INSTRUMENT_