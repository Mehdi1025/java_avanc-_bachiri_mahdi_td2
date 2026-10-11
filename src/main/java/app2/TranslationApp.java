package app2;

import app2.ai.AlbertTranslationClient;
import app2.service.LocalizationReport;
import app2.service.LocalizationService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class TranslationApp {

    private static final String DEFAULT_MODEL = "meta-llama/Meta-Llama-3.1-8B-Instruct";
    private static final String DEFAULT_SOURCE_FILE = "messages.properties";

    private TranslationApp() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }

        Path resourcesDirectory = Path.of(args[0]);
        boolean overwrite = false;
        String modelName = DEFAULT_MODEL;
        List<String> targetLanguages = new ArrayList<>();

        for (int index = 1; index < args.length; index++) {
            String argument = args[index];
            if ("--force".equals(argument)) {
                overwrite = true;
            } else if (argument.startsWith("--model=")) {
                modelName = argument.substring("--model=".length());
            } else {
                targetLanguages.add(argument);
            }
        }

        if (targetLanguages.isEmpty()) {
            System.err.println("Erreur : aucune langue cible fournie.");
            printUsage();
            System.exit(1);
        }

        Path sourceFile = resourcesDirectory.resolve(DEFAULT_SOURCE_FILE);
        AlbertTranslationClient client = new AlbertTranslationClient(modelName);
        LocalizationService service = new LocalizationService(client);

        LocalizationReport report = service.addLocalizations(
                sourceFile,
                resourcesDirectory,
                targetLanguages,
                overwrite
        );

        System.out.println(report);

        if (report.hasFailures()) {
            System.exit(2);
        }
    }

    private static void printUsage() {
        System.err.println("""
                Usage: TranslationApp <resourcesDirectory> <langue1> [langue2 ...] [--force] [--model=NOM]

                Exemples:
                  TranslationApp demo/sample-app/src/main/resources anglais allemand espagnol
                  TranslationApp demo/sample-app/src/main/resources en de es --force

                Langues acceptées : anglais/en, allemand/de, espagnol/es, ...
                Variable requise : ALBERT_API_KEY
                """);
    }
}
