package com.example.model

data class BankInstitutionPreset(
    val id: String,
    val name: String,
    val shortName: String,
    val defaultAccountName: String,
    val webPortalUrl: String,
    val appPackageName: String,
    val defaultCategory: String = "Treasury Money Market",
    val defaultApy: Double = 4.85,
    val playStoreUrl: String = "https://play.google.com/store/apps/details?id=$appPackageName"
)

object BankInstitutionCatalog {
    val presets = listOf(
        BankInstitutionPreset(
            id = "preset_jpmorgan",
            name = "J.P. Morgan Private Bank",
            shortName = "J.P. Morgan",
            defaultAccountName = "J.P. Morgan Treasury Money Market",
            webPortalUrl = "https://privatebank.jpmorgan.com/gl/en/log-in",
            appPackageName = "com.jpmorgan.privatebank",
            defaultCategory = "Treasury Money Market (Liquid)",
            defaultApy = 4.85
        ),
        BankInstitutionPreset(
            id = "preset_chase",
            name = "Chase Mobile & Private Client",
            shortName = "Chase",
            defaultAccountName = "Chase Private Client Reserve",
            webPortalUrl = "https://secure.chase.com",
            appPackageName = "com.chase.sig.android",
            defaultCategory = "Liquid Operating Account",
            defaultApy = 4.50
        ),
        BankInstitutionPreset(
            id = "preset_treasurydirect",
            name = "U.S. TreasuryDirect",
            shortName = "TreasuryDirect",
            defaultAccountName = "U.S. Treasury Direct Institutional Account",
            webPortalUrl = "https://www.treasurydirect.gov/RS/PWOLGate",
            appPackageName = "gov.treasury.treasurydirect",
            defaultCategory = "Short-Term U.S. Treasury Bills",
            defaultApy = 4.95
        ),
        BankInstitutionPreset(
            id = "preset_bofa",
            name = "Bank of America Private Bank / Merrill",
            shortName = "BofA Merrill",
            defaultAccountName = "Bank of America Preferred Liquidity",
            webPortalUrl = "https://www.bankofamerica.com",
            appPackageName = "com.infonow.bofa",
            defaultCategory = "Treasury Liquidity Sweep",
            defaultApy = 4.70
        ),
        BankInstitutionPreset(
            id = "preset_fidelity",
            name = "Fidelity Investments",
            shortName = "Fidelity",
            defaultAccountName = "Fidelity Government Money Market (SPAXX)",
            webPortalUrl = "https://www.fidelity.com",
            appPackageName = "com.fidelity.android",
            defaultCategory = "Government Money Market",
            defaultApy = 4.75
        ),
        BankInstitutionPreset(
            id = "preset_schwab",
            name = "Charles Schwab Wealth Advisory",
            shortName = "Schwab",
            defaultAccountName = "Schwab Value Advantage Money Fund (SWVXX)",
            webPortalUrl = "https://www.schwab.com",
            appPackageName = "com.schwab.mobile",
            defaultCategory = "Government Cash Reserve",
            defaultApy = 4.80
        ),
        BankInstitutionPreset(
            id = "preset_wells",
            name = "Wells Fargo Advisors / Private Bank",
            shortName = "Wells Fargo",
            defaultAccountName = "Wells Fargo Treasury Sweep",
            webPortalUrl = "https://www.wellsfargo.com",
            appPackageName = "com.wf.wellsfargomobile",
            defaultCategory = "Liquid Cash Sweep",
            defaultApy = 4.60
        ),
        BankInstitutionPreset(
            id = "preset_citi",
            name = "Citibank Private Bank",
            shortName = "Citi",
            defaultAccountName = "Citi Private Treasury Pool",
            webPortalUrl = "https://online.citi.com",
            appPackageName = "com.citi.citimobile",
            defaultCategory = "Treasury Money Market",
            defaultApy = 4.65
        ),
        BankInstitutionPreset(
            id = "preset_morganstanley",
            name = "Morgan Stanley Wealth Management",
            shortName = "Morgan Stanley",
            defaultAccountName = "Morgan Stanley Preferred Savings",
            webPortalUrl = "https://www.morganstanley.com",
            appPackageName = "com.morganstanley.wealth",
            defaultCategory = "Wealth Management Sweep",
            defaultApy = 4.75
        ),
        BankInstitutionPreset(
            id = "preset_huntington",
            name = "Huntington Private Bank",
            shortName = "Huntington",
            defaultAccountName = "Huntington Private Commercial Sweep",
            webPortalUrl = "https://www.huntington.com",
            appPackageName = "com.huntington.m",
            defaultCategory = "Regional Commercial Reserve",
            defaultApy = 4.70
        ),
        BankInstitutionPreset(
            id = "preset_custom",
            name = "Custom Institution / Family Office",
            shortName = "Custom Bank",
            defaultAccountName = "Institutional Custodial Account",
            webPortalUrl = "https://",
            appPackageName = "",
            defaultCategory = "Liquid Reserves",
            defaultApy = 4.85
        )
    )

    fun findPresetByInstitution(institution: String): BankInstitutionPreset? {
        val lower = institution.lowercase()
        return presets.firstOrNull { preset ->
            lower.contains(preset.shortName.lowercase()) ||
            lower.contains(preset.name.lowercase()) ||
            preset.name.lowercase().contains(lower)
        }
    }
}
