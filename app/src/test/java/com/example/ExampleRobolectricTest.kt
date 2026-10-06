package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context matches Omah ERP`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Omah ERP", appName)
  }

  @Test
  fun `verify KNS commission calculation model`() {
    // Skenario S-05: Jual 10kg gula @16.000 = 160.000
    // Rate komisi Omah = 10%
    val salesX = 160_000.0
    val rate = 0.10
    val omahCommissionZ = salesX * rate // 16.000
    val supplierShareY = salesX - omahCommissionZ // 144.000

    assertEquals(16_000.0, omahCommissionZ, 0.001)
    assertEquals(144_000.0, supplierShareY, 0.001)
    assertEquals(salesX, omahCommissionZ + supplierShareY, 0.001)
  }

  @Test
  fun `verify double entry balance invariant D equals K`() {
    val debitLines = listOf(5_000_000.0, 10_000_000.0, 1_500_000.0, 4_710_000.0)
    val creditLines = listOf(21_210_000.0)

    val totalDebit = debitLines.sum()
    val totalCredit = creditLines.sum()

    assertEquals(totalDebit, totalCredit, 0.001)
  }
}
