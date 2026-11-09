package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MockVaultAccountDataProvider
import com.example.data.TrustState
import com.example.model.EntityTaxClassification
import com.example.model.LlcTaxEquitySimState
import com.example.model.StateTaxNexus
import com.example.model.VaultAccountCategory
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
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Smith Trust", appName)
  }

  @Test
  fun `verify trust blueprint baseline calculations`() {
    val state = TrustState()
    assertEquals(100_000_000.0, state.totalVaultCorpus, 0.01)
    assertTrue("Gross annual yield should exceed $3.5M", state.grossAnnualYield > 3_500_000.0)
    assertTrue("Pure surplus should be positive", state.pureSurplusAnnual > 2_000_000.0)
    assertEquals(11, state.familyCards.size)
    assertEquals(7, state.properties.size)
    assertEquals(6, state.fleet.size)
  }

  @Test
  fun `verify mock vault account data provider`() {
    val provider = MockVaultAccountDataProvider()
    val accounts = provider.getSummaryAccounts()

    // Verify 11 distinct sovereign vault accounts
    assertEquals(11, accounts.size)

    // Total balance matches $166,387,500.00
    val totalBalance = provider.getTotalBalance()
    assertEquals(166_387_500.0, totalBalance, 0.01)

    // Verify category filtering
    val fixedIncomeAccounts = provider.getAccountsByCategory(VaultAccountCategory.FIXED_INCOME)
    assertEquals(3, fixedIncomeAccounts.size)
    assertEquals(100_000_000.0, provider.getCategoryTotal(VaultAccountCategory.FIXED_INCOME), 0.01)

    val cashAccounts = provider.getAccountsByCategory(VaultAccountCategory.LIQUID_CASH)
    assertEquals(4, cashAccounts.size)
    assertEquals(62_287_500.0, provider.getCategoryTotal(VaultAccountCategory.LIQUID_CASH), 0.01)

    // Blended APY is positive (~4.0%)
    val blendedApy = provider.getBlendedApy()
    assertTrue("Blended APY should be > 3%", blendedApy > 3.0)
  }

  @Test
  fun `verify llc tax and equity simulator calculations`() {
    val sim = LlcTaxEquitySimState(
      entityName = "Smith Asset Holdings LLC",
      hypotheticalAssetValue = 15_000_000.0,
      annualNoiYieldPct = 8.0,
      taxClassification = EntityTaxClassification.DISREGARDED_SINGLE_MEMBER,
      stateNexus = StateTaxNexus.DELAWARE,
      depreciationShieldPct = 20.0,
      trustEquityPct = 70.0,
      managingMemberPct = 20.0,
      familyPoolPct = 10.0
    )

    // Pre-Tax Cash Flow = $15M * 8% = $1,200,000
    assertEquals(1_200_000.0, sim.grossAnnualDistribution, 0.01)

    // Total Equity should sum to 100%
    assertEquals(100.0, sim.totalEquityPct, 0.01)

    // Verify Depreciation Shield reduces taxable income
    assertTrue("Depreciation deduction should be positive", sim.annualDepreciationDeduction > 0)
    assertTrue("Taxable income should be lower than gross", sim.taxableIncome < sim.grossAnnualDistribution)

    // Net After-Tax Cash Flow should be positive and distributed accurately
    assertTrue("Net after-tax distribution should be positive", sim.netAfterTaxDistribution > 0)
    val sumOfMemberCash = sim.trustAnnualDistribution + sim.managingMemberAnnualDistribution + sim.familyPoolAnnualDistribution
    assertEquals(sim.netAfterTaxDistribution, sumOfMemberCash, 1.0)

    // Member book equity values should sum to total asset value ($15M)
    val sumOfEquityValues = sim.trustEquityValue + sim.managingMemberEquityValue + sim.familyPoolEquityValue
    assertEquals(15_000_000.0, sumOfEquityValues, 0.01)

    // Future exit proceeds should account for appreciation
    assertTrue("Exit valuation should exceed initial asset value", sim.projectedExitValuation > 15_000_000.0)
    assertTrue("Net exit proceeds should be positive", sim.netExitProceeds > 0)
  }

  @Test
  fun `verify family entity loan ledger table integrity`() {
    val state = TrustState()
    val loans = state.loans

    assertTrue("Should have multiple entity loans", loans.size >= 5)

    // Verify entity obligors are tracked
    val entities = loans.map { it.entityObligor }.distinct()
    assertTrue("Should track Real Estate LLC", entities.contains("Smith Real Estate Holdings LLC"))
    assertTrue("Should track Master Trust", entities.contains("Anthony Smith Master Trust"))
    assertTrue("Should track Fleet LLC", entities.contains("Smith Aviation & Fleet LLC"))

    // Verify maturities and interest rates are non-blank
    loans.forEach { loan ->
      assertTrue("Maturity date should be populated", loan.maturityDate.isNotBlank())
      assertTrue("APR should be positive", loan.apr > 0)
      assertTrue("Lender should be populated", loan.lender.isNotBlank())
    }

    // Verify total debt service calculation
    val totalDebt = loans.sumOf { it.balance }
    val totalService = loans.sumOf { it.annualDebtService }
    assertTrue("Total principal debt should exceed $2M", totalDebt > 2_000_000.0)
    assertTrue("Annual debt service should be positive", totalService > 0)
  }

  @Test
  fun `verify fleet monitoring module asset tracking and maintenance schedules`() {
    val state = TrustState()
    val fleet = state.fleet

    // Verify 6 assets across Aircraft, Ground Vehicles, and Marine Craft
    assertEquals(6, fleet.size)

    val aircraft = fleet.filter { it.assetCategory.equals("Aircraft", ignoreCase = true) }
    assertEquals(2, aircraft.size)
    assertTrue("Aircraft tail numbers tracked", aircraft.any { it.registrationNumber == "Tail N704AS" })
    assertTrue("Helicopter tail numbers tracked", aircraft.any { it.registrationNumber == "Tail N505ST" })

    val vehicles = fleet.filter { it.assetCategory.equals("Vehicle", ignoreCase = true) }
    assertEquals(3, vehicles.size)

    val watercraft = fleet.filter { it.assetCategory.equals("Watercraft", ignoreCase = true) }
    assertEquals(1, watercraft.size)

    // Valuation calculations
    assertTrue("Total fleet market value should exceed $7.5M", state.totalFleetMarketValue > 7_500_000.0)
    assertTrue("Total fleet capital invested should exceed $8M", state.totalFleetInvested > 8_000_000.0)

    // Maintenance schedule and status verification
    fleet.forEach { asset ->
      assertTrue("Registration number must not be blank", asset.registrationNumber.isNotBlank())
      assertTrue("Next maintenance date must not be blank", asset.nextMaintenanceDate.isNotBlank())
      assertTrue("Maintenance schedule must not be blank", asset.maintenanceSchedule.isNotBlank())
      assertTrue("Location/Hangar must not be blank", asset.location.isNotBlank())
      assertTrue("Telemetry usage must not be blank", asset.telemetryUsage.isNotBlank())
      assertTrue("Estimated current valuation must be positive", asset.estimatedCurrentValuation > 0)
    }

    // Verify maintenance status distribution
    assertTrue("At least one asset should be Certified Flight-Ready", fleet.any { it.maintenanceStatus == "Certified Flight-Ready" })
    assertTrue("At least one asset should be Service Due Soon", fleet.any { it.maintenanceStatus == "Service Due Soon" })
    assertTrue("At least one asset should be in Maintenance Hold", fleet.any { it.maintenanceStatus == "In Maintenance Hold" })
  }

  @Test
  fun `verify family chase sapphire reserve cards and aunt amy minors custodial escrow`() {
    val state = TrustState()
    val cards = state.familyCards

    // Verify exactly 11 family members have cards
    assertEquals(11, cards.size)

    val expectedNames = listOf(
      "Mom", "Dad", "Lana", "Arrin", "Nana", "Shauna", "Aunt Amy", "Uncle Mike", "Danny", "Katie", "Bethany"
    )
    expectedNames.forEach { name ->
      assertTrue("Family member $name should have a card", cards.any { it.cardholderName.equals(name, ignoreCase = true) })
    }

    // Verify each card is Chase Sapphire Reserve with 19k limit
    cards.forEach { card ->
      assertEquals(19_000.0, card.annualCap, 0.01)
      assertTrue("Should be Chase Sapphire Reserve", card.cardProduct.contains("Sapphire", ignoreCase = true))
      assertTrue("Should have remaining cap tracked", card.remainingCap >= 0)
    }

    // Total cards budget = 11 * 19k = $209,000
    assertEquals(209_000.0, state.activeFamilyCardsAnnualCap, 0.01)

    // Verify Aunt Amy's minor custodial fund for her 6 young kids
    val minorFund = state.minorCustodialFund
    assertEquals(6, minorFund.numberOfChildren)
    assertEquals(19_000.0, minorFund.annualPerChild, 0.01)
    assertEquals(114_000.0, minorFund.totalAnnualAllocation, 0.01) // 6 * 19k = $114,000
    assertTrue("Escrow balance should be funded", minorFund.accumulatedBalance >= 114_000.0)

    // Verify custodial bank account exists in bankAccounts list
    val custodialAccount = state.bankAccounts.find { it.id == "bank_amy_minors_custodial" }
    assertTrue("Separate custodial escrow bank account must exist", custodialAccount != null)
    assertEquals(114_000.0, custodialAccount!!.balance, 0.01)
    assertEquals(4.85, custodialAccount.apy, 0.01)

    // Total family gifting and custodial allocation = $209,000 + $114,000 = $323,000
    assertEquals(323_000.0, state.totalFamilyGiftingAndCustodialAnnual, 0.01)
  }

  @Test
  fun `verify notifications, photo uri tracking, and gemini assistant context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val state = TrustState()

    // 1. Verify Notification Channels initialization
    com.example.util.NotificationHelper.initNotificationChannels(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val alertsChannel = notificationManager.getNotificationChannel(com.example.util.NotificationHelper.CHANNEL_SOVEREIGN_ALERTS)
    assertTrue("Sovereign alerts notification channel should exist", alertsChannel != null)

    // 2. Verify Photo URI support on Properties and Fleet
    val sampleProperty = state.properties.first().copy(photoUri = "content://media/external/images/media/42")
    assertEquals("content://media/external/images/media/42", sampleProperty.photoUri)

    val sampleVehicle = state.fleet.first().copy(photoUri = "content://media/external/images/media/99")
    assertEquals("content://media/external/images/media/99", sampleVehicle.photoUri)

    // 3. Verify Gemini Assistant Context generation
    val assistant = com.example.ai.TrustGeminiAssistant()
    val briefing = assistant.buildSystemContext(state)
    assertTrue("Briefing should reference Master Vault", briefing.contains("Master Vault Corpus"))
    assertTrue("Briefing should reference 11 Sapphire cards", briefing.contains("11 authorized Chase Sapphire Reserve"))
    assertTrue("Briefing should reference Aunt Amy's minors escrow", briefing.contains("Aunt Amy's 6 Minor Children Custodial Escrow"))
    assertTrue("Briefing should reference Pilatus PC-12", briefing.contains("Pilatus PC-12 NGX"))
    assertTrue("Briefing should reference Bell 505", briefing.contains("Bell 505 Jet Ranger X"))
  }

  @Test
  fun `verify updated bank accounts with JPM liquid fund and April 15 2027 T-Bills tax escrow`() {
    val state = TrustState()
    val accounts = state.bankAccounts

    // 1. Verify J.P. Morgan Treasury Money Market account
    val jpmLiquid = accounts.find { it.id == "bank_jpm_liquid" }
    assertTrue("J.P. Morgan Treasury MMA account must exist", jpmLiquid != null)
    assertEquals(31_761_543.0, jpmLiquid!!.balance, 0.01)
    assertEquals(4.85, jpmLiquid.apy, 0.01)
    assertTrue("Should be primary sweep", jpmLiquid.isPrimarySweep)
    assertTrue("Should mention houses or cars in notes", jpmLiquid.purposeNotes.contains("houses", ignoreCase = true))

    // 2. Verify April 15, 2027 T-Bills Tax Escrow account
    val tbillsTax = accounts.find { it.id == "bank_tbills_tax_2027" }
    assertTrue("April 15, 2027 Tax Escrow account must exist", tbillsTax != null)
    assertEquals(29_101_957.0, tbillsTax!!.balance, 0.01)
    assertEquals(4.95, tbillsTax.apy, 0.01)
    assertEquals("April 15, 2027", tbillsTax.maturityDate)
    assertTrue("Should mention tax liability in notes", tbillsTax.purposeNotes.contains("tax", ignoreCase = true))

    // 3. Verify Vault Yield Tax Reinvestment Reserve
    val vaultTaxSweep = accounts.find { it.id == "bank_vault_yield_tax" }
    assertTrue("Vault yield tax sweep account must exist", vaultTaxSweep != null)
    assertEquals(1_310_000.0, vaultTaxSweep!!.balance, 0.01)
    assertEquals(4.90, vaultTaxSweep.apy, 0.01)

    // 4. Verify Aunt Amy's Minors Escrow ($114k)
    val custodial = accounts.find { it.id == "bank_amy_minors_custodial" }
    assertTrue("Aunt Amy's custodial escrow must exist", custodial != null)
    assertEquals(114_000.0, custodial!!.balance, 0.01)

    // 5. Total Bank & T-Bills Portfolio
    assertEquals(62_287_500.0, state.totalBankBalance, 0.01)
    assertTrue("Total bank interest yield should exceed $3M/yr", state.totalBankInterest > 3_000_000.0)
    assertTrue("Blended APY should be ~4.9%", state.blendedBankApy > 4.8)
  }

  @Test
  fun `verify JPMorgan treasury monthly spending trends and D3 chart dataset`() {
    val spendingData = com.example.ui.components.DefaultTreasurySpendingData.trailing12Months
    assertEquals(12, spendingData.size)

    // Total spend should exceed $3M across trailing 12 months
    val totalSpend = spendingData.sumOf { it.totalSpend }
    assertTrue("Total spend should be substantial", totalSpend > 3_000_000.0)

    val avgSpend = totalSpend / spendingData.size
    assertTrue("Monthly average spend should be > $200k", avgSpend > 200_000.0)

    // Verify peak month (September $620k for Cedar compound runway)
    val maxMonth = spendingData.maxByOrNull { it.totalSpend }
    assertTrue("Peak month should exist", maxMonth != null)
    assertEquals("September", maxMonth!!.monthName)
    assertEquals(620_000.0, maxMonth.totalSpend, 0.01)

    // Verify treasury coverage ratio against $31,761,543 balance
    val treasuryBalance = 31_761_543.0
    val coverageMonths = treasuryBalance / avgSpend
    assertTrue("Treasury balance should cover over 50 months of spending", coverageMonths > 50.0)
  }

  @Test
  fun `verify light mode theme configuration state`() {
    val darkConfig = com.example.model.AppThemeConfig(isLightMode = false)
    val lightConfig = com.example.model.AppThemeConfig(isLightMode = true)

    assertTrue("Default should be dark", !darkConfig.isLightMode)
    assertTrue("Light mode should be active when enabled", lightConfig.isLightMode)
  }

  @Test
  fun `verify vault account editing and addition`() {
    val provider = MockVaultAccountDataProvider()
    val initialTotal = provider.getTotalBalance()

    val newAccount = com.example.model.VaultAccount(
      id = "custom_new_sweep",
      name = "Grand Rapids Commercial Liquidity Account",
      institution = "Huntington Bank",
      accountMask = "•••• 4410",
      category = VaultAccountCategory.LIQUID_CASH,
      balance = 500_000.0,
      apy = 4.75,
      status = "Operational",
      notes = "Local commercial sweep facility",
      isTaxExempt = false
    )

    provider.addAccount(newAccount)
    assertEquals(initialTotal + 500_000.0, provider.getTotalBalance(), 0.01)

    val updatedAccount = newAccount.copy(balance = 750_000.0)
    provider.updateAccount(updatedAccount)
    assertEquals(initialTotal + 750_000.0, provider.getTotalBalance(), 0.01)

    provider.deleteAccount("custom_new_sweep")
    assertEquals(initialTotal, provider.getTotalBalance(), 0.01)
  }

  @Test
  fun `verify bank account app package, web portal url, and daily interest yield`() {
    val state = TrustState()
    val jpm = state.bankAccounts.find { it.id == "bank_jpm_liquid" }
    assertTrue("J.P. Morgan account must exist", jpm != null)
    assertEquals("com.jpmorgan.privatebank", jpm!!.appPackageName)
    assertTrue("Web portal URL should point to J.P. Morgan", jpm.webPortalUrl.contains("jpmorgan.com"))
    assertTrue("Daily interest yield should be positive", jpm.dailyInterest > 0)

    val expectedDaily = 31_761_543.0 * (4.85 / 100.0) / 365.0
    assertEquals(expectedDaily, jpm.dailyInterest, 0.01)

    val tbills = state.bankAccounts.find { it.id == "bank_tbills_tax_2027" }
    assertTrue("Treasury Direct account must exist", tbills != null)
    assertEquals("gov.treasury.treasurydirect", tbills!!.appPackageName)
    assertTrue("Web portal URL should point to TreasuryDirect", tbills.webPortalUrl.contains("treasurydirect.gov"))
  }

  @Test
  fun `verify bank institution catalog presets and search`() {
    val presets = com.example.model.BankInstitutionCatalog.presets
    assertTrue("Should contain multiple presets", presets.size >= 8)

    val jpmPreset = com.example.model.BankInstitutionCatalog.findPresetByInstitution("J.P. Morgan Private Bank")
    assertTrue("J.P. Morgan preset found", jpmPreset != null)
    assertEquals("com.jpmorgan.privatebank", jpmPreset!!.appPackageName)

    val chasePreset = com.example.model.BankInstitutionCatalog.findPresetByInstitution("Chase")
    assertTrue("Chase preset found", chasePreset != null)
    assertEquals("com.chase.sig.android", chasePreset!!.appPackageName)
    assertTrue("Chase web portal URL set", chasePreset.webPortalUrl.contains("chase.com"))

    val fedPreset = com.example.model.BankInstitutionCatalog.findPresetByInstitution("TreasuryDirect")
    assertTrue("TreasuryDirect preset found", fedPreset != null)
    assertTrue("TreasuryDirect web portal set", fedPreset!!.webPortalUrl.contains("treasurydirect.gov"))
  }
}



