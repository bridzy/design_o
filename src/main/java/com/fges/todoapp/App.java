package com.fges.todoapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.apache.commons.cli.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            String fileName = cmd.getOptionValue("s");
            List<String> positionalArgs = cmd.getArgList();
            if (positionalArgs.isEmpty()) {
                System.err.println("Missing Command");
                return null;
            }
            String commandType = positionalArgs.get(0);
            return new Command(fileName, commandType, positionalArgs);
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

    public Command(String fileName, String commandType, List<String> arguments) {
        this.fileName = fileName;
        this.commandType = commandType;
        this.arguments = arguments;
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
}

interface FileFormatHandler {
    String readContent(String fileName) throws IOException;
    void writeContent(String fileName, String content) throws IOException;
    void processInsertCommand(String todo);
    void processListCommand();
}

class JsonFormatHandler implements FileFormatHandler {
    private String content;

    public String readContent(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            content = Files.readString(path);
        } else {
            content = "";
        }
        return content;
    }

    public void writeContent(String fileName, String content) throws IOException {
        Files.writeString(Paths.get(fileName), content);
    }

    public void processInsertCommand(String todo) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode actualObj = mapper.readTree(content);
            if (actualObj instanceof MissingNode || !actualObj.isArray()) {
                actualObj = JsonNodeFactory.instance.arrayNode();
            }

            ((ArrayNode) actualObj).add(todo);
            content = actualObj.toString();
        } catch (IOException e) {
            content = JsonNodeFactory.instance.arrayNode().toString();
        }
    }

    public void processListCommand() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode actualObj = mapper.readTree(content);
            if (actualObj instanceof ArrayNode) {
                actualObj.forEach(node -> System.out.println("- " + node.asText()));
            }
        } catch (IOException e) {
            System.err.println("Error reading JSON content");
        }
    }
}

class CsvFormatHandler implements FileFormatHandler {
    private String content;

    public String readContent(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        if (Files.exists(path)) {
            content = Files.readString(path);
        } else {
            content = "";
        }
        return content;
    }

    public void writeContent(String fileName, String content) throws IOException {
        Files.writeString(Paths.get(fileName), content);
    }

    public void processInsertCommand(String todo) {
        if (!content.endsWith("\\n") && !content.isEmpty()) {
            content += "\\n";
        }
        content += todo;
    }

    public void processListCommand() {
        Arrays.stream(content.split("\\n")).forEach(todo -> System.out.println("- " + todo));
    }
}

class TodoProcessor {
    private FileFormatHandler formatHandler;

    public int processCommand(Command command) throws IOException {
        if (command.getFileName().endsWith(".json")) {
            formatHandler = new JsonFormatHandler();
        } else if (command.getFileName().endsWith(".csv")) {
            formatHandler = new CsvFormatHandler();
        } else {
            System.err.println("Unsupported file format");
            return 1;
        }

        String fileContent = formatHandler.readContent(command.getFileName());

        if (command.getCommandType().equals("insert")) {
            if (command.getArguments().size() < 2) {
                System.err.println("Missing TODO name");
                return 1;
            }
            String todo = command.getArguments().get(1);
            formatHandler.processInsertCommand(todo);
        } else if (command.getCommandType().equals("list")) {
            formatHandler.processListCommand();
        } else {
            System.err.println("Invalid Command");
            return 1;
        }

        formatHandler.writeContent(command.getFileName(), fileContent);
        return 0;
    }
}
