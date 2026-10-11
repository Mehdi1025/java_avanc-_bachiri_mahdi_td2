# BACHIRI Mehdi — TD n°2 : Internationalisation et localisation par IA

Deux applications Java 21 complémentaires, livrées dans un même dépôt Maven :

- **App1** : internationalise un projet Java (extraction des chaînes, génération de `messages.properties`, modification du code source).
- **App2** : ajoute des localisations via l''API Albert (LangChain4j), avec contrôles sur les réponses IA.

> **Nom de dépôt imposé :** `BACHIRI_Mehdi_TD2`  
> Renommez le dépôt GitHub si nécessaire et invitez le compte `Miage-Mulhouse` comme collaborateur.

---

## Prérequis

- Java 21
- Maven Wrapper (`mvnw` / `mvnw.cmd`) inclus dans le dépôt
- Clé API Albert (uniquement pour l''app2 en manuel)

---

## Compilation et tests

```powershell
.\mvnw.cmd test
```

**15 tests JUnit** s''exécutent **sans clé API** (l''API Albert est simulée via `FakeTranslationClient`).

---

## Configuration de la clé Albert (App2)

**Ne jamais committer la clé API.**

Option 1 — variable d''environnement :

```powershell
$env:ALBERT_API_KEY = "votre-cle"
```

Option 2 — fichier local `.env` (ignoré par Git) :

```powershell
copy .env.example .env
# Editez .env et renseignez ALBERT_API_KEY
```

Vérifier les modèles disponibles :

```powershell
curl -sS https://albert.api.etalab.gouv.fr/v1/models -H "Authorization: Bearer $env:ALBERT_API_KEY"
```

Modèle par défaut : `ministral-3-8b-instruct-2512`

---

## Préparer le classpath (Windows)

```powershell
.\mvnw.cmd compile dependency:build-classpath "-Dmdep.outputFile=target/cp.txt"
$cp = Get-Content target/cp.txt -Raw
```

---

## Application 1 — Internationaliser un projet

```powershell
java -cp "target/classes;$cp" app1.InternationalizationApp <racineProjet> [dossierResources]
```

Exemple sur le projet de démonstration :

```powershell
java -cp "target/classes;$cp" app1.InternationalizationApp demo/sample-app demo/sample-app/src/main/resources
```

**Effets :**
- parcourt les `.java` (hors `target/`, `.git/`, `.mvn/`, etc.)
- génère `messages.properties`
- remplace les chaînes par `ResourceBundle.getBundle("messages").getString("msg_X")`

---

## Application 2 — Localiser via l''IA

```powershell
java -cp "target/classes;$cp" app2.TranslationApp <dossierResources> <langue1> [langue2 ...] [--force] [--model=NOM]
```

Exemple (3 langues en un seul lancement) :

```powershell
java -cp "target/classes;$cp" app2.TranslationApp demo/sample-app/src/main/resources anglais allemand espagnol
```

Options :
- `--force` : remplace une traduction existante
- `--model=NOM` : modèle Albert (défaut : `ministral-3-8b-instruct-2512`)

**Contrôles App2 :**
- conservation des clés (`msg_1`, `msg_2`, ...)
- validation des placeholders `{0}`, `{1}`, ...
- pas d''écrasement silencieux (fichiers existants ignorés sauf `--force`)
- rapport des fichiers créés / ignorés / en échec
- réponse IA invalide : exception, aucune ressource corrompue

---

## Projet de démonstration

`demo/sample-app/` — projet Maven multi-paquetages (`com.demo.app`, `com.demo.service`, `com.demo.ui`).

Le dépôt contient déjà le résultat de l''internationalisation et des traductions :

| Fichier | Rôle |
|---|---|
| `messages.properties` | messages source (français) |
| `messages_en.properties` | anglais |
| `messages_de.properties` | allemand |
| `messages_es.properties` | espagnol |

### Exécuter la démo

```powershell
cd demo/sample-app
..\..\mvnw.cmd compile exec:java
```

### Tester en plusieurs langues

```powershell
# Anglais
$env:JAVA_TOOL_OPTIONS = "-Duser.language=en -Duser.country=US"
..\..\mvnw.cmd exec:java

# Allemand
$env:JAVA_TOOL_OPTIONS = "-Duser.language=de -Duser.country=DE"
..\..\mvnw.cmd exec:java

# Espagnol
$env:JAVA_TOOL_OPTIONS = "-Duser.language=es -Duser.country=ES"
..\..\mvnw.cmd exec:java
```

---

## Architecture

```
src/main/java/
├── app1/
│   ├── extractor/     ProjectScanner, StringExtractor, StringLiteralFilters
│   ├── modifier/      PropertiesGenerator, CodeModifier
│   └── InternationalizationApp.java
└── app2/
    ├── ai/            AlbertTranslationClient, TranslationValidator, ...
    ├── properties/    PropertiesReader, PropertiesWriter
    ├── service/       LocalizationService, LocalizationReport, LanguageCodes
    └── TranslationApp.java
demo/sample-app/       Projet de démonstration
```

---

## Choix fonctionnel vs impératif

| Traitement | Approche | Justification |
|---|---|---|
| Parcours et filtrage des fichiers `.java` | **Fonctionnelle** (Streams, `Files::isRegularFile`) | Exploration sans effet de bord |
| Extraction et dédoublonnage des chaînes | **Fonctionnelle** (Streams, `Predicate`, `Collectors`, `distinct`) | Pipeline de sélection/regroupement |
| Validation des placeholders `{n}` | **Fonctionnelle** (Stream sur regex) | Contrôle déclaratif des paramètres |
| Parsing JSON de la réponse IA | **Fonctionnelle** puis validation | Transformation de données avant écriture |
| Écriture des `.properties` | **Impérative** (`for`, try-with-resources) | Effet de bord explicite |
| Modification des sources Java (AST) | **Impérative** (`for`, try-with-resources) | Effet de bord ; pas de `peek()` |
| Appels API Albert | **Impérative** | Effet de bord réseau isolé dans le client |

---

## Stratégie App1 et limites

**Chaînes internationalisées :**
- littéraux `StringLiteralExpr` analysés via **JavaParser** (pas de regex sur le fichier entier)
- les commentaires ne sont **pas** confondus avec du code (analyse AST)

**Chaînes exclues :**
- vides, `blank`, longueur 1, uniquement des caractères spéciaux
- déjà internationalisées (`getString(...)`, `getBundle(...)`)

**Autres limites :**
- concaténations `"A" + x + "B"` découpées en plusieurs clés (limitation documentée)
- pas d''introspection ni d''exécution du projet analysé
- `target/`, `.git/`, dépendances et fichiers générés non modifiés
- second passage sur un projet déjà traité : aucune double transformation

---

## Git — mini-GitFlow

```powershell
git switch main
git pull --ff-only origin main
git switch -c feature/nom-fonctionnalite
.\mvnw.cmd test
git add ...
git commit --trailer "Co-authored-by: Cursor <cursoragent@cursor.com>" -m "Message clair"
git switch main
git merge --no-ff feature/nom-fonctionnalite
.\mvnw.cmd test
git push origin main
```

Remotes :
- `origin` : votre dépôt privé
- `upstream` : dépôt enseignant (`Miage-Mulhouse/complements-java-2026-2027`)

---

## Références

- [Java 21 — Stream API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/stream/package-summary.html)
- [Java 21 — ResourceBundle](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/ResourceBundle.html)
- [Albert API](https://albert.api.etalab.gouv.fr/v1)
- [LangChain4j — OpenAI compatible](https://docs.langchain4j.dev/)