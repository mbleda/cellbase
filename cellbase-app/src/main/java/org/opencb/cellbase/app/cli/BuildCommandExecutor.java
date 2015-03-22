package org.opencb.cellbase.app.cli;

import com.beust.jcommander.ParameterException;
import org.opencb.cellbase.app.serializers.CellBaseFileSerializer;
import org.opencb.cellbase.app.serializers.CellBaseSerializer;
import org.opencb.cellbase.app.serializers.json.JsonParser;
import org.opencb.cellbase.app.transform.*;
import org.opencb.cellbase.app.transform.utils.FileUtils;
import org.opencb.cellbase.core.CellBaseConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Created by imedina on 03/02/15.
 */
public class BuildCommandExecutor extends CommandExecutor {

    // TODO: these two constants should be defined in the 'download' module
    public static final String GWAS_INPUT_FILE_NAME = "gwascatalog.txt";
    public static final String DBSNP_INPUT_FILE_NAME = "dbSnp142-00-All.vcf.gz";

    private CliOptionsParser.BuildCommandOptions buildCommandOptions;

    private File ensemblScriptsFolder;
    private String input = null;
    private Path output = null;

    private CellBaseConfiguration.SpeciesProperties.Species species;

    public BuildCommandExecutor(CliOptionsParser.BuildCommandOptions buildCommandOptions) {
        super(buildCommandOptions.commonOptions.logLevel, buildCommandOptions.commonOptions.verbose,
                buildCommandOptions.commonOptions.conf);

        this.buildCommandOptions = buildCommandOptions;
        this.ensemblScriptsFolder = new File(System.getProperty("basedir") + "/bin/ensembl-scripts/");

        if(buildCommandOptions.input != null) {
            input = buildCommandOptions.input;
        }
        if(buildCommandOptions.output != null) {
            output = Paths.get(buildCommandOptions.output);
        }
    }


    /**
     * Parse specific 'build' command options
     */
    public void execute() {
        try {
            checkOutputDir();

            // We need to get the Species object from the CLI name
            // This can be the scientific or common name, or the ID
            for (CellBaseConfiguration.SpeciesProperties.Species sp: configuration.getAllSpecies()) {
                if (buildCommandOptions.species.equalsIgnoreCase(sp.getScientificName())
                        || buildCommandOptions.species.equalsIgnoreCase(sp.getCommonName())
                        || buildCommandOptions.species.equalsIgnoreCase(sp.getId())) {
                    species = sp;
                    break;
                }
            }

            if (buildCommandOptions.build != null) {
                CellBaseParser parser = null;

                switch (buildCommandOptions.build) {
                    case "genome-sequence":
                        parser = buildGenomeSequence();
                        break;
                    case "gene":
                        parser = buildGene();
                        break;
                    case "regulation":
                        parser = buildRegulation();
                        break;
                    case "variation":
                        parser = buildVariation();
                        break;
                    case "variation-phen-annot":
                        parser = buildVariationPhenotypeAnnotation();
                        break;
                    case "vep":
                        parser = buildVep();
                        break;
                    case "protein":
                        parser = buildProtein();
                        break;
                    case "ppi":
                        parser = getInteractionParser();
                        break;
                    case "conservation":
                        parser = buildConservation();
                        break;
                    case "drug":
                        parser = buildDrugParser();
                        break;
                    case "clinvar":
                        parser = buildClinvar();
                        break;
                    case "cosmic":
                        parser = buildCosmic();
                        break;
                    case "gwas":
                        parser = buildGwas();
                        break;
                    default:
                        logger.error("Build option '" + buildCommandOptions.build + "' is not valid");
                }

                if (parser != null) {
                    try {
                        parser.parse();
                    } catch (Exception e) {
                        logger.error("Error executing 'build' command " + buildCommandOptions.build + ": " + e.getMessage(), e);
                    }
                    parser.disconnect();
                }
            }
        } catch (ParameterException e) {
            logger.error("Error parsing build command line parameters: " + e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void checkOutputDir(){
        if (!output.toFile().exists()) {
            throw new ParameterException("Output directory " + output + " doesn't exist");
        } else if (!output.toFile().isDirectory()){
            throw new ParameterException(output + " is not a directory");
        }
    }

    private CellBaseParser buildGenomeSequence() {
        /**
         * To get some extra info about the genome such as chromosome length or cytobands
         * we execute the following script
         */
        try {
            String outputFileName = output + "/genome_info.json";
            List<String> args = Arrays.asList("--species", species.getScientificName(), "-o", outputFileName,
                    "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs());
            String geneInfoLogFileName = output + "/genome_info.log";

            boolean downloadedGenomeInfo = false;
            downloadedGenomeInfo = runCommandLineProcess(ensemblScriptsFolder, "./genome_info.pl", args, geneInfoLogFileName);

            if (downloadedGenomeInfo) {
                logger.info(outputFileName + " created OK");
            } else {
                logger.error("Genome info for " + species.getScientificName() + " cannot be downloaded");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        Path fastaFile = getInputFileFromCommandLine();
        CellBaseSerializer serializer = new JsonParser(output, "genome_sequence");
        return new GenomeSequenceFastaParser(fastaFile, serializer);
    }

    private CellBaseParser getInteractionParser()  {
        Path psimiTabFile = getInputFileFromCommandLine();
        String species = buildCommandOptions.species;
        checkMandatoryOption("species", species);
        CellBaseSerializer serializer = new JsonParser(output, "protein_protein_interaction");
        return new InteractionParser(psimiTabFile, species, serializer);
    }

    private CellBaseParser buildConservation() {
        Path conservationFilesDir = getInputDirFromCommandLine();
        // TODO: chunk size is not really used in ConvervedRegionParser, remove?
        //int conservationChunkSize = Integer.parseInt(commandLine.getOptionValue(CellBaseMain.CHUNK_SIZE_OPTION, "0"));
        int conservationChunkSize = 0;
        CellBaseFileSerializer serializer = new JsonParser(output);
        return new ConservedRegionParser(conservationFilesDir, conservationChunkSize, serializer);
    }

    private CellBaseParser buildProtein() {
        Path uniprotSplitFilesDir = getInputDirFromCommandLine();
        String species = buildCommandOptions.species;
        checkMandatoryOption("species", species);
        CellBaseSerializer serializer = new JsonParser(output, "protein");
        return new ProteinParser(uniprotSplitFilesDir, species, serializer);

    }

    private void getProteinFunctionPredictionMatrices(CellBaseConfiguration.SpeciesProperties.Species sp, Path geneFolder) throws IOException, InterruptedException {
        logger.info("Downloading protein function prediction matrices ...");

        // run protein_function_prediction_matrices.pl
        String proteinFunctionProcessLogFile = geneFolder.resolve("protein_function_prediction_matrices.log").toString();
        List<String> args = Arrays.asList("--species", sp.getScientificName(), "--outdir", geneFolder.toString(),
                "--ensembl-libs", configuration.getDownload().getEnsembl().getLibs());

        boolean proteinFunctionPredictionMatricesObtaines = runCommandLineProcess(ensemblScriptsFolder,
                "./protein_function_prediction_matrices.pl",
                args,
                proteinFunctionProcessLogFile);

        // check output
        if (proteinFunctionPredictionMatricesObtaines) {
            logger.info("Protein function prediction matrices created OK");
        } else {
            logger.error("Protein function prediction matrices for " + sp.getScientificName() + " cannot be downloaded");
        }
    }

    private CellBaseParser buildVep() {
        Path vepFile = getInputFileFromCommandLine();
        CellBaseFileSerializer serializer = new JsonParser(output);
        return new VariantEffectParser(vepFile, serializer);
    }

    private CellBaseParser buildVariationPhenotypeAnnotation() {
        Path variationFilesDir = getInputDirFromCommandLine();
        CellBaseSerializer serializer = new JsonParser(output, "variation_phenotype_annotation");
        return new VariationPhenotypeAnnotationParser(variationFilesDir, serializer);
    }

    private CellBaseParser buildVariation() {
        Path variationFilesDir = getInputDirFromCommandLine();
        CellBaseFileSerializer serializer = new JsonParser(output);
        return new VariationParser(variationFilesDir, serializer);

    }

    private CellBaseParser buildRegulation() {
        Path regulatoryRegionFilesDir = getInputDirFromCommandLine();
        CellBaseSerializer serializer = new JsonParser(output, "regulatory_region");
        return new RegulatoryRegionParser(regulatoryRegionFilesDir, serializer);

    }

    private CellBaseParser buildGene() {
        Path inputDir = getInputDirFromCommandLine();
        String genomeFastaFile = buildCommandOptions.referenceGenomeFile;
        checkMandatoryOption("reference-genome-file", genomeFastaFile);
        CellBaseSerializer serializer = new JsonParser(output, "gene");
        GeneParser geneParser = new GeneParser(inputDir, Paths.get(genomeFastaFile), serializer);
        return geneParser;
    }

    private CellBaseParser buildDrugParser() {
        throw new ParameterException("'drug' builder is not implemented yet");
//        Path drugFile = getInputFileFromCommandLine();
//        CellBaseSerializer serializer = new JsonParser(output, "drug");
//        return new DrugParser(drugFile, serializer);
    }


    private CellBaseParser buildGwas() throws IOException {
        Path inputDir = getInputDirFromCommandLine();
        Path gwasFile = inputDir.resolve(GWAS_INPUT_FILE_NAME);
        FileUtils.checkPath(gwasFile);
        Path dbsnpFile = inputDir.resolve(DBSNP_INPUT_FILE_NAME);
        FileUtils.checkPath(dbsnpFile);
        CellBaseSerializer serializer = new JsonParser(output, "gwas");
        return new GwasParser(gwasFile, dbsnpFile, serializer);
    }

    private CellBaseParser buildCosmic()  {
        Path cosmicFilePath = getInputFileFromCommandLine();
        //MutationParser vp = new MutationParser(Paths.get(cosmicFilePath), mSerializer);
        // this parser works with cosmic file: CosmicCompleteExport_vXX.tsv (XX >= 70)
        CellBaseSerializer serializer = new JsonParser(output, "cosmic");
        return new CosmicParser(cosmicFilePath, serializer);
    }

    private CellBaseParser buildClinvar() {
        Path clinvarFile = getInputFileFromCommandLine();

        String assembly = buildCommandOptions.assembly;
        checkMandatoryOption("assembly", assembly);
        if (!assembly.equals(ClinVarParser.GRCH37_ASSEMBLY) && !assembly.equals(ClinVarParser.GRCH38_ASSEMBLY)) {
            throw new ParameterException("Assembly '" + assembly + "' is not valid. Possible values: " + ClinVarParser.GRCH37_ASSEMBLY + ", " + ClinVarParser.GRCH38_ASSEMBLY);
        }

        CellBaseSerializer serializer = new JsonParser(output, "clinvar");
        return new ClinVarParser(clinvarFile, assembly, serializer);
    }

    private Path getInputFileFromCommandLine() {
        File inputFile = new File(input);
        if (inputFile.exists()) {
            if (inputFile.isDirectory()) {
                throw new ParameterException(input + " is a directory: it must be a file for " + buildCommandOptions.build + " builder");
            } else {
                return Paths.get(input);
            }
        } else {
            throw new ParameterException("File '" + input + "' doesn't exist");
        }
    }

    private Path getInputDirFromCommandLine(){
        File inputDirectory = new File(input);
        if (inputDirectory.exists()) {
            if (inputDirectory.isDirectory()) {
                return Paths.get(input);
            } else {
                throw new ParameterException("'" + input + "' is not a directory");
            }
        } else {
            throw new ParameterException("Folder '" + input + "' doesn't exist");
        }
    }

    private void checkMandatoryOption(String option, String value){
        if (value == null) {
            throw new ParameterException("'" + option + "' option is mandatory for '" + buildCommandOptions.build + "' builder");
        }
    }

}
