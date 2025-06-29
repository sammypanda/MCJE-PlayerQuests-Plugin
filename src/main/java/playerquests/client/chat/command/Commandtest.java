package playerquests.client.chat.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.test.TestUtility;
import playerquests.utility.test.Testdatabase;

public class Commandtest extends ChatCommand {

    Map<String, Class<? extends TestUtility>> tests = Map.of(
        "database", Testdatabase.class
    );

    public Commandtest() {
        super("test");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        switch (args.length) {
            case 1: // provide options for module testing
                return new ArrayList<>(this.tests.keySet());
            default:
                return List.of();
        }
    }

    @Override
    public boolean execute(CommandSender sender, Command command, String label, String[] args) {
        final boolean isPlayer = sender instanceof Player;

        // exit if not a player
        if (!isPlayer) {
            ChatUtils.message("Cannot run tests as a non-player")
                .type(MessageType.ERROR)
                .style(MessageStyle.PLAIN)
                .target(MessageTarget.CONSOLE)
                .send();
            return true;
        }

        Player player = (Player) sender;
        ClientDirector clientDirector = new ClientDirector(player);

        // do all if no args
        if (args.length <= 1) {
            return runAllTests(player, clientDirector);
        }

        // Run specific test based on keyword
        final String testKeyword = args[0].toLowerCase();
        return runSpecificTest(player, clientDirector, testKeyword);
    }

    private boolean runSpecificTest(Player player, ClientDirector clientDirector, String testKeyword) {
        Class<? extends TestUtility> testClass = tests.get(testKeyword);
        
        if (testClass == null) {
            ChatUtils.message("Unknown test module: " + testKeyword)
                .type(MessageType.ERROR)
                .target(MessageTarget.PLAYER)
                .player(player)
                .send();
            return false;
        }

        try {
            TestUtility testInstance = testClass.getDeclaredConstructor(ClientDirector.class).newInstance(clientDirector);
            List<TestUtility.TestResult> results = testInstance.runTests();

            sendTestResults(player, results); return true;
        } catch (Exception e) {
            ChatUtils.message("Failed to run test: " + e.getMessage())
                .type(MessageType.ERROR)
                .target(MessageTarget.PLAYER)
                .player(player)
                .send();

            e.printStackTrace(); return false;
        }
    }

    private boolean runAllTests(Player sender, ClientDirector clientDirector) {
        this.tests.keySet().forEach(testKeyword -> {
            this.runSpecificTest(sender, clientDirector, testKeyword);
        });
        return true;
    }

    private void sendTestResults(Player player, List<TestUtility.TestResult> results) {
        int totalTests = results.size();
        int totalPassed = (int) results.stream().filter(r -> r.didTestPass).count();

        // Detailed results
        results.forEach(result -> {
            MessageType msgType = result.didTestPass ? MessageType.NOTIF : MessageType.ERROR;
            String status = result.didTestPass ? "PASS" : "FAIL";
            String message = String.format("[%s] %s: %s", 
                status, 
                result.className, 
                result.testLabel.isEmpty() ? result.testName : result.testLabel);

            if (result.testError != null) {
                message += " - " + result.testError.getMessage();
            }

            ChatUtils.message(message)
                .type(msgType)
                .target(MessageTarget.PLAYER)
                .style(MessageStyle.SIMPLE)
                .player(player)
                .send();
        });

        // Console logging
        String consoleReport = results.stream()
            .map(r -> String.format("[%s] %s.%s - %s", 
                r.didTestPass ? "PASS" : "FAIL", 
                r.className, 
                r.testName,
                r.testLabel.isEmpty() ? r.testName : r.testLabel))
            .collect(Collectors.joining("\n"));

        Bukkit.getLogger().info("Test Results for " + player.getName() + ":\n" + consoleReport);

        // Summary message
        ChatUtils.message(String.format("Test Results: %d/%d passed", totalPassed, totalTests))
            .type(totalPassed == totalTests ? MessageType.NOTIF : MessageType.ERROR)
            .target(MessageTarget.PLAYER)
            .player(player)
            .send();
    }
}