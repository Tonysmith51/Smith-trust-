package com.example.model

enum class VaultAccountCategory(val displayName: String) {
    ALL("All Accounts"),
    FIXED_INCOME("Fixed Income"),
    LIQUID_CASH("Cash & MMAs"),
    STRATEGIC_COLLATERAL("Collateral"),
    ESCROW_RESERVES("Reserves & Escrow")
}

data class VaultAccount(
    val id: String,
    val name: String,
    val institution: String,
    val accountMask: String,
    val category: VaultAccountCategory,
    val balance: Double,
    val apy: Double,
    val status: String = "Active",
    val notes: String = "",
    val isTaxExempt: Boolean = false,
    val webPortalUrl: String = "",
    val appPackageName: String = ""
) {
    val annualYield: Double get() = balance * (apy / 100.0)
    val monthlyYield: Double get() = annualYield / 12.0
}
