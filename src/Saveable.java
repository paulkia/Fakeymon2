import org.json.simple.JSONObject;

/*
 * All save-able objects (i.e. objects that can be saved to JSON files), such as Player, Monster, Attack, and Item,
 * implement Saveable.
 */
interface Saveable {
    /**
     * Requires Saveable objects to have a toJSON() method that returns that object in JSON format, so that it can be
     * saved and parsed by JSON objects.
     * @return an instance of a Saveable object in JSON format as a JSONObject.
     */
    JSONObject toJSON();
}
