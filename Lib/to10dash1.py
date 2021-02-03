# -*- coding: UTF-8 -*-
import java.util as ju
import java.lang as jl
import gov.nist.microanalysis.EPQLibrary as epq

def to10dash1(action_code, sample_id, specs, instrument_analysis_date, analysis_lab_id="N", srm_id="IIIE", pkg_lab_id="N", instrument="E", instrument_number="2", spot_size="5", operator="NWMR", spectrometer="EDS15", comment={}):
	"""to10dash1(
	action_code, 
	sample_id, 
	specs, 
	instrument_analysis_date, 
	analysis_lab_id="N", 
	srm_id="IIIE", 
	pkg_lab_id="N", 
	instrument="I", 
	instrument_number="1", 
	spot_size="5", 
	operator="NWMR", 
	spectrometer="EDS15", 
	comment={}
)
	* action_code = "N", "R" or "D"
	* sample_id unique id
	* specs = selected() or a list of spectra
	* instrument_analysis_date = "10-DEC-2020" or equivalent for date of acquisition
	* srm_id = "IIIX" where "X" is the letter of your block
	* pkg_lab and analysis_lab_id = your lab ID letter
	* instrument - Instrument id
	* spot_size a number in unspecified units as a string
	* operator = "Your initials"
	* spectrometer = the spectrometer ID
	* comment = { element("Fe"):"Eat more iron", element("Si"):"Sillycone" } or similar
	
The function prints a line per element for each element in the "MicroanalyticalComposition" property of the selected spectra.  The selected spectra are
assumed to come from the same material.  The "_error" columns reflect the standard deviation between the spectra not the single spectrum uncertainty."""
	def savg(vals):
		s=0.0
		for val in vals:
			s=s+val
		return s/len(vals)
	def sstd(vals):
		s, s2 = savg(vals), 0.0
		for val in vals:
			s2+=(val-s)*(val-s)
		return jl.Math.sqrt(s2/len(vals))
	elms = ju.TreeSet()
	props = specs[0].getProperties()
	e0 = props.getNumericProperty(epq.SpectrumProperties.BeamEnergy)
	beam_current = "%0.3f" % ( props.getNumericProperty(epq.SpectrumProperties.FaradayBegin), )
	for spec in specs:
		mat = spec.getProperties().getObjectProperty(epq.SpectrumProperties.MicroanalyticalComposition)
		elms.addAll(mat.getElementSet())
	for elm in elms:
		comment.setifabsent(elm,"")
	wfa, afa, ks, cr = {}, {}, {}, []
	for spec in specs:
		props = spec.getProperties()
		mat = props.getObjectProperty(epq.SpectrumProperties.MicroanalyticalComposition)
		krs = props.getObjectProperty(epq.SpectrumProperties.OptimalKRatios)
		cr.append(epq.SpectrumUtils.integrate(spec,100.0, 1000.0*e0)/props.getNumericProperty(epq.SpectrumProperties.LiveTime))
		for elm in elms:
			wfa.setifabsent(elm,[]), afa.setifabsent(elm,[]), ks.setifabsent(elm,[]), 
			wfa[elm].append(mat.weightFraction(elm, False))
			afa[elm].append(mat.atomicPercent(elm))
			xrts = krs.optimalDatum(elm)
			ks[elm].append(krs.getKRatio(xrts) if xrts!=None else 0.0)
	full = ""
	for elm in elms:
		weight_percent = "%0.2f" % ( 100.0*savg(wfa[elm]), )
		weight_percent_error = "%0.2f" % ( 100.0*sstd(wfa[elm]), )
		atom_percent = "%0.2f" % ( 100.0*savg(afa[elm]), )
		atom_percent_error = "%0.2f" % ( 100.0*sstd(afa[elm]), )
		amount = ( "MAJOR" if weight_percent > 10.0 else ( "MINOR" if weight_percent > 1.0 else "TRACE" ) )
		k_value = "%0.4f" % ( savg(ks[elm]), )
		normalization_factor = ""
		normalization_model = ""
		count_rate = "%0.0f" % ( savg(cr))
		diffracting_crystal = "NA"
		incident_beam_energy = "%0.1f" % ( e0, )
		star_elemental_comments = comment[elm]
		element = str(jl.String(elm.toAbbrev()).toUpperCase())
		item = ( action_code, sample_id, srm_id, pkg_lab_id, analysis_lab_id, element, instrument, instrument_number, 
			instrument_analysis_date, operator, weight_percent, weight_percent_error, atom_percent, atom_percent_error, 
			amount, k_value, normalization_factor, normalization_model, count_rate, diffracting_crystal, spectrometer, 
			e0, beam_current, spot_size, star_elemental_comments )
		res = "%1s%07i %-6s %1s %1s %-3s %-2s %-3s %-11s %12s %12s %12s %12s %12s %5s %12s %12s %3s %12s %-6s %5s %4s %5s %5s %s" % item
		full = "%s%s\n" % (full, res)
		print(res)
	return full[0:-1]
