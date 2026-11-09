package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class TrustState(
    val vaultLegs: List<VaultLeg> = DefaultData.getDefaultVaultLegs(),
    val bankAccounts: List<BankAccount> = DefaultData.getDefaultBankAccounts(),
    val loans: List<LoanItem> = DefaultData.getDefaultLoans(),
    val familyCards: List<FamilyCard> = DefaultData.getDefaultFamilyCards(),
    val insurancePolicies: List<InsurancePolicy> = DefaultData.getDefaultInsurancePolicies(),
    val properties: List<TrustProperty> = DefaultData.getDefaultProperties(),
    val fleet: List<FleetVehicle> = DefaultData.getDefaultFleet(),
    val llcs: List<SubsidiaryLlc> = DefaultData.getDefaultLlcs(),
    val minorCustodialFund: MinorCustodialFund = DefaultData.getDefaultMinorCustodialFund(),
    val beverageSim: BeverageSimState = BeverageSimState(),
    val snackSim: SnackSimState = SnackSimState(),
    val compoundingSim: CompoundingSimState = CompoundingSimState(),
    val jpReserve: JpMorganReserveState = JpMorganReserveState(),
    val llcTaxEquitySim: LlcTaxEquitySimState = LlcTaxEquitySimState(),
    val themeConfig: AppThemeConfig = AppThemeConfig()
) {
    // Top-Level Calculations
    val totalVaultCorpus: Double get() = vaultLegs.sumOf { it.allocation }
    val vaultAnnualYield: Double get() = vaultLegs.sumOf { it.annualIncome }
    val blendedVaultApy: Double get() = if (totalVaultCorpus > 0) (vaultAnnualYield / totalVaultCorpus) * 100.0 else 0.0

    val totalBankBalance: Double get() = bankAccounts.sumOf { it.balance }
    val totalBankInterest: Double get() = bankAccounts.sumOf { it.annualInterest }
    val blendedBankApy: Double get() = if (totalBankBalance > 0) (totalBankInterest / totalBankBalance) * 100.0 else 0.0

    val grossAnnualYield: Double get() = vaultAnnualYield + totalBankInterest
    val monthlyCashSweep: Double get() = grossAnnualYield / 12.0
    val dailyCashSweep: Double get() = grossAnnualYield / 365.0
    val quarterlyCashSweep: Double get() = grossAnnualYield / 4.0

    // Outflows
    val totalDebtServiceAnnual: Double get() = loans.sumOf { it.annualDebtService }
    val activeFamilyCardsAnnualCap: Double get() = familyCards.filter { !it.isFrozen }.sumOf { it.annualCap }
    val totalFamilyCardsYtdSpend: Double get() = familyCards.sumOf { it.ytdSpend }
    val minorCustodialEscrowAnnual: Double get() = minorCustodialFund.totalAnnualAllocation
    val totalFamilyGiftingAndCustodialAnnual: Double get() = activeFamilyCardsAnnualCap + minorCustodialEscrowAnnual
    val totalInsurancePremiumsAnnual: Double get() = insurancePolicies.sumOf { it.annualPremium }
    val totalRealEstateEscrowAnnual: Double get() = properties.sumOf { it.annualEscrow }
    val totalFleetUpkeepAnnual: Double get() = fleet.sumOf { it.annualUpkeep }

    val totalAnnualOutflows: Double get() = jpReserve.annualSpend +
            totalDebtServiceAnnual +
            activeFamilyCardsAnnualCap +
            minorCustodialEscrowAnnual +
            totalInsurancePremiumsAnnual +
            totalRealEstateEscrowAnnual +
            totalFleetUpkeepAnnual

    val pureSurplusAnnual: Double get() = grossAnnualYield - totalAnnualOutflows
    val monthlyPureSurplus: Double get() = pureSurplusAnnual / 12.0

    // Asset Holdings & Balance Sheet AUM
    val totalRealEstateInvested: Double get() = properties.sumOf { it.closingInvested }
    val totalFleetInvested: Double get() = fleet.sumOf { it.totalDeliveredCost }
    val totalFleetMarketValue: Double get() = fleet.sumOf { it.estimatedCurrentValuation }
    val totalInsuranceCashValue: Double get() = insurancePolicies.sumOf { it.cashValue }
    val totalInsuranceDeathBenefit: Double get() = insurancePolicies.sumOf { it.deathBenefit }
    val totalActiveDebtBalances: Double get() = loans.sumOf { it.balance }

    // Whole life policy borrowing capacity
    val wholeLifeMaxBorrowLine: Double get() = totalInsuranceCashValue * 0.90
    val wholeLifePolicyLoanBalance: Double get() = loans.firstOrNull { it.category.contains("Policy", ignoreCase = true) }?.balance ?: 0.0
    val remainingPolicyBorrowCapacity: Double get() = (wholeLifeMaxBorrowLine - wholeLifePolicyLoanBalance).coerceAtLeast(0.0)

    val trustAum: Double get() = totalVaultCorpus + totalBankBalance + totalRealEstateInvested + totalFleetInvested + totalInsuranceCashValue - totalActiveDebtBalances
}

class TrustRepository(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("smith_trust_vault_prefs", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(loadInitialState())
    val state: StateFlow<TrustState> = _state.asStateFlow()

    fun updateState(transform: (TrustState) -> TrustState) {
        val updated = transform(_state.value)
        _state.value = updated
        saveState(updated)
    }

    fun resetToDefaults() {
        val defaultState = TrustState()
        _state.value = defaultState
        saveState(defaultState)
    }

    private fun loadInitialState(): TrustState {
        val raw = prefs.getString("saved_state_json", null) ?: return TrustState()
        return try {
            val root = JSONObject(raw)

            // Theme
            val paletteStr = root.optString("palette", ColorPaletteMode.ROYAL_OBSIDIAN.name)
            val palette = try { ColorPaletteMode.valueOf(paletteStr) } catch (_: Exception) { ColorPaletteMode.ROYAL_OBSIDIAN }
            val customAccent = if (root.has("customAccent")) root.getLong("customAccent") else null
            val isAmoled = root.optBoolean("isAmoled", false)
            val glass = root.optBoolean("glass", true)
            val isLightMode = root.optBoolean("isLightMode", false)
            val themeConfig = AppThemeConfig(palette, customAccent, isAmoled, glass, isLightMode)

            // Simulators
            val bevCans = root.optDouble("bevCans", 85000.0)
            val bevPrice = root.optDouble("bevPrice", 2.75)
            val bevMargin = root.optDouble("bevMargin", 48.0)
            val bevRetainer = root.optDouble("bevRetainer", 12000.0)
            val bev = BeverageSimState(bevCans, bevPrice, bevMargin, bevRetainer)

            val snackBags = root.optDouble("snackBags", 50000.0)
            val snackPrice = root.optDouble("snackPrice", 3.50)
            val snackMargin = root.optDouble("snackMargin", 42.0)
            val snackRetainer = root.optDouble("snackRetainer", 8500.0)
            val snack = SnackSimState(snackBags, snackPrice, snackMargin, snackRetainer)

            val compSpend = root.optDouble("compSpend", 400000.0)
            val compYears = root.optInt("compYears", 10)
            val comp = CompoundingSimState(compSpend, compYears)

            val jpSpend = root.optDouble("jpSpend", 180000.0)
            val jp = JpMorganReserveState(jpSpend)

            val bankAccounts = if (root.has("bankAccounts")) {
                val arr = root.getJSONArray("bankAccounts")
                val list = mutableListOf<BankAccount>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    list.add(
                        BankAccount(
                            id = o.optString("id", java.util.UUID.randomUUID().toString()),
                            institution = o.optString("institution", "J.P. Morgan Private Bank"),
                            accountName = o.optString("accountName", "Treasury Account"),
                            accountNumberMask = o.optString("accountNumberMask", "•••• 0000"),
                            balance = o.optDouble("balance", 0.0),
                            apy = o.optDouble("apy", 4.85),
                            isPrimarySweep = o.optBoolean("isPrimarySweep", false),
                            category = o.optString("category", "Treasury Money Market"),
                            maturityDate = o.optString("maturityDate").takeIf { it.isNotBlank() },
                            purposeNotes = o.optString("purposeNotes", ""),
                            webPortalUrl = o.optString("webPortalUrl", ""),
                            appPackageName = o.optString("appPackageName", ""),
                            lastSyncTime = o.optString("lastSyncTime", "Live Connected"),
                            isLiveConnected = o.optBoolean("isLiveConnected", true)
                        )
                    )
                }
                if (list.isNotEmpty()) list else DefaultData.getDefaultBankAccounts()
            } else {
                DefaultData.getDefaultBankAccounts()
            }

            TrustState(
                themeConfig = themeConfig,
                bankAccounts = bankAccounts,
                beverageSim = bev,
                snackSim = snack,
                compoundingSim = comp,
                jpReserve = jp
            )
        } catch (_: Exception) {
            TrustState()
        }
    }

    private fun saveState(s: TrustState) {
        try {
            val root = JSONObject()
            root.put("palette", s.themeConfig.palette.name)
            s.themeConfig.customAccentArgb?.let { root.put("customAccent", it) }
            root.put("isAmoled", s.themeConfig.isTrueAmoled)
            root.put("glass", s.themeConfig.enableGlassmorphism)
            root.put("isLightMode", s.themeConfig.isLightMode)

            root.put("bevCans", s.beverageSim.monthlyCans)
            root.put("bevPrice", s.beverageSim.pricePerCan)
            root.put("bevMargin", s.beverageSim.grossMarginPct)
            root.put("bevRetainer", s.beverageSim.monthlyRetainer)

            root.put("snackBags", s.snackSim.monthlyBags)
            root.put("snackPrice", s.snackSim.pricePerBag)
            root.put("snackMargin", s.snackSim.grossMarginPct)
            root.put("snackRetainer", s.snackSim.monthlyRetainer)

            root.put("compSpend", s.compoundingSim.annualLifestyleSpend)
            root.put("compYears", s.compoundingSim.horizonYears)
            root.put("jpSpend", s.jpReserve.annualSpend)

            val bankArray = JSONArray()
            s.bankAccounts.forEach { b ->
                val obj = JSONObject()
                obj.put("id", b.id)
                obj.put("institution", b.institution)
                obj.put("accountName", b.accountName)
                obj.put("accountNumberMask", b.accountNumberMask)
                obj.put("balance", b.balance)
                obj.put("apy", b.apy)
                obj.put("isPrimarySweep", b.isPrimarySweep)
                obj.put("category", b.category)
                obj.put("maturityDate", b.maturityDate ?: "")
                obj.put("purposeNotes", b.purposeNotes)
                obj.put("webPortalUrl", b.webPortalUrl)
                obj.put("appPackageName", b.appPackageName)
                obj.put("lastSyncTime", b.lastSyncTime)
                obj.put("isLiveConnected", b.isLiveConnected)
                bankArray.put(obj)
            }
            root.put("bankAccounts", bankArray)

            prefs.edit().putString("saved_state_json", root.toString()).apply()
        } catch (_: Exception) {
            // graceful ignore
        }
    }
}
