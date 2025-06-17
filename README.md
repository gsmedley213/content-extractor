# content-extractor

**Part of the [Gutenberg Annotation Pipeline](#project-context).**

`content-extractor` is a Java service that extracts content from Project Gutenberg HTML files and creates new HTML files where each extracted element is marked so it can be associated with its content later.

---

## Pipeline Overview

This repository is one stage of a multi-step process for creating annotated versions of public domain books:

1. **content-extractor**  
   Extracts content from Project Gutenberg HTML and saves a new HTML file with elements marked to associate them with extracted content.

2. **llm-annotate**  
   Consumes extracted content and uses a language model to generate notes for it, saving results as a notes JSON file.

3. **html-annotate**  
   Combines the marked HTML and notes JSON to generate an annotated HTML file with the notes rendered like tooltips.

---

## How It Works

- Reads a Project Gutenberg HTML file from a shared directory.
- Tries to extract just the text of the book from the HTML file.
- Writes a new HTML file, adding an attribute with an id to each element it extracted content from, for later association.

---

## Usage

### 1. Configuration

- All settings are specified in a YAML file (typically `application.yml`).
- Example configuration is provided in `application.example.yml`.
- Key properties:
    - `shared`: Path to the root shared directory used by all pipeline stages.
    - `directory`: Book-specific subdirectory (e.g., `livestock_and_armour`).
    - `prefix`: Book file prefix (e.g., `pg51244`).
    - `description`: Human readable description. Will be passed to the LLM for context when generating notes.

### 2. Preparing your Book

Before running, the user should:
- Download a zipped HTML version of a Project Gutenberg eBook. (e.g. https://www.gutenberg.org/cache/epub/51244/pg51244-h.zip)
- Create a directory in the `shared` directory defined in their configurations.
- This directory should have a human-readable name, but without spaces. (e.g. `livestock_and_armour`)
- Extract the contents of the zip from Project Gutenberg to this directory.
- The directory should now have a file that starts with `pg#` and ends with `-images.html`.
- Either copy part of the description from the Project Gutenberg page, or read the book and write your own.
- Add an entry under `file:books` in your application.yml with `directory` being the directory you created, `prefix` being the prefix you found early, and `description` being your description.

```yaml
file:
  shared: ../shared/books           # Path to the shared directory used by all services
  books:
    - directory: livestock_and_armour
      prefix: pg51244
      description: >
        "The Livestock Producer and Armour" by Armour and Company is a scientific publication written in the early 20th century.
```

### 3. Running

```sh
./gradlew bootRun --args='--directory=livestock_and_armour --run=1'
```
- `--directory`: Subdirectory for the book (matches `directory` in config).
- `--run`: (Optional) Run number for organizing outputs.

### 4. Outputs

- The service writes:
    - A marked-up HTML file (`<prefix>-marked-<run>.html`)
    - A job metadata JSON file (`AnnotationJob-<prefix>-<run>.json`)
- Both are saved in the book's subdirectory under the shared directory.

---

## Project Context

This project is part of the **Gutenberg Annotation Pipeline**:

1. **content-extractor**  
   Extracts and marks content from Gutenberg HTML for later annotation.

2. **llm-annotate**  
   Generates notes using a language model based on the marked content.

3. **html-annotate**  
   Adds generated notes as annotations to the marked HTML.

---

## Development

- Java 21
- Spring Boot
- Configuration via YAML (`application.yml`)
- See `application.example.yml` for a template.

---

## License

This project is licensed under the [MIT License](LICENSE).

## See Also

- [llm-annotate](https://github.com/gsmedley213/llm-annotate)
- [html-annotate](https://github.com/gsmedley213/html-annotate)