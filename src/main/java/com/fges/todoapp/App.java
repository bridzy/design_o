package com.fges.todoapp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.cli.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class App {

    public static void main(String[] args) throws Exception {
        System.exit(exec(args));
    }

    public static int exec(String[] args) throws IOException {
        CommandLineHandler cmdHandler = new CommandLineHandler();
        Command command = cmdHandler.parse(args);
        if (command == null) {
            return 1;
        }

        TodoProcessor processor = new TodoProcessor();
        return processor.processCommand(command);
    }
}

class CommandLineHandler {
    public Command parse(String[] args) {
        Options options = new Options();
        options.addRequiredOption("s", "source", true, "File containing the todos");
        options.addOption("d", "done", false, "Mark todo as done");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String fileName = cmd.getOptionValue("s");
            boolean isDone = cmd.hasOption("d");
            List<String> positionalArgs = cmd.getArgList();

            if (positionalArgs.isEmpty()) {
                System.err.println("Missing Command");
                return null;
            }
            String commandType = positionalArgs.get(0);

            return new Command(fileName, commandType, positionalArgs, isDone);
        } catch (ParseException ex) {
            System.err.println("Fail to parse arguments: " + ex.getMessage());
            return null;
        }
    }
}

class Command {
    private String fileName;
    private String commandType;
    private List<String> arguments;
    private boolean isDone;

    public Command(String fileName, String commandType, List<String> arguments, boolean isDone) {
        this.fileName = fileName;
        this.commandType = commandType;
        this.arguments = arguments;
        this.isDone = isDone;
    }

    public String getFileName() {
        return fileName;
    }

    public String getCommandType() {
        return commandType;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public boolean isDone() {
        return isDone;
    }
}

interface FileFormatHandler {
    void processInsertCommand(String fileName, String todo, boolean isDone) throws IOException;
    void processListCommand(String fileName, boolean onlyDone) throws IOException;
}

class JsonFormatHandler implements FileFormatHandler {
    private ObjectMapper mapper = new ObjectMapper();

    public void processInsertCommand(String fileName, String todo, boolean isDone) throws IOException {
        Path path = Paths.get(fileName);
        List<ObjectNode> todos = new ArrayList<>();
        if (Files.exists(path)) {
            String content = Files.readString(path);
            todos = mapper.readValue(content, new TypeReference<List<ObjectNode>>(){});
        }

        ObjectNode newTodo = mapper.createObjectNode();
        newTodo.put("task", todo);
        newTodo.put("done", isDone);
        todos.add(newTodo);

        Files.writeString(path, mapper.writeValueAsString(todos));
    }

    public void processListCommand(String fileName, boolean onlyDone) throws IOException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            String content = Files.readString(path);
            List<ObjectNode> todos = mapper.readValue(content, new TypeReference<List<ObjectNode>>(){});
            todos.stream()
                    .filter(todo -> !onlyDone || todo.get("done").asBoolean())
                    .forEach(todo -> System.out.println((todo.get("done").asBoolean() ? "Done: " : "") + todo.get("task").asText()));
        }
    }
}

class CsvFormatHandler implements FileFormatHandler {
    @Override
    public void processInsertCommand(String fileName, String todo, boolean isDone) throws IOException {
        Path path = Paths.get(fileName);
        String newTodoLine = todo + "," + isDone;

        List<String> existingTodos;
        if (Files.exists(path)) {
            existingTodos = Files.readAllLines(path);
        } else {
            existingTodos = List.of();
        }

        existingTodos.add(newTodoLine);
        Files.write(path, existingTodos);
    }

    @Override
    public void processListCommand(String fileName, boolean onlyDone) throws IOException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            List<String> lines = Files.readAllLines(path);
            lines.stream()
                    .filter(line -> !onlyDone || line.endsWith(",true"))
                    .forEach(line -> {
                        String[] parts = line.split(",");
                        if (parts.length > 1 && Boolean.parseBoolean(parts[1])) {
                            System.out.println("Done: " + parts[0]);
                        } else {
                            System.out.println(parts[0]);
                        }
                    });
        }
    }
}


class TodoProcessor {
    private FileFormatHandler formatHandler;

    public int processCommand(Command command) throws IOException {
        if (command.getFileName().endsWith(".json")) {
            formatHandler = new JsonFormatHandler();
        } else {
            System.err.println("Unsupported file format");
            return 1;
        }

        if (command.getCommandType().equals("insert")) {
            if (command.getArguments().size() < 2) {
                System.err.println("Missing TODO name");
                return 1;
            }
            String todo = command.getArguments().get(1);
            formatHandler.processInsertCommand(command.getFileName(), todo, command.isDone());
        } else if (command.getCommandType().equals("list")) {
            formatHandler.processListCommand(command.getFileName(), command.isDone());
        } else {
            System.err.println("Invalid Command");
            return 1;
        }
        return 0;
    }
}
