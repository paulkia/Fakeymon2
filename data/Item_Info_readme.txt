"ID" (int)              sorts the item by drop rate, where higher drops have smaller IDs.
"Name" (String)         identifies the item's name, as displayed to the user.
"Type" (String)         categorizes the item for the program.
"Value1" (String)       is the percentage or numerical value of the item's primary effect.
"Value2" (String)       is the percentage or numerical value of the item's secondary effect.
"Use" (int)             provides whether the item can be used.
    0 means it cannot be used                                           (in Pokemon, an example would be leftovers)
    1 means it can be used at any time on our mon                       (in Pokemon, an example would be an oran berry)
    2 means it can be used at any time but not on our mon
    3 means it can be used exclusively in battle on our mon             (in Pokemon, an example would be X Attack)
    4 means it can be used exclusively in battle but not on our mon     (in Pokemon, an example would be a Poke ball)
    5 means it can be used exclusively out of battle on our mon         (in Pokemon, an example would be HM04)
    6 means it can be used exclusively out of battle, not on our mon    (in Pokemon, an example would be a Max Repel)
"HoldItem" (boolean)    provides whether the mon can use the item if it is held.
"SellsFor" (int)        provides how much money earned if sold to a shop. If 0, this item cannot be sold to shops.
"Costs" (int)           provides how much it costs if bought from shop. If 0, this item is not found in shops.
"DropRate" (int)        provides how likely it is to drop from defeating a Fakeymon. 37.5% chance for an item to drop after battle.
"Desc" (String)         is the description, as displayed to the user.

A mon holding a Healyhoo or Superhoo in battle will use if at <= 50% hp.
A mon holding a Megahoo or Legendhoo ... if at <= 50% hp.
A mon holding a Gas bomb ... if at <= 20% hp. This counts as fleeing from battle.

Whistle hoo & hou are used every turn, only usable in battle.
    Hou can restore -10% to 20% of a mon's hp per turn, meaning it can kill a mon.

A rock inflicts 25hp damage; cannot crit. A mon holding this in battle will use at random each turn with 25%
    chance after its primary attack if the enemy is still alive. If used, the item is destroyed.

A stick inflicts 20hp damage; 40% chance of crit. A mon holding this in battle will use at random each turn with 25%
    chance after its primary attack if the enemy is still alive. If used, the item is destroyed.

Find more item information in src/data/ItemData.json.