import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.sound.sampled.*;

import org.apache.commons.io.FileUtils;
import org.json.simple.*;

/*
 * Paulkia 2020.
 * A collection of general final values and functions used throughout the game.
 * All other classes extend this universal class.
 */

class Fakeyverse {

    // A map from all item names (Strings) to item properties (Items, specified in DATA_DIR/Item.java)
    static final Map<String, Item> GAME_ITEMS = initItems();
    // A map from all attack names (Strings) to attack properties (Attacks, specified in DATA_DIR/Attack.java)
    static final Map<String, Attack> GAME_ATKS = initAtks();
    // An ArrayList containing all Monster signatures as Monster objects (data specified in DATA_DIR/Fakeydex.json)
    static final List<Monster> GAME_MONS = initMons();

    static final String[] STAT_TYPE = new String[]{"HP", "attack", "defense", "speed"}, // Stat types in game
            STAT_CHANGE_ADJ = new String[]{"", " sharply", " drastically"}; // Adjectives when stats are boosted

    static final int HP = 0, ATK = 1, DEF = 2, SPE = 3, // Stat type indices
            ADJ_DIVISOR = 30; // Stat change adjective chooser; divide effect by 30 to get desired adjective.

    static final String
            // Local directory paths.
            DATA_DIR = "data/",
            SAVE_DIR = DATA_DIR + "saves/",
            MUSIC_DIR = DATA_DIR + "music/",
            SFX_DIR = DATA_DIR + "sfx/",
    // Strings that help to format the game in an attractive way. Frequently used when printing.
    BAR = "---------",
            NL = "\n",
            DIVIDER = "-------------------------------------------------------------------\n",
    // File names of various sound effects.
    DAMAGE_SFX = "damage",
            ENTER_SFX = "enter",
            ESC_SFX = "esc",
            HEAL_SFX = "heal",
            ITEM_SFX = "item",
            STAT_BOOST_SFX = "stat_boost",
    // File names of various .wav songs.
    CENTER_MUSIC = "center",
            CEUS_MUSIC = "ceus",
            CEUS2_MUSIC = "ceus2",
            MENU_MUSIC = "menu",
            SCENARIO_MUSIC = "scenario",
            BATTLE_MUSIC = "wild",
            HEAL_MUSIC = "healyhoo",
    // Name of held item after Fakeyceus evolves
    STARTER_ATK = "Soft Slap",
            FAKEYCEUS_ITEM = "Whistle-hoo";

    static final int
            // General globals
            START_XP = 96, // Amount of XP given to player at the start.
            NUM_STATS = STAT_TYPE.length, // Number of stats per Fakeymon
            DEFAULT_STAT = 1, // Minimum/default stat for each mon when initializing stats.
            FAKEYCENTER_KILLS = 2, // User finds a Fakeycenter once every FAKEYCENTER_KILLS kills.
            MAX_CLERK_ITEMS = 6, // Max number of items a clerk can hold in a Fakeymart
            FAKEYCEUS_KILLS = 10, // Number of kills before Fakeyceus appears
            SHORT_TIME = 600, // 600 milliseconds pause time
            MED_TIME = 900, // ... pause time
    // Probability variables
    DROP_RATE = 100, // Probability of enemy mon dropping held item after battle if enemy.holdItem != null
            STEAL_CHANCE = 65, // Probability of successfully stealing from Fakeymart
            MAX_CASH = 15, // Max cash an enemy mon can hold in the first battle; used as a multiplier as kills increase
            HOLD_ITEM_PROBABILITY = 85, // Chance of enemy mon holding an item
            JOIN_TEAM = 50, // Chance of enemy deciding to join our team
            NEW_MOVE = 65, // % Chance of learning new move after investing all XP
            DEFAULT_BAG = 3, // Default bag size for new player
    // Battle variables
    MIN_ENEMY_XP = 4 - NUM_STATS * DEFAULT_STAT, // Minimum extra XP an enemy can have
            FIRST_MONS = 3, // First N mons in the Fakeydex that can be found within the first battle
            ENEMY_XP_RATE = 25, // Rate at which each enemies become stronger (per battle)
            DEFAULT_MULTIPLIER = 2, // Default damage multiplier when calculating attack damage
            STRUGGLE_RANGE = 25, // A mon will use any usable held item if mon.tempStats[HP] <= STRUGGLE_RANGE
            CRIT_CHANCE = 10, // % Chance of a crit in battle
            CRIT_MULTIPLIER = 2, // Multiplies DEFAULT_MULTIPLIER by this when a critical hit
            EVADE_CHANCE = 5, // % Chance of partial dodge in battle
            EVADE_MULTIPLIER = 2, // Divides DEFAULT_MULTIPLIER by this when a partial dodge
    // Trivial global variables
    ENEMY_FOUND = 35, // Chance of finding enemy per for loop iteration when searching for enemies
            SAVE_DOTS = 3, // Number of dots printed when [ Autosaving... ]
            SEARCH_TIME = 5, // Number of times a search is attempted before "No enemies found... > ",
            TEXT_TIME = 6;

    static List<String> OLD_LADY_TEXT = new ArrayList<>(Arrays.asList(
            "I wouldn't try stealing from the clerk if I were you. You have a " + STEAL_CHANCE + "% chance of successfully stealing,\n" +
                    "but otherwise the guards confiscate all your items and you get kicked out! Believe me, I learned the hard way.",
            "There is a " + JOIN_TEAM + "% chance of an enemy joining your team after you kill it.",
            "There is a " + CRIT_CHANCE + "% chance of an attack resulting in a critical hit, doubling damage.",
            "There is a " + EVADE_CHANCE + "% chance of an attack being partially evaded, halving damage.",
            "A Fakeymon has a " + NEW_MOVE + "% chance of learning a new move each time it gains XP.",
            //"If you invest in HP, your temporary HP also rises. It's a good way to temporarily heal a Fakeymon.",
            "All wild Fakeymon names are anagrams of Pokémon names except Fakeyceus.",
            "In Fakeymon, I was implemented on 2/21/2018. You are currently playing Fakeymon2, in which I was implemented on 9/20/2020.\n" +
                    "Gosh what a horrible year."
    ));

    static final Random RANDOM = new Random(); // Global random object used throughout the game
    private static final Scanner SCAN = new Scanner(System.in); // Global scanner to interpret user input

    static Clip music, sfx; // Clip objects managing sound throughout the game.

    /**
     * Initializes GAME_ITEMS by generating a map that maps items from their String name to their Item data.
     * Takes in data from DATA_DIR/ItemData.json.
     *
     * @return TreeMap containing
     */
    private static Map<String, Item> initItems() {
        Map<String, Item> result = new TreeMap<>();
        String saveData = "";
        try {
            saveData = FileUtils.readFileToString(new File(DATA_DIR + "ItemData.json"), "UTF-8");
        } catch (IOException io) {
            io.printStackTrace();
            System.exit(0);
        }
        List<JSONObject> itemData = (ArrayList<JSONObject>) ((JSONObject) JSONValue.parse(saveData)).get("items");
        for (JSONObject obj : itemData)
            result.put(obj.get("name").toString(), new Item(obj));
        return result;
    }

    /**
     * Initializes GAME_ATKS by generating a map that maps attacks from their String name to their Attack data.
     * Takes in data from DATA_DIR/AttackData.json.
     *
     * @return TreeMap containing
     */
    private static Map<String, Attack> initAtks() {
        Map<String, Attack> result = new TreeMap<>();
        String saveData = "";
        try {
            saveData = FileUtils.readFileToString(new File(DATA_DIR + "AttackData.json"), "UTF-8");
        } catch (IOException io) {
            io.printStackTrace();
            System.exit(0);
        }
        List<JSONObject> atkData = (ArrayList<JSONObject>) ((JSONObject) JSONValue.parse(saveData)).get("attacks");
        for (JSONObject obj : atkData)
            result.put(obj.get("name").toString(), new Attack(obj));
        return result;
    }

    /**
     * Initializes GAME_MONS by generating a List containing data of all Fakeymon from the Fakeydex
     * Takes in data from DATA_DIR/Fakeydex.json.
     *
     * @return List of Fakeymon, sorted by ID.
     */
    private static List<Monster> initMons() {
        List<Monster> result = new ArrayList<>();
        String fakeyDex = "";
        try {
            fakeyDex = FileUtils.readFileToString(new File(DATA_DIR + "Fakeydex.json"), "UTF-8");
        } catch (IOException io) {
            io.printStackTrace();
            System.exit(0);
        }
        List<JSONObject> monData = (ArrayList<JSONObject>) ((JSONObject) JSONValue.parse(fakeyDex)).get("Fakeydex");
        for (JSONObject obj : monData)
            result.add(new Monster(obj));
        return result;
    }

    // Battle functions

    /**
     * Returns how many HP points a Fakeymon should restore given the input variables.
     *
     * @param tempHP         - The current HP stat of the Fakeymon.
     * @param maxHP          - The max possible HP stat of the Fakeymon.
     * @param hpRestoreValue - A string containing an integer or an integer + % to determine the quantity of HP
     *                       to restore.
     * @return The amount of HP that should be restored.
     */
    static int healMon(int tempHP, int maxHP, String hpRestoreValue) {
        int hpGain;
        if (!hpRestoreValue.contains("%"))
            hpGain = Integer.parseInt(hpRestoreValue);
        else {
            int percentValue = Integer.parseInt(hpRestoreValue.substring(0, hpRestoreValue.length() - 1));
            double percentGain = percentValue / 100.0;
            hpGain = (int) Math.round(maxHP * percentGain);
        }
        return tempHP >= maxHP ? 0 : Math.min(hpGain, maxHP - tempHP);
    }

    /**
     * Returns the integer value for which a stat should temporarily increase (most likely due to an attack).
     *
     * @param mon          - The monster that should receive a temporary stat increase.
     * @param statIndex    - The index of the stat that should increase. Often specified by HP, ATK, DEF, or SPE.
     * @param percentBoost - The percent increase of the stat.
     * @return The change in the given stat on the given mon specified by the given percent.
     */
    static int statBoost(Monster mon, int statIndex, double percentBoost) {
        double boostValue = percentBoost / 100;
        return (int) Math.round(mon.stats[statIndex] * boostValue);
    }

    /**
     * Given an enemy monster, returns the XP gain that a winning monster should receive.
     *
     * @param mon - The defeated enemy whose stats are being read.
     * @return The amount of XP the winning monster should receive.
     */
    static int generateXp(Monster mon) {
        int defHP = (mon.stats[HP] + mon.stats[DEF]) / 2;
        return defHP + RANDOM.nextInt(defHP / 2);
    }

    /**
     * Chooses an item at random in GAME_ITEMS based on item drop rates (lower rate items will be more rare).
     *
     * @return The name of the item that was generated.
     */
    static String generateItem() {
        String item = null;
        int location = RANDOM.nextInt(100);
        for (Item i : GAME_ITEMS.values()) {
            location -= i.dropRate;
            if (location < 0) {
                item = i.name;
                break;
            }
        }
        return item;
    }

    // Misc

    /**
     * The message displayed when a player attempts to escape from the final battle.
     *
     * @throws Exception When there is an issue running the ENTER_SFX Clip (discussed in comments in music(String)),
     *                   or Thread.sleep throws InterruptedException (from p(String)).
     */
    static void escCeus2Msg() throws Exception {
        p("There's no escaping a god. > ");
        enter();
    }

    /**
     * Plays a specified song.
     *
     * @param songName - The name of the music file to be continuously looped (without the .wav extension).
     * @throws Exception When the specified wav file is of an unsupported format or the system's audio device cannot
     *                   be accessed.
     */
    static void music(String songName) throws Exception {
        if (music != null) music.stop();
        AudioInputStream sound = AudioSystem.getAudioInputStream(new File(MUSIC_DIR + songName + ".wav"));
        music = AudioSystem.getClip();
        music.open(sound);
        music.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     * Plays a specified sound effect.
     *
     * @param sfxName - The name of the sound effect file to be run (without the .wav extension).
     * @throws Exception When the specified wav file is of an unsupported format or the system's audio device cannot
     *                   be accessed.
     */
    static void sfx(String sfxName) throws Exception {
        AudioInputStream sound = AudioSystem.getAudioInputStream(new File(SFX_DIR + sfxName + ".wav"));
        sfx = AudioSystem.getClip();
        sfx.open(sound);
        sfx.start();
    }

    /**
     * Takes in a line of user input using SCAN and returns it (trimmed). Plays the ENTER_SFX sound effect.
     *
     * @return The input string, trimmed, from the user.
     * @throws Exception When ENTER_SFX is unsupported or the system's audio device cannot be accessed.
     */
    static String enter() throws Exception {
        String result = SCAN.nextLine().trim();
        sfx(ENTER_SFX);
        return result;
    }

    /**
     * Called whenever user input is needed. Takes the first token in a line of user input and returns it.
     * Asks for the user to confirm input. If the user confirms, the input is returned; else repeatMsg is printed.
     *
     * @param repeatMsg - The message that should be repeated to the user if the user provides input that the user
     *                  is not satisfied with.
     * @return The first token from the user input.
     * @throws Exception if Thread.sleep is interrupted (from method p(String)) or there is an issue with running
     *                   the ENTER_SFX clip
     */
    static String input(String repeatMsg) throws Exception {
        while (true) {
            String input = enter();
            Scanner inputScanner = new Scanner(input);
            if (inputScanner.hasNext()) {
                String token = inputScanner.next();
                p("Please confirm that you would like to use the word " + token + ". (yes/no) --> ");
                if (confirm())
                    return token;
                else p(repeatMsg);
            }
        }
    }

    /**
     * Called whenever user input is needed. Takes the line of user input and returns it.
     * Asks for the user to confirm input. If the user confirms, the input is returned; else repeatMsg is printed.
     *
     * @param repeatMsg - The message that should be repeated to the user if the user provides input that the user
     *                  is not satisfied with.
     * @return The user input, trimmed by the enter() method.
     * @throws Exception under the same conditions as input(String).
     */
    static String inputl(String repeatMsg) throws Exception {
        while (true) {
            String input = enter();
            if (!input.isEmpty()) {
                p("Please confirm that you would like to use the word/phrase " + input + ". (yes/no) --> ");
                if (confirm())
                    return input;
                else p(repeatMsg);
            }
        }
    }

    /**
     * An infinite loop that only returns a value once the user inputs (ignoring case) Strings "yes" or "no".
     * Otherwise prints "Invalid input. Please try again. --> ".
     *
     * @return Whether the user inputted "yes".
     * @throws Exception under the same conditions as input(String).
     */
    static Boolean confirm() throws Exception {
        while (true) {
            String confirm = enter();
            if (!confirm.isEmpty()) {
                if (confirm.equalsIgnoreCase("yes"))
                    return true;
                else if (confirm.equalsIgnoreCase("no"))
                    return false;
                else
                    p("Invalid input. Please try again. --> ");
            }
        }
    }

    /**
     * An infinite loop that only ends once the user has provided a valid input integer from ints min to max, inclusive.
     * If boolean cancel is true, then the character X is also a valid response.
     *
     * @param min    - The minimum valid input from the user. This value should always be strictly positive.
     * @param max    - The maximum valid input from the user.
     * @param cancel - Whether (ignoring case) X is a valid input from the user.
     * @return A user integer input from min to max, minus 1. Returns -1 if the user input was 'X' and cancel is true.
     * @throws Exception for the same reasons as input(String).
     */
    static int option(int min, int max, boolean cancel) throws Exception {
        while (true) {
            String input = enter();
            if (input.equalsIgnoreCase("X") && cancel)
                return -1;
            if (!input.isEmpty()) {
                try {
                    int option = Integer.parseInt(input); // Exception possible if bad user input.
                    if (option < min || option > max)
                        throw new Exception();
                    return option - 1;
                } catch (Exception e) {
                    p("Invalid input. Please try again.\n--> ");
                }
            }
        }
    }

    /**
     * Given a String array and a String key, uses O(N) search to find whether the key (ignoring case) exists in the
     * array.
     *
     * @param arr - The array being scanned.
     * @param key - The String being searched for in arr.
     * @return -1 if the array is null or the key is not found in the array. Otherwise, it returns the index in which
     * the key lies in the array.
     */
    static int indexOf(String[] arr, String key) {
        if (arr == null) return -1;
        for (int i = 0; i < arr.length; i++)
            if (arr[i].equalsIgnoreCase(key))
                return i;
        return -1;
    }

    /**
     * Given a JSONObject and a String key, returns the String value resulting from that key.
     *
     * @param key        - The key in the key-value pair for the value being searched for.
     * @param jsonObject - The JSON object containing the data being searched for.
     * @return null if the value is not found. Otherwise, it returns the value.
     */
    static String getStrFromJSON(String key, JSONObject jsonObject) {
        Object result = jsonObject.get(key);
        return result == null ? null : result.toString();
    }

    /**
     * Same as getStrFromJSON but with a String array as a value.
     *
     * @param key        - The key in the key-value pair for the value being searched for.
     * @param jsonObject - The JSON object containing the data being searched for.
     * @return null if the value is not found. Otherwise, it returns the value.
     */
    static String[] getStrArrFromJSON(String key, JSONObject jsonObject) {
        if (jsonObject.get(key) == null)
            return null;
        Object[] arr = ((List<String>) jsonObject.get(key)).toArray();
        String[] result = new String[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = (String) arr[i];
        return result;
    }

    /**
     * Same as getStrFromJSON but with a Integer as a value.
     *
     * @param key        - The key in the key-value pair for the value being searched for.
     * @param jsonObject - The JSON object containing the data being searched for.
     * @return null if the value is not found. Otherwise, it returns the value.
     */
    static Integer getIntFromJSON(String key, JSONObject jsonObject) {
        Object result = jsonObject.get(key);
        return result == null ? null : ((Long) result).intValue();
    }

    /**
     * Same as getIntFromJSON but with a Integer array as a value.
     *
     * @param key        - The key in the key-value pair for the value being searched for.
     * @param jsonObject - The JSON object containing the data being searched for.
     * @return null if the value is not found. Otherwise, it returns the value.
     */
    static Integer[] getIntArrFromJSON(String key, JSONObject jsonObject) {
        if (jsonObject.get(key) == null)
            return null;
        Object[] arr = ((List<Integer>) jsonObject.get(key)).toArray();
        Integer[] result = new Integer[arr.length];
        for (int i = 0; i < arr.length; i++)
            result[i] = arr[i] == null ? null : ((Long) arr[i]).intValue();
        return result;
    }

    /**
     * Same as getIntFromJSON but with a Boolean as a value.
     *
     * @param key        - The key in the key-value pair for the value being searched for.
     * @param jsonObject - The JSON object containing the data being searched for.
     * @return null if the value is not found. Otherwise, it returns the value.
     */
    static Boolean getBoolFromJSON(String key, JSONObject jsonObject) {
        Object result = jsonObject.get(key);
        return result == null ? null : Boolean.parseBoolean(result.toString());
    }

    /**
     * Pauses the thread for millis milliseconds.
     *
     * @param millis - Number of milliseconds for which the program should be paused.
     * @throws InterruptedException if Thread.sleep(millis) is interrupted.
     */
    static void pause(int millis) throws InterruptedException {
        Thread.sleep(millis);
    }

    /**
     * Takes in a String and returns the same string with the first char being lowercase.
     *
     * @param str - The input string.
     * @return The modified string with first char being lowercase.
     */
    static String lowercase(String str) {
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * Takes in a String str and returns the str capitalized.
     *
     * @param str - The string to be capitalized.
     * @return The capitalized version of str.
     */
    static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Prints the given message with TEXT_TIME milliseconds of pause time between each character.
     *
     * @param message - The message to be printed to the console.
     * @throws InterruptedException if Thread.sleep is interrupted when pausing the thread in between characters.
     */
    static void p(Object message) throws InterruptedException {
        String msg = message.toString();
        for (int i = 0; i < msg.length(); i++) {
            System.out.print(msg.charAt(i));
            Thread.sleep(TEXT_TIME);
        }
    }

}
