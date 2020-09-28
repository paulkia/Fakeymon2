import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
 * Paulkia 2020.
 * Player object that stores all of a user's data. Used to save and load users from json files in
 * SAVE_DIR + user.name + .json.
 */
class Player extends Fakeyverse implements Saveable {
    // The user's name. Title of the user's save file.
    String name;
    // The user's team, as a list of Monsters.
    List<Monster> team;
    // The user's bag, as a list of Items.
    List<String> bag;
    /*
     * bagSize = the maximum number of items the user can carry. Can be increased if other bags are bought.
     * kills = used when evaluating whether or not the final battle should be activated.
     */
    int bagSize, money, kills, score;
    /*
     * xpShare = whether the old lady has already given the player an XP Share (so she doesn't give it twice).
     * finalBattle = whether the user has already beaten Fakeyceus 1.
     */
    boolean xpShare, finalBattle;

    /**
     * The default constructor. Sets all fields to default values.
     */
    Player() {
        def();
        this.name = "";
        bagSize = DEFAULT_BAG;
        money = kills = score = 0;
        xpShare = false;
    }

    /**
     * Constructor that takes in JSONObject containing data from a user's previous save state.
     * Sets all fields to values specified in userData.
     * @param userData - The data from the save file to be loaded into the game.
     */
    Player(JSONObject userData) {
        def();
        this.name = userData.get("name").toString();

        List<JSONObject> teamData = (List<JSONObject>) userData.get("team");
        for (JSONObject mon : teamData)
            team.add(new Monster(mon));

        bag.addAll((List<String>) userData.get("bag"));

        this.bagSize = getIntFromJSON("bagSize", userData);
        this.money = getIntFromJSON("money", userData);
        this.kills = getIntFromJSON("kills", userData);
        this.score = getIntFromJSON("score", userData);

        this.xpShare = getBoolFromJSON("xpShare", userData);
    }

    /**
     * Converts this object into a JSONObject to be saved to a .json save file.
     * @return this Player object as a JSONObject.
     */
    public JSONObject toJSON() {
        JSONObject userData = new JSONObject();
        userData.put("name", name);

        userData.put("team", team);

        userData.put("bag", bag);

        userData.put("bagSize", bagSize);
        userData.put("money", money);
        userData.put("kills", kills);
        userData.put("score", score);

        userData.put("xpShare", xpShare);

        return userData;
    }

    /**
     * @return toJSON().toString().
     */
    public String toString() {
        return toJSON().toString();
    }

    /**
     * All duplicate lines of code found in all constructors to set up objects are written here instead.
     */
    private void def() {
        team = new ArrayList<>();
        bag = new ArrayList<>();
    }

}
