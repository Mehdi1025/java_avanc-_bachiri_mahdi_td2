package app1;

import app1.extractor.ProjectScanner;
import app1.extractor.StringExtractor;
import app1.modifier.CodeModifier;
import app1.modifier.PropertiesGenerator;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class InternationalizationApp {

    private InternationalizationApp() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("""
                    Usage: InternationalizationApp <projectRoot> [resourcesDirectory]

                    Exemple:
                      InternationalizationApp demo/sample-app demo/sample-app/src/main/resources
                    """);
            System.exit(1);
        }

        Path projectRoot = Path.of(args[0]);
        Path resourcesDirectory = args.length > 1
                ? Path.of(args[1])
                : projectRoot.resolve("src/main/resources");

        List<Path> javaFiles = ProjectScanner.scanJavaFiles(projectRoot);
        Map<String, String> extractedStrings = StringExtractor.extractFromFiles(javaFiles);

        if (extractedStrings.isEmpty()) {
            System.out.println("Aucune chaîne à internationaliser détectée.");
            return;
        }

        PropertiesGenerator.generate(extractedStrings, resourcesDirectory);
        CodeModifier.modify(javaFiles, extractedStrings);

        System.out.println("Internationalisation terminée : "
                + extractedStrings.size() + " message(s) extrait(s), "
                + javaFiles.size() + " fichier(s) Java analysé(s).");
        System.out.println("Fichier généré : " + resourcesDirectory.resolve("messages.properties"));
    }
}
