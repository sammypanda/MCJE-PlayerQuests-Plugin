package playerquests.utility.test;

import java.util.concurrent.CompletableFuture;

import playerquests.client.ClientDirector;
import playerquests.utility.annotation.PlayerQuestsTest;
import playerquests.utility.singleton.PlayerQuests;

public class Testdatabase extends TestUtility {

    public Testdatabase(ClientDirector clientDirector) {
        super(clientDirector);
    }

    @PlayerQuestsTest(label = "Database connection", priority = 0)
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.completedFuture(PlayerQuests.getDatabase().canConnect());
    }
}
