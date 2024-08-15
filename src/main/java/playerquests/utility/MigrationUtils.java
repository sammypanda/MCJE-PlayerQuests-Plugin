package playerquests.utility;

/**
 * Provides the changes for each version.
 * For example: the changes to the database.
 */
public class MigrationUtils {
    // Get migration query for version 0.4
    public static String dbV0_4() {
        return "ALTER TABLE quests ADD COLUMN toggled TEXT DEFAULT true;";
    }

    // Get migration query for version 0.5
    public static String dbV0_5() {
        throw new IllegalArgumentException("No migration query for version v0.5.1");
    }

    // Get migration query for version 0.5.1
    public static String dbV0_5_1() {
        return """
            -- Begin a transaction to ensure all operations are atomic
            BEGIN TRANSACTION;
            
            -- Drop temporary tables if they exist
            DROP TABLE IF EXISTS temp_diary_quests;
            DROP TABLE IF EXISTS temp_diaries;
            DROP TABLE IF EXISTS temp_quests;
            DROP TABLE IF EXISTS temp_players;
            
            -- Create temporary tables for migration
            CREATE TABLE temp_players (
                uuid TEXT PRIMARY KEY NOT NULL
            );
            
            CREATE TABLE temp_quests (
                id TEXT PRIMARY KEY NOT NULL
            );
            
            CREATE TABLE temp_diaries (
                id TEXT PRIMARY KEY NOT NULL,
                player TEXT UNIQUE,
                FOREIGN KEY (player) REFERENCES temp_players(uuid)
            );
            
            CREATE TABLE temp_diary_quests (
                id TEXT PRIMARY KEY NOT NULL,
                stage TEXT NOT NULL,
                action TEXT,
                quest TEXT,
                diary TEXT,
                FOREIGN KEY (quest) REFERENCES temp_quests(id),
                FOREIGN KEY (diary) REFERENCES temp_diaries(id)
            );
            
            -- Migrate data from old tables to temporary tables
            INSERT INTO temp_players (uuid)
            SELECT uuid FROM players;
            
            INSERT INTO temp_quests (id)
            SELECT id FROM quests;
            
            INSERT INTO temp_diaries (id, player)
            SELECT 'diary_' || p.uuid, p.uuid
            FROM diaries AS d
            JOIN players AS p ON d.player = p.id;
            
            INSERT INTO temp_diary_quests (id, stage, action, quest, diary)
            SELECT dq.id, dq.stage, dq.action, dq.quest, 'diary_' || p.uuid
            FROM diary_quests AS dq
            JOIN diaries AS d ON dq.diary = d.id
            JOIN players AS p ON d.player = p.id;
            
            -- Drop old tables
            DROP TABLE IF EXISTS diary_quests;
            DROP TABLE IF EXISTS diaries;
            DROP TABLE IF EXISTS quests;
            DROP TABLE IF EXISTS players;
            
            -- Rename temporary tables to match new schema
            ALTER TABLE temp_players RENAME TO players;
            ALTER TABLE temp_quests RENAME TO quests;
            ALTER TABLE temp_diaries RENAME TO diaries;
            ALTER TABLE temp_diary_quests RENAME TO diary_quests;
            
            -- Remove unused sequence tracking (next increments)
            DELETE FROM sqlite_sequence WHERE name = 'diaries';
            DELETE FROM sqlite_sequence WHERE name = 'players';
            
            -- Commit the transaction if everything is successful
            COMMIT;
        """;
    }
}