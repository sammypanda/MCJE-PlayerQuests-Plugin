package playerquests.utility;

import playerquests.utility.ChatUtils.MessageStyle;
import playerquests.utility.ChatUtils.MessageTarget;
import playerquests.utility.ChatUtils.MessageType;
import java.util.Map;
import java.util.HashMap;

/**
 * Provides SQL migration queries for updating the database schema to X from the last real version.
 * <p>
 * This class maintains a registry of migration queries keyed by version numbers.
 * These migrations ensure that the database structure is consistent with the 
 * application's requirements for different versions.
 * </p>
 */
public class MigrationUtils {

    /**
     * Registry of migration queries keyed by version strings.
     */
    private static final Map<String, String> MIGRATIONS = new HashMap<>();

    static {
        // Initialize all migrations
        MIGRATIONS.put("0.4", 
            "ALTER TABLE quests ADD COLUMN toggled TEXT DEFAULT true;");
            
        MIGRATIONS.put("0.5.1", """
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
        """);
        
        MIGRATIONS.put("0.7", """
            -- Begin a transaction to ensure all operations are atomic
            BEGIN TRANSACTION;

            ALTER TABLE quests ADD COLUMN inventory TEXT NOT NULL DEFAULT "{ }"  ;

            -- Commit the transaction if everything is successful
            COMMIT;
        """);
        
        MIGRATIONS.put("0.8", """
            -- Begin a transaction to ensure all operations are atomic
            BEGIN TRANSACTION;

            DROP TABLE diary_quests;

            -- Commit the transaction if everything is successful
            COMMIT;
        """);
        
        MIGRATIONS.put("0.10", """
            BEGIN TRANSACTION;

            ALTER TABLE plugin ADD COLUMN citizens2 BOOLEAN NOT NULL DEFAULT FALSE;

            COMMIT;
        """);
        
        MIGRATIONS.put("0.10.1", """
            BEGIN TRANSACTION;

            ALTER TABLE quests ADD COLUMN inventory TEXT NOT NULL DEFAULT "";

            COMMIT;
        """);
        
        MIGRATIONS.put("0.10.4", """
            BEGIN TRANSACTION;
            
            -- Create replacement plugin table
            CREATE TABLE IF NOT EXISTS temp_plugin (
                plugin TEXT PRIMARY KEY,
                version TEXT NOT NULL,
                CONSTRAINT single_row_constraint UNIQUE (plugin)
            );

            -- Migrate data from old plugib table to temporary plugin table
            INSERT INTO temp_plugin (plugin, version)
            SELECT plugin.plugin, plugin.version
            FROM plugin;

            -- Drop old plugin table
            DROP TABLE IF EXISTS plugin;

            -- Rename temporary tables to match new schema
            ALTER TABLE temp_plugin RENAME TO plugin;

            -- Commit the transaction if everything is successful
            COMMIT;
        """);
    }

    /**
     * Should be accessed statically.
     */
    private MigrationUtils() {}

    /**
     * Gets the migration query for the specified version.
     *
     * @param version the database version to get the migration for
     * @return The SQL query string for the specified version
     * @throws IllegalArgumentException if no migration exists for the version
     */
    public static String getMigration(String version) {
        if (!MIGRATIONS.containsKey(version)) {
            throw new IllegalArgumentException("No migration query for version v" + version);
        }
        
        // Special case for 0.10.4 that requires a notification
        if ("0.10.4".equals(version)) {
            ChatUtils.message("Entities changed in this update, for all your quest files you need to change entity values from 'type:CHICKEN,color:black' to 'CHICKEN'; meaning, remove everything except the name of the entity.")
                .style(MessageStyle.PRETTY)
                .type(MessageType.WARN)
                .target(MessageTarget.WORLD)
                .send();
        }
        
        return MIGRATIONS.get(version);
    }
}