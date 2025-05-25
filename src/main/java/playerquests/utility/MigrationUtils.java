package playerquests.utility;

/**
 * Provides SQL migration queries for updating the database schema to X from the last real version.
 * <p>
 * Each method in this class returns a SQL query or script that applies the necessary changes for
 * a specific version of the database schema. These migrations ensure that the database structure
 * is consistent with the application's requirements for different versions.
 * </p>
 */
public class MigrationUtils {

    /**
     * Should be accessed statically.
     */
    private MigrationUtils() {}

    /**
     * Gets the migration query for version 0.4.
     * <p>
     * This query adds a new column to the `quests` table. The column `toggled` is added with a
     * default value of `true`. This might be used to indicate whether a quest is active or not.
     * </p>
     *
     * @return The SQL query string for migrating to version 0.4.
     */
    public static String dbV0_4() {
        return "ALTER TABLE quests ADD COLUMN toggled TEXT DEFAULT true;";
    }

    /**
     * Gets the migration query for version 0.5.
     * <p>
     * This version does not have a migration query defined. Calling this method will throw an
     * {@link IllegalArgumentException} to indicate that no migration is available for this version.
     * </p>
     *
     * @return This method does not return a query; it always throws an exception.
     * @throws IllegalArgumentException if called.
     */
    public static String dbV0_5() {
        throw new IllegalArgumentException("No migration query for version v0.5.1");
    }

    /**
     * Gets the migration query for version 0.5.1.
     *
     * This query script performs a series of operations to migrate data from old tables to new
     * tables and update the schema:
     * <ul>
     *     <li>Begins a transaction to ensure atomicity of the operations.</li>
     *     <li>Drops temporary tables if they exist.</li>
     *     <li>Creates new temporary tables for storing migrated data.</li>
     *     <li>Migrates data from old tables to the newly created temporary tables.</li>
     *     <li>Drops old tables that are being replaced.</li>
     *     <li>Renames temporary tables to match the new schema.</li>
     *     <li>Removes unused sequences related to the old schema.</li>
     *     <li>Commits the transaction if all operations are successful.</li>
     * </ul>
     *
     * @return The SQL script string for migrating to version 0.5.1.
     */
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

    /**
     * Gets the migration query for version 0.7.
     *
     * This query script adds the capability for quest inventories.
     * <ul>
     *     <li>Begins a transaction to ensure atomicity of the operations.</li>
     *     <li>Adds the inventory column to the quests table.</li>
     * </ul>
     *
     * @return The SQL script string for migrating to version 0.7.
     */
    public static String dbV0_7() {
        return """
            -- Begin a transaction to ensure all operations are atomic
            BEGIN TRANSACTION;

            ALTER TABLE quests ADD COLUMN inventory TEXT NOT NULL DEFAULT "{ }"  ;

            -- Commit the transaction if everything is successful
            COMMIT;
        """;
    }

    /**
     * Gets the migration query for version 0.8.
     *
     * This query removes db relics of the old way quest actions
     * were implemented.
     *
     * @return The SQL script string for migrating to version 0.8.
     */
    public static String dbV0_8() {
        return """
            -- Begin a transaction to ensure all operations are atomic
            BEGIN TRANSACTION;

            DROP TABLE diary_quests;

            -- Commit the transaction if everything is successful
            COMMIT;
        """;
    }

    public static String dbV0_10() {
        return """
            BEGIN TRANSACTION;

            ALTER TABLE plugin ADD COLUMN citizens2 BOOLEAN NOT NULL DEFAULT FALSE;

            COMMIT;
        """;
    }
}
