package playerquests.client.chat.command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import playerquests.client.ClientDirector;
import playerquests.utility.ChatUtils;
import playerquests.utility.FileUtils;
import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import playerquests.utility.test.TestUtility;
import playerquests.utility.test.Testdatabase;
import playerquests.utility.test.Testquest;
import playerquests.utility.test.TestUtility.TestResult;

public class Commandtest extends ChatCommand {

    Map<String, Class<? extends TestUtility>> tests = Map.of(
        "database", Testdatabase.class,
        "quest", Testquest.class
    );
    private String testLogFilename;

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
        if (args.length == 0) {
            runAllTests(clientDirector);
            return true;
        }

        // Run specific test based on keyword
        final String testKeyword = args[0].toLowerCase();
        runSpecificTest(clientDirector, testKeyword);
        return true;
    }

    private void runSpecificTest(ClientDirector clientDirector, String testKeyword) {
        Player player = clientDirector.getPlayer();

        // get test module
        Class<? extends TestUtility> testClass = this.tests.get(testKeyword);

        // if the test module is null, send message and exit
        if (testClass == null) {
            ChatUtils.message(String.format("Could not find '%s' test module", testKeyword))
                .style(MessageStyle.PRETTY)
                .type(MessageType.ERROR)
                .player(player)
                .send();
            return;
        }

        // try to construct an instance of the test module
        try {
            TestUtility testUtility = testClass.getDeclaredConstructor(ClientDirector.class).newInstance(clientDirector);

            // define what happens when each test completes, and when the entire module completes
            testUtility.runTests(
                (testResult) -> {
                    sendTestSingular(clientDirector, testResult);
                }, 
                (testResults) -> {
                    sendTestSummary(clientDirector, testResults);
                }
            );

        // catch if the test module could not be ran
        } catch (Exception e) {
            String baseMessage = String.format("Could not run the '%s' test module", testKeyword);

            // send a simple message to the player
            ChatUtils.message(baseMessage+"\nCheck the console for more details")
                .style(MessageStyle.PRETTY)
                .type(MessageType.ERROR)
                .player(player)
                .send();
            
            // send a detailed message to the console
            ChatUtils.message(baseMessage+", Cause: "+e.getMessage())
                .style(MessageStyle.PRETTY)
                .type(MessageType.ERROR)
                .target(MessageTarget.CONSOLE)
                .send();
        }
    }

    private void runAllTests(ClientDirector clientDirector) {
        // prep test log
        this.testLogFilename = "test.log";
        try {
            // delete old if exists
            FileUtils.delete(testLogFilename);
        } catch (IOException e) {
            // Not a problem
        } 

        // run tests
        this.tests.keySet().forEach(testKeyword -> {
            this.runSpecificTest(clientDirector, testKeyword);
        });
    }

    private void sendTestSingular(ClientDirector clientDirector, TestResult testResult) {
        Player player = clientDirector.getPlayer();

        // determine decorations based on passing
        String prefix = testResult.didTestPass ? "[PASS]" : "[FAIL]";
        NamedTextColor color = testResult.didTestPass ? NamedTextColor.GREEN : NamedTextColor.RED;
        String icon = testResult.didTestPass ? "✅" : "❌";

        // compose message body
        Component message = Component.empty()
            .append(Component.text("("+testResult.className+") "))
            .append(Component.text(testResult.testLabel+": "))
            .append(Component.text(prefix+" "))
            .append(Component.text(icon))
            .color(color);

        // compose message specs & send message to player
        ChatUtils.message(message)
            .style(MessageStyle.SIMPLE)
            .type(MessageType.NOTIF)
            .player(player)
            .send();
    }

    private void sendTestSummary(ClientDirector clientDirector, List<TestResult> testResults) {
        Player player = clientDirector.getPlayer();

        // compute passed tests
        List<TestResult> passedTests = testResults.stream().filter(result -> result.didTestPass).toList();

        // compose message
        String message = String.format("%s: %d/%d Tests Passed!", testResults.getFirst().className, passedTests.size(), testResults.size());

        // send message to player
        ChatUtils.message(message)
            .style(MessageStyle.SIMPLE)
            .type(MessageType.NOTIF)
            .player(player)
            .send();

        // send to log
        try {
            FileUtils.append(
                this.testLogFilename, 
                String.join(
                    "", 
                    testResults.stream().map(res -> res.toString()+"\n").toList()
                ).getBytes(StandardCharsets.UTF_8)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}