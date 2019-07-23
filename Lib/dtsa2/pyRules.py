"""Mechanisms for defining Zeppelin rule sets in Python."""

import gov.nist.microanalysis.Graf as graf

class PythonRules (graf.IRuleSet):
    """PythonRules(name, ruleList, [transforms=None], [apply=None])
    Create a class implementing graf.IRuleSet that evaluates rules written in Python. 
Arguments:
    ruleList: A list of (( name0, rule0 ), (name1, rule1 ), ... ) where the nameX is the rule name and rule1 is a Python function of one argument (a dictionary of particle data).
    
        def ruleU(tvm):
            return tvm.get(92,0.0)>10.0

        Note: It is often better to use tvm.get(key, default) rather than tvm[key] because the later fails when key is not in the map.

        e.g. ruleList=( ( "Na with U", lambda tvm : (tvm.get(11,0.0)>tvm.get(92,0.0)) and (tvm.get(92,0.0)>10) ),
                        ( "U norm", lambda tvm : tvm[2000]>0.49 ),
                        ( "Ti-bearing", lambda tvm: tvm.get(22,0.0)>20.0 ),
                        ( "U-bearing", ruleU ), # see below
                        ( "Fe-Cr", lambda tvm : (tvm.get(24,0.0)>10) and (tvm.get(26,0.0)>10) ), 
                        ( "Other", lambda tvm : True ) )

    name: The name argument is a user-friendly name for the rule set. 
    transforms: The 'transforms' argument is some optional Python function that is executed before the rule set is applied. 'transforms' takes one argument, the map containing the particle data items and returns a dictionary of key-values which is merged into a copy of the original tvm. It can be used to create new items in the dictionary 'tvm' (see evaluate(...)) which can then be used in the ruleList.
        def trans(tvm):
            n = tvm[11]+tvm[92]+tvm[24]
            return { 2000: tvm.get(92,0.0)/n, 2001: tvm.get(24,0.0)/n, 2002: tvm.get(11,0.0)/n }

    apply: Useful when this rule set is the basis for a CompoundRule set.  If apply returns False, then this rule set will always return '#Unclassified'
        def apply(tvm):
            return tvm.get(92,0.0)>1.0"""
    def __init__(self, name, ruleList, transforms=None, apply=None):
        self._rule = ruleList
        self._name = name
        self._transforms = transforms
        self._apply = apply

    def evaluate(self, tvm):
        tvmc = dict(tvm)
        if (not self._apply) or self._apply(tvmc):
            if self._transforms:
                tvmc.update(self._transforms(tvmc))
            for i in range(0, len(self._rule)):
                if self._rule[i][1](tvmc):
                    return i
        return len(self._rule)

    def ruleName(self, i):
        if i<len(self._rule):
            return self._rule[i][0]
        else:
            return "#Unclassified"

    def ruleCount(self):
        return len(self._rule)+1

    def getName(self):
        return self._name

    def __str__(self):
        return self._name

        
class CompoundRuleSet (graf.IRuleSet):

    """CompoundRuleSet(name, ruleSetList)
    The CompoundRuleSet class provides a mechanism to build up complex rule sets based on sets of simpler rule sets.  For example, you might build a CompoundRule set based on a series of rule sets for Steels, Aluminum Alloys, Magnet materials, etc.  If the base rule set is derived from PythonRules you can use the 'apply' argument to short circuit application of a rule set if it is clearly inappropriate.
    Evaluates in order the rule sets from which this CompoundRule is constructed.  The function returns the index of the first rule to evaluate True (unless the rule is the last in any sub-rule set). The last rule of each sub-rule set is assumed to be '#Unclassified'."""
    def __init__(self, name, rules):
        self._name = name
        self._ruleSets = list(rules)

    """append(ruleSet)
    Append a rule set to the end of the list of rule sets.  This will become the last rule set to be evaluated."""
    def append(self, ruleSet):
        self._ruleSets.append(ruleSet)
        
    def evaluate(self, tvm):
        cx=0
        for ruleSet in self._ruleSets:
            res=ruleSet.evaluate(tvm)
            if res+1<ruleSet.ruleCount():
                return cx+res
            cx=cx+(ruleSet.ruleCount()-1)
        return cx
        
    def ruleName(self, i):
        cx=i
        for ruleSet in self._ruleSets:
            if cx<ruleSet.ruleCount()-1:
                return ruleSet.ruleName(cx)
            cx=cx-(ruleSet.ruleCount()-1)
        return "#Unclassified"
            
    def ruleCount(self):
        cx=0
        for ruleSet in self._ruleSets:
            cx=cx+(ruleSet.ruleCount()-1)
        return cx+1
        
    def getName(self):
        return self._name

    def __str__(self):
        return self._name

    def listRuleSets(self):
        for ruleSet in self._ruleSets:
            print "%s\t%d" % (ruleSet, ruleSet.ruleCount())


# These constants define the standard Zeppelin column contents
APA_COMPHASH = 1059
APA_SOURCEINDEX = 1058
APA_FITQUAL = 1057
APA_DISTANCE = 1056
APA_RMSVIDEO = 1055
APA_EDGEROUGHNESS = 1054
APA_VOIDCOUNT = 1053
APA_VOIDAREA = 1052
APA_ROUGHNESS = 1051
APA_CLASS = 1050
APA_DENSITY = 1049
APA_USERVAR9 = 1048
APA_USERVAR8 = 1047
APA_USERVAR7 = 1046
APA_USERVAR6 = 1045
APA_USERVAR5 = 1044
APA_USERVAR4 = 1043
APA_USERVAR3 = 1042
APA_USERVAR2 = 1041
APA_USERVAR1 = 1040
APA_USERVAR0 = 1039
APA_TYPE4ET = 1038
APA_TOTALCOUNTS = 1037
APA_EDSTIME = 1036
APA_VIDEO = 1035
APA_PCT4 = 1034
APA_PCT3 = 1033
APA_PCT2 = 1032
APA_PCT1 = 1031
APA_COUNTS4 = 1030
APA_COUNTS3 = 1029
APA_COUNTS2 = 1028
APA_COUNTS1 = 1027
APA_ATOMICNUM4 = 1026
APA_ATOMICNUM3 = 1025
APA_ATOMICNUM2 = 1024
APA_ATOMICNUM1 = 1023
APA_ACTION = 1022
APA_MAGINDEX = 1021
APA_MAG = 1020
APA_ORIENT = 1019
APA_PERIM = 1018
APA_AREA = 1017
APA_ASPECT = 1016
APA_DPERP = 1015
APA_DMIN = 1014
APA_DMAX = 1013
APA_DAVE = 1012
APA_YFERET = 1011
APA_XFERET = 1010
APA_YCG = 1009
APA_XCG = 1008
APA_YCENT = 1007
APA_XCENT = 1006
APA_YABS = 1005
APA_XABS = 1004
APA_MAGFIELDNUM = 1003
APA_FIELDNUM = 1002
APA_PARTNUM = 1001

import java.io as jio

def reclassify(zepFile, ruleSet):
    """reclassify(zepFile, ruleSet)
Arguments:
    zepFile: Is a string containing the full path of the Zeppelin file
    ruleSet: An instance of a class that implements graf.IRuleSet"""
    z = graf.Zeppelin(zepFile)
    z.evaluateClass(ruleSet)
    for i in range(0,100):
        newName=zepFile.replace(".hdz","_%s.hdz" % ( str(ruleSet), ) )
        if not jio.File(newName).exists():
            print "Writing resulting data set to "+newName
            z.write(newName)
            break
    return z
        
    
