# Common standard materials to add to the database...
def addToDatabase(formula, name):
	mat = parseChemicalFormula(formula, name=name)
	if not Database.findStandard(mat.getName()):
		Database.addStandard(mat)
		print "Added %s" % mat

addToDatabase("AgBr", name="Silver Bromide")
addToDatabase("Ag2S", name="Silver Sulphide")
addToDatabase("AgCl", name="Silver Chloride")
addToDatabase("Ag2S", name="Silver Sulphide")
addToDatabase("Ag2Te", name="Hessite")
addToDatabase("Ag2Te3", name="Silver Telluride")
addToDatabase("Al2O3", name="Corundum")
addToDatabase("AlF3", name="Aluminum Fluoride")
addToDatabase("Al2SiO5", name="Kyanite")
addToDatabase("BaSO4", name="Baryte")
addToDatabase("BaF2", name="Barium Fluoride")
addToDatabase("BaTiSi3O9", name="Benitoite")
addToDatabase("Bi2Se3", name="Bismuth Selenide")
addToDatabase("BiSiO", name="Bismuth Silicide")
addToDatabase("Bi2Te3", name="Bismuth Telluride")
addToDatabase("BN", name="Boron Nitride")
addToDatabase("B4C", name="Boron Carbide")
addToDatabase("B2O3", name="Boron Trioxide")
addToDatabase("MgCaSi2O6", name="Diopside")
addToDatabase("Ca5(PO4)3F", name="Fluorapatite")
addToDatabase("CaCO3", name="Calcite")
addToDatabase("CaF2", name="Fluorite")
addToDatabase("CaSiO3", name="Wollastonite")
addToDatabase("CaSO4", name="Anhydrite")
addToDatabase("CaWO4", name="Sheelite")
addToDatabase("CaMoO4", name="Molybdate")
addToDatabase("CdSe", name="Cadmium Selenium")
addToDatabase("CdTe", name="Cadmium Telluride")
addToDatabase("CeAl2", name="Cerium Alumate")
addToDatabase("CeF2", name="Cerium Fluoride")
addToDatabase("CeO2", name="Cerium Oxide")
addToDatabase("CdS", name="Cadmium Sulphide")
addToDatabase("CoO", name="Cobalt Oxide")
addToDatabase("Co3O4", name="Cobalt (II,III) Oxide")
addToDatabase("CoSi2", name="Cobalt Silicide")
addToDatabase("Cr3C", name="Chrome Carbide")
addToDatabase("CrN", name="Chromium Nitride")
addToDatabase("CsBr", name="Caesium Bromide")
addToDatabase("CsI", name="Caesium Iodide")
addToDatabase("CsNO", name="Caesium Nitrate")
addToDatabase("CuI", name="Copper Iodide")
addToDatabase("CuO", name="Copper Oxide")
addToDatabase("CuS", name="Copper Sulphide")
addToDatabase("CuSO4", name="Copper Sulphate")
addToDatabase("DyF3", name="Dysprosium Fluoride")
addToDatabase("ErF3", name="Erbium Fluoride")
addToDatabase("Eu2O3", name="Europium Oxide")
addToDatabase("EuF3", name="Europium Fluoride")
addToDatabase("FeAsS", name="Arsenopyrite")
addToDatabase("Fe3Al2Si3O12", name="Almandine")
addToDatabase("FeCaSi2O6", name="Hedenbergite")
addToDatabase("Fe2O3", name="Hematite")
addToDatabase("Fe2P", name="Iron Phosphide")
addToDatabase("Fe3C", name="Iron carbide")
addToDatabase("FeO", name="Ferrous Oxide")
addToDatabase("FeSi2", name="Iron Silicide")
addToDatabase("FeS", name="Pyrite")
addToDatabase("Fe2SiO4", name="Fayalite")
addToDatabase("GaAs", name="Gallium Arsenide")
addToDatabase("Ga2Se3", name="Gallium Selenide")
addToDatabase("GaAs", name="Gallium Arsenide")
addToDatabase("GaN", name="Gallium Nitride")
addToDatabase("GaP", name="Gallium Phosphide")
addToDatabase("GaS", name="Gallium Sulphide")
addToDatabase("GaSb", name="Gallium Antimonide")
addToDatabase("Gd3Ga5O12", name="Gadolinium Gallium Garnet")
addToDatabase("Gd2O3", name="Gadolinium Oxide")
addToDatabase("GdF3", name="Gadolinium Fluoride")
addToDatabase("HfO2", name="Hafnium Oxide")
addToDatabase("HgS", name="Cinnabar")
addToDatabase("HgTe", name="Mercury Telluride")
addToDatabase("InAs", name="Indium Arsenide")
addToDatabase("InP", name="Indium Phosphide")
addToDatabase("InSb", name="Indium Antimonide")
addToDatabase("KAlSi3O8", name="Orthoclase")
addToDatabase("KCl", name="Sylvite")
addToDatabase("KBr", name="Potassium Bromide")
addToDatabase("KI", name="Pottasium Iodide")
addToDatabase("LaB6", name="Lanthanum Hexaboride")
addToDatabase("La2O3", name="Lanthanum Oxide")
addToDatabase("LaF3", name="Lanthanum Fluoride")
addToDatabase("LiF", name="Lithium Fluoride")
addToDatabase("Li2Nb2O6", name="Lithium Niobate")
addToDatabase("Li2Ta2O6", name="Lithium Tantalate")
addToDatabase("LuF3", name="Lutetium Fluoride")
addToDatabase("LuSi2", name="Lutetium Silicide")
addToDatabase("Mg2Sn", name="Magnesium Tin")
addToDatabase("MgO", name="Periclase")
addToDatabase("MgAl2O4", name="Spinel")
addToDatabase("MgF2", name="Magnesium Fluoride")
addToDatabase("Mg2SiO4", name="Forsterite")
addToDatabase("MnF2", name="Manganese Fluoride")
addToDatabase("MnTiO3", name="Manganese Titanate")
addToDatabase("MnSiO3", name="Rhodonite")
addToDatabase("MoS2", name="Molybdenite")
addToDatabase("MoO3", name="Molybdenum Oxide")
addToDatabase("Na3AlF6", name="Cryolite")
addToDatabase("NaF", name="Sodium Fluoride")
addToDatabase("NaCl", name="Halite")
addToDatabase("NaAlSi3O8", name="Albite")
addToDatabase("Nb2O5", name="Niobium Oxide")
addToDatabase("Nd2O3", name="Neodymium Oxide")
addToDatabase("NdF3", name="Neodymium Flouride")
addToDatabase("NiO", name="Nickel Oxide")
addToDatabase("NiAs", name="Nickel Arsenide")
addToDatabase("NiSO4", name="Nickel Sulphate")
addToDatabase("NiP", name="Nickel Phosphide")
addToDatabase("PbS", name="Galena")
addToDatabase("PbF2", name="Lead Fluoride")
addToDatabase("PbO", name="Lead Oxide")
addToDatabase("PbTe", name="Lead Telluride")
addToDatabase("PbSe", name="Lead Selenide")
addToDatabase("PrF3", name="Praseodymium Fluoride")
addToDatabase("RbBr", name="Rubidium Bromide")
addToDatabase("RbI", name="Rubidium Iodide")
addToDatabase("Sb2S3", name="Antimony Sulphide")
addToDatabase("SiO2", name="Quartz")
addToDatabase("Si3N4", name="Silicon Nitride")
addToDatabase("SiC", name="Silicon Carbide")
addToDatabase("Sm2O3", name="Samarium Oxide")
addToDatabase("SmF3", name="Samarium Fluoride")
addToDatabase("SnO2", name="Cassiterite")
addToDatabase("SrF2", name="Strontium Fluoride")
addToDatabase("SrSO4", name="Celestine")
addToDatabase("SrTiO3", name="Strontium Titanate")
addToDatabase("TaS2", name="Tantalum Silicide")
addToDatabase("TbF3", name="Terbium Fluoride")
addToDatabase("TbSi2", name="Terbium Silicate")
addToDatabase("TeO2", name="Tellurite")
addToDatabase("ThO2", name="Thorium Oxide")
addToDatabase("TiC", name="Titanium Carbide")
addToDatabase("TiN", name="Titanium Nitride")
addToDatabase("TiS2", name="Titanium Sulfide")
addToDatabase("TiO", name="Titanium Oxide")
addToDatabase("TiO2", name="Rutile")
addToDatabase("TlBr", name="Thallium Bromide")
addToDatabase("TlI", name="Thallium Iodide")
addToDatabase("TmSi2", name="Thulium Silicide")
addToDatabase("UO2", name="Uranium Oxide")
addToDatabase("VC", name="Vanadium Carbide")
addToDatabase("V2O5", name="Vanadium Oxide")
addToDatabase("WC", name="Tungsten Carbide")
addToDatabase("WSi2", name="Tungsten Silicide")
addToDatabase("Y2O3", name="Yttrium Oxide")
addToDatabase("Y3Al5O12", name="Yttrium Aluminum Garnet")
addToDatabase("YbF3", name="Ytterbium Fluoride")
addToDatabase("ZnAs", name="Zinc Arsenide")
addToDatabase("ZnS", name="Sphalerite")
addToDatabase("ZnSe", name="Zinc Selenide")
addToDatabase("ZnTe", name="Zinc Telluride")
addToDatabase("ZrO2", name="Zirconium Oxide")
addToDatabase("ZrSiO4", name="Zircon")
addToDatabase("ZrB2", name="Zironium Boride")
addToDatabase("ZrC", name="Zirconium Carbide")
addToDatabase("ZrN", name="Zirconium Nitride")

pureElements = ( "Al", "Sb", "As", "Be", "Bi", "B", "Cd", "C", "Cr", "Co", "Cu", "Dy", "Er", "Ge", "Gd", "Au", "Hf", "Ho", "In", "Ir", "Fe", "La", "Pb", "Mg", "Mn", "Mo", "Ni", "Nb", "Os", "Pd", "Pt", "Re", "Rh", "Ru", "Sc", "Se", "Si", "Ag", "Ta", "Te", "Th", "Sn", "Ti", "Tl", "Tm", "W", "U", "V", "Y", "Yb", "Zn", "Zr" )

def addPure(elm):
	addToDatabase(elm, element(elm).toAbbrev())
	
map(addPure, pureElements)

NIST  = ( ("SRM 101g", (( "C", 0.000136), ("Mn", 0.00085), ("P", 0.00007), ("S", 0.000078), ("Si",0.0108), ("Cu", 0.00029), ("Ni",0.1000), ("Cr",0.1846), ("V", 0.00041), ("Mo",0.00004), ("Co",0.0009))),
		  ("SRM C1287", (( "C", 0.0036, ("Mn",0.0166), ("P", 0.00029), ("S",0.00024), ("Si",0.0166), ("Cu",0.0058), ("Ni", 0.2116), ("Cr",0.2398), ("V",0.0009), ("Mo",0.0046), ("Co",0.0031), ("Ti",0.0005), ("Pb",0.00008))),
		  ( "SRM 160b", (( "C", 0.000445, 0.000014),("Co",0.001052,0.000057),("Cr",0.1837,0.0021),("Cu",0.001734,0.000075),("Mn",0.01619,0.00075),("Mo",0.02386,0.00024),("Ni",0.1235,0.0022), ("S",0.0175,0.0032),("V",0.000508,0.000034))),
		  ( "SRM-121d", (("C",0.00067, 0.00010),("Cr",0.1750,0.0015),("Cu",0.001205,0.000057),("Mn",0.0181,0.0016),("Mo"
		  "SRM-343a", 
		  "SRM-361", 
		  "SRM-132b", 
		  ( "SRM-478", (( "Cu", 0.728, 0.005), ("Zn", 0.271, 0.002)) ),
		  "SRM-481", 
		  "SRM-482", 
		  ( "SRM-710", (( "SiO2", 0.705), ("Na2O",0.087), ("K2O",0.077), ("CaO",0.116),("Sb2O3",0.011))),
		  "SRM-1872", 
		  "SRM-1134", 
		  "SRM-1160", 
		  "SRM-1276a", 
		  "SRM-C2400", 
		  "SRM-872", 
		  "SRM-179", 
		  "SRM-480" )
