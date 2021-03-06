package org.opencb.cellbase.core.lib;

import org.opencb.cellbase.core.lib.api.*;
import org.opencb.cellbase.core.lib.api.core.*;
import org.opencb.cellbase.core.lib.api.systems.PathwayDBAdaptor;
import org.opencb.cellbase.core.lib.api.systems.ProteinProteinInteractionDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.MirnaDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.RegulatoryRegionDBAdaptor;
import org.opencb.cellbase.core.lib.api.regulatory.TfbsDBAdaptor;
import org.opencb.cellbase.core.lib.api.variation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;


public abstract class DBAdaptorFactory {

	protected Logger logger;
	
//	protected static ResourceBundle resourceBundle;
//	protected static Config applicationProperties;
//	protected static Map<String, String> speciesAlias;
//	static {
//		speciesAlias = new HashMap<String, String>(20);
//
//		// reading application.properties file
//		cellbaseResourceBundle = ResourceBundle.getBundle("cellbase");
//		try {
//			cellbaseProperties = new Config(cellbaseResourceBundle);
//			String[] speciesArray = cellbaseProperties.getProperty("SPECIES").split(",");
//			String[] alias = null;
//			for(String species: speciesArray) {
//				species = species.toUpperCase();
//				alias = cellbaseProperties.getProperty(species + ".ALIAS").split(",");
//				for(String al: alias) {
//					speciesAlias.put(al, species);
//				}
//				// For to recognize the species code
//				speciesAlias.put(species, species);
//			}
//		} catch (IOException e) {
//			cellbaseProperties = new Config();
//			e.printStackTrace();
//		}
//	}
	
	public DBAdaptorFactory() {
		logger = LoggerFactory.getLogger(DBAdaptorFactory.class);
//		logger.setLevel(Logger.ERROR_LEVEL);
	}

//	protected String getSpeciesVersionPrefix(String species, String version) {
//		String speciesPrefix = null;
//		if(species != null && !species.equals("")) {
//			// coding an alias to application code species
//			species = speciesAlias.get(species);
//			// if 'version' parameter has not been provided the default version is selected
//			if(version == null || version.trim().equals("")) {
//				version = applicationProperties.getProperty(species+".DEFAULT.VERSION").toUpperCase();
////				logger.debug("HibernateDBAdaptorFactory in createSessionFactory(): 'version' parameter is null or empty, it's been set to: '"+version+"'");
//			}
//
//			// setting database configuration for the 'species.version'
//			speciesPrefix = species.toUpperCase() + "." + version.toUpperCase();
//		}
//
//		return speciesPrefix;
//	}

	public abstract void setConfiguration(Properties properties);
	
	public abstract void open(String species, String version);
	
	public abstract void close();


	public abstract GeneDBAdaptor getGeneDBAdaptor(String species);
	
	public abstract GeneDBAdaptor getGeneDBAdaptor(String species, String assembly);

	
	public abstract TranscriptDBAdaptor getTranscriptDBAdaptor(String species);
	
	public abstract TranscriptDBAdaptor getTranscriptDBAdaptor(String species, String assembly);
	
	
	public abstract ChromosomeDBAdaptor getChromosomeDBAdaptor(String species);
	
	public abstract ChromosomeDBAdaptor getChromosomeDBAdaptor(String species, String assembly);
	

	public abstract ExonDBAdaptor getExonDBAdaptor(String species);
	
	public abstract ExonDBAdaptor getExonDBAdaptor(String species, String assembly);
	

//	public abstract GenomicRegionFeatureDBAdaptor getFeatureMapDBAdaptor(String species);
//	
//	public abstract GenomicRegionFeatureDBAdaptor getFeatureMapDBAdaptor(String species, String assembly);
	
	
	public abstract VariantEffectDBAdaptor getGenomicVariantEffectDBAdaptor(String species);
	
	public abstract VariantEffectDBAdaptor getGenomicVariantEffectDBAdaptor(String species, String assembly);


    public abstract VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species);

    public abstract VariantAnnotationDBAdaptor getVariantAnnotationDBAdaptor(String species, String assembly);

	
	public abstract ProteinDBAdaptor getProteinDBAdaptor(String species);
	
	public abstract ProteinDBAdaptor getProteinDBAdaptor(String species, String assembly);
	
	
	public abstract SnpDBAdaptor getSnpDBAdaptor(String species);
	
	public abstract SnpDBAdaptor getSnpDBAdaptor(String species,  String version);

	
	public abstract GenomeSequenceDBAdaptor getGenomeSequenceDBAdaptor(String species);
	
	public abstract GenomeSequenceDBAdaptor getGenomeSequenceDBAdaptor(String species, String assembly);


	public abstract CytobandDBAdaptor getCytobandDBAdaptor(String species);
	
	public abstract CytobandDBAdaptor getCytobandDBAdaptor(String species, String assembly);


	public abstract XRefsDBAdaptor getXRefDBAdaptor(String species);
	
	public abstract XRefsDBAdaptor getXRefDBAdaptor(String species, String assembly);
	
	
	public abstract TfbsDBAdaptor getTfbsDBAdaptor(String species);
	
	public abstract TfbsDBAdaptor getTfbsDBAdaptor(String species, String assembly);


	public abstract RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species);
	
	public abstract RegulatoryRegionDBAdaptor getRegulatoryRegionDBAdaptor(String species, String assembly);
	
	
	public abstract MirnaDBAdaptor getMirnaDBAdaptor(String species);
	
	public abstract MirnaDBAdaptor getMirnaDBAdaptor(String species, String assembly);


	public abstract MutationDBAdaptor getMutationDBAdaptor(String species);
	
	public abstract MutationDBAdaptor getMutationDBAdaptor(String species, String assembly);


    public abstract ClinVarDBAdaptor getClinVarDBAdaptor(String species);

    public abstract ClinVarDBAdaptor getClinVarDBAdaptor(String species, String assembly);


	public abstract ClinicalDBAdaptor getClinicalDBAdaptor(String species);

	public abstract ClinicalDBAdaptor getClinicalDBAdaptor(String species, String assembly);


	public abstract CpGIslandDBAdaptor getCpGIslandDBAdaptor(String species);
	
	public abstract CpGIslandDBAdaptor getCpGIslandDBAdaptor(String species, String assembly);
	
	
	public abstract StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species);
	
	public abstract StructuralVariationDBAdaptor getStructuralVariationDBAdaptor(String species, String assembly);
	
	
	public abstract PathwayDBAdaptor getPathwayDBAdaptor(String species);
	
	public abstract PathwayDBAdaptor getPathwayDBAdaptor(String species, String assembly);


    public abstract ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species);

    public abstract ProteinProteinInteractionDBAdaptor getProteinProteinInteractionDBAdaptor(String species, String assembly);


//    public abstract RegulatoryRegionDBAdaptor getRegulationDBAdaptor(String species);
//
//    public abstract RegulatoryRegionDBAdaptor getRegulationDBAdaptor(String species, String assembly);


    public abstract VariationDBAdaptor getVariationDBAdaptor(String species);

    public abstract VariationDBAdaptor getVariationDBAdaptor(String species, String assembly);


    public abstract ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species);

    public abstract ConservedRegionDBAdaptor getConservedRegionDBAdaptor(String species, String assembly);


    public abstract ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor(String species);

    public abstract ProteinFunctionPredictorDBAdaptor getProteinFunctionPredictorDBAdaptor(String species, String assembly);


    public abstract VariationPhenotypeAnnotationDBAdaptor getVariationPhenotypeAnnotationDBAdaptor(String species);

    public abstract VariationPhenotypeAnnotationDBAdaptor getVariationPhenotypeAnnotationDBAdaptor(String species, String assembly);
}
