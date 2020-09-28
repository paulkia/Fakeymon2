import org.json.simple.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/*
 * Paulkia 2020.
 * Attack object containing all data in an attack. All Monster instances have a List<Attack> attacks.
 */
public class Attack extends Fakeyverse implements Saveable, Comparable<Attack> {
    /*
     * Desc = description of this attack
     */
    String name, desc;
    /*
     * Type = type of attack. So far, only types defined are 'status' and 'physical'
     * Sfx = special effects of the attack, such as 'resting' for Hyper Impact.
     */
    private String[] type, sfx;
    /*
     * Power = power value of the attack.
     * Acc = accuracy of the attack (% chance of landing).
     */
    private Integer ID, power, acc;
    /*
     * Stats = user's stats affected by this attack.
     * SfxVal = value of the special effect. For Jaw Breaker, this is the percent chance that the attack is a crit.
     */
    private Integer[] stats, sfxVal;

    /**
     * Constructor that takes in a JSONObject in Java.Object form.
     *
     * @param attack - The object being read to set the fields for this object.
     */
    public Attack(Object attack) {
        this((JSONObject) attack);
    }

    /**
     * Constructor that takes in a JSONObject in Java.Object form.
     *
     * @param attack - The object being read to set the fields for this object.
     */
    public Attack(JSONObject attack) {
        name = getStrFromJSON("name", attack);
        desc = getStrFromJSON("desc", attack);

        type = getStrArrFromJSON("type", attack);
        sfx = getStrArrFromJSON("sfx", attack);

        ID = getIntFromJSON("ID", attack);
        power = getIntFromJSON("power", attack);
        acc = getIntFromJSON("acc", attack);

        stats = getIntArrFromJSON("stats", attack);
        sfxVal = getIntArrFromJSON("sfx-val", attack);
    }

    /**
     * Called when the Monster uses this attack. Takes in the attacker and defender to manage the effects of the attack.
     * If type contains 'status' and the attack lands (or accuracy is null), the attacker's stats are modified.
     * If type contains 'physical' and the attack lands, the defender takes at least 1 damage.
     *
     * @param attacker - The monster using this attack.
     * @param defender - The monster getting attacked.
     * @throws Exception if sfx(String), p(String), enter() throw an exception.
     */
    public void use(Monster attacker, Monster defender) throws Exception {
        List<String> atkText = new LinkedList<>();
        atkText.add(attacker.name + " used " + name + "! > ");
        if (acc == null || RANDOM.nextInt(100) < acc) { // if hits
            if (indexOf(type, "physical") > -1) {
                double multiplier = DEFAULT_MULTIPLIER;
                int critChance = indexOf(sfx, "hi-crit");
                critChance = critChance != -1 ? sfxVal[critChance] : CRIT_CHANCE;
                if (RANDOM.nextInt(100) < critChance) {
                    atkText.add("A critical hit! > ");
                    multiplier *= CRIT_MULTIPLIER; // Crit
                }
                if (RANDOM.nextInt(100) < EVADE_CHANCE) {
                    atkText.add(defender.name + " partially evaded the attack! > ");
                    multiplier /= EVADE_MULTIPLIER;
                }
                int damage = (int) Math.round(multiplier * power *
                        (attacker.tempStats[ATK]) / (defender.tempStats[DEF])) + 1;
                defender.tempStats[HP] -= Math.min(damage, defender.tempStats[HP]);
                atkText.add(defender.name + " took " + damage + " damage! > ");
            }
            if (indexOf(type, "status") > -1)
                for (int i = 0; i < NUM_STATS; i++)
                    if (stats[i] != null) {
                        int statValue = stats[i];
                        int adjIndex = statValue / ADJ_DIVISOR;
                        int statChange = i == HP ?
                                healMon(attacker.tempStats[HP], attacker.stats[HP], statValue + "%") :
                                statBoost(attacker, i, statValue);
                        attacker.tempStats[i] += statChange;
                        atkText.add(attacker.name + "'s " + STAT_TYPE[i] + " rose" + STAT_CHANGE_ADJ[adjIndex] + "! > ");
                    }

            if (indexOf(sfx, "resting") > -1)
                attacker.resting = true;
        } else
            atkText.add(RANDOM.nextInt(100) < 50 ?
                    attacker.name + "'s attack missed! > " :
                    defender.name + " avoided the attack! > ");
        for (String msg : atkText)
            if (msg.length() > 0) {
                if (msg.contains("damage"))
                    sfx(DAMAGE_SFX);
                else if (msg.contains("HP"))
                    sfx(HEAL_SFX);
                else if (msg.contains("rose"))
                    sfx(STAT_BOOST_SFX);
                p(msg);
                enter();
            }
    }

    /**
     * Converts this into a JSONObject to save the game.
     * @return this object in JSONObject format.
     */
    public JSONObject toJSON() {
        JSONObject result = new JSONObject();
        result.put("name", name);
        result.put("desc", desc);

        result.put("type", Arrays.asList(type));
        result.put("sfx", sfx == null ? null : Arrays.asList(sfx));

        result.put("ID", ID);
        result.put("power", power);
        result.put("acc", acc);

        result.put("stats", stats == null ? null : Arrays.asList(stats));
        result.put("sfx-val", sfxVal == null ? null : Arrays.asList(sfxVal));
        return result;
    }

    /**
     * Returns this Attack in elegant String format to be displayed to the user.
     * @return this Attack in display format for the user to see.
     */
    public String print() {
        if (indexOf(type, "physical") > -1)
            return name + " (" + power + " PWR/" + (acc != null ? acc : "X") + " ACC) : " + desc;
        return name + " : " + desc;
    }

    /**
     * @return toJSON().toString().
     */
    public String toString() {
        return toJSON().toString();
    }

    /**
     * Compares this Attack's ID to other's ID.
     * @param other - the other Attack being compared.
     * @return ID - other.ID.
     */
    public int compareTo(Attack other) {
        return ID - other.ID;
    }
}
