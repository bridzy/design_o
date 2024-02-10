package com.fges.todoapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.cli.*;
import java.util.stream.Collectors;

interface TodoFileManager {
    void insert(String todo, boolean isDone, Path filePath) throws IOException;
    void list(Path filePath, boolean onlyDone) throws IOException;
}

// Gestionnaire de fichiers JSON
class JsonFileManager implements TodoFileManager {
    @Override
    public void insert(String todo, boolean isDone, Path filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
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
            ObjectMapper mapper = new ObjectMapper();
            JsonNode actualObj = mapper.readTree(Files.readString(filePath));
            if (actualObj.isArray()) {
                for (JsonNode node : actualObj) {
                    JsonNode taskNode = node.get("task");
                    JsonNode doneNode = node.get("done");
                    // Vérifie si les champs "task" et "done" existent et sont non null avant de les utiliser
                    boolean done = (doneNode != null) && doneNode.asBoolean();
                    // Utiliser une valeur par défaut ou sauter l'élément si la tâche est null
                    String task = (taskNode != null) ? taskNode.asText() : "Unknown Task";
                    if (!onlyDone || done) {
                        System.out.println("- " + (done ? "Done: " : "") + task);
                    }
                }
            }
        }
    }

}

// Gestionnaire de fichiers CSV
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
}

public class App {

    public static void main(String[] args) throws Exception {
        System.exit(exec(args));
    }

    public static int exec(String[] args) throws IOException {
        Options cliOptions = new Options();
        CommandLineParser parser = new DefaultParser();
        cliOptions.addRequiredOption("s", "source", true, "File containing the todos");
        cliOptions.addOption("d", "done", false, "Mark todo as done or list only done todos");

        CommandLine cmd;
        try {
            cmd = parser.parse(cliOptions, args);
        } catch (ParseException ex) {
            System.err.println("Fail to parse arguments: " + ex.getMessage());
            return 1;
        }

        String fileName = cmd.getOptionValue("s");
        boolean isDone = cmd.hasOption("d");
        List<String> positionalArgs = cmd.getArgList();

        if (positionalArgs.isEmpty()) {
            System.err.println("Missing Command");
            return 1;
        }

        String command = positionalArgs.get(0);
        Path filePath = Paths.get(fileName);

        TodoFileManager fileManager;
        if (fileName.endsWith(".json")) {
            fileManager = new JsonFileManager();
        } else if (fileName.endsWith(".csv")) {
            fileManager = new CsvFileManager();
        } else {
            System.err.println("Unsupported file format");
            return 1;
        }

        switch (command) {
            case "insert":
                if (positionalArgs.size() < 2) {
                    System.err.println("Missing TODO name");
                    return 1;
                }
                String todo = positionalArgs.get(1);
                fileManager.insert(todo, isDone, filePath);
                break;
            case "list":
                fileManager.list(filePath, isDone); // Use isDone to determine if only completed todos should be listed
                break;
            default:
                System.err.println("Unknown command");
                return 1;
        }

        System.err.println("Done.");
        return 0;
    }
}
