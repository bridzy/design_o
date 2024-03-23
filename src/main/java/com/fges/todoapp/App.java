package com.fges.todoapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.cli.*;
import java.util.stream.Collectors;
import java.nio.file.StandardOpenOption;


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
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void insert(String todo, boolean isDone, Path filePath) throws IOException {
        ArrayNode todos = readTodosFromFile(filePath);
        todos.add(createTodoNode(todo, isDone));
        writeTodosToFile(filePath, todos);
    }

    @Override
    public void list(Path filePath, boolean onlyDone) throws IOException {
        ArrayNode todos = readTodosFromFile(filePath);
        todos.forEach(node -> {
            boolean done = node.get("done").asBoolean();
            String task = node.get("task").asText();
            if (!onlyDone || done) {
                System.out.println("- " + (done ? "[DONE] " : "[TODO] ") + task);
            }
        });
    }

    @Override
    public List<String[]> readAll(Path filePath) throws IOException {
        List<String[]> todosList = new ArrayList<>();
        ArrayNode todos = readTodosFromFile(filePath);
        todos.forEach(node -> {
            String task = node.get("task").asText();
            boolean done = node.get("done").asBoolean();
            todosList.add(new String[]{task, String.valueOf(done)});
        });
        return todosList;
    }

    private ArrayNode readTodosFromFile(Path filePath) throws IOException {
        if (Files.exists(filePath)) {
            JsonNode jsonNode = mapper.readTree(Files.readString(filePath));
            if (jsonNode.isArray()) {
                return (ArrayNode) jsonNode;
            }
        }
        return mapper.createArrayNode();
    }

    private ObjectNode createTodoNode(String todo, boolean isDone) {
        ObjectNode todoNode = mapper.createObjectNode();
        todoNode.put("task", todo);
        todoNode.put("done", isDone);
        return todoNode;
    }

    private void writeTodosToFile(Path filePath, ArrayNode todos) throws IOException {
        Files.writeString(filePath, mapper.writeValueAsString(todos));
    }
}
/**
 * Gestionnaire de fichiers pour le format CSV.
 */
class CsvFileManager implements TodoFileManager {

    @Override
    public void insert(String todo, boolean isDone, Path filePath) throws IOException {
        // Ensure to handle commas in todo text properly by enclosing the entire todo in quotes if not already done
        if (!todo.startsWith("\"")) {
            todo = "\"" + todo + "\"";
        }
        String todoLine = String.format("%s,%s\n", todo, isDone);
        Files.writeString(filePath, todoLine, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    @Override
    public void list(Path filePath, boolean onlyDone) throws IOException {
        readTodosFromFile(filePath).stream()
                .filter(todo -> !onlyDone || Boolean.parseBoolean(todo[1]))
                .forEach(todo -> System.out.println("- " + (Boolean.parseBoolean(todo[1]) ? "[DONE] " : "[TODO] ") + todo[0]));
    }

    @Override
    public List<String[]> readAll(Path filePath) throws IOException {
        return readTodosFromFile(filePath);
    }

    private List<String[]> readTodosFromFile(Path filePath) throws IOException {
        if (Files.exists(filePath)) {
            return Files.readAllLines(filePath).stream()
                    .map(this::parseTodoLine)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private String[] parseTodoLine(String line) {
        // Enhanced parsing to handle todos containing commas by considering the first comma as part of the todo if enclosed in quotes
        boolean isInQuotes = line.startsWith("\"");
        String[] parts;

        if (isInQuotes) {
            int firstComma = line.indexOf("\",");
            if (firstComma != -1) {
                String task = line.substring(1, firstComma);
                String done = line.substring(firstComma + 2);
                parts = new String[] { task, done };
            } else {
                parts = new String[]{"Unknown Task", "false"};
            }
        } else {
            parts = line.split(",", 2);
        }

        if (parts.length == 2) {
            parts[0] = parts[0].replace("\"", ""); // Remove surrounding quotes if present
            return parts;
        }
        return new String[]{"Unknown Task", "false"};
    }
}
/**
 * Application principale pour la gestion des tâches TODO.
 */

public class App {

    public static void main(String[] args) {
        System.exit(exec(args));
    }

    public static int exec(String[] args) {
        Options options = new Options();
        setupCommandLineOptions(options);

        CommandLine cmd = parseCommandLineArguments(args, options);
        if (cmd == null) return 1; // Error in parsing command line options

        return processCommand(cmd);
    }

    private static void setupCommandLineOptions(Options options) {
        options.addRequiredOption("s", "source", true, "File containing the todos");
        options.addOption("d", "done", false, "Mark todo as done or list only done todos");
        options.addOption("o", "output", true, "Output file for migration");
        options.addOption("h", "help", false, "Show help");
    }

    private static CommandLine parseCommandLineArguments(String[] args, Options options) {
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("h")) {
                formatter.printHelp("todo-app", options);
                return null;
            }
            return cmd;
        } catch (ParseException e) {
            System.err.println("Error parsing command line options: " + e.getMessage());
            formatter.printHelp("todo-app", options);
            return null;
        }
    }

    private static int processCommand(CommandLine cmd) {
        if (cmd == null) return 1; // Early exit if cmd is null, indicating help was requested or an error occurred

        Path sourceFilePath = Paths.get(cmd.getOptionValue("s"));
        try {
            String command = cmd.getArgList().get(0).toLowerCase();
            switch (command) {
                case "insert":
                    return handleInsertCommand(cmd, sourceFilePath);
                case "list":
                    return handleListCommand(cmd, sourceFilePath);
                case "migrate":
                    return handleMigrateCommand(cmd, sourceFilePath);
                default:
                    System.err.println("Unknown command: " + command);
                    return 1;
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            return 1;
        }
    }

    private static int handleInsertCommand(CommandLine cmd, Path sourceFilePath) throws IOException {
        if (cmd.getArgList().size() < 2) {
            System.err.println("Missing TODO name");
            return 1;
        }
        String todo = cmd.getArgList().get(1);
        boolean isDone = cmd.hasOption("d");
        getFileManager(sourceFilePath).insert(todo, isDone, sourceFilePath);
        System.out.println("TODO inserted successfully");
        return 0;
    }

    private static int handleListCommand(CommandLine cmd, Path sourceFilePath) throws IOException {
        boolean onlyDone = cmd.hasOption("d");
        getFileManager(sourceFilePath).list(sourceFilePath, onlyDone);
        return 0;
    }

    private static int handleMigrateCommand(CommandLine cmd, Path sourceFilePath) throws IOException {
        String outputFileName = cmd.getOptionValue("o");
        if (outputFileName == null) {
            System.err.println("Missing output file for migration");
            return 1;
        }
        Path outputFilePath = Paths.get(outputFileName);
        TodoFileManager sourceManager = getFileManager(sourceFilePath);
        TodoFileManager outputManager = getFileManager(outputFilePath);

        sourceManager.readAll(sourceFilePath).forEach(todo -> {
            try {
                outputManager.insert(todo[0], Boolean.parseBoolean(todo[1]), outputFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        System.out.println("Migration completed successfully");
        return 0;
    }

    private static TodoFileManager getFileManager(Path filePath) {
        if (filePath.toString().endsWith(".json")) {
            return new JsonFileManager();
        } else if (filePath.toString().endsWith(".csv")) {
            return new CsvFileManager();
        } else {
            throw new IllegalArgumentException("Unsupported file format: " + filePath);
        }
    }
}
