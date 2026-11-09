package com.example.model

data class VaultLeg(
    val id: String,
    val name: String,
    val subtitle: String,
    val allocation: Double,
    val apy: Double
) {
    val annualIncome: Double get() = allocation * (apy / 100.0)
}

data class BankAccount(
    val id: String,
    val institution: String,
    val accountName: String,
    val accountNumberMask: String,
    val balance: Double,
    val apy: Double,
    val isPrimarySweep: Boolean = false,
    val category: String = "Treasury Money Market",
    val maturityDate: String? = null,
    val purposeNotes: String = "",
    val webPortalUrl: String = "",
    val appPackageName: String = "",
    val lastSyncTime: String = "Live Connected",
    val isLiveConnected: Boolean = true
) {
    val annualInterest: Double get() = balance * (apy / 100.0)
    val monthlyInterest: Double get() = annualInterest / 12.0
    val dailyInterest: Double get() = annualInterest / 365.0
}

enum class LoanPaymentMode(val displayName: String) {
    AMORTIZING("Amortizing"),
    INTEREST_ONLY("Interest-Only"),
    PAID_OFF("Paid Off")
}

data class LoanItem(
    val id: String,
    val name: String,
    val category: String,
    val collateral: String,
    val balance: Double,
    val apr: Double,
    val monthlyPayment: Double,
    val mode: LoanPaymentMode,
    val entityObligor: String = "Smith Real Estate Holdings LLC",
    val maturityDate: String = "Oct 2039",
    val lender: String = "J.P. Morgan Private Wealth"
) {
    val annualDebtService: Double get() = if (mode == LoanPaymentMode.PAID_OFF || balance <= 0.0) 0.0 else monthlyPayment * 12.0
}

data class FamilyCard(
    val id: String,
    val cardholderName: String,
    val relation: String,
    val lastFour: String,
    val ytdSpend: Double,
    val annualCap: Double = 19000.0,
    val isFrozen: Boolean = false,
    val cardProduct: String = "Chase Sapphire Reserve® (Visa Infinite)"
) {
    val remainingCap: Double get() = (annualCap - ytdSpend).coerceAtLeast(0.0)
    val utilizationPct: Float get() = if (annualCap > 0) ((ytdSpend / annualCap) * 100).toFloat().coerceIn(0f, 100f) else 0f
}

data class MinorCustodialFund(
    val id: String = "custodial_amy_minors",
    val title: String = "Aunt Amy's Minors Custodial Trust Escrow",
    val institution: String = "J.P. Morgan Private Bank Custody",
    val accountNumberMask: String = "•••• 9812",
    val annualPerChild: Double = 19000.0,
    val numberOfChildren: Int = 6,
    val accumulatedBalance: Double = 114000.0,
    val apy: Double = 4.85,
    val maturityAge: Int = 18,
    val guardian: String = "Aunt Amy (Guardian) • Anthony Smith (Trustee)",
    val notes: String = "\$19,000/yr per child held in sovereign trust escrow for Aunt Amy's 6 minor children until age of majority."
) {
    val totalAnnualAllocation: Double get() = annualPerChild * numberOfChildren
    val monthlyCustodialSweep: Double get() = totalAnnualAllocation / 12.0
    val annualInterestYield: Double get() = accumulatedBalance * (apy / 100.0)
}

data class InsurancePolicy(
    val id: String,
    val name: String,
    val carrier: String,
    val category: String, // Whole Life ILIT, Key-Person Term, Platinum PPO, Global Evac
    val deathBenefit: Double,
    val cashValue: Double,
    val annualPremium: Double,
    val coverageHighlights: String
)

data class TrustProperty(
    val id: String,
    val name: String,
    val location: String,
    val listPrice: Double,
    val closingInvested: Double,
    val occupants: String,
    val annualEscrow: Double, // taxes, insurance, HOA, upkeep
    val notes: String,
    val photoUri: String? = null
)

data class FleetVehicle(
    val id: String,
    val yearModel: String,
    val dealer: String,
    val preTaxPrice: Double,
    val salesTaxRate: Double = 6.0, // MI 6% default
    val annualUpkeep: Double,
    val notes: String,
    val assetCategory: String = "Vehicle", // "Aircraft", "Vehicle", "Watercraft"
    val registrationNumber: String = "VIN •••• 9821", // Tail # or VIN/HIN
    val estimatedCurrentValuation: Double = preTaxPrice * 0.95,
    val nextMaintenanceDate: String = "Dec 15, 2026",
    val maintenanceSchedule: String = "Annual Certified Inspection",
    val maintenanceStatus: String = "Certified Active", // "Certified Flight-Ready", "Service Due Soon", "Certified Active", "In Maintenance Hold"
    val location: String = "Swaney TC Flagship Garage",
    val telemetryUsage: String = "7,500 Mi",
    val photoUri: String? = null
) {
    val salesTaxAmount: Double get() = preTaxPrice * (salesTaxRate / 100.0)
    val totalDeliveredCost: Double get() = preTaxPrice + salesTaxAmount
    val valueRetentionPct: Double get() = if (totalDeliveredCost > 0) (estimatedCurrentValuation / totalDeliveredCost) * 100.0 else 100.0
    val unrealizedValueDelta: Double get() = estimatedCurrentValuation - totalDeliveredCost
}

data class SubsidiaryLlc(
    val id: String,
    val name: String,
    val entityType: String,
    val purpose: String,
    val ownershipPct: Double = 100.0
)

data class BeverageSimState(
    val monthlyCans: Double = 85000.0,
    val pricePerCan: Double = 2.75,
    val grossMarginPct: Double = 48.0,
    val monthlyRetainer: Double = 12000.0
) {
    val annualGrossRevenue: Double get() = monthlyCans * 12.0 * pricePerCan
    val annualGrossMargin: Double get() = annualGrossRevenue * (grossMarginPct / 100.0)
    val annualRetainerExpense: Double get() = monthlyRetainer * 12.0
    val annualNetOperatingIncome: Double get() = annualGrossMargin - annualRetainerExpense
}

data class SnackSimState(
    val monthlyBags: Double = 50000.0,
    val pricePerBag: Double = 3.50,
    val grossMarginPct: Double = 42.0,
    val monthlyRetainer: Double = 8500.0
) {
    val annualGrossRevenue: Double get() = monthlyBags * 12.0 * pricePerBag
    val annualGrossMargin: Double get() = annualGrossRevenue * (grossMarginPct / 100.0)
    val annualRetainerExpense: Double get() = monthlyRetainer * 12.0
    val annualNetOperatingIncome: Double get() = annualGrossMargin - annualRetainerExpense
}

data class CompoundingSimState(
    val annualLifestyleSpend: Double = 400000.0,
    val horizonYears: Int = 10
)

data class JpMorganReserveState(
    val annualSpend: Double = 180000.0,
    val travelCredit: Double = 300.0
) {
    // 8x travel (25% share), 4x dining/travel (35% share), 1x base (40% share)
    val pointsTravel: Double get() = (annualSpend * 0.25) * 8.0
    val pointsDining: Double get() = (annualSpend * 0.35) * 4.0
    val pointsBase: Double get() = (annualSpend * 0.40) * 1.0
    val totalUrPoints: Double get() = pointsTravel + pointsDining + pointsBase
    val estimatedPointValueCash: Double get() = totalUrPoints * 0.015 // 1.5c redemption
}

enum class ColorPaletteMode(val displayName: String, val primaryHex: Long, val surfaceHex: Long, val backgroundHex: Long) {
    ROYAL_OBSIDIAN("Royal Obsidian", 0xFFA855F7, 0xFF140D26, 0xFF080410),
    EMERALD_SOVEREIGN("Emerald Sovereign", 0xFF10B981, 0xFF091F18, 0xFF040F0B),
    IMPERIAL_GOLD("Imperial Gold", 0xFFF59E0B, 0xFF1F1707, 0xFF0E0A02),
    MIDNIGHT_SAPPHIRE("Midnight Sapphire", 0xFF38BDF8, 0xFF0B192A, 0xFF040B13),
    CRIMSON_DYNASTY("Crimson Dynasty", 0xFFF43F5E, 0xFF240D14, 0xFF0E0407)
}

data class AppThemeConfig(
    val palette: ColorPaletteMode = ColorPaletteMode.ROYAL_OBSIDIAN,
    val customAccentArgb: Long? = null,
    val isTrueAmoled: Boolean = false,
    val enableGlassmorphism: Boolean = true,
    val isLightMode: Boolean = false
)
