package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.game.TwentyNineViewModel
import com.example.game.audio.SoundEffectsManager
import com.example.game.model.SocialPlatform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("29 Card Game", appName)
  }

  @Test
  fun `social platforms define diamond rewards per share`() {
    assertTrue(SocialPlatform.YOUTUBE.diamondRewardPerShare > 0)
    assertTrue(SocialPlatform.FACEBOOK.diamondRewardPerShare > 0)
    assertTrue(SocialPlatform.INSTAGRAM.diamondRewardPerShare > 0)
    assertTrue(SocialPlatform.TWITTER_X.diamondRewardPerShare > 0)
    assertTrue(SocialPlatform.WHATSAPP.diamondRewardPerShare > 0)
  }

  @Test
  fun `sound effects manager initializes and handles distinct sound requests without crashing`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val soundManager = SoundEffectsManager(context)
    assertNotNull(soundManager)

    // Verify calling distinct sound functions executes cleanly
    soundManager.playButtonClick()
    soundManager.playNavTabClick()
    soundManager.playPositiveAction()
    soundManager.playCancelClick()
    soundManager.playSocialShareChime()
    soundManager.playDiamondShine()
    soundManager.playBidChime()
    soundManager.playTrumpReveal()
    soundManager.playTrickWin()

    soundManager.release()
  }

  @Test
  fun `linking social account awards diamonds and persists state`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = TwentyNineViewModel(app)

    val initialDiamonds = viewModel.playerEconomy.value.diamonds
    viewModel.linkSocialAccount(SocialPlatform.YOUTUBE, "https://youtube.com/@royal29champ")

    val updatedDiamonds = viewModel.playerEconomy.value.diamonds
    // First time linking awards 15 diamonds
    assertEquals(initialDiamonds + 15, updatedDiamonds)

    // Account should be recorded in linked list
    val linked = viewModel.linkedSocialAccounts.value
    assertTrue(linked.any { it.platform == SocialPlatform.YOUTUBE })

    // Sharing via social platform grants daily share reward (1 diamond once per day)
    val rewardPerShare = 1
    viewModel.recordSocialShare(SocialPlatform.YOUTUBE)

    val finalDiamonds = viewModel.playerEconomy.value.diamonds
    assertEquals(updatedDiamonds + rewardPerShare, finalDiamonds)
  }
}
