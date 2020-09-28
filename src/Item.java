import org.json.simple.JSONObject;

/*
 * Paulkia 2020.
 * Item object. All Player instances have a List<Item> representing their bag.
 */
class Item extends Fakeyverse implements Saveable, Comparable<Item> {

    /*
     * Type = the effect of the item. Interpreted by program in use(Player, Monster, Monster)
     * Desc = description.
     * Value1 = first calculation in case the item modifies numerical values. String because percent % items exist.
     * Value2 = second calculation in case the item modifies numerical values. String because percent % items exist.
     */
    String name, type, desc, value1, value2;

    /*
     * Use indicates in which situations the item can be used.
     *      0 means the item cannot be used                                    (in Pokemon, an example would be leftovers)
     *      1 means it can be used at any time on our mon                      (in Pokemon, an example would be an oran berry)
     *      2 means it can be used at any time but not on our mon
     *      3 means it can be used exclusively in battle on our mon            (in Pokemon, an example would be X Attack)
     *      4 means it can be used exclusively in battle but not on our mon    (in Pokemon, an example would be a Poke ball)
     *      5 means it can be used exclusively out of battle on our mon        (in Pokemon, an example would be HM04)
     *      6 means it can be used exclusively out of battle, not on our mon   (in Pokemon, an example would be a Max Repel)
     * SellsFor indicates how much the item can be sold for at a shop.
     * Costs indicates the price of the item at a shop.
     * Droprate is the percent chance that a wild mon is holding the item with respect to other items.
     */
    int ID, use, sellsFor, costs, dropRate;

    // Whether the item can have an effect if it is held.
    boolean holdItem;

    /**
     * Constructor when loading item data from DATA_DIR/ItemData.json.
     *
     * @param item - The JSONObject containing all data with respect to this item.
     */
    Item(JSONObject item) {
        name = getStrFromJSON("name", item);
        type = getStrFromJSON("type", item);
        desc = getStrFromJSON("desc", item);
        value1 = getStrFromJSON("value1", item);
        value2 = getStrFromJSON("value2", item);

        ID = getIntFromJSON("ID", item);
        use = getIntFromJSON("use", item);
        sellsFor = getIntFromJSON("sellsFor", item);
        costs = getIntFromJSON("costs", item);
        dropRate = getIntFromJSON("dropRate", item);

        holdItem = getBoolFromJSON("holdItem", item);
    }

    /**
     * Executes the item's use on the user or user's general situation. Not related to monsters.
     *
     * @param user - the Player object using the item.
     * @return Whether the item was used.
     * @throws Exception - if use(Player, Monster, boolean) throws an exception.
     */
    public boolean use(Player user) throws Exception {
        return use(user, null, null);
    }

    /**
     * Executes the item's use on the Monster mon. friend provides whether mon is on the user's team or if it is
     * an enemy.
     *
     * @param user   - The Player.
     * @param mon    - The Monster that this item is being used on.
     * @param friend - Whether mon is on the user's team or if it is an enemy mon.
     * @return Whether the item was successfully used.
     * @throws Exception if use(Player, Monster, Monster) throws an exception.
     */
    public boolean use(Player user, Monster mon, boolean friend) throws Exception {
        return friend ? use(user, mon, null) : use(user, null, mon);
    }

    /**
     * Executes the item's use on user, mon, and/or enemy.
     * If type = heal and mon's HP is not full: heals mon.
     * If type = xheal: heals mon's temporary HP to higher than max HP.
     * If type = bag and value1 > user.bagSize: increases bagSize to value1
     * If type = dam: lowers enemy's hp
     * If type = esc and user.finalBattle = false: escapes user from battle.
     * Else prints 'This item cannot be used at the moment. > '
     * @param user  - The Player, if item is general purpose (such as Smoke Bomb).
     * @param mon   - The Player's monster, if item is being used on mon.
     * @param enemy - The enemy if in battle, if item is being used on enemy.
     * @return Whether the item was used.
     * @throws Exception if sfx(String), music(String), p(String), enter(), escCeus2Msg() throw an exception.
     */
    public boolean use(Player user, Monster mon, Monster enemy) throws Exception {
        boolean used = false;
        if (type.equalsIgnoreCase("heal")) {
            if (mon.tempStats[HP] < mon.stats[HP]) {
                mon.tempStats[HP] += healMon(mon.tempStats[HP], mon.stats[HP], value1);
                used = true;
                sfx(HEAL_SFX);
                p(mon.name + "'s HP rose from the " + name + "! > ");
            } else
                p(mon.name + "'s HP is already full. > ");
            enter();
        } else if (type.equalsIgnoreCase("xheal")) { // xheal
            String hpRestoreValue = value1;
            int percentValue = Integer.parseInt(hpRestoreValue.substring(0, hpRestoreValue.length() - 1));
            double percentGain = ((double) percentValue) / 100;
            mon.tempStats[HP] = (int) Math.round(mon.stats[HP] * percentGain);
            used = true;
            p(mon.name + "'s HP rose from the " + name + "! > ");
            enter();
        } else if (type.equalsIgnoreCase("bag") && user.bagSize < Integer.parseInt(value1)) {
            user.bagSize = Integer.parseInt(value1);
            used = true;
            p("Your bag has increased in size, and can now hold up to " + user.bagSize + " items! > ");
            enter();
        } else if (type.equalsIgnoreCase("bag")) {
            p("This item would increase your bag size to allow for " + Integer.parseInt(value1) + " items,\n" +
                    "but your bag can already hold " + user.bagSize + " items. > ");
            enter();
        } else if (type.equalsIgnoreCase("dam")) {
            int damageValue = Integer.parseInt(value1);
            if (value2 != null && RANDOM.nextInt(100) < Integer.parseInt(value2)) {
                damageValue *= CRIT_MULTIPLIER;
                p("A critical hit! > ");
                enter();
            }
            enemy.tempStats[HP] -= damageValue;
            used = true;
            sfx(DAMAGE_SFX);
            p(enemy.name + " took " + damageValue + " damage from the " + name + "! > ");
            enter();
        } else if (type.equalsIgnoreCase("esc")) {
            if (!user.finalBattle) {
                used = true;
                sfx(ESC_SFX);
                p("Got away safely! > ");
                enter();
                music(SCENARIO_MUSIC);
            } else escCeus2Msg();
        } else {
            p("This item cannot be used at the moment. > ");
            enter();
        }
        return used;
    }

    /**
     * Prints the item in an elegant way to display to the user.
     * @return a String version of the item containing its name and description.
     */
    public String print() {
        return name + " : " + desc;
    }

    /**
     * Turns this Item object into a JSONObject object.
     * @return this object in terms of its JSONObject equivalent.
     */
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();

        result.put("name", name);
        result.put("type", type);
        result.put("desc", desc);
        result.put("value1", value1);
        result.put("value2", value2);

        result.put("use", use);
        result.put("sellsFor", sellsFor);
        result.put("costs", costs);
        result.put("dropRate", dropRate);

        result.put("holdItem", holdItem);

        return result;
    }

    /**
     * @return toJSON().toString().
     */
    public String toString() {
        return toJSON().toString();
    }

    /**
     * Compares this Item's ID to other's ID.
     * @param other - the other Item being compared.
     * @return ID - other.ID.
     */
    public int compareTo(Item other) {
        return ID - other.ID;
    }
}
