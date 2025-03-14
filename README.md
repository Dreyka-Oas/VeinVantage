
# Vein Vantage ✨

Enhance your mining experience with Vein Vantage! This mod adds unique enchantments to make resource gathering faster, more efficient, and more rewarding. Explore each enchantment in detail below:

***

## Vein Sweep ⚒️ **Chain Mining**

**In short:** Mine veins of similar blocks instantly! (Shift + click) 💥

<details>
  <summary>⚙️ Technical Details</summary>

- **Trigger:** `BlockBreakEvent` (Shift + Server Player)
- **Condition:** `vein_sweep` enchantment (level > 0) on tool
- **Algorithm:** BFS (Breadth-First Search) for similar adjacent blocks
- **Limit:** `maxBlocks = level * 15`
- **Delay:** 150ms/block (ExecutorService - 4 threads)
- **Execution:** Main thread via `ServerLevel`
- **Video:** [Vein Sweep video](https://youtu.be/sopLYpXaVdI) 🎥
</details>

***

## Auto Repair 🛠️ **Automatic Repair**

**In short:** Your tool repairs itself while you mine! (70% chance) 🔧

<details>
  <summary>⚙️ Technical Details</summary>

- **Trigger:** `BlockBreakEvent`
- **Condition:** `auto_repair` enchantment on tool
- **Probability:** 70% (if tool damaged)
- **Repair:**
    - Base: 2% damage
    - Bonus: +1%/level (level > 1)
    - Max: 10%
- **Application:** Tool durability adjustment
- **Video:** [AutoRepair video](https://youtu.be/pFN7gfGffDQ) 🎬
</details>

***

## Clearing Strike ⚡ **Area Strike**

**In short:** Expand your mining area based on your orientation! 🌪️

<details>
  <summary>⚙️ Technical Details</summary>

- **Trigger:** `BlockBreakEvent`
- **Condition:** `clearing_strike` enchantment on tool
- **Range:**
    - Base: 1 block
    - Increase: Range = base + (level × 1.5)
- **Orientation:** Vertical/Horizontal (view angle)
- **Effect:** Destruction of similar adjacent blocks
- **Video:** [ClearingStrike video](https://youtu.be/49d1hwGpd5s) 📹
</details>

***

## Ore Blessing ⛏️ **Ore Buff**

**In short:** Get bonuses mining ore: Haste, Speed, Regeneration! 🍀

<details>
  <summary>⚙️ Technical Details</summary>

- **Trigger:** `BlockBreakEvent`
- **Ore:** Broken block ID contains "ore"
- **Condition:** `ore_blessing` enchantment on tool
- **Effects:**
    - Haste ⚡
    - Speed 🚀
    - Regeneration ❤️
- **Probabilities:** 30% base + 10%/level (max 100%)
- **Duration & Potency:** Duration +20 ticks/level, Speed/Regen Amplifier = (level - 1)
- **Video:** [Ore Blessing video](https://youtu.be/tbiZE7Szu3A) 🎥
</details>
