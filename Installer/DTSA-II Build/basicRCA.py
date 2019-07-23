# -*- coding: utf-8 -*-
# Description: A simple script for RCA particle analysis
# Author: Nicholas Ritchie
# Modified: 28-Sep-2015
project = "Training"
analyst = "McPNNIST"
sample = "Q19801-10"

fov = 0.256 # mm
maxFields = 0 # or 0 to analyse all fields
maxPartPerField = 10 #
maxParticles = 10000
seed = None # or an integer value (randomizes field selection for maxFields)
measureThreshold = (32, 255) # lower, upper
measureDwell = 4 # microseconds
searchThreshold = (96, 255) # lower, upper
searchDwell = 1 # microseconds

pts = fromXML(u'<org.python.core.PyList>\n  <objtype resolves-to="org.python.core.PyType$TypeResolver">\n    <underlying__class>org.python.core.PyList</underlying__class>\n    <module>__builtin__</module>\n    <name>list</name>\n  </objtype>\n  <delegator class="org.python.core.PySequence$DefaultIndexDelegate">\n    <outer-class class="org.python.core.PyList" reference="../.."/>\n  </delegator>\n  <list>\n    <org.python.core.PyObjectDerived>\n      <objtype class="org.python.core.PyJavaType" resolves-to="org.python.core.PyType$TypeResolver">\n        <module>gov.nist.microanalysis.EPQLibrary</module>\n        <name>StageCoordinate</name>\n      </objtype>\n      <attributes class="StageCoordinate">\n        <mData class="tree-map">\n          <entry>\n            <Axis>X</Axis>\n            <double>1.27515625</double>\n          </entry>\n          <entry>\n            <Axis>Y</Axis>\n            <double>-14.83203125</double>\n          </entry>\n          <entry>\n            <Axis>Z</Axis>\n            <double>9.070625</double>\n          </entry>\n          <entry>\n            <Axis>R</Axis>\n            <double>-0.0</double>\n          </entry>\n          <entry>\n            <Axis>T</Axis>\n            <double>-0.0</double>\n          </entry>\n        </mData>\n      </attributes>\n      <slots/>\n    </org.python.core.PyObjectDerived>\n    <org.python.core.PyObjectDerived>\n      <objtype class="org.python.core.PyJavaType" reference="../../org.python.core.PyObjectDerived/objtype"/>\n      <attributes class="StageCoordinate">\n        <mData class="tree-map">\n          <entry>\n            <Axis>X</Axis>\n            <double>4.00125</double>\n          </entry>\n          <entry>\n            <Axis>Y</Axis>\n            <double>-12.82078125</double>\n          </entry>\n          <entry>\n            <Axis>Z</Axis>\n            <double>9.070625</double>\n          </entry>\n          <entry>\n            <Axis>R</Axis>\n            <double>-0.0</double>\n          </entry>\n          <entry>\n            <Axis>T</Axis>\n            <double>-0.0</double>\n          </entry>\n        </mData>\n      </attributes>\n      <slots/>\n    </org.python.core.PyObjectDerived>\n    <org.python.core.PyObjectDerived>\n      <objtype class="org.python.core.PyJavaType" reference="../../org.python.core.PyObjectDerived/objtype"/>\n      <attributes class="StageCoordinate">\n        <mData class="tree-map">\n          <entry>\n            <Axis>X</Axis>\n            <double>0.390625</double>\n          </entry>\n          <entry>\n            <Axis>Y</Axis>\n            <double>-12.1665625</double>\n          </entry>\n          <entry>\n            <Axis>Z</Axis>\n            <double>9.070625</double>\n          </entry>\n          <entry>\n            <Axis>R</Axis>\n            <double>-0.0</double>\n          </entry>\n          <entry>\n            <Axis>T</Axis>\n            <double>-0.0</double>\n          </entry>\n        </mData>\n      </attributes>\n      <slots/>\n    </org.python.core.PyObjectDerived>\n  </list>\n  <gListAllocatedStatus>3</gListAllocatedStatus>\n</org.python.core.PyList>')
tiling=circularTiling(pts)

# Create the EDS analysis vectors
stds = { "C": "C std.msa", "Al": "Al std.msa", "Cu":"Cu std.msa", "Ni":"Ni std.msa", 
    "Na": "NaCl std.msa", "Cl": "NaCl std.msa", "O": "MgO std.msa", "Si": "Si std.msa", 
	"Fe": "Fe std.msa", "Ca": "CaF2 std.msa", "Cr": "Cr std.msa", "Ni":"Ni std.msa", 
	"Cu": "Cu std.msa", "Ti": "Ti std.msa", "Mg": "Mg std.msa", 
	"S": "FeS2 std.msa", "Zn" : "Zn std.msa", "Ba":"BaF2 std.msa" 
}
vecs = buildVectors(stds,strip=["C","O"],path="C:\\Data\\standards\\Combined\\20 keV\\MSA")

rules = graf.BasicRuleSet("C:\\Data\\Rule Files\\MA319 Rule File JUNE 2012.zrr")

try:
	# Create the RCA object, initialize and execute the analysis
	rca=buildRCA(project, sample, vecs, rules, analyst=analyst)
	rca.setFieldOfView(fov, overlap = 1.0)
	rca.setMeasureThreshold(low=measureThreshold[0], high = measureThreshold[1], dwell = int(measureDwell*1000), measureStep = 8)
	rca.setSearchThreshold(low=searchThreshold[0], high = searchThreshold[1], dwell = int(searchDwell*1000), maxPartPerField=maxPartPerField, maxPart=maxParticles)
	try:
		rca.perform(randomizedTiling(tiling, seed=seed))
	except jl.Throwable, ex:
		print "Error analyzing %s" % sample
		print str(ex)
	finally:
		rca.postSummary(jl.System.out)
finally:
	if not terminated:
		turnOff()