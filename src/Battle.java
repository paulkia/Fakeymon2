import java.util.*;

/*
 * Paulkia 2020.
 * Battle object handles all functions and properties of a battle between two Monster instances.
 */
public class Battle extends Game {
    // The user's data, stored in a Player object
    private Player user;
    // The user's current mon in battle; the enemy in battle
    private Monster mon, enemy;
    // Whether this is the final battle; whether the user has ran away
    private boolean fakeyceus, runAway;

    /**
     * The constructor, taking in the user's data and whether this battle is the final battle.
     *
     * @param user      - The player's data.
     * @param fakeyceus - Whether this fight is the final battle against the boss, Fakeyceus.
     * @throws Exception if start() throws an exception.
     */
    public Battle(Player user, boolean fakeyceus) throws Exception {
        this.user = user;
        this.fakeyceus = fakeyceus;
        start();
    }

    /**
     * The main battle process. Sets up the battle, calls battleLoop() to run the battle, and uses resulting data
     * at the end to update the user and user's team objects.
     *
     * @throws Exception if music(String), p(String), enter(), battleLoop(), endBattle() throw an exception.
     */
    private void start() throws Exception {
        music(fakeyceus ? CEUS_MUSIC : BATTLE_MUSIC);
        enemy = fakeyceus ?
                new Monster(GAME_MONS.get(GAME_MONS.size() - 2), user.kills) :
                new Monster(GAME_MONS.get(RANDOM.nextInt(Math.min(user.kills + FIRST_MONS, GAME_MONS.size() - 1))),
                        user.kills);
        enemy.name = "Wild " + enemy.name;
        if (fakeyceus)
            enemy.holdItem = null;
        mon = user.team.get(0);
        p(DIVIDER);
        System.out.print(enemy.ascii);
        p(NL + DIVIDER);
        p("A " + lowercase(enemy.name) + " appeared! > ");
        enter();
        p("Go, " + mon.name + "! > ");
        enter();
        battleLoop();
        if (enemy.tempStats[HP] == 0) {
            endBattle();
        }
        enemy = null;
        mon.resetBattleStats();
        if (FAKEYCEUS_KILLS > user.kills) {
            p("Fakeyceus is " + (FAKEYCEUS_KILLS - user.kills) + " step(s) away... > ");
            enter();
        }
    }

    /**
     * Loops continuously until the enemy has no more HP or the user runs away. If all of the user's mons die,
     * System.exit(0) is called.
     *
     * @throws Exception if p(String), option(int, int, boolean), chooseAttack(), enter(), changeMon(), openBag(),
     *                   tryExcape(), deadMon(), ceusEvolve() throw an exception.
     */
    private void battleLoop() throws Exception {
        while (enemy.tempStats[HP] > 0 && !runAway) {
            p(DIVIDER);
            p(enemy.printLimited());
            p(DIVIDER);
            p("What will " + mon.name + " do?\nType '1' to choose an attack, '2' to see stats, " +
                    "'3' to use a different mon, '4' to open your bag,\nor 'X' to attempt to run. --> ");
            int battleOption = option(1, 4, true);
            if (battleOption == 0) chooseAttack();
            else if (battleOption == 1) {
                System.out.print(user.team.get(0).print());
                p("> ");
                enter();
            } else if (battleOption == 2) changeMon();
            else if (battleOption == 3) openBag();
            else if (tryEscape()) runAway = true;
            if (mon.tempStats[HP] == 0)
                deadMon();
            if (enemy.tempStats[HP] == 0 && fakeyceus && !user.finalBattle)
                ceusEvolve();
        }
    }

    /**
     * Lists all of mon's attacks to the user. The user is given the option to choose an attack to use or cancel.
     *
     * @throws Exception if Thread.sleep(String) throws an InterruptedException, or if p(String),
     *                   option(int, int, boolean), Monster.attack(Monster), Monster.attack(int, Monster),
     *                   checkItems(Monster, Monster) throw an exception.
     */
    private void chooseAttack() throws Exception {
        p(BAR + " [ Attacks ] " + BAR + NL);
        for (int i = 1; i <= mon.attacks.size(); i++) {
            System.out.print("'" + i + "' | " + GAME_ATKS.get(mon.attacks.get(i - 1)).print() + "\n");
            Thread.sleep(TEXT_TIME);
        }
        p("'X' to cancel.\n--> ");
        int attackOption = option(1, mon.attacks.size(), true);
        if (attackOption != -1) {
            if (mon.tempStats[SPE] > enemy.tempStats[SPE]) {
                mon.attack(attackOption, enemy);
                if (enemy.tempStats[HP] > 0)
                    enemy.attack(mon);
            } else {
                enemy.attack(mon);
                if (mon.tempStats[HP] > 0)
                    mon.attack(attackOption, enemy);
            }
            if (mon.tempStats[HP] > 0)
                checkItems(mon, enemy);
            if (enemy.tempStats[HP] > 0)
                checkItems(enemy, mon);
        }

    }

    /**
     * If the user has no other mons, states 'You have no other mons. > "
     * Else lists the user's team. The user may select a different mon to use for the remainder of the battle or cancel.
     *
     * @throws Exception if p(String), enter(), option(int, int, boolean), Monster.attack(Monster) throw an exception.
     */
    private void changeMon() throws Exception {
        if (user.team.size() == 1) {
            p("You have no other mons. > ");
            enter();
        } else {
            p("Which mon would you like to use?\n"); // Can't use chooseMon since one is in battle
            for (int i = 1; i < user.team.size(); i++) {
                Monster otherMon = user.team.get(i);
                p("'" + i + "' | " + otherMon.name + " (" + otherMon.tempStats[HP] + "/" +
                        otherMon.stats[HP] + " HP, " + otherMon.stats[ATK] + " atk, " +
                        otherMon.stats[DEF] + " def, " + otherMon.stats[SPE] + " speed)\n");
            }
            p("'X' to cancel.\n--> ");
            int switchOption = option(1, user.team.size() - 1, true);
            if (switchOption != -1) {
                mon.resetBattleStats();
                p(mon.name + ", come back! > ");
                enter();
                user.team.add(0, user.team.remove(switchOption + 1));
                mon = user.team.get(0);
                p("Go, " + mon.name + "! > ");
                enter();
                enemy.attack(mon);
            }
        }
    }

    /**
     * Called if the user attempts to escape from the battle without a Smoke Bomb. Compares the user's speed to the
     * enemy's to determine whether the user can escape from the battle.
     *
     * @return Whether the user can escape from the battle.
     * @throws Exception if sfx(String), p(String), enter(), music(String), p(String), Monster.attack(Monster),
     *                   escCeus2Msg() throw an exception.
     */
    private boolean tryEscape() throws Exception {
        boolean escaped = false,
                canFlee = mon.stats[SPE] >= enemy.stats[SPE] ||
                        RANDOM.nextInt(100) < (int) Math.round(100.0 * mon.stats[SPE] / enemy.stats[SPE]);
        if (!user.finalBattle) {
            if (canFlee) {
                sfx(ESC_SFX);
                p("Got away safely! > ");
                enter();
                music(SCENARIO_MUSIC);
                escaped = true;
            } else {
                p("Can't escape! > ");
                enter();
                enemy.attack(mon);
            }
        } else escCeus2Msg();
        return escaped;
    }

    /**
     * Called if mon's hp <= 0. If the user has at least one other Monster in user.team, the user may choose which mon
     * to switch into. Else, quits the game.
     *
     * @throws Exception if p(String), enter(), printBag(), option(int, int, boolean), chooseMon(String), quit()
     *                   throw an exception.
     */
    private void deadMon() throws Exception {
        if (user.team.size() > 1) {
            user.team.remove(mon);
            p(mon.name + " has died! > ");
            enter();
            if (mon.holdItem != null) {
                p(mon.name + " dropped the held " + mon.holdItem + "! > ");
                enter();
                if (user.bag.size() < user.bagSize)
                    user.bag.add(mon.holdItem);
                else {
                    p("Which item would you like to swap it with? > ");
                    printBag();
                    p("('X' to cancel.) --> ");
                    int option = option(1, user.bagSize, true);
                    if (option != -1) {
                        user.bag.add(mon.holdItem);
                        mon.holdItem = user.bag.remove(option);
                    }
                    p("The " + mon.holdItem + " was left behind. > ");
                    enter();
                }
            }
            int newMon = chooseMon("Who would you like to send into battle?\n", false);
            user.team.add(0, user.team.remove(newMon));
            mon = user.team.get(0);
            p("Go, " + mon.name + "! > ");
            enter();
        } else {
            p(mon.name + ", your last Fakeymon, has died. > ");
            enter();
            quit();
        }
    }

    /**
     * Called if Fakeyceus dies and user.finalBattle = false. Sets user.finalBattle to true and resets the enemy field,
     * this time to be Fakeyceus2, i.e. the last monster as defined in Fakeydex.json.
     *
     * @throws Exception if p(String), enter(), pause(int), music(String) throw an exception.
     */
    private void ceusEvolve() throws Exception {
        user.finalBattle = true;
        p(enemy.name + " died! > ");
        enter();
        p(DIVIDER);
        music.stop();
        for (int i = 0; i < SAVE_DOTS; i++) {
            p(".");
            pause(SHORT_TIME);
        }
        p("what? > ");
        enter();
        p("Fakeyceus evolved! > ");
        enter();
        mon.resetStats();
        enemy = new Monster(GAME_MONS.get(GAME_MONS.size() - 1), user.kills);
        enemy.holdItem = FAKEYCEUS_ITEM;
        music(CEUS2_MUSIC);
    }

    /**
     * End prompt after every battle finishes.
     * If called after Fakeyceus is killed, prints congratulation message and quits the game.
     * Else enemy generates XP, drops cash, possibly drops item, possibly joins the user's team.
     *
     * @throws Exception if p(String), enter(), quit(), music(String), distXp(Monster), newItem(), newTeammate(),
     *                   confirm(), fakeyCenter() throws an exception.
     */
    private void endBattle() throws Exception {
        user.kills++;
        p(enemy.name + " died! > ");
        enter();
        p(DIVIDER);
        if (fakeyceus) {
            p("Congratulations! You have slain Fakeyceus. > ");
            enter();
            p(NL);
            quit();
        }
        music(SCENARIO_MUSIC);
        int xpGain = generateXp(enemy);
        for (int i = 0; i < user.team.size(); i++) {
            Monster teammate = user.team.get(i);
            if (i == 0 || (teammate.holdItem != null &&
                    teammate.holdItem.equalsIgnoreCase("XP Share"))) {
                teammate.xp += xpGain;
                p(teammate.name + " gained " + xpGain + " xp points! > ");
                enter();
                distXp(teammate);
            }
        }
        user.score += xpGain;
        int cashDropped = RANDOM.nextInt(MAX_CASH * (user.kills / 2 + 1)) + 1;
        user.money += cashDropped;
        p(enemy.name + " dropped $" + cashDropped + "! > ");
        enter();
        if (RANDOM.nextInt(100) < DROP_RATE && enemy.holdItem != null) newItem();
        if (RANDOM.nextInt(100) < JOIN_TEAM)
            newTeammate();
        if (user.kills % FAKEYCENTER_KILLS == 0) {
            p("You found a Fakeymon Center to heal your mons! Would you like to enter? (yes/no) --> ");
            if (confirm())
                fakeyCenter();
        }
    }

    /**
     * Scenario that runs when the user enters a Fakeycenter. The player may heal mons and enter a Fakeymart. If the
     * player enters the mart, calls fakeyMart().
     *
     * @throws Exception if music(String), p(String), option(int, int, boolean), pause(int), fakeyMart() throw an
     *                   exception.
     */
    private void fakeyCenter() throws Exception {
        music(CENTER_MUSIC);
        p(DIVIDER);
        p("Welcome to the Fakeymon center!\n");
        while (true) {
            p("Type '1' to heal your mons, '2' to go to the Fakeymart, or 'X' to leave. --> ");
            int option = option(1, 2, true);
            if (option == 0) {
                for (Monster mon : user.team)
                    mon.resetStats();
                music(HEAL_MUSIC);
                System.out.print("Fake-");
                pause(SHORT_TIME);
                System.out.print("fake-");
                pause(MED_TIME);
                System.out.print("fakey-");
                pause(SHORT_TIME);
                System.out.print("mon!");
                pause(SHORT_TIME);
                music(CENTER_MUSIC);
                p("\nYour Fakeymon have been fully healed. > ");
                enter();
                p(DIVIDER);
            } else if (option == 1)
                try {
                    fakeyMart();
                } catch (Exception e) {
                    break;
                }
            else break;
        }
        music(SCENARIO_MUSIC);
    }

    /**
     * Generates items that the clerk holds. Provides several options to the player.
     * 1 allows the user to speak with the clerk and buy or sell items.
     * 2 allows the user to interact with their mons.
     * 3 allows the user to interact with their bag and items.
     * 4 allows the user to interact with the old lady. First interaction awards the user with a free XP share.
     * Otherwise, the old lady provides several tips, all defined in OLD_LADY_TEXT.
     * 5 allows the user to attempt to steal an item. A user has a STEAL_CHANCE% chance to steal an item. Otherwise,
     * their bag is cleared.
     * X allows the user to leave the shop.
     *
     * @throws Exception PRIMARILY if the user attempts to steal an item but gets caught.
     *                   Otherwise, throws if p(String), option(int, int, boolean), enter(), pause(int), sfx(String),
     *                   printBag(), openBag(), confirm(), monStatus() throw an exception.
     */
    private void fakeyMart() throws Exception {
        List<String> clerkItems = new ArrayList<>(GAME_ITEMS.keySet());
        for (int i = 0; i < clerkItems.size(); i++)
            if (GAME_ITEMS.get(clerkItems.get(i)).costs == 0) {
                clerkItems.remove(i);
                i--;
            }
        Collections.shuffle(clerkItems);
        clerkItems = clerkItems.subList(0, MAX_CLERK_ITEMS);
        while (true) {
            p(DIVIDER);
            p("Type 1 to speak with the clerk, '2' to view the status of a Fakeymon, '3' to open your bag,\n" +
                    "'4' to speak to the old lady, '5' to attempt to steal an item,\n" +
                    "or 'X' to leave. --> ");
            int option = option(1, 5, true);
            if (option == 0)
                while (true) {
                    p("Clerk: What can I do for you? [1. Buy] [2. Sell] [X. Cancel] --> ");
                    option = option(1, 2, true);
                    if (option == 0) {
                        if (clerkItems.size() == 0) {
                            p("Clerk: Unfortunately, we are completely out of stock. > ");
                            enter();
                        } else {
                            while (true) {
                                p(DIVIDER);
                                p("Clerk: Which item would you like to buy? (Current balance: $" + user.money + ")\n");
                                pause(SHORT_TIME);
                                for (int i = 0; i < clerkItems.size(); i++) {
                                    Item item = GAME_ITEMS.get(clerkItems.get(i));
                                    p(i + 1 + ") $" + item.costs + " " + item.name + " : " + item.desc + "\n");
                                }
                                p("'X' to cancel. --> ");
                                option = option(1, clerkItems.size(), true);
                                if (option != -1) {
                                    Item item = GAME_ITEMS.get(clerkItems.get(option));
                                    if (user.money < item.costs) {
                                        p("Clerk: I'm sorry, but your $" + user.money +
                                                " balance is too low to purchase the\n" +
                                                item.name + " which costs $" + item.costs + ". > ");
                                        enter();
                                    } else if (user.bag.size() == user.bagSize) {
                                        p("Clerk: I'm sorry, but you have no room left in your bag! > ");
                                        enter();
                                    } else {
                                        sfx(ITEM_SFX);
                                        p("Purchased the " + item.name + ". > ");
                                        enter();
                                        user.money -= item.costs;
                                        clerkItems.remove(option);
                                        user.bag.add(item.name);
                                    }
                                } else break;
                            }
                        }
                    }
                    else if (option == 1) {
                        if (user.bag.size() < 1) {
                            p("Clerk: You currently have no items. > ");
                            enter();
                        } else {
                            p("Clerk: What would you like to sell?\n");
                            printBag();
                            p("('X' to cancel.) --> ");
                            option = option(1, user.bag.size(), true);
                            if (option != -1) {
                                Item item = GAME_ITEMS.get(user.bag.get(option));
                                p("Clerk: this " + item.name + " sells for $" + item.sellsFor + ".\n" +
                                        "Is this an acceptable price? (yes/no) --> ");
                                if (confirm()) {
                                    p("Sold the " + item.name + ". > ");
                                    enter();
                                    user.bag.remove(option);
                                    if (item.costs > 0)
                                        clerkItems.add(item.name);
                                    user.money += item.sellsFor;
                                }
                            }
                        }
                    } else break;
                }
            else if (option == 1) monStatus();
            else if (option == 2) openBag();
            else if (option == 3) {
                if (!user.xpShare) {
                    p("Old lady: Did you know that the only Fakeymon that gains XP after a battle is the one who earned the kill?\n" +
                            "If you want to train weaker Fakeymon without killing them, you might enjoy this XP Share! > ");
                    enter();
                    if (user.bag.size() < user.bagSize) {
                        user.bag.add("XP Share");
                        user.xpShare = true;
                        sfx(ITEM_SFX);
                        p("Obtained the XP Share! > ");
                        enter();
                    } else {
                        p("Old lady: Looks like you don't have enough room in your bag. Come back to me once you have space. > ");
                        enter();
                    }
                } else {
                    p("Old lady: " + OLD_LADY_TEXT.get(0) + " > ");
                    enter();
                    OLD_LADY_TEXT.add(OLD_LADY_TEXT.remove(0));
                }
            } else if (option == 4) {
                boolean steal = RANDOM.nextInt(100) < STEAL_CHANCE;
                int stealIndex = RANDOM.nextInt(clerkItems.size());
                if (steal) {
                    String item = clerkItems.remove(stealIndex);
                    user.bag.add(item);
                    p("Stole the " + item + "! > ");
                    enter();
                } else {
                    p("Clerk: I'm calling security! > ");
                    enter();
                    p("You were caught by a security guard. > ");
                    enter();
                    p("Guard: I see you trying to steal a " +
                            clerkItems.get(stealIndex) + ". > ");
                    enter();
                    p("Guard: I'm confiscating everything in your bag as punishment. > ");
                    user.bag.clear();
                    enter();
                    p("You were kicked out of the center. > ");
                    enter();
                    music(SCENARIO_MUSIC);
                    throw new Exception();
                }
            } else break;
        }
    }

    /**
     * Prompt when an enemy holding an item drops the item (DROP_RATE% chance). Allows the user to obtain the item.
     *
     * @throws Exception if p(String), confirm(), sfx(String), enter(), printBag(), option(int, int, boolean) throw
     *                   an exception.
     */
    private void newItem() throws Exception {
        Item droppedItem = GAME_ITEMS.get(enemy.holdItem);
        enemy.holdItem = null;
        p(enemy.name + " dropped a(n) " + droppedItem.name + "!\n" +
                "Info: " + droppedItem.desc + "\n" +
                "Would you like to pick it up? (yes/no) --> ");
        if (confirm())
            if (user.bag.size() < user.bagSize) {
                user.bag.add(droppedItem.name);
                sfx(ITEM_SFX);
                p("Obtained the " + droppedItem.name + "! > ");
                enter();
            } else {
                String discardedItem = droppedItem.name;
                p("The bag is full! Would you like to swap it for a different item in the bag? " +
                        "(yes/no) --> ");
                if (confirm()) {
                    printBag();
                    p("Which element would you like to swap it out for? ('X' to cancel.) --> ");
                    int swapElement = option(1, user.bagSize, true);
                    if (swapElement != -1) {
                        String removedElement = user.bag.remove(swapElement);
                        user.bag.add(droppedItem.name);
                        discardedItem = removedElement;
                    }
                }
                p("The " + discardedItem + " was left behind. > ");
                enter();
            }
    }

    /**
     * Prompt when new monster joins the team (JOIN_TEAM% chance). Allows the user to add enemy to their team and
     * rename the enemy.
     *
     * @throws Exception if p(String), confirm(), inputl(String), sfx(String), enter() throw an exception.
     */
    private void newTeammate() throws Exception {
        p(enemy.name + " would like to join your team! Would you like to add " + lowercase(enemy.name) +
                " to your team? (yes/no) --> ");
        if (confirm()) {
            p("Would you like to rename " + lowercase(enemy.name) + "? (yes/no) --> ");
            if (confirm()) {
                p("What should " + lowercase(enemy.name) + "'s new name be? ('Cancel option' to cancel.) --> ");
                String newName = inputl("What should " + lowercase(enemy.name) +
                        "'s new name be? ('Cancel option' to cancel.) --> ");
                if (!newName.equalsIgnoreCase("cancel option"))
                    enemy.name = newName.split(" ")[0];
            }
            user.team.add(enemy);
            enemy.resetStats();
            if (enemy.name.contains("Wild "))
                enemy.name = enemy.name.substring(5);
            sfx(ITEM_SFX);
            p(enemy.name + " joined the team! > ");
            enter();
        }
    }

    /**
     * Checks held items for local Monster instance mon. If struggle(mon) and usable(item), mon will use held item.
     * If the held item is of type hold-heal, then the mon will receive hp from the item.
     *
     * @param mon   - The monster whose item is being checked. Not necessarily this.mon.
     * @param enemy - the enemy to mon. Not necessarily this.enemy.
     * @throws Exception if p(String), enter(), Item.use(Player, Monster, Monster), sfx(String) throw an exception.
     */
    private void checkItems(Monster mon, Monster enemy) throws Exception    {
        Item item = mon.holdItem == null ? null : GAME_ITEMS.get(mon.holdItem);
        if (item != null && item.holdItem) {
            if (struggle(mon) && usable(item)) {
                p(mon.name + " used the held " + mon.holdItem + "! > ");
                enter();
                if (item.use(user, mon, enemy)) {
                    if (item.type.equalsIgnoreCase("esc"))
                        runAway = true;
                    mon.holdItem = null;
                }
            } else if (item.type.equalsIgnoreCase("hold-heal")) {
                String healPercent;
                if (item.name.contains("hoo"))
                    healPercent = item.value1;
                else {
                    int min = Integer.parseInt(item.value1.substring(0, item.value1.length() - 1));
                    int max = Integer.parseInt(item.value2.substring(0, item.value2.length() - 1));
                    healPercent = RANDOM.nextInt(min + max) - min + "%";
                }
                int healValue = healMon(mon.tempStats[HP], mon.stats[HP], healPercent);

                if (healValue != 0) {
                    mon.tempStats[HP] += healValue;
                    sfx(HEAL_SFX);
                    p(mon.name + " restored " + healValue + " hp from the held " + item.name + "! > ");
                    enter();
                }
            }
        }
    }

    /**
     * Distributes mon's hp proportionally. If the monster has twice as much attack as defense, this will be maintained
     * as mon.xp is being distributed.
     *
     * @param mon - The mon whose stats are increasing.
     * @throws Exception if p(String), enter() throw an exception.
     */
    private void distXp(Monster mon) throws Exception {
        int statSum = 0;
        int changeSum = 0;
        for (Integer stat : mon.stats)
            statSum += stat;
        for (int i = 0; i < NUM_STATS - 1; i++) {
            int stat = mon.stats[i];
            int statChange = (int) Math.round(stat / (double) statSum * mon.xp);
            changeSum += statChange;
            mon.stats[i] += statChange;
            mon.tempStats[i] += statChange;
        }
        mon.tempStats[NUM_STATS - 1] = mon.stats[NUM_STATS - 1] += mon.xp - changeSum;

        mon.xp = 0;
        if (mon.attacks.size() >= 1 && RANDOM.nextInt(100) < NEW_MOVE) {
            List<Attack> atks = new ArrayList<>(GAME_ATKS.values());
            Set<String> knownAtks = new HashSet<>(mon.attacks);
            for (int i = 0; i < atks.size(); i++) {
                Attack atk = atks.get(i);
                if (knownAtks.contains(atk.name)) {
                    atks.remove(atk);
                    i--;
                }
            }
            Collections.sort(atks);
            String newAtk = atks.get(Math.min(atks.size() - 1, RANDOM.nextInt(user.kills * 2))).name;
            mon.attacks.add(newAtk);
            p(mon.name + " learned " + newAtk + "! > ");
            enter();
        }
    }

    /**
     * Returns whether the given mon is "struggling." This returns true iff the mon's temporary HP <= STRUGGLE_RANGE.
     *
     * @param mon - The mon that is being evaluated to see if it is struggling.
     * @return Whether the mon is struggling.
     */
    private boolean struggle(Monster mon) {
        return ((int) Math.floor((double) mon.tempStats[HP] / mon.stats[HP] * 100)) <= STRUGGLE_RANGE;
    }

    /**
     * If the user's bag is empty, displays 'You have no items. > '
     * Else prints the user's bag and allows the user to select an item or to cancel.
     * If the user selects an item, the user is given the following options.
     * 1 allows the user to use the item (if the item is usable(Item)).
     * 2 allows the user to give the item to a mon.
     * 3 allows the user to trash the item.
     * X allows the user to cancel.
     *
     * @throws Exception if p(String), enter(), printBag(), option(int, int, boolean), chooseMon(String),
     *                   Item.use(Player, Monster, boolean), confirm() throw an exception.
     */
    private void openBag() throws Exception {
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
                        } else if (item.use(user, enemy, false)) {
                            user.bag.remove(item.name);
                            runAway = item.type.equalsIgnoreCase("esc");
                        }
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
                            user.bag.remove(item.name);
                            p("Tossed the " + item.name + ". > ");
                            enter();
                        }
                    }
                }
            } else break;
        }
    }

    /**
     * Returns whether the given item is usable in battle. i.e., returns whether 0 < Item.use < 5.
     *
     * @param item - The item being evaluated.
     * @return whether the item is usable.
     */
    private boolean usable(Item item) {
        if (item == null)
            return false;
        int use = item.use;
        return 0 < use && use < 5;
    }
}
