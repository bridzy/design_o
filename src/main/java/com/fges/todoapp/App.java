package com.fges.todoapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.*;

/**
 * Interface définissant les opérations pour gérer les fichiers de tâches TODO.
 */
interface TodoFileManager {
    void insert(String todo, boolean isDone, Path filePath) throws IOException;
    void list(Path filePath, boolean onlyDone) throws IOException;
    List<String[]> readAll(Path filePath) throws IOException;
}

/**
 * Gestionnaire de fichiers pour le format JSON.
 */
class JsonFileManager implements TodoFileManager {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void insert(String todo, boolean isDone, Path filePath) throws IOException {
        JsonNode actualObj = Files.exists(filePath) ? mapper.readTree(Files.readString(filePath)) : JsonNodeFactory.instance.arrayNode();
        if (actualObj instanceof ArrayNode) {
            ObjectNode todoNode = JsonNodeFactory.instance.objectNode();
            todoNode.put("task", todo);
            todoNode.put("done", isDone);
            ((ArrayNode) actualObj).add(todoNode);
            Files.writeString(filePath, mapper.writeValueAsString(actualObj));
        }
    }

    @Override
    public void list(Path filePath, boolean onlyDone) throws IOException {
        if (Files.exists(filePath)) {
            JsonNode actualObj = mapper.readTree(Files.readString(filePath));
            if (actualObj.isArray()) {
                for (JsonNode node : actualObj) {
                    JsonNode taskNode = node.get("task");
                    JsonNode doneNode = node.get("done");
                    boolean done = (doneNode != null) && doneNode.asBoolean();
                    String task = (taskNode != null) ? taskNode.asText() : "Unknown Task";
                    if (!onlyDone || done) {
                        System.out.println("- " + (done ? "Done: " : "") + task);
                    }
                }
            }
        }
    }

    @Override
    public List<String[]> readAll(Path filePath) throws IOException {
        List<String[]> todos = new ArrayList<>();
        if (Files.exists(filePath)) {
            JsonNode actualObj = mapper.readTree(Files.readString(filePath));
            if (actualObj.isArray()) {
                for (JsonNode node : actualObj) {
                    String task = node.has("task") ? node.get("task").asText() : "Unknown Task";
                    boolean done = node.has("done") && node.get("done").asBoolean();
                    todos.add(new String[]{task, String.valueOf(done)});
                }
            }
        }
        return todos;
    }
}

/**
 * Gestionnaire de fichiers pour le format CSV.
 */
class CsvFileManager implements TodoFileManager {
    @Override
    public void insert(String todo, boolean isDone, Path filePath) throws IOException {
        String content = Files.exists(filePath) ? Files.readString(filePath) : "";
        content += "\"" + todo + "\"," + isDone + "\n";
        Files.writeString(filePath, content);
    }

    @Override
    public void list(Path filePath, boolean onlyDone) throws IOException {
        if (Files.exists(filePath)) {
            String content = Files.readString(filePath);
            Arrays.stream(content.split("\n"))
                    .filter(line -> !onlyDone || line.endsWith("true"))
                    .forEach(line -> {
                        String[] parts = line.split(",");
                        String task = parts[0].replace("\"", "");
                        boolean done = Boolean.parseBoolean(parts[1]);
                        System.out.println("- " + (done ? "Done: " : "") + task);
                    });
        }
    }

    @Override
    public List<String[]> readAll(Path filePath) throws IOException {
        List<String[]> todos = new ArrayList<>();
        if (Files.exists(filePath)) {
            String content = Files.readString(filePath);
            Arrays.stream(content.split("\n")).forEach(line -> {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    String task = parts[0].replace("\"", "");
                    boolean done = Boolean.parseBoolean(parts[1]);
                    todos.add(new String[]{task, String.valueOf(done)});
                }
            });
        }
        return todos;
    }
}

/**
 * Application principale pour la gestion des tâches TODO.
 */
public class App {
    public static void main(String[] args) {
        System.exit(exec(args));
    }

    /**
     * Exécute l'application en fonction des arguments de la ligne de commande.
     */
    public static int exec(String[] args) {
        Options cliOptions = new Options();
        CommandLineParser parser = new DefaultParser();
        cliOptions.addRequiredOption("s", "source", true, "File containing the todos");
        cliOptions.addOption("d", "done", false, "Mark todo as done or list only done todos");
        cliOptions.addOption("o", "output", true, "Output file for migration");

        CommandLine cmd = null;
        try {
            cmd = parser.parse(cliOptions, args);
        } catch (ParseException ex) {
            System.err.println("Error parsing command line options: " + ex.getMessage());
            return 1;
        }

        String fileName = cmd.getOptionValue("s");
        boolean isDone = cmd.hasOption("d");
        String outputFileName = cmd.getOptionValue("o");
        List<String> positionalArgs = cmd.getArgList();

        if (positionalArgs.isEmpty()) {
            System.err.println("Missing Command");
            return 1;
        }

        String command = positionalArgs.get(0);
        Path filePath = Paths.get(fileName);
        Path outputPath = outputFileName != null ? Paths.get(outputFileName) : null;

        try {
            switch (command) {
                case "insert":
                    if (positionalArgs.size() < 2) {
                        System.err.println("Missing TODO name");
                        return 1;
                    }
                    String todo = positionalArgs.get(1);
                    getFileManager(filePath).insert(todo, isDone, filePath);
                    break;
                case "list":
                    getFileManager(filePath).list(filePath, isDone);
                    break;
                case "migrate":
                    if (outputPath == null) {
                        System.err.println("Missing output file for migration");
                        return 1;
                    }
                    migrate(filePath, outputPath);
                    break;
                default:
                    System.err.println("Unknown command");
                    return 1;
            }
        } catch (IOException ex) {
            System.err.println("An I/O error occurred: " + ex.getMessage());
            return 1;
        }

        System.err.println("Done.");
        return 0;
    }

    /**
     * Migre les tâches TODO d'un fichier source à un fichier de destination, respectant le format de fichier.
     */
    private static void migrate(Path sourcePath, Path outputPath) throws IOException {
        TodoFileManager sourceManager = getFileManager(sourcePath);
        List<String[]> todos = sourceManager.readAll(sourcePath);
        TodoFileManager outputManager = getFileManager(outputPath);
        for (String[] todo : todos) {
            outputManager.insert(todo[0], Boolean.parseBoolean(todo[1]), outputPath);
        }
    }

    /**
     * Retourne une instance de TodoFileManager appropriée en fonction de l'extension du fichier.
     */
    private static TodoFileManager getFileManager(Path filePath) {
        if (filePath.toString().endsWith(".json")) {
            return new JsonFileManager();
        } else if (filePath.toString().endsWith(".csv")) {
            return new CsvFileManager();
        } else {
            throw new IllegalArgumentException("Unsupported file format");
        }
    }
}