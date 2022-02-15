"ID" (int)              currently has no use. Each attack should have a unique but possibly arbitrary ID.
"Name" (String)         identifies the attack's name, as displayed to the user.
"Desc" (String)         is the description, as displayed to the user.
"Type" (String[])       categorizes the attack to understand its effects. Can be physical, special, status, physical & status, or special & status.
"Stats" (String[])      lists all personal stats affected by the attack.
"StatValue" (int)       indicates to what extent given stat(s) has/have been modified.
"Power" (int)           indicates the strength of the attack.
"Acc" (int)             indicates the accuracy of an attack as a percent.

"Stat rose": < 30%
"Stat rose sharply": < 60%
else "Stat rose drastically".

- Critical hits. I.e. crit, crits.
Crits do double damage but do not ignore status modifications.
Every attack has CRIT_CHANCE chance to inflict a crit.

- Evasion
Evasion cuts the attack damage in half.
Every attack has evasion chance of EVADE_CHANCE.