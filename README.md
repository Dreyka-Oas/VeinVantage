# Vein Vantage – Enchantments ✨

## Vein Sweep ⚒️
**Micro description:**  
Triggers a chain destruction of similar blocks when mining (while holding Shift) if the tool has the `vein_sweep` enchantment. 💥

<details>
  <summary>Spoiler 🕵️‍♂️</summary>

**Technical Details:**

- **Trigger:**  
  The `BlockBreak` event starts the procedure, which checks that the player is a `ServerPlayer` and is holding down the Shift key. ⏱️

- **Enchantment Check:**  
  The main hand tool is inspected for the `vein_sweep` enchantment.  
  If its level is greater than 0, the procedure continues. 🔍

- **Algorithm:**  
  Uses a flood fill algorithm (BFS) to identify adjacent blocks with the same state as the initial block. 🌊

- **Limitation and Delay:**  
  The maximum number of blocks to destroy is calculated as:  
  `maxBlocksToDestroy = enchantmentLevel * 15`  
  Each block is destroyed with a delay of 150ms using an `ExecutorService` (4 threads). ⏳

- **Main Thread Execution:**  
  The actual block destruction is executed on the main server thread via `ServerLevel`. 🧵

- **Video:** [Vein Sweep video](https://youtu.be/sopLYpXaVdI) 🎥

</details>

<hr style="border: 2px solid #FF5733;">

## Auto Repair 🛠️
**Micro description:**  
Automatically repairs the damaged tool when mining if the `auto_repair` enchantment is present, with roughly a 70% chance. 🔧

<details>
  <summary>Spoiler 🔍</summary>

**Technical Details:**

- **Trigger:**  
  The `BlockBreak` event initiates the procedure. 🚀

- **Enchantment Check:**  
  The main hand tool is checked for the `auto_repair` enchantment. 🔎

- **Repair Conditions:**  
  The repair applies if the tool is damaged, with a 70% probability. 🎲

- **Repair Calculation:**  
  - **Base:** Repairs 2% of the current damage.  
  - **Bonus:** +1% per enchantment level (beyond level 1).  
  - **Cap:** Total repair is capped at 10%. 📈

- **Application:**  
  The tool's damage value is adjusted accordingly. 🔄

- **Video:** [AutoRepair video](https://youtu.be/pFN7gfGffDQ) 🎬

</details>

<hr style="border: 2px solid #FF5733;">

## Clearing Strike ⚡
**Micro description:**  
Extends the destruction area during mining based on the player's orientation and the level of the `clearing_strike` enchantment. 🌪️

<details>
  <summary>Spoiler 👀</summary>

**Technical Details:**

- **Trigger:**  
  The `BlockBreak` event triggers the procedure. 🎯

- **Enchantment Check:**  
  The main hand tool is inspected for the `clearing_strike` enchantment. 🔍

- **Range Calculation:**  
  - **Base:** 1 block.  
  - **Increase:** Range = base + (enchantmentLevel × 1.5). ➕

- **Orientation:**  
  The direction of the effect (vertical or horizontal) is determined by the player's viewing angle. 🔄

- **Effect:**  
  Adjacent blocks with the same state as the initial block are destroyed. 💣

- **Video:** [ClearingStrike video](https://youtu.be/49d1hwGpd5s) 📹

</details>

<hr style="border: 2px solid #FF5733;">

## Ore Blessing ⛏️
**Micro description:**  
Grants beneficial effects (Haste, Speed, Regeneration) to the player when breaking an ore, if the `ore_blessing` enchantment is present. 🍀

<details>
  <summary>Spoiler 🌟</summary>

**Technical Details:**

- **Trigger:**  
  The procedure is launched via the `BlockBreak` event. 🚩

- **Ore Identification:**  
  The broken block is considered an ore if its identifier contains the word `"ore"`. 🔎

- **Enchantment Check:**  
  The main hand tool must have the `ore_blessing` enchantment. ✨

- **Applied Effects:**  
  - **Haste (Dig Speed)** ⚡  
  - **Speed (Movement Speed)** 🚀  
  - **Regeneration** ❤️

- **Probabilities:**  
  Each effect has a base chance of 30%, increased by 10% per level (up to 100%). 🎲

- **Duration and Potency:**  
  The duration of each effect increases by 20 ticks per level, and the amplifier for Speed and Regeneration is set to `(enchantmentLevel - 1)`. ⏳

- **Video:** [Ore Blessing video](https://youtu.be/tbiZE7Szu3A) 🎥

</details>
