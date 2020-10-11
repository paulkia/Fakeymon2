import java.io.*;
import java.util.*;

import org.json.simple.*;
import org.apache.commons.io.FileUtils;

import javax.sound.sampled.Clip;

/*
 * Paulkia 2020.
 * The object handling all of the game's scenarios and actions.
 */
public class Game extends Fakeyverse {

    private static Player user; //
    // Main processes/scenarios

    /**
     * Main method.
     *
     * @param args (main method)
     * @throws Exception if intro(), newGame(), Fakeyverse.music(String), or gameLoop() throw an exception.
     */
    public static void main(String[] args) throws Exception {
        user = null;
        music(MENU_MUSIC);
        p("\n\n\t\t\t███████   █████   ██   ██  ███████  ██    ██  ███    ███   ██████   ███    ██ \n" +
                "\t\t\t██       ██   ██  ██  ██   ██        ██  ██   ████  ████  ██    ██  ████   ██\n" +
                "\t\t#\t█████    ███████  █████    █████      ████    ██ ████ ██  ██    ██  ██ ██  ██\t#\n" +
                "\t\t\t██       ██   ██  ██  ██   ██          ██     ██  ██  ██  ██    ██  ██  ██ ██\n" +
                "\t\t\t██       ██   ██  ██   ██  ███████     ██     ██      ██   ██████   ██   ████ \n\n");
        while (true)
            if (intro() || newGame()) break;
        music(SCENARIO_MUSIC);
        gameLoop();
    }

    /**
     * Intro sequence. Prints the ascii banner and provides options to the player to begin the game.
     * 1 Starts a new game
     * 2 Loads a save file
     * 3 Prints credits
     * X Quits the game
     *
     * @return True if a save file is loaded, false otherwise.
     * @throws Exception if p(String), option(int, int, boolean), confirm(), pause(int), enter(), inputl(String),
     *                   credits(), quit() throw an exception.
     */
    private static boolean intro() throws Exception {
        while (true) {
            p(DIVIDER);
            p("Type '1' to start a new game, '2' to open a save file, '3' to view credits, " +
                    "or 'X' to quit the game.\n--> ");
            int choice = option(1, 3, true);
            if (choice == 0) {
                p("Are you sure you would like to start a new game? (yes/no) --> ");
                if (confirm()) {
                    user = new Player();
                    p("Starting new game!\n");
                    pause(SHORT_TIME);
                    break;
                }
            } else if (choice == 1) {
                while (true) {
                    p("What is the username of the save file you would like to load? " +
                            "(Write 'cancel option' to cancel.) --> ");
                    String input = inputl("Please type the username of the user you would like to load, " +
                            "or 'cancel option' to cancel. --> ");
                    File saveFile = new File(SAVE_DIR + input.toLowerCase() + ".json");
                    if (input.equalsIgnoreCase("cancel option"))
                        break;
                    else if (saveFile.exists()) {
                        try {
                            loadFile(saveFile); // Possible exception from Player(JSONObject) constructor.
                            return true;
                        } catch (Exception e) {
                            e.printStackTrace();
                            p("This file data is corrupted. The game is no longer playable.\n" +
                                    "Please choose a different save file or start a new game. >");
                            enter();
                            p(DIVIDER);
                        }
                    } else {
                        p("That user was not found. > ");
                        enter();
                    }
                }
                if (user != null)
                    break;
            } else if (choice == 2) {
                credits();
            } else {
                p("Are you sure you would like to quit the game? (yes/no) --> ");
                if (confirm())
                    quit();
                else p(DIVIDER);
            }
        }
        return false;
    }

    /**
     * Sequence if the player decides to start a new game.
     * Here, a player chooses their name and generates their starter Fakeymon. Gives the starter START_ATK as its
     * first attack.
     *
     * @return True if entire newGame() sequence is completed (creating a new player), false if it is interrupted.
     * @throws Exception if enter(), input(String), p(String), option(int, int, boolean), confirm() throw an exception.
     */
    private static boolean newGame() throws Exception {
        p(DIVIDER
                + "Whenever you see a '>' character at the end of a statement, please press enter to continue. >");
        enter();
        p(DIVIDER);
        p("What is your name? (Cannot contain \\/:*?\"<>|) --> ");
        while (true) {
            String invalid = "\\/:*?\"<>|";
            String input = input("Please choose a valid username. (Cannot contain \\/:*?\"<>|) --> ");
            for (int i = 0; i < invalid.length(); i++) {
                if (input.indexOf(invalid.charAt(i)) != -1)
                    input = "";
            }
            if (!input.equals("")) {
                user.name = input;
                break;
            } else p("That username is invalid. Please choose a valid username. (Cannot contain \\/:*?\"<>|) --> ");
        }

        if (new File(SAVE_DIR + user.name.toLowerCase() + ".json").exists()) {
            p("This user already exists. Are you sure you would like to overwrite this save file? (yes/no) --> ");
            if (!confirm()) {
                return false;
            }
        }

        p("You get to begin the game with a starter! What would you like to name it? --> ");
        String starterName = input("Please choose a name for your starter. --> ");
        Monster mon = new Monster(starterName);
        user.team.add(mon);

        p(DIVIDER +
                "You get to start " + starterName + " off with a total of " + (START_XP + NUM_STATS * DEFAULT_STAT)
                + " experience points! >");
        enter();
        p("You may choose what " + starterName + "'s stats are! " +
                "The four categories are: hit points, attack, defense, and speed. >");
        enter();
        p(DIVIDER);
        mon.xp = START_XP;

        boolean dissatisfied = true;
        while (dissatisfied) {
            int tempXp = mon.xp;
            Integer[] stats = mon.stats;
            Integer[] tempStats = Arrays.copyOf(stats, stats.length);
            int[] statChanges = new int[NUM_STATS];

            for (int i = 0; i < NUM_STATS - 1 && tempXp > 0; i++) {
                p(tempXp + " XP remaining. How many " + STAT_TYPE[i] + " points would you like to invest for "
                        + mon.name + "? (Current " + STAT_TYPE[i] + ": " + tempStats[i] + ") --> ");
                int statChange = option(0, tempXp, false) + 1;
                tempStats[i] += statChange;
                statChanges[i] = statChange;
                tempXp -= statChange;
            }
            statChanges[NUM_STATS - 1] = tempXp;
            tempStats[NUM_STATS - 1] += tempXp;
            p("Observe the following stat changes.\n");
            for (int i = 0; i < NUM_STATS; i++) {
                p("-\t" + capitalize(STAT_TYPE[i]) + " (+" + statChanges[i] + ") : " + stats[i] + " --> "
                        + tempStats[i] + "\n");
            }
            p("Do you accept these new stats for " + mon.name + "? (yes/no) --> ");
            if (confirm()) {
                mon.stats = tempStats;
                for (int i = 0; i < statChanges.length; i++)
                    mon.tempStats[i] += statChanges[i];
                mon.xp = 0;
                dissatisfied = false;
            }
            p(DIVIDER);
        }

        mon.attacks.add(STARTER_ATK);
        p(starterName + " is now ready to fight!\n" +
                "Fakeymon is all about using your pet monsters to fight in battle as a tool, " +
                "until they all die or you use them to defeat Fakeyceus. >");
        enter();
        return true;
    }

    /**
     * Takes in a file and attempts to load user data from it.
     *
     * @param file - The file object containing user information.
     * @throws Exception if FileUtils.readFileToString(File, String), p(String), enter() throw an exception.
     */
    private static void loadFile(File file) throws Exception {
        String saveData = FileUtils.readFileToString(file, "UTF-8");
        JSONObject userData = (JSONObject) JSONValue.parse(saveData);
        user = new Player(userData);
        p("\n\t Welcome back, " + user.name + "! > ");
        enter();
    }

    /**
     * A loop that is constantly repeated until the program exits. The player is given certain options each iteration.
     * 1 allows the user to engage in battle through battle().
     * 2 allows the user to view the stats of a Fakeymon through monStatus().
     * 3 allows the user to open their bag through openBag().
     * X allows the user to quit the game.
     *
     * @throws Exception if save(), p(String), option(int, int, boolean), battle(), monStatus(), openBag(), confirm(),
     *                   or quit() throw an exception.
     */
    private static void gameLoop() throws Exception {
        while (true) {
            save();
            p(DIVIDER +
                    "Please type '1' to search for enemies, '2' to view current status of a Fakeymon,\n" +
                    "'3' to open your bag, or 'X' to quit the game. \n--> ");
            int choice = option(1, 3, true);
            if (choice == 0) battle();
            else if (choice == 1) monStatus();
            else if (choice == 2) openBag();
            else {
                p("Are you sure you would like to quit the game? (yes/no) --> ");
                if (confirm())
                    quit();
            }
        }
    }

    /**
     * Manages battles against enemy mons. See more in Battle.java.
     *
     * @throws Exception if p(String), pause(int), enter(), or Battle(Player, boolean) throw an exception.
     *                   See Battle.java.
     */
    private static void battle() throws Exception {
        boolean fakeyceus = FAKEYCEUS_KILLS <= user.kills, enemyFound = fakeyceus;
        for (int i = 0; i < SEARCH_TIME && !enemyFound; i++) {
            p("Searching for enemies...\n");
            pause(SHORT_TIME);
            if (RANDOM.nextInt() < ENEMY_FOUND)
                enemyFound = true;
        }
        if (enemyFound) {
            new Battle(user, fakeyceus);
        } else {
            p("No enemies found... > ");
            enter();
        }
    }

    // GameLoop options

    /**
     * Displays a mon's status. Provides different options to the user.
     * 1 allows the user to put this mon at the front of the party so the mon begins the next battle.
     * 2 allows the user to rename the mon.
     * 3 allows the user to give or take an item from the mon.
     * X allows the user to cancel.
     *
     * @throws Exception if chooseMon(String, boolean), p(String), option(int, int, boolean), enter(), inputl(String),
     *                   confirm(), printBag() throw an exception.
     */
    static void monStatus() throws Exception {
        int monIndex = user.team.size() < 2 ?
                0 :
                chooseMon("Which Fakeymon's status would you like to view?\n", true);
        if (monIndex != -1) {
            System.out.print((user.team.get(monIndex).print()));
            Monster mon = user.team.get(monIndex);
            while (true) {
                p("Type '1' to put " + mon.name + " at the front of your party, " +
                        "'2' to rename " + mon.name + ", '3' to " +
                        (mon.holdItem == null ? "give " + mon.name + " an" : "take " + mon.name + "'s") +
                        " item, or 'X' to exit.\n--> ");
                int action = option(1, 3, true);
                if (action == 0) {
                    if (user.team.get(0).equals(mon))
                        p(mon.name + " is already at the front of your party. > ");
                    else {
                        user.team.remove(mon);
                        user.team.add(0, mon);
                        p(mon.name + " is now at the front of your party. > ");
                    }
                    enter();
                } else if (action == 1) {
                    String oldName = mon.name;
                    p("What would you like to rename " + oldName + " to? (Write 'cancel option' to cancel.) --> ");
                    String newName = inputl("Please provide a new name for " + oldName + ", " +
                            "or write 'cancel option' to cancel. --> ");
                    if (!newName.equalsIgnoreCase("cancel option")) {
                        mon.name = newName.split(" ")[0];
                        p("The name " + oldName + " for this Fakeymon has been changed to " + mon.name + ". > ");
                        enter();
                    }
                } else if (action == 2)
                    if (mon.holdItem != null) {
                        if (user.bag.size() < user.bagSize) {
                            String item = mon.holdItem;
                            mon.holdItem = null;
                            user.bag.add(item);
                            p("Took the " + item + " from " + mon.name + ". > ");
                            enter();
                        } else {
                            p("Your bag is full. Would you like to swap the " + mon.holdItem +
                                    " with some other item in your bag?\n(yes/no) --> ");
                            if (confirm()) {
                                printBag();
                                p("Which item would you like to give " + mon.name + " instead of the " + mon.holdItem +
                                        "?\n('X' to cancel.) --> ");
                                int option = option(1, user.bagSize, true);
                                if (option != -1) {
                                    String heldItem = mon.holdItem;
                                    mon.holdItem = user.bag.remove(option);
                                    user.bag.add(heldItem);
                                    p("Took the " + heldItem + " from " + mon.name + " and handed it a(n) " +
                                            mon.holdItem + ". > ");
                                    enter();
                                }
                            }
                        }
                    } else if (user.bag.size() == 0) {
                        p("You have no items. > ");
                        enter();
                    } else {
                        printBag();
                        p("Which item would you like to give " + mon.name + "? ('X' to cancel.) --> ");
                        int option = option(1, user.bagSize, true);
                        if (option != -1) {
                            mon.holdItem = user.bag.remove(option);
                            p("Handed the " + mon.holdItem + " to " + mon.name + ". > ");
                            enter();
                        }
                    }
                else break;
            }
        }
    }

    /**
     * Allows a user to view their bag. From there, they may select an item or X to cancel. Once an item is selected,
     * they are given a few options.
     * 1 allows the user to use an item, using variations Item.use(...)
     * 2 allows the user to give the item to a Fakeymon.
     * 3 allows the user to trash the item.
     * X allows the user to cancel.
     *
     * @throws Exception if p(String), enter(), printBag(), option(int, int, boolean), chooseMon(String, boolean),
     *                   Item.use(Player) throw an exception.
     */
    private static void openBag() throws Exception {
        if (user.bag.size() == 0) {
            p("You have no items. > ");
            enter();
        } else while (true) {
            printBag();
            p("Type the index of the item you would like to select, or type 'X' to cancel. --> ");
            int index = option(1, user.bagSize, true);
            if (index != -1) {
                if (index >= user.bag.size()) {
                    p("This slot is empty. > ");
                    enter();
                } else {
                    Item item = GAME_ITEMS.get(user.bag.get(index));
                    p("Would you like to '1' use the item, '2' give it to a mon, or '3' trash the item?\n" +
                            "('X' to cancel.) --> ");
                    int choice = option(1, 3, true);
                    if (choice == 0 && usable(item)) {
                        if (item.use % 2 == 1) {
                            int mon = chooseMon("On which mon would you like to use this item?\n", true);
                            if (mon != -1 && item.use(user, user.team.get(mon), true))
                                user.bag.remove(item.name);
                        } else if (item.use(user))
                            user.bag.remove(item.name);
                    } else if (choice == 0) {
                        p("This item cannot be used here. > ");
                        enter();
                    } else if (choice == 1) {
                        int mon = chooseMon("Which mon would you like to hand this item to?\n", true);
                        if (mon != -1) {
                            Monster monster = user.team.get(mon);
                            if (monster.holdItem == null) {
                                user.bag.remove(item.name);
                                monster.holdItem = item.name;
                                p("Handed the " + item.name + " to " + monster.name + ". > ");
                                enter();
                            } else {
                                p(monster.name + " is already holding a " + monster.holdItem +
                                        ". Would you like to swap these items? (yes/no) --> ");
                                if (confirm()) {
                                    String monItem = monster.holdItem;
                                    user.bag.remove(item.name);
                                    monster.holdItem = item.name;
                                    user.bag.add(monItem);
                                }
                            }
                        }
                    } else if (choice == 2) {
                        p("Are you sure you want to trash the " + item.name + "? (yes/no) --> ");
                        if (confirm()) {
                            p("Tossed the " + user.bag.remove(item.name) + ". > ");
                            enter();
                        }
                    }
                }
            } else break;
        }
    }

    /**
     * Returns whether the item is usable given the player's current situation.
     *
     * @param item - The item which we are evaluating.
     * @return False if the item is null or the item can only be used in battle. True otherwise.
     */
    private static boolean usable(Item item) {
        return item != null && item.use != 3 && item.use != 4;
    }

    /**
     * Prints out the user's bag in a clear, human-readable way, and provides indices for the player to select items
     * if desired.
     *
     * @throws Exception if p(String) throws an exception, or Thread.sleep(long) throws an InterruptedException.
     */
    static void printBag() throws Exception {
        p("- - Bag: - -\nBalance: $" + user.money + "\n");
        for (int i = 0; i < user.bagSize; i++) {
            if (i >= user.bag.size())
                System.out.print(i + 1 + ")\t[ empty ]\n");
            else
                System.out.print(i + 1 + ")\t" + GAME_ITEMS.get(user.bag.get(i)).print() + "\n");
            Thread.sleep(TEXT_TIME);
        }
    }

    /**
     * Prints out the message msg, prints out the user's team with names and stats, and allows a user to choose a mon.
     * If cancel is true, then 'X' is a viable choice for a mon, returning 0.
     *
     * @param msg    - The message that should be printed to the user before printing the user's team.
     * @param cancel - Whether the user is allowed to cancel choosing a monster via the input 'X'.
     * @return The index of the mon that the user aims to select.
     * @throws Exception if Thread.sleep(long) throws an InterruptedException, or p(), option(int, int, boolean) throw
     *                   an exception.
     */
    static int chooseMon(String msg, boolean cancel) throws Exception {
        List<Monster> team = user.team;
        p(msg);
        for (int i = 0; i < team.size(); i++) {
            Monster otherMon = user.team.get(i);
            System.out.print("'" + (i + 1) + "' | " + otherMon.name + " (" + otherMon.tempStats[HP] + "/" +
                    otherMon.stats[HP] + " HP, " + otherMon.stats[ATK] + " atk, " +
                    otherMon.stats[DEF] + " def, " + otherMon.stats[SPE] + " speed)\n");
            Thread.sleep(TEXT_TIME);
        }
        if (cancel)
            p("'X' to cancel.\n--> ");
        else p("\n--> ");
        return option(1, team.size(), cancel);
    }

    // Misc

    /**
     * Called in each iteration of gameLoop(). Saves user's data to SAVE_DIR + user.name + .json.
     * Prints [ Autosaving... ]
     *
     * @throws Exception if PrintStream(File) throws a FileNotFoundException, p(String), pause(int) throw an exception.
     */
    private static void save() throws Exception {
        PrintStream saveStream = new PrintStream(new File(SAVE_DIR +
                user.name.toLowerCase() + ".json"));
        saveStream.print(user.toJSON());

        p("\n[ Autosaving");
        for (int i = 0; i < SAVE_DOTS; i++) {
            System.out.print(".");
            pause(SHORT_TIME);
        }
        p(" ]\n");
    }

    /**
     * Prints credits.
     *
     * @throws Exception if p(String) throws an exception.
     */
    private static void credits() throws Exception {
        String[] credits = ("- - - - - - - - - - - - - - - [ Credits ] - - - - - - - - - - - - - - - -\n" +
                "| Developer\t\t//\tPaulkia https://github.com/paulkia\t|\n" +
                "| Assist. Design\t//\tMax\t\t\t\t\t|\n" +
                "| Music (Menu)\t\t//\t@Bulbamike/Bulby https://www.youtube.com/watch?v=KsLbPopWlqM\n" +
                "|\t(Scenario)\t//\tShivy\t\t\t\t\t|\n" +
                "|\t(Wild Mon)\t//\tLiterally just a GBC https://www.youtube.com/watch?v=2ANS0WTejeU&t=16s\n" +
                "|\t(FKYcenter)\t//\tHypePixel.Hoenn https://www.youtube.com/watch?v=HokVYk76-n8\n" +
                "|\t(FKYceus)\t//\tPokémon Company: GSC Legendary Trio\t|\n" +
                "|\t(FKYceus2)\t//\tPokémon Company: N Theme (remix)\t|\n" +
                "| Ascii Art\t\t//\tMaija Haavisto\t\t\t\t|\n" + // Most sprites
                //"|\t\t\t\t\t//\tStinkoman\t\t\t\t\t\t\t|\n" + // Old Fakeyceus
                "|\t\t\t//\tMatheus Faria\t\t\t\t|\n" + //
                "|\t\t\t//\tTyrranos\t\t\t\t|\n" +
                "|\t\t\t//\tDomz Ninja\t\t\t\t|\n" +
                "|\t\t\t//\tGhostOfLegault\t\t\t\t|\n" +
                "|\t\t\t//\tS. Lemmings\t\t\t\t|\n" +
                "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -\n").split("\n");
        for (int i = 0; i < credits.length; i++) {
            System.out.println(credits[i]);
            Thread.sleep(TEXT_TIME);
        }
    }

    /**
     * Prompt when a user quits the program. Thank you message and credits are printed.
     *
     * @throws Exception if p(String), pause(int), credits(), enter() throw an exception.
     */
    static void quit() throws Exception {
        p("Thank you for playing Fakeymon.\n");
        pause(SHORT_TIME);
        credits();
        p("> ");
        enter();
        System.exit(0);
    }

}
