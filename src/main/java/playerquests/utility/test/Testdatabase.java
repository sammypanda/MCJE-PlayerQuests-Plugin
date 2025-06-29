package playerquests.utility.test;

import playerquests.client.ClientDirector;
import playerquests.utility.annotation.PlayerQuestsTest;
import playerquests.utility.singleton.PlayerQuests;

public class Testdatabase extends TestUtility {

    public Testdatabase(ClientDirector clientDirector) {
        super(clientDirector);
    }

    @PlayerQuestsTest(label = "Database connection", priority = 0)
    public boolean testConnection() {
        return PlayerQuests.getDatabase().canConnect();
    }
}
