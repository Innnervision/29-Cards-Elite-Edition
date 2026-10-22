package com.example.game.model

data class AdventureTask(
    val id: String,
    val description: String,
    val isCompleted: Boolean = false,
    val rewardText: String = ""
)

data class AdventureLevel(
    val levelNumber: Int,
    val title: String,
    val subtitle: String,
    val city: String,
    val chapter: String,
    val loreDescription: String,
    val opponent1Name: String,
    val opponent2Name: String,
    val partnerName: String = "Vikram (Coach)",
    val targetPips: Int = 4,
    val minBidTarget: Int = 16,
    val rewardCoins: Long,
    val rewardDiamonds: Int,
    val rewardXp: Int,
    val tasks: List<AdventureTask>,
    val starsEarned: Int = 0,
    val isUnlocked: Boolean = false,
    val isCompleted: Boolean = false
)

data class AdventureProgress(
    val currentLevelNumber: Int = 1,
    val totalStars: Int = 0,
    val levelsCompleted: Int = 0,
    val levels: List<AdventureLevel> = defaultAdventureLevels()
)

fun defaultAdventureLevels(): List<AdventureLevel> = listOf(
    AdventureLevel(
        levelNumber = 1,
        title = "Chai Stall Rookies",
        subtitle = "College Street Bidding",
        city = "Kolkata",
        chapter = "Chapter 1: Streets of Bengal",
        loreDescription = "Over earthen cups of steaming chai, the local college champions challenge you to master bidding foundations.",
        opponent1Name = "Subhash (College St)",
        opponent2Name = "Bimal (Tea Master)",
        partnerName = "Vikram (Coach)",
        targetPips = 4,
        minBidTarget = 16,
        rewardCoins = 800L,
        rewardDiamonds = 15,
        rewardXp = 150,
        tasks = listOf(
            AdventureTask("task_bid_16", "Place a successful bid of 16 or higher", rewardText = "★ Star 1"),
            AdventureTask("task_capture_jack", "Capture at least one Jack (3 pts) in tricks", rewardText = "★ Star 2"),
            AdventureTask("task_win_level_1", "Win the match against Subhash & Bimal", rewardText = "★ Star 3")
        ),
        starsEarned = 0,
        isUnlocked = true
    ),
    AdventureLevel(
        levelNumber = 2,
        title = "Howrah River Docks",
        subtitle = "Trump Card Gambit",
        city = "Kolkata",
        chapter = "Chapter 1: Streets of Bengal",
        loreDescription = "The Hooghly river dockers play sharp. Hide your Trump card wisely and spring it when they least expect it.",
        opponent1Name = "Gouranga (Boatman)",
        opponent2Name = "Tapan (Foreman)",
        partnerName = "Vikram (Coach)",
        targetPips = 4,
        minBidTarget = 17,
        rewardCoins = 1200L,
        rewardDiamonds = 20,
        rewardXp = 200,
        tasks = listOf(
            AdventureTask("task_reveal_trump", "Tactically reveal the hidden Trump card", rewardText = "★ Star 1"),
            AdventureTask("task_capture_15_pts", "Win 15 or more card points in a round", rewardText = "★ Star 2"),
            AdventureTask("task_win_level_2", "Win the match without letting opponents reach 3 pips", rewardText = "★ Star 3")
        ),
        starsEarned = 0,
        isUnlocked = false
    ),
    AdventureLevel(
        levelNumber = 3,
        title = "Old Dhaka Club",
        subtitle = "The Jack Hunter",
        city = "Dhaka",
        chapter = "Chapter 2: East Bengal Circuit",
        loreDescription = "In the heritage parlor of Old Dhaka, masters calculate every Jack and Nine with surgical precision.",
        opponent1Name = "Kamal (Dhaka Ace)",
        opponent2Name = "Sohail (Old Town)",
        partnerName = "Vikram (Coach)",
        targetPips = 5,
        minBidTarget = 18,
        rewardCoins = 1800L,
        rewardDiamonds = 25,
        rewardXp = 280,
        tasks = listOf(
            AdventureTask("task_capture_2_jacks", "Capture at least 2 Jacks in a single round", rewardText = "★ Star 1"),
            AdventureTask("task_bid_18", "Win a high-stakes bid of 18 or above", rewardText = "★ Star 2"),
            AdventureTask("task_win_level_3", "Claim total victory in Dhaka Club", rewardText = "★ Star 3")
        ),
        starsEarned = 0,
        isUnlocked = false
    ),
    AdventureLevel(
        levelNumber = 4,
        title = "Marine Drive High Stakes",
        subtitle = "Royal Pair Declaration",
        city = "Mumbai",
        chapter = "Chapter 3: Western Metropolis",
        loreDescription = "Bombay's elite card circle plays with Royal Pairs. A King and Queen in Trump changes the contract by 4 points!",
        opponent1Name = "Dev (South Bombay)",
        opponent2Name = "Karan (Marine Drive)",
        partnerName = "Vikram (Coach)",
        targetPips = 5,
        minBidTarget = 19,
        rewardCoins = 2500L,
        rewardDiamonds = 35,
        rewardXp = 350,
        tasks = listOf(
            AdventureTask("task_declare_pair", "Successfully declare a King & Queen Pair", rewardText = "★ Star 1"),
            AdventureTask("task_defend_bid", "Defeat or set an opponent's high bid", rewardText = "★ Star 2"),
            AdventureTask("task_win_level_4", "Conquer the Mumbai high-rollers", rewardText = "★ Star 3")
        ),
        starsEarned = 0,
        isUnlocked = false
    ),
    AdventureLevel(
        levelNumber = 5,
        title = "Chittagong Port Vault",
        subtitle = "Double Stakes Showdown",
        city = "Chittagong",
        chapter = "Chapter 4: Coastal Showdown",
        loreDescription = "The maritime tycoons challenge you to Doubled and Redoubled contracts. Mistakes here cost double points!",
        opponent1Name = "Rashid (Port Boss)",
        opponent2Name = "Anwar (Cargo King)",
        partnerName = "Vikram (Coach)",
        targetPips = 6,
        minBidTarget = 20,
        rewardCoins = 3500L,
        rewardDiamonds = 50,
        rewardXp = 450,
        tasks = listOf(
            AdventureTask("task_bid_20", "Place and fulfill a bold bid of 20 or higher", rewardText = "★ Star 1"),
            AdventureTask("task_last_trick", "Win the decisive 8th trick for bonus point", rewardText = "★ Star 2"),
            AdventureTask("task_win_level_5", "Defeat the Chittagong Port tycoons", rewardText = "★ Star 3")
        ),
        starsEarned = 0,
        isUnlocked = false
    ),
    AdventureLevel(
        levelNumber = 6,
        title = "Grand Mughal Palace",
        subtitle = "The 29 Grandmaster",
        city = "Delhi",
        chapter = "Chapter 5: Emperor's Throne",
        loreDescription = "The final pinnacle. Face Grandmaster Rudra and Shiv at the imperial court. Only a true 29 Legend can claim the Golden Crown.",
        opponent1Name = "Rudra (Grandmaster)",
        opponent2Name = "Shiv (Palace Master)",
        partnerName = "Vikram (Grand Coach)",
        targetPips = 6,
        minBidTarget = 21,
        rewardCoins = 6000L,
        rewardDiamonds = 100,
        rewardXp = 700,
        tasks = listOf(
            AdventureTask("task_capture_all_jacks", "Capture 3 or more Jacks in the match", rewardText = "★ Star 1"),
            AdventureTask("task_bid_21", "Successfully achieve a contract of 21+ points", rewardText = "★ Star 2"),
            AdventureTask("task_win_level_6", "Dethrone the Delhi Grandmasters & become 29 Emperor", rewardText = "★ Star 3")
        ),
        starsEarned = 0,
        isUnlocked = false
    )
)
