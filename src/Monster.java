import org.json.simple.*;

import java.util.*;

/*
 * Paulkia 2020.
 * Monster object defining a Fakeymon.
 */
class Monster extends Fakeyverse implements Saveable, Comparable<Monster> {

    // ascii: the ascii art of this monster in String format.
    String name, ascii;
    // ID: only relevant for enemies. xp: how much spare xp this monster has that can be distributed.
    Integer ID, xp;
    // The maximum stat of any monster when send into battle.
    Integer[] stats;
    // The monster's current stats, typically less than or equal to their stats default,
    // completely reset at Fakeycenters.
    Integer[] tempStats;
    List<String> attacks;
    String holdItem;
    // Whether the monster used, in the previous turn, Hyper Impact or any other attack that causes resting for a turn.
    boolean resting;

    /**
     * Constructor for generating random wild Fakeymon.
     *
     * @param profile - The type of monster that this monster should be modeled after.
     */
    Monster(Monster profile, int kills) {
        def();

        name = profile.name;
        ascii = profile.ascii;
        stats = profile.stats.clone();
        attacks = new ArrayList<>(profile.attacks);

        xp = kills * ENEMY_XP_RATE + MIN_ENEMY_XP;
        if (RANDOM.nextInt(100) < HOLD_ITEM_PROBABILITY)
            holdItem = generateItem();

        int sum = 0;
        for (int i = 0; i < NUM_STATS; i++)
            sum += stats[i];
        for (int i = 0; i < NUM_STATS; i++)
            stats[i] += (int) Math.round(xp * ((double) stats[i]) / sum);

        xp = 0;
        tempStats = stats.clone();
    }

    /**
     * Constructor for starter Fakeymon.
     *
     * @param name - Name of starter.
     */
    Monster(String name) {
        def();
        this.name = name;
        xp = 0;
    }

    /**
     * Constructor used when loading save data.
     *
     * @param mon - All data of this mon given by JSON object.
     */
    Monster(JSONObject mon) {
        def();
        name = mon.get("name").toString();
        ascii = mon.get("ascii") == null ? null : mon.get("ascii").toString();

        ID = getIntFromJSON("ID", mon);
        xp = getIntFromJSON("xp", mon);

        stats = getIntArrFromJSON("stats", mon);
        tempStats = mon.get("tempStats") == null ? stats : getIntArrFromJSON("tempStats", mon);

        attacks.addAll((List<String>) mon.get("attacks"));

        Object item = mon.get("holdItem");
        holdItem = item == null ? null : (String) item;
    }

    /**
     * Allows this Monster object to attack the defender Monster. Chooses an attack at random.
     *
     * @param defender - The Monster getting attacked.
     * @throws Exception if attack(int, Monster) throws an exception.
     */
    void attack(Monster defender) throws Exception {
        attack(RANDOM.nextInt(attacks.size()), defender);
    }

    /**
     * This monster object attacks the defender Monster with its attacks.get(AttackIndex) attack.
     *
     * @param attackIndex - The index of the attack that is being used, from the attacks field.
     * @param defender    - The monster getting attacked.
     * @throws Exception if p(String), enter(), or Attack.use(Monster, Monster) throw an exception.
     */
    void attack(int attackIndex, Monster defender) throws Exception {
        Attack atk = GAME_ATKS.get(attacks.get(attackIndex));
        if (resting) {
            p(name + " must recharge! > ");
            enter();
            resting = false;
        } else
            atk.use(this, defender);
    }

    /**
     * Resets all temporary stats of this Monster except temporary HP.
     */
    void resetBattleStats() {
        resetStats(false);
    }

    /**
     * Resets all temporary stats of this Monster to their stats[] values.
     */
    public void resetStats() {
        resetStats(true);
    }

    /**
     * Resets temporary stats of this Monster. If full, it also resets temporary HP.
     * @param full - Whether temporary HP should be reset to maximum HP.
     */
    private void resetStats(boolean full) {
        for (int i = 0; i < NUM_STATS; i++)
            if (full || !STAT_TYPE[i].equalsIgnoreCase("HP"))
                tempStats[i] = stats[i];
        resting = false;
    }

    /**
     * Turns this Monster object into a JSONObject object.
     * @return this object in terms of its JSONObject equivalent.
     */
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();

        result.put("name", name);

        result.put("ID", ID);
        result.put("xp", xp);

        result.put("stats", Arrays.asList(stats));
        result.put("tempStats", Arrays.asList(tempStats));

        result.put("attacks", attacks);

        result.put("holdItem", holdItem);
        return result;
    }

    /**
     * Formats this monster to be displayed for the player.
     * @return this monster in an attractive String format.
     */
    public String print() {
        String result = BAR + " [ " + name + " ] " + BAR + NL;
        result += "Held item: " + (holdItem == null ? "[none]" : holdItem) + NL;
        // result += "Spare XP: " + xp + "\n";
        result += "[ Stats ]\n";
        for (int i = 0; i < NUM_STATS; i++) {
            result += "- " + capitalize(STAT_TYPE[i]) + ": [" + tempStats[i] + "/" + stats[i] + "]\n";
        }
        result += "[ Attacks ]\n";
        for (String atk : attacks) {
            result += "- " + GAME_ATKS.get(atk).print() + "\n";
        }
        result += BAR + " [ " + name + " ] " + BAR + NL;
        return result;
    }

    /**
     * Like print() but with less information.
     * @return A string containing only the stats and name of this monster.
     */
    public String printLimited() {
        String result = "[ " + name + "'s Stats ]\n";
        for (int i = 0; i < NUM_STATS; i++)
            result += "- " + capitalize(STAT_TYPE[i]) + ": [" + tempStats[i] + "/" + stats[i] + "]\n";
        return result;
    }

    /**
     * @return toJSON().toString().
     */
    public String toString() {
        return toJSON().toString();
    }

    /**
     * Checks if two monsters are equal by comparing Monster.name/xp/stats/tempStats/attacks
     * @param other - The other monster being compared.
     * @return Whether this and other are equal.
     */
    public boolean equals(Monster other) {
        return name.equals(other.name) && xp == other.xp && stats == other.stats && tempStats == other.tempStats &&
                attacks.equals(other.attacks);
    }

    /**
     * Compares two monsters by their ID.
     * @param other - The other monster being compared.
     * @return ID - other.ID.
     */
    public int compareTo(Monster other) {
        return ID - other.ID;
    }

    /**
     * All duplicate lines of code found in all constructors to set up objects are written here instead.
     */
    private void def() {
        stats = new Integer[NUM_STATS];
        tempStats = new Integer[NUM_STATS];
        attacks = new ArrayList<>();
        Arrays.fill(stats, DEFAULT_STAT);
        Arrays.fill(tempStats, DEFAULT_STAT);
        resting = false;
        holdItem = null;
    }
}