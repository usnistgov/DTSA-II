'''
      Title: EPMA Standard Utilities
     Author: NWMR
       Date: 23-Feb-2010
Description: A set of utility functions for identifying appropriate standards from those available in the NIST SMSD EPMA standard block collection.

Methods are:
   stdsContaining(elm, min=1.0): Returns a list of standards containing at least min weight percent of elm
   stdsContainingAll(elms,min=1.0): Returns a list of standards containing at least min weight percent of all elements in elms.
   stdsContainingSome(elms,min=1.0,tol=2): Returns a list of standards containing at least tol of the elements in elms with at least a weight percentage of min.
   compMetric(c1,c2): A metric function for determining the similarity of two Composition objects.
   similarStandards(comp,tol=0.5): Returns a list of standards similar to comp for which the metric function compMetric returns a value less than tol.
   blocksContaining(mat): Returns a list of the standard blocks containing standard materials named mat'''


def dump(comp):
   if isinstance(comp,str):
      comp=safematerial(comp)
   print "%s\t%s" % (comp, "\t".join("%0.4f" % comp.weightFraction(epq.Element.byAtomicNumber(z),0) for z in range(0,100)))

MissingMaterials = []

def safematerial(str):
   try:
      return material(str)
   except epq.EPQException:
      MissingMaterials.append(str)
   return None

try:
   populate
except NameError:
   populate=False
   
K546=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Mg, epq.Element.Al, epq.Element.P, epq.Element.Ti, epq.Element.Cr, epq.Element.Ni, epq.Element.Ge, epq.Element.Zr, epq.Element.Ba, epq.Element.Eu, epq.Element.Th, epq.Element.U, epq.Element.O],[0.0216, 0.2170, 0.0017, 0.0986, 0.0043, 0.0034, 0.0014, 0.0041, 0.0051, 0.0064, 0.0096, 0.0106, 0.0016, 0.0020, 0.6125],'K546')

if populate:   
   def addToDB(list):
      for comp in list:
         if not dt2.DTSA2.getSession().findStandard(comp.getName()):
            dt2.DTSA2.getSession().addStandard(comp)

   K249=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.Ba, epq.Element.Ta, epq.Element.Pb, epq.Element.Bi, epq.Element.O],[0.0265, 0.1402, 0.0896, 0.0819, 0.3945, 0.0224, 0.2449],'K249')
   K508=epq.Composition([epq.Element.B, epq.Element.Si, epq.Element.Sr, epq.Element.O],[0.0926, 0.1206, 0.3754, 0.4114],'K508')
   K227=epq.Composition([epq.Element.Si, epq.Element.Pb, epq.Element.O],[0.0935, 0.7427, 0.1639],'K227')
   K309=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.Fe, epq.Element.Ba, epq.Element.O],[0.0794, 0.1870, 0.1072, 0.1049, 0.1343, 0.3872],'K309')
   K961=epq.Composition([epq.Element.Na, epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.P, epq.Element.K, epq.Element.Ca, epq.Element.Ti, epq.Element.Mn, epq.Element.Fe, epq.Element.O],[0.0297, 0.0302, 0.0582, 0.2992, 0.0022, 0.0249, 0.0357, 0.0120, 0.0032, 0.0350, 0.4698],'K961')
   K1080=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.Ti, epq.Element.Sr, epq.Element.Zr, epq.Element.Lu, epq.Element.O],[0.0279, 0.0062, 0.0121, 0.0794, 0.1870, 0.1072, 0.0120, 0.1268, 0.0074, 0.0176, 0.4165],'K1080')

   addToDB([K249,K508,K227,K309,K961,K1080])

   K2307=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.Ti, epq.Element.Mn, epq.Element.Zn, epq.Element.O],[0.0794, 0.2571, 0.1072, 0.0300, 0.0316, 0.0402, 0.4546],'K2307')
   K252=epq.Composition([epq.Element.Si, epq.Element.Mn, epq.Element.Co, epq.Element.Cu, epq.Element.Zn, epq.Element.Ba, epq.Element.O],[0.1870, 0.0316, 0.0393, 0.0399, 0.0803, 0.3135, 0.3083],'K252')
   K975=epq.Composition([epq.Element.Li, epq.Element.Al, epq.Element.Si, epq.Element.K, epq.Element.Ca, epq.Element.Rb, epq.Element.Sr, epq.Element.Ag, epq.Element.Ce, epq.Element.Tb, epq.Element.Yb, epq.Element.Ta, epq.Element.O],[0.0105, 0.0892, 0.2102, 0.0187, 0.0675, 0.0206, 0.1005, 0.0104, 0.0183, 0.0195, 0.0198, 0.0184, 0.3966],'K975')

   addToDB([K2307,K252,K975])

   K456=epq.Composition([epq.Element.Si, epq.Element.Pb, epq.Element.O],[0.1345, 0.6612, 0.2043],'K456')
   K1013=epq.Composition([epq.Element.Mg, epq.Element.Al, epq.Element.P, epq.Element.Ti, epq.Element.Cr, epq.Element.Ni, epq.Element.Ge, epq.Element.Zr, epq.Element.Ba, epq.Element.Eu, epq.Element.Th, epq.Element.U, epq.Element.O],[0.0524, 0.0582, 0.3340, 0.0022, 0.0023, 0.0026, 0.0033, 0.0041, 0.0061, 0.0067, 0.0011, 0.0013, 0.5259],'K1013')
   K963=epq.Composition([epq.Element.Mg, epq.Element.Si, epq.Element.P, epq.Element.Ti, epq.Element.Cr, epq.Element.Ni, epq.Element.Zn, epq.Element.Ge, epq.Element.Zr, epq.Element.Ba, epq.Element.Eu, epq.Element.Th, epq.Element.U, epq.Element.O],[0.0014, 0.2199, 0.0036, 0.0028, 0.0030, 0.0034, 0.0300, 0.0042, 0.0053, 0.3952, 0.0088, 0.0013, 0.0017, 0.3192],'K963')
   K495=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Al, epq.Element.O],[0.0232, 0.2331, 0.1058, 0.6379],'K495')
   K489=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Al, epq.Element.Si, epq.Element.Ti, epq.Element.Fe, epq.Element.Zn, epq.Element.Zr, epq.Element.Ba, epq.Element.Ce, epq.Element.Ta, epq.Element.Pb, epq.Element.O],[0.0000, 0.0006, 0.0015, 0.2186, 0.0028, 0.0032, 0.0299, 0.0052, 0.3930, 0.0080, 0.0103, 0.0119, 0.3149],'K489')
   K490=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Al, epq.Element.Si, epq.Element.Ti, epq.Element.Fe, epq.Element.Zr, epq.Element.Ce, epq.Element.Ta, epq.Element.Pb, epq.Element.O],[0.0217, 0.2175, 0.0989, 0.0020, 0.0033, 0.0038, 0.0063, 0.0097, 0.0125, 0.0144, 0.6098],'K490')

   addToDB([K456,K1013,K963,K495,K489,K490])

   Anorthite = safematerial("CaAl2Si2O8")
   Anorthite.setName("Anorthite")
   Albite = safematerial("NaAlSi3O8")
   Albite.setName("Albite")
   Diopside = safematerial("MgCaSi2O6")
   Diopside.setName("Diopside")
   Apatite = safematerial("Ca5(PO3)4OH")
   Apatite.setName('Apatite')

   addToDB([Anorthite,Albite,Diopside,Apatite])

   Benitoite=safematerial('BaTiSi3O9')
   Benitoite.setName('Benitoite')
   Enstatite=safematerial('Mg2Si2O6')
   Enstatite.setName('Enstatite')
   K371=epq.Composition([epq.Element.Si, epq.Element.Zn, epq.Element.Ba, epq.Element.O],[0.2308, 0.0307, 0.4192, 0.3193],'K371')
   K229=epq.Composition([epq.Element.Si, epq.Element.Pb, epq.Element.O],[0.1402, 0.6498, 0.2099],'K229')
   K408=epq.Composition([epq.Element.Si, epq.Element.Eu, epq.Element.Pb, epq.Element.O],[0.1402, 0.0432, 0.6034, 0.2132],'K408')
   K409=epq.Composition([epq.Element.Na, epq.Element.Al, epq.Element.Si, epq.Element.Fe, epq.Element.O],[0.0742, 0.0794, 0.2571, 0.1555, 0.4339],'K409')

   addToDB([Benitoite,Enstatite,K371,K229,K408,K409])

   K230=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.Zn, epq.Element.Ba, epq.Element.Ta, epq.Element.Pb, epq.Element.O],[0.0265, 0.1402, 0.0402, 0.0896, 0.0409, 0.4177, 0.2449],'K230')
   K240=epq.Composition([epq.Element.Mg, epq.Element.Si, epq.Element.Ti, epq.Element.Zn, epq.Element.Zr, epq.Element.Ba, epq.Element.O],[0.0302, 0.1870, 0.0600, 0.0402, 0.0740, 0.2687, 0.3400],'K240')
   K249=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.Ba, epq.Element.Ta, epq.Element.Pb, epq.Element.Bi, epq.Element.O],[0.0265, 0.1402, 0.0896, 0.0819, 0.3945, 0.0224, 0.2449],'K249')
   K251=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.Ba, epq.Element.Ta, epq.Element.Pb, epq.Element.Bi, epq.Element.O],[0.0132, 0.1402, 0.0896, 0.0409, 0.4410, 0.0448, 0.2302],'K251')
   K252=epq.Composition([epq.Element.Si, epq.Element.Mn, epq.Element.Co, epq.Element.Cu, epq.Element.Zn, epq.Element.Ba, epq.Element.O],[0.1870, 0.0316, 0.0393, 0.0399, 0.0803, 0.3135, 0.3083],'K252')
   K253=epq.Composition([epq.Element.Si, epq.Element.Mn, epq.Element.Co, epq.Element.Cu, epq.Element.Zn, epq.Element.Ba, epq.Element.O],[0.1870, 0.0632, 0.0197, 0.0200, 0.0803, 0.3135, 0.3164],'K253')
   K309=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.Fe, epq.Element.Ba, epq.Element.O],[0.0794, 0.1870, 0.1072, 0.1049, 0.1343, 0.3872],'K309')
   K326=epq.Composition([epq.Element.B, epq.Element.Na, epq.Element.Mg, epq.Element.Si, epq.Element.Ca, epq.Element.O],[0.0932, 0.0148, 0.1815, 0.1398, 0.0572, 0.5135],'K326')
   K366=epq.Composition([epq.Element.Si, epq.Element.Pb, epq.Element.O],[0.1345, 0.6612, 0.2043],'K366')
   K369=epq.Composition([epq.Element.Ge, epq.Element.Pb, epq.Element.O],[0.2865, 0.5451, 0.1684],'K369')
   K373=epq.Composition([epq.Element.Na, epq.Element.Si, epq.Element.Zn, epq.Element.Ba, epq.Element.U, epq.Element.O],[0.1043, 0.3185, 0.0494, 0.1038, 0.0005, 0.4234],'K373')
   K376=epq.Composition([epq.Element.Na, epq.Element.Si, epq.Element.Zn, epq.Element.Ba, epq.Element.U, epq.Element.O],[0.1038, 0.3166, 0.0492, 0.1034, 0.0053, 0.4218],'K376')
   K378=epq.Composition([epq.Element.Na, epq.Element.Si, epq.Element.Zn, epq.Element.Ba, epq.Element.U, epq.Element.O],[0.1019, 0.3105, 0.0484, 0.1016, 0.0210, 0.4166],'K378')
   K371=epq.Composition([epq.Element.Si, epq.Element.Zn, epq.Element.Ba, epq.Element.O],[0.2308, 0.0307, 0.4192, 0.3193],'K371')
   K408=epq.Composition([epq.Element.Si, epq.Element.Eu, epq.Element.Pb, epq.Element.O],[0.1402, 0.0432, 0.6034, 0.2132],'K408')
   K409=epq.Composition([epq.Element.Na, epq.Element.Al, epq.Element.Si, epq.Element.Fe, epq.Element.O],[0.0742, 0.0794, 0.2571, 0.1555, 0.4339],'K409')
   K453=epq.Composition([epq.Element.Ge, epq.Element.Pb, epq.Element.O],[0.2865, 0.5451, 0.1684],'K453')
   K456=epq.Composition([epq.Element.Si, epq.Element.Pb, epq.Element.O],[0.1345, 0.6612, 0.2043],'K456')
   K458=epq.Composition([epq.Element.Si, epq.Element.Zn, epq.Element.Ba, epq.Element.O],[0.2308, 0.0307, 0.4192, 0.3193],'K458')
   K489=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Al, epq.Element.Si, epq.Element.Ti, epq.Element.Fe, epq.Element.Zn, epq.Element.Zr, epq.Element.Ba, epq.Element.Ce, epq.Element.Ta, epq.Element.Pb, epq.Element.O],[0.0000, 0.0006, 0.0015, 0.2186, 0.0028, 0.0032, 0.0299, 0.0052, 0.3930, 0.0080, 0.0103, 0.0119, 0.3149],'K489')
   K490=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Al, epq.Element.Si, epq.Element.Ti, epq.Element.Fe, epq.Element.Zr, epq.Element.Ce, epq.Element.Ta, epq.Element.Pb, epq.Element.O],[0.0217, 0.2175, 0.0989, 0.0020, 0.0033, 0.0038, 0.0063, 0.0097, 0.0125, 0.0144, 0.6098],'K490')
   K491=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Al, epq.Element.Si, epq.Element.Ti, epq.Element.Fe, epq.Element.Ge, epq.Element.Zr, epq.Element.Ce, epq.Element.Ta, epq.Element.Pb, epq.Element.O],[0.0000, 0.0003, 0.0008, 0.0009, 0.0016, 0.0018, 0.2636, 0.0030, 0.0046, 0.0059, 0.5510, 0.1665],'K491')
   K493=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Al, epq.Element.Si, epq.Element.Ti, epq.Element.Fe, epq.Element.Zr, epq.Element.Ce, epq.Element.Ta, epq.Element.Pb, epq.Element.O],[0.0000, 0.0004, 0.0011, 0.1304, 0.0019, 0.0022, 0.0036, 0.0055, 0.0072, 0.6413, 0.2063],'K493')
   K495=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Al, epq.Element.O],[0.0232, 0.2331, 0.1058, 0.6379],'K495')
   K496=epq.Composition([epq.Element.Mg, epq.Element.Al, epq.Element.P, epq.Element.O],[0.0545, 0.0605, 0.3471, 0.5380],'K496')
   K497=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.P, epq.Element.Ti, epq.Element.Fe, epq.Element.Zr, epq.Element.Ce, epq.Element.Ta, epq.Element.Pb, epq.Element.O],[0.0000, 0.0005, 0.0521, 0.0578, 0.0013, 0.3318, 0.0021, 0.0024, 0.0040, 0.0062, 0.0080, 0.0092, 0.5245],'K497')
   K508=epq.Composition([epq.Element.B, epq.Element.Si, epq.Element.Sr, epq.Element.O],[0.0926, 0.1206, 0.3754, 0.4114],'K508')
   K521=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Ce, epq.Element.O],[0.0325, 0.2113, 0.2035, 0.5527],'K521')
   K523=epq.Composition([epq.Element.Mg, epq.Element.Si, epq.Element.P, epq.Element.Ti, epq.Element.Cr, epq.Element.Ni, epq.Element.Ge, epq.Element.Zr, epq.Element.Ba, epq.Element.Eu, epq.Element.Pb, epq.Element.Th, epq.Element.U, epq.Element.O],[0.0010, 0.1290, 0.0025, 0.0019, 0.0021, 0.0024, 0.0029, 0.0036, 0.0055, 0.0060, 0.6341, 0.0010, 0.0011, 0.2071],'K523')
   K873=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.Mn, epq.Element.Ge, epq.Element.Ba, epq.Element.Ce, epq.Element.Ta, epq.Element.Pb, epq.Element.O],[0.0026, 0.1145, 0.0032, 0.1700, 0.2194, 0.0041, 0.0041, 0.2274, 0.2546],'K873')
   K919=epq.Composition([epq.Element.Si, epq.Element.Cr, epq.Element.Mn, epq.Element.Fe, epq.Element.Co, epq.Element.Ni, epq.Element.Cu, epq.Element.Ba, epq.Element.Ce, epq.Element.O],[0.2010, 0.0068, 0.0190, 0.0210, 0.0213, 0.0236, 0.0240, 0.3403, 0.0244, 0.3186],'K919')
   K961=epq.Composition([epq.Element.Na, epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.P, epq.Element.K, epq.Element.Ca, epq.Element.Ti, epq.Element.Mn, epq.Element.Fe, epq.Element.O],[0.0297, 0.0302, 0.0582, 0.2992, 0.0022, 0.0249, 0.0357, 0.0120, 0.0032, 0.0350, 0.4698],'K961')
   K963=epq.Composition([epq.Element.Mg, epq.Element.Si, epq.Element.P, epq.Element.Ti, epq.Element.Cr, epq.Element.Ni, epq.Element.Zn, epq.Element.Ge, epq.Element.Zr, epq.Element.Ba, epq.Element.Eu, epq.Element.Th, epq.Element.U, epq.Element.O],[0.0014, 0.2199, 0.0036, 0.0028, 0.0030, 0.0034, 0.0300, 0.0042, 0.0053, 0.3952, 0.0088, 0.0013, 0.0017, 0.3192],'K963')
   K968=epq.Composition([epq.Element.Mg, epq.Element.P, epq.Element.Ti, epq.Element.Cr, epq.Element.Ni, epq.Element.Ge, epq.Element.Zr, epq.Element.Ba, epq.Element.Eu, epq.Element.Pb, epq.Element.Th, epq.Element.U, epq.Element.O],[0.0008, 0.0020, 0.0016, 0.0017, 0.0019, 0.2627, 0.0030, 0.0045, 0.0049, 0.5491, 0.0008, 0.0009, 0.1662],'K968')
   K975=epq.Composition([epq.Element.Li, epq.Element.Al, epq.Element.Si, epq.Element.K, epq.Element.Ca, epq.Element.Rb, epq.Element.Sr, epq.Element.Ag, epq.Element.Ce, epq.Element.Tb, epq.Element.Yb, epq.Element.Ta, epq.Element.O],[0.0105, 0.0892, 0.2102, 0.0187, 0.0675, 0.0206, 0.1005, 0.0104, 0.0183, 0.0195, 0.0198, 0.0184, 0.3966],'K975')
   K978=epq.Composition([epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.K, epq.Element.Ca, epq.Element.Ti, epq.Element.Rb, epq.Element.Sr, epq.Element.Cs, epq.Element.La, epq.Element.Eu, epq.Element.Gd, epq.Element.O],[0.0136, 0.0892, 0.2102, 0.0187, 0.0675, 0.0067, 0.0206, 0.1005, 0.0212, 0.0192, 0.0194, 0.0195, 0.3939],'K978')
   K979=epq.Composition([epq.Element.Li, epq.Element.Al, epq.Element.Si, epq.Element.K, epq.Element.Ca, epq.Element.Ti, epq.Element.Sr, epq.Element.Nb, epq.Element.Sb, epq.Element.Cs, epq.Element.Sm, epq.Element.Dy, epq.Element.O],[0.0105, 0.0892, 0.2102, 0.0187, 0.0675, 0.0135, 0.1005, 0.0157, 0.0084, 0.0212, 0.0194, 0.0196, 0.4058],'K979')
   K1001=epq.Composition([epq.Element.Na, epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.K, epq.Element.Ca, epq.Element.Rb, epq.Element.Sr, epq.Element.Pr, epq.Element.Nd, epq.Element.Er, epq.Element.Pb, epq.Element.O],[0.0167, 0.0136, 0.0892, 0.2102, 0.0187, 0.0675, 0.0206, 0.1005, 0.0192, 0.0193, 0.0197, 0.0104, 0.3947],'K1001')
   K1008=epq.Composition([epq.Element.Na, epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.Rb, epq.Element.Sr, epq.Element.Zr, epq.Element.Cs, epq.Element.Ba, epq.Element.Ho, epq.Element.Tm, epq.Element.Yb, epq.Element.O],[0.0167, 0.0892, 0.2102, 0.0675, 0.0206, 0.1005, 0.0083, 0.0212, 0.0202, 0.0196, 0.0197, 0.0198, 0.3868],'K1008')
   K1010=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.K, epq.Element.Ca, epq.Element.Ge, epq.Element.Rb, epq.Element.Sr, epq.Element.Te, epq.Element.Cs, epq.Element.La, epq.Element.Eu, epq.Element.Gd, epq.Element.O],[0.0892, 0.2102, 0.0187, 0.0675, 0.0156, 0.0206, 0.1005, 0.0090, 0.0212, 0.0192, 0.0194, 0.0195, 0.3896],'K1010')
   K1053=epq.Composition([epq.Element.F, epq.Element.Si, epq.Element.Cl, epq.Element.Br, epq.Element.I, epq.Element.Pb, epq.Element.O],[0.0039, 0.0935, 0.0064, 0.0109, 0.0137, 0.7102, 0.1613],'K1053')
   K1070=epq.Composition([epq.Element.Mg, epq.Element.Si, epq.Element.Ca, epq.Element.Zn, epq.Element.Ba, epq.Element.Pb, epq.Element.O],[0.0754, 0.1870, 0.0893, 0.1004, 0.1120, 0.0928, 0.3431],'K1070')
   K1080=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.Ti, epq.Element.Sr, epq.Element.Zr, epq.Element.Lu, epq.Element.O],[0.0279, 0.0062, 0.0121, 0.0794, 0.1870, 0.1072, 0.0120, 0.1268, 0.0074, 0.0176, 0.4165],'K1080')
   K1092=epq.Composition([epq.Element.Li, epq.Element.Al, epq.Element.Si, epq.Element.Ta, epq.Element.O],[0.0929, 0.0529, 0.3039, 0.0409, 0.5094],'K1092')
   K1098=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Al, epq.Element.Pb, epq.Element.O],[0.0232, 0.2020, 0.1058, 0.0928, 0.5761],'K1098')
   K1132=epq.Composition([epq.Element.Li, epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.Cr, epq.Element.Ni, epq.Element.Mo, epq.Element.Ba, epq.Element.O],[0.0232, 0.0794, 0.2139, 0.1072, 0.0051, 0.0118, 0.0167, 0.1343, 0.4134],'K1132')
   K1012=epq.Composition([epq.Element.Na, epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.P, epq.Element.K, epq.Element.Ca, epq.Element.Ga, epq.Element.Se, epq.Element.In, epq.Element.Cs, epq.Element.Ba, epq.Element.Ce, epq.Element.O],[0.0148, 0.0905, 0.0794, 0.1870, 0.0063, 0.0166, 0.0040, 0.0149, 0.0071, 0.0165, 0.0189, 0.1343, 0.0163, 0.3934],'K1012')
   K1229=epq.Composition([epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.Sr, epq.Element.Ba, epq.Element.O],[0.0177, 0.0934, 0.2200, 0.1051, 0.0746, 0.0790, 0.4102],'K1229')
   K1234=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.Ba, epq.Element.W, epq.Element.O],[0.0915, 0.2156, 0.1236, 0.1549, 0.0159, 0.3985],'K1234')
   K1235=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.Pd, epq.Element.Ba, epq.Element.Hf, epq.Element.O],[0.0886, 0.2116, 0.1215, 0.0174, 0.1523, 0.0170, 0.3917],'K1235')
   K1236=epq.Composition([epq.Element.Sc, epq.Element.Ge, epq.Element.Pb, epq.Element.O],[0.0065, 0.4095, 0.3713, 0.2127],'K1236')
   K1012=epq.Composition([epq.Element.Na, epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.P, epq.Element.K, epq.Element.Ca, epq.Element.Ga, epq.Element.Se, epq.Element.In, epq.Element.Cs, epq.Element.Ba, epq.Element.Ce, epq.Element.O],[0.0148, 0.0905, 0.0794, 0.1870, 0.0063, 0.0166, 0.0040, 0.0149, 0.0071, 0.0165, 0.0189, 0.1343, 0.0163, 0.3934],'K1012')

   addToDB([K227,K229,K230,K240,K249,K251,K252,K253,K309,K326,K366,K369,K373,K376,K378,K371,K408,K409,K453,K456,K458,K489,K490,K491,K493,K495,K496,K497,K508,K521,K523,K873,K919,K961,K963,K968,K975,K978,K979,K1001,K1008,K1010,K1053,K1070,K1080,K1092,K1098,K1132,K1012,K1229,K1234,K1235,K1236,K1012])

   K447=epq.Composition([epq.Element.Li, epq.Element.B, epq.Element.Al, epq.Element.O],[0.0232, 0.2331, 0.1058, 0.6379],'K447')
   K711=epq.Composition([epq.Element.Na, epq.Element.Si, epq.Element.K, epq.Element.Pb, epq.Element.O],[0.0185, 0.2150, 0.0467, 0.4207, 0.2935],'K711')
   K884=epq.Composition([epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.Fe, epq.Element.O],[0.0200, 0.1152, 0.2233, 0.0755, 0.1160, 0.4500],'K884')
   K970=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.K, epq.Element.Ca, epq.Element.V, epq.Element.Ga, epq.Element.As, epq.Element.Sr, epq.Element.Cd, epq.Element.Sn, epq.Element.Cs, epq.Element.Bi, epq.Element.O],[0.0794, 0.1870, 0.0166, 0.1072, 0.0112, 0.0074, 0.0151, 0.1268, 0.0175, 0.0158, 0.0189, 0.0179, 0.3791],'K970')
   K1228=epq.Composition([epq.Element.Li, epq.Element.Na, epq.Element.Al, epq.Element.Si, epq.Element.K, epq.Element.Ca, epq.Element.Rb, epq.Element.Cs, epq.Element.Ba, epq.Element.O],[0.0139, 0.0223, 0.0794, 0.1870, 0.0249, 0.1072, 0.0274, 0.0283, 0.1343, 0.3753],'K1228')4
   K1504=epq.Composition([epq.Element.
   
   K1546=epq.Composition([epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.P, epq.Element.Ca, epq.Element.Ti, epq.Element.V, epq.Element.Mn, epq.Element.Co, epq.Element.Cu, epq.Element.Zn, epq.Element.O],[0.0724, 0.0505, 0.1823, 0.0282, 0.0858, 0.0300, 0.0112, 0.0126, 0.0142, 0.0160, 0.0643, 0.4326],'K1546')
   K1597=epq.Composition([epq.Element.Mg, epq.Element.Si, epq.Element.K, epq.Element.O],[0.1206, 0.2805, 0.1660, 0.4329],'K1597')
   K2115=epq.Composition([epq.Element.Si, epq.Element.Pb, epq.Element.Bi, epq.Element.O],[0.0935, 0.3713, 0.3588, 0.1764],'K2115')
   K1788=epq.Composition([epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.Ta, epq.Element.O],[0.0603, 0.1058, 0.1402, 0.3276, 0.3660],'K1788')
   K2757=epq.Composition([epq.Element.Si, epq.Element.Zn, epq.Element.Cs, epq.Element.Ba, epq.Element.Eu, epq.Element.O],[0.1730, 0.0288, 0.3508, 0.0605, 0.1337, 0.2533],'K2757')
   K2654=epq.Composition([epq.Element.Al, epq.Element.Si, epq.Element.Ca, epq.Element.La, epq.Element.O],[0.1323, 0.1169, 0.1787, 0.2132, 0.3590],'K2654')
   K2618=epq.Composition([epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.Ba, epq.Element.Ta, epq.Element.O],[0.0603, 0.0794, 0.1402, 0.1343, 0.2649, 0.3209],'K2618')
   K2612=epq.Composition([epq.Element.Na, epq.Element.Si, epq.Element.Zn, epq.Element.Ba, epq.Element.Sm, epq.Element.O],[0.0731, 0.2231, 0.0346, 0.0727, 0.2587, 0.3378],'K2612')
   K2580=epq.Composition([epq.Element.Li, epq.Element.Si, epq.Element.Ge, epq.Element.Zr, epq.Element.Ba, epq.Element.Hf, epq.Element.Pb, epq.Element.O],[0.0465, 0.1870, 0.0694, 0.0740, 0.0896, 0.0848, 0.0928, 0.3559],'K2580')
   K2577=epq.Composition([epq.Element.Mg, epq.Element.Al, epq.Element.Si, epq.Element.Zn, epq.Element.Ta, epq.Element.O],[0.0603, 0.1058, 0.1402, 0.0803, 0.2457, 0.3676],'K2577')
   K2573=epq.Composition([epq.Element.Na, epq.Element.Si, epq.Element.Zn, epq.Element.Ba, epq.Element.Nd, epq.Element.O],[0.0731, 0.2231, 0.0346, 0.0727, 0.2572, 0.3393],'K2573')
   K2575=epq.Composition([epq.Element.Na, epq.Element.Si, epq.Element.Zn, epq.Element.Ba, epq.Element.Gd, epq.Element.O],[0.0731, 0.2231, 0.0346, 0.0727, 0.2603, 0.3362],'K2575')

   addToDB([K447,K711,K884,K970,K1228,K1546,K1597,K2115,K1788,K2757,K2654,K2618,K2612,K2580,K2577,K2573, K2575])

   addToDB([K1053,K1070,K711,K1235,K1236,K1546,K1597,K2115,K1788,K1234,K1228,K1012,K1010,K1001,K884,K873,K2757,K2654,K2618,K2612,K2580,K2577,K2573,K2575])

   K2754=epq.Composition([epq.Element.Cu, epq.Element.Sr, epq.Element.Y, epq.Element.Ba, epq.Element.O],[0.0799, 0.2930, 0.1181, 0.3135, 0.1955],'K2754')
   addToDB([K2754])

   SRM93a=epq.Composition(map(element,('B','O','Na','Mg','Al','Si','Cl','K','Ca','Ti','Fe','Zr')),map(lambda a : 0.01*a,(3.90,53.8,2.95,0.003,1.21,37.8,0.060,0.012,0.01,0.008,0.020,0.031)),'SRM93a')
   
   addToDB([SRM93a])

# Mystery glasses "K1638", "K1591", "K1628", "K1617", "K1589", "Arenal Hornblende", "Juan de Fuca"
SMA = map(safematerial,( "Be", "B", "C", "Mg", "Al", "Si", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "GaP", "Ge", "As", "Se", "ZnS", "Y", "Zr", "Nb", "Mo", "K249", "K508", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "Ce", "Dy", "Er", "Lu", "Hf", "Ta", "W", "Re", "Ir", "Pt", "Au", "Tl", "Pb", "Bi", "PbSe", "K227", "K309", "K411", "K412", "K961", "SiO2", "Al2O3", "MgO", "FeS", "Benitoite", "Fluorapatite", "SrF2", "GdF3", "K1080" ))
SMB = map(safematerial,( "Be", "B", "C", "Mg", "Al", "Si", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "GaP", "Ge", "As", "Se", "ZnS", "Y", "Zr", "Nb", "Mo", "K249", "NdF3", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "Ce", "Dy", "Er", "Lu", "Hf", "Ta", "W", "Re", "Ir", "Pt", "Au", "Tl", "Pb", "Bi", "PbSe", "K227", "K309", "K411", "K412", "K961", "SiO2", "Al2O3", "MgO", "FeS", "Benitoite", "Fluorapatite", "K1504", "GdF3", "K979" ))
SMC = map(safematerial,( "Be", "B", "C", "Mg", "Al", "Si", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "GaP", "Ge", "As", "Se", "ZnS", "Y", "Zr", "Nb", "Mo", "K975", "K508", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "Ce", "Dy", "Er", "Lu", "Hf", "Ta", "W", "Re", "Ir", "Pt", "Au", "Tl", "Pb", "Bi", "PbSe", "K227", "K309", "K411", "K412", "K961", "SiO2", "Al2O3", "MgO", "FeS", "Benitoite", "Fluorapatite", "SrF2", "K2307", "GdF3", "K252" ))
SMD = map(safematerial,( 'Be','B','C','Mg','Al','Si','Sc','Ti','V','Cr','Mn','Fe','Co','Ni','Cu','Zn','GaP','Ge','As','Se','ZnS','Y','Zr','Nb','Mo','K1012','Rh','Pd','Ag','Cd','Sn','Sb','Te','Ce','Dy','Er','Lu','Hf','Ta','W','Re','Ir','Pt','Au','Tl','Bi','PbSe','K227','K309','K411','K412','K961','SiO2','Al2O3','MgO','FeS','Benitoite','Fluorapatite','CaF2','GdF3','K1080'))


HOMOII = map(safematerial, ("K411", "K456", "K412", "K1013", "K963", "K495", "K489", "K490","K1529"))
HOMOIII = map(safematerial, ('Diopside', 'Benitoite', 'Al2O3', 'SiO2', 'MgO', 'FeS', 'K411', 'K412', 'K530', 'K2852'))
# unknown 
EMI = map(safematerial, ("U", "Be", "B", 'C', 'Mg', 'Al', 'Si', 'V', 'GaP', 'Sc', 'Ti', 'Cr', 'Mn', 'Fe', 'Co', 'Ni', 'Cu', 'Zn', 'Ge', 'As', 'Gd(PO3)3', 'Se', 'Y', 'Zr', 'Nb', 'Mo', 'Apatite', 'In', 'Rh', 'Pd', 'Ag', 'Cd', 'Sn' 'Sb', 'Te', 'Pt', 'Hf', 'Ta', 'W', 'Re', 'Os', 'Ir', 'Au', 'Al2O3', 'PbSe', 'Bi', 'Ce', 'Gd', 'Tb', 'Benitoite', 'Er', 'Mo', 'Dy', 'Lu', 'K411', 'K412', 'K227', 'K309', 'K961', 'SiO2', 'FeS2', 'CdSe', 'ZnS', 'MgO'))
EMII = map(safematerial, ( "Be", "B", "C", "Mg", "Al", "Si", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Ni", "Cu", "Zn", "Ge", "As", "Se", "Y", "Zr", "Nb", "Mo", "Rh", "Pd", "Ag", "Cd", "In", "Sn", "Sb", "Te", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "Pb", "Bi", "Ce", "Gd", "Dy", "Er", "Lu", "U" ))
GMIIB=map(safematerial, ('K227','K229','K230','K240','K249','K251','K252','K253','K309','K326','K366','K369','K373','K376','K378','K371','K408','K409','K411','K412','K453','K456','K458','K489','K490','K491','K493','K495','K496','K497','K508','K521','K523','K873','K919','K961','K963','K968','K975','K978','K979','K1001','K1008','K1010','K1053','K1070','K1080','K1092','K1098','K1132','K1012','K1229','K1234','K1235','K1236','K1012'))
HTCSC = map(safematerial, ('NdF3', 'K411', 'K412', 'HgTe', 'SrF2', 'Bi', 'SrSO4', 'CdTe', 'PrF3', 'CaF2', 'LaF3', 'PbSe', 'SrTiO3', 'Fe', 'TiO2', 'BaCO3', 'Mg', 'Al', 'Si', 'Y2O3', 'Al2O3', 'MgO', 'Y', 'K3022', 'K2754', 'SrTe', 'SrS', 'CuO', 'Cu2O', 'Cu', 'C', 'Bi2Se3', 'SiO2', 'SiO', 'Fe2O3', 'CuS', 'SrWO4', 'BaTiO3', 'SrMoO4', 'TlBr', 'Bi2Te3', 'TlBr', 'GeO2', 'Mg', 'Ca', 'Mn', 'Zn', 'Tl', 'GeTe', 'K3022'))
GMVI= map(safematerial, ('K1053','K1070','K711','MgO','K1235','K1236','K1546','K1597','K2115','K1788','K1234','K1228','K1012','K1010','K1001','K884','K873','K2757','K412','K411','K2654','K2618','K2612','K2580','K2577','K2573','K2575','K529', 'K530', 'K2852', 'K2624'))
# omitted 
GMIIIA=map(safematerial, ('K227','K229','K230','K240','K249','K251','K252','K253','K309','K326','K366','K369','K373','K376','K378','K371','K408','K409','K411','K412','K447','K453','K456','K458','K489','K490','K491','K493','K495','K496','K497','K508','K521','K523','K873','K919','K961','K963','K968','K970','K975','K978','K979','K1001','K1008','K1010','K1012','K1053','K1070','K1080','K1092','K1098','K1132','K1012','K1228','K1229','K1234','K1235','K1236'))
CF_CIRCULAR = map(safematerial, ("Be", "B", "K409", "Mg", "MgO", "Al", "Al2O3", "Si", "SiO2", "Apatite", "ZnS", "CdS", "FeS2", "Sc", "K411", "K412", "Ti", "V", "Cr", "Mn", "Fe", "Co", "Te", "Sb", "Fe", "Cd", "Ag", "Pd", "Rh", "Mo", "Nb", "Zr", "Y", "Se", "CdSe", "As", "Ge", "GaP", "Zn", "Cu", "Ni", "BaF2", "K371", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "K229", 'Bi', 'Tb', 'Dy', 'Benitoite', 'Enstatite', 'U', 'K408', 'Gd(PO3)3', 'Marja bute olivine' ))
# Chart hard to read (some missing, other may be errors)
CF_MIXED = map(safematerial, ( "B", "Be", "C", "Anorthite", "Albite", "Al2O3", "Mg", "MgO", "Si", "CaSO4", "CdS", "CuS", "FeS2", "PbS", "CaCO3", "Sc", "Ti", "V", "Cr", "Mn", "Fe", "Ni", "Co", "ZnS", "Ge", "GaP", "As", "Nb", "K412", "Benitoite", "K508", "Y", "Zr", "CdSe", "SiO2", "Mo", "Pd", "Ag", "Cd", "Sn", "InAs", "Sb", "Te", "Eu(PO3)3", "Gd(PO3)3", "Gd", "Tb", "Dy", "Lu", "Hf", "Ta", "W", "Re", "Os", "Ir", "Pt", "Au", "K227", "PbTe", "Bi", "U", "Lu", "Diopside" ))

GMIII = map(safematerial, ('GaP','ThF4','PbSe','InP','GaTe','Ce','K493','K491','K489','K490','K497','K1529','K3123','K1529','K2852','K411','K412','K963','K458','K495','K453','K456','K546','K1013','K523','K968','K496','Li2B4O7','Eu(PO3)3','K523','K496','K495','K458','K456','Ti','PbTe','PbS','Pb','Al2O3','SiO2','MgO','K227','K1013','K546','K968','K963','K453','GaP','Cr','Ge','Zr','Zn','Ni','K3122'))

GM_DS = map(safematerial, ('K373','K375','K376','K377','K378','K447','K546','K970','K1013','K1222','K1404','K1406','K1412','K1420','K1421','K1502','K1504','K1506','K1526','K1527','D256','D262','D312','D330','D360','L137','SRM93a','SRM610','SRM612','SRM617','SRM616','SRM709','SRM710','SRM711','SRM717'))


StdBlocks = { "Standard Mount A" : SMA, 
              "Standard Mount C" : SMC, 
              "Standard Mount D" : SMD, 
              "Homogeneous II" : HOMOII,
              "Homogeneous III" : HOMOIII,
              'Element Mount I' : EMI,
              'Element Mount II' : EMII,
              'High TC Superconductor' : HTCSC,
              'Glass Mount III' : GMIII,
              'Glass Mount III-A' : GMIIIA,
              'Glass Mount II-B' : GMIIB,
              'Glass Mount VI' : GMVI,
              'Chuck\'s Circular' : CF_CIRCULAR,
              'Chuck\'s Mixed' : CF_MIXED,
              'Glass Mount DS' : GM_DS
             }
             
def standardSet(blocks):
   res={}
   for name, block in blocks.iteritems():
      for mat in block:
         if mat:
            res[mat.getName()]=mat
   return res

AllStandards = standardSet(StdBlocks)


def stdsContaining(elm, min=1.0):
   '''stdsContaining(elm,min=1.0)
   Returns a list of standards containing at least min weight percent of elm.'''
   elm=element(elm)
   res=[]
   for std in AllStandards.values():
      if std.getElementSet().contains(elm) and (std.weightFraction(elm,0)>0.01*min):
         res.append(std)
   res.sort(lambda a,b: (1 if a.weightFraction(elm,0)<b.weightFraction(elm,0) else (-1 if a.weightFraction(elm,0)<b.weightFraction(elm,0) else 0)))
   return res      
   
   
def stdsContainingAll(elms,min=1.0):
   '''stdsContaining(elm,min=1.0)
   Returns a list of standards containing at least min weight percent of all elements in elms.'''
   elms=map(element, elms)
   res=[]
   for std in AllStandards.values():
      all=True
      for elm in elms:
         all=all and std.getElementSet().contains(elm) and (std.weightFraction(elm,0)>0.01*min)
      if all:
         res.append(std)
   return res
   
def stdsContainingSome(elms,min=1.0,tol=2):
   '''stdsContainingSome(elms,min=1.0,tol=2)
   Returns a list of standards containing at least tol of the elements in elms with at least
   a weight percentage of min.'''
   elms=map(element, elms)
   res={}
   for std in AllStandards.values():
      cx=0
      for elm in elms:
         if std.getElementSet().contains(elm) and (std.weightFraction(elm,0)>0.01*min):
            cx=cx+1
      if cx>=tol:
         if not res.has_key(cx):
            res[cx]=[]
         res[cx].append(std)
   tmp=[(cx,tuple(stds)) for cx, stds in res.iteritems()]
   tmp.sort(lambda a,b:(1 if a[0]<b[0] else (-1 if a[0]>b[0] else 0)))
   return tuple(tmp)
   
def compMetric(c1,c2):
   '''compMetric(c1,c2)
   A metric function for determining the similarity of two Composition objects.'''
   elms=list(c1.getElementSet())
   for elm in c2.getElementSet():
      if not (elm in elms):
         elms.append(elm)
   res=0.0
   for elm in elms:
      res=res+abs(c1.weightFraction(elm,0)-c2.weightFraction(elm,0))
   return res
         
def similarStandards(comp,tol=0.5):
   '''similarStandards(comp,tol=0.5)
   Returns a list of standards similar to comp for which the metric function
   compMetric returns a value less than tol.'''
   comp=material(comp)
   res=[]
   for std in AllStandards.values():
      cm=compMetric(comp,std)
      if cm<tol:
         res.append((cm,std))
   res.sort(lambda a,b: (-1 if a[0]<b[0] else (1 if a[0]>b[0] else 0)))
   return res

   
def blocksContaining(mat):
   '''blocksContaining(mat)
   Returns a list of the standard blocks containing standard materials named mat'''
   res=[]
   if isinstance(mat,epq.Composition):
      mat=mat.getName()
   for name, mats in StdBlocks.iteritems():
      for bm in mats:
         if bm and (mat==bm.getName()):
            res.append(name)
   return tuple(res)
   
def dumpComp(comp):
   '''dumpComp(comp)
   Write the composition to human readable format.'''
   comp=material(comp)
   print "Name: %s" % comp.getName()
   print "Z\tElm\tWgt%\tN[wgt%]\tAtom%"
   for elm in comp.getElementSet():
      print "%d\t%s\t%0.2f\t%0.2f\t%0.2f" % (elm.getAtomicNumber(), elm.toAbbrev(), 100.0*comp.weightFraction(elm,0),100.0*comp.weightFraction(elm,1),100.0*comp.atomicPercent(elm))
   print '----\t----\t----\t----\t----'
   print"%0.2f\t%d\t%0.2f\t100.00\t100.00" % (comp.weightAvgAtomicNumber(),comp.getElementCount(),100.0*comp.sumWeightFraction())
   
print __doc__
   
print '\nUnidentified materials: %s' % ', '.join(MissingMaterials)

print '\nKnown blocks: %s' % ', '.join(StdBlocks.keys())