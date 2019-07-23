def assertThresh(thresh):
	assert len(thresh)>1 
	assert isinstance(thresh[0],int) and thresh[0]>=0 and thresh[0]<=255 
	assert isinstance(thresh[1],int) and thresh[1]>=0 and thresh[1]<=255 
	assert thresh[0]!=thresh[1]

class RCAAnalysis:
	"""Build an object that describes the individual APA RC analysis you wish to perform."""

	def __init__(self, sample, tiling, fov, measureThresh = None, measureDwell=None, searchThresh = None, searchDwell = None, rules = None, vecs = None, project = None, overlap = None):
		self._sample = sample
		self._tiling = tiling
		self._fov = fov
		self._project = project
		if measureThresh:
			assertThresh(measureThresh)
		self._measureThresh = measureThresh
		if searchThresh:
			assertThresh(searchThresh)
		self._searchTresh = searchThresh
		assert measureDwell==None or (isinstance(measureDwell,int) and (measureDwell>=1) and (measureDwell<=100))
		self._measureDwell = measureDwell
		assert searchDwell==None or (isinstance(searchDwell,int) and (searchDwell>=1) and (searchDwell<=100))
		self._searchDwell = searchDwell
		assert (rules==None) or isinstance(rules, graf.IRuleSet)
		self._rules = rules
		assert (vecs==None) or isinstance(rules, fq.SchamberVectors)
		self._vecs = vecs

	def perform(self, proj):
		project = (self._project if self._project else proj.getProject())
		sample = self._sample
		vecs = (self._vecs if self._vecs else proj.getVectors())
		rules = (self._rules if self._rules else proj.getRules())
		rca=buildRCA(project, sample, vecs, rules, analyst=proj.getAnalyst())
		overlap = (self._overlap if self._overlap else proj.getOverlap())
		rca.setFieldOfView(self._fov, overlap, ppmRes=11)
		measureThresh = (self._measureThresh if self._measureThresh else proj.getMeasureThreshold())
		measureDwell = (self._measureDwell if self._measureDwell else proj.getMeasureDwell())
		rca.setMeasureThreshold(low=measureThresh[0], high = measureThresh[1], dwell = int(measureDwell*1000), measureStep = 8)
		searchThresh = (self._searchThresh if self._searchThresh else proj.getSearchThreshold())
		searchDwell = (self._searchDwell if self._searchDwell else proj.getSearchDwell())
		rca.setSearchThreshold(low=searchThresh[0], high = searchThresh[1], dwell = int(searchDwell*1000))
		rca.configEDS(self, vecs, rules, realTime=0.3)
		rca.perform(self._tiling)
		
		
class SIAnalysis:
	"""SIAnalysis: Defines a mechanism to collect single spectrum image objects."""
	def __init__(self, name, stgPos, fov, frameCount=1, dwell=9, dim=(1024, 1024), subRaster=None, rotation=0.0, toRPL=True, vecs = None):
		self._name=name
		self._stgPos = stgPos
		self._fov = fov
		self._frameCount = frameCount
		self._dwell =dwell
		self._dim = dim
		self._subRaster = subRaster
		self._rotation
		self._vectors = vecs
		
	def perform(self, proj):
		setProjectBasedPaths(proj.getProject(),"SI")
		moveTo(stgPos)
		time.sleep(1.0)
		collectSI(self._name, self._fov, frameCount=self._frameCount, dwell=self._dwell, dim=self._dim, subRaster=self._subRaster, rotation=self._rotation)
		if toRPL:
			SItoRPL(self._name)
		if self._vectors:
			SItoMap(self._name, self._vecs, subSample=4, thresh=0.2, label=True)

class CollectImage:
	"""SIAnalysis: Defines a mechanism to collect single image objects."""
	def __init__(self, name, stgPos, fov, dwell=9, dim=(1024, 1024), subRaster=None, rotation=0.0, toRPL=True, vecs = None):
		self._name=name
		self._stgPos = stgPos
		self._fov = fov
		self._frameCount = frameCount
		self._dwell =dwell
		self._dim = dim
		self._subRaster = subRaster
		self._rotation
		self._vectors = vecs
		
	def perform(self, proj):
		setProjectBasedPaths(proj.getProject(),"SI")
		moveTo(stgPos)
		time.sleep(1.0)
		collectImages(self._name, self._fov, dims=self._dim, dwell=self._dwell, subRaster=self._subRaster, rotation=self._rotation)

class Project:
	"""Project is designed to simplify the configuration of multiple similar analyses.  It allows the user to specify default values for many parameters which become the default values for all analyses in the project unless specifically overwritten in the SingleRCA instance."""
	

	def __init__(self, project, analyst, rules = None, vecs=None, measureThresh = (32, 255), searchThresh = (96, 255), searchDwell = 1, measureDwell = 4, overlap = 1.0):
		"""Project(project, analyst, rules, vecs=None, measureThresh = (32, 255), searchThresh = (96, 255), searchDwell = 1, measureDwell = 4, overlap = 1.0)
project: Project name
analyst: Analyst name
rules: A rule set for classifying the resulting particles
vecs: An optional vector set for analyzing the spectra
measureThresh = (min, max): minimum and maximum gray scale levels in range 0 to 255 inclusive		
searchThresh = (min, max): minimum and maximum gray scale levels in range 0 to 255 inclusive		
measureDwell: Measurement dwell in microseconds
searchDwell: Search dwell in microseconds
overlap: Field overlap <1 is overlap, >1 is border
	Builds a Project object to define the default project parameters."""
		assert isinstance(project, str)
		self._project = str(project)
		assert isinstance(analyst, str)
		self._analyst = str(analyst)
		assert (rules==None) or isinstance(rules, graf.IRuleSet)
		self._rules = rules
		assert (vecs==None) or isinstance(rules, fq.SchamberVectors)
		self._vecs = vecs
		assert (overlap>0.1) or (overlap<10.0)
		self._overlap = overlap
		assertThresh(measureThresh)
		self._measureThresh = measureThresh
		assert isinstance(measureDwell,int) and (measureDwell>=1) and (measureDwell<=100)
		self._measureDwell = measureDwell
		assert isinstance(searchDwell,int) and (searchDwell>=1) and (searchDwell<=100)
		self._searchDwell = searchDwell
		assertThresh(searchThresh)
		self._searchThresh = searchThresh
		self._anals = []
		
	def add(self, analysis):
		self._anals.append(analysis)
		
	def getProject(self):
		return self._project
		
	def getAnalyst(self):
		return self._analyst
	
	def getVectors(self):
		return self._vecs
	
	def getRules(self):
		return self._rules
		
	def getOverlap(self):
		return self._overlap
	
	def getMeasureThreshold(self):
		return self._measureThresh
		
	def getMeasureDwell(self):
		return self._measureDwell
		
	def getSearchThreshold(self):
		return self._searchThresh
		
	def getSearchDwell(self):
		return self._searchDwell
		
	def perform(self):
		for anal in self._anals:
			try:
				if terminated:
					break
				anal.perform(self)
			except Exception, e:
				print e
				print "Error performing: " + anal