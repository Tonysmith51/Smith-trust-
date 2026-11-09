package com.example.data

import com.example.model.VaultAccount
import com.example.model.VaultAccountCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface VaultAccountDataProvider {
    val accountsFlow: StateFlow<List<VaultAccount>>
    fun getSummaryAccounts(): List<VaultAccount>
    fun getAccountsByCategory(category: VaultAccountCategory): List<VaultAccount>
    fun getTotalBalance(): Double
    fun getCategoryTotal(category: VaultAccountCategory): Double
    fun getBlendedApy(): Double
    fun getTotalAnnualYield(): Double
    fun refreshAccounts()
    fun updateAccount(account: VaultAccount)
    fun addAccount(account: VaultAccount)
    fun deleteAccount(id: String)
}

class MockVaultAccountDataProvider : VaultAccountDataProvider {

    private val initialAccounts = listOf(
        VaultAccount(
            id = "va_tbills",
            name = "3-Mo US Treasury Bills & Short Paper",
            institution = "US Treasury Direct & J.P. Morgan Custody",
            accountMask = "•••• 1092",
            category = VaultAccountCategory.FIXED_INCOME,
            balance = 33000000.0,
            apy = 3.28,
            status = "Sovereign Tier-1",
            notes = "Ultra-liquid sovereign debt with rolling 90-day maturities; collateralized at par.",
            isTaxExempt = false
        ),
        VaultAccount(
            id = "va_treasuries",
            name = "10-Year US Treasury Benchmark Ladder",
            institution = "BNY Mellon Institutional Sovereign Desk",
            accountMask = "•••• 3381",
            category = VaultAccountCategory.FIXED_INCOME,
            balance = 33000000.0,
            apy = 3.45,
            status = "Benchmark Ladder",
            notes = "Semi-annual coupon distributions automatically swept to master cash pool.",
            isTaxExempt = false
        ),
        VaultAccount(
            id = "va_munis",
            name = "AAA Michigan Tax-Exempt Munis",
            institution = "Michigan Municipal Finance Authority",
            accountMask = "•••• 7704",
            category = VaultAccountCategory.FIXED_INCOME,
            balance = 34000000.0,
            apy = 3.88,
            status = "Triple Tax-Exempt",
            notes = "Exempt from Federal, Michigan State, and local municipal income taxes (IRC §103).",
            isTaxExempt = true
        ),
        VaultAccount(
            id = "va_jpm",
            name = "J.P. Morgan Treasury Money Market (CapEx & Acquisitions)",
            institution = "J.P. Morgan Private Bank",
            accountMask = "•••• 9821",
            category = VaultAccountCategory.LIQUID_CASH,
            balance = 31761543.0,
            apy = 4.85,
            status = "Liquid CapEx Reserve",
            notes = "Primary liquid fund to buy houses, vehicles, aircraft, and CapEx while earning money market interest.",
            isTaxExempt = false,
            webPortalUrl = "https://privatebank.jpmorgan.com/gl/en/log-in",
            appPackageName = "com.jpmorgan.privatebank"
        ),
        VaultAccount(
            id = "va_tbills_tax",
            name = "Short-Term U.S. T-Bills Tax Escrow",
            institution = "J.P. Morgan / U.S. Treasury Direct",
            accountMask = "•••• 2027",
            category = VaultAccountCategory.LIQUID_CASH,
            balance = 29101957.0,
            apy = 4.95,
            status = "Due April 15, 2027",
            notes = "Tax escrow invested in U.S. T-Bills maturing April 15, 2027 (state & local tax-exempt).",
            isTaxExempt = true,
            webPortalUrl = "https://www.treasurydirect.gov/RS/PWOLGate",
            appPackageName = "gov.treasury.treasurydirect"
        ),
        VaultAccount(
            id = "va_vault_yield_tax",
            name = "Vault Yield Tax Reinvestment (Rolling T-Bills)",
            institution = "J.P. Morgan / U.S. Treasury Direct",
            accountMask = "•••• 3700",
            category = VaultAccountCategory.LIQUID_CASH,
            balance = 1310000.0,
            apy = 4.90,
            status = "Rolling T-Bills",
            notes = "Automated 37% tax sweep on \$100M vault earnings reinvested in rolling short-term T-Bills.",
            isTaxExempt = true,
            webPortalUrl = "https://privatebank.jpmorgan.com/gl/en/log-in",
            appPackageName = "com.jpmorgan.privatebank"
        ),
        VaultAccount(
            id = "va_amy_minors",
            name = "Aunt Amy's Minors Custodial Escrow (6 Children)",
            institution = "J.P. Morgan Private Bank Custody",
            accountMask = "•••• 9812",
            category = VaultAccountCategory.LIQUID_CASH,
            balance = 114000.0,
            apy = 4.85,
            status = "Custodial Escrow",
            notes = "Dedicated custodial trust account holding \$19,000/yr for each of Aunt Amy's 6 minor children.",
            isTaxExempt = false,
            webPortalUrl = "https://privatebank.jpmorgan.com/gl/en/log-in",
            appPackageName = "com.jpmorgan.privatebank"
        ),
        VaultAccount(
            id = "va_life_ilit",
            name = "Pacific Life Whole Life Cash Value Reserve",
            institution = "Pacific Life Private Wealth",
            accountMask = "•••• 1109",
            category = VaultAccountCategory.STRATEGIC_COLLATERAL,
            balance = 2250000.0,
            apy = 4.75,
            status = "90% Credit Facility",
            notes = "Irrevocable Life Insurance Trust (ILIT); provides $2,025,000 non-taxable borrowing line.",
            isTaxExempt = true
        ),
        VaultAccount(
            id = "va_delaware",
            name = "Delaware Asset Holdings Liquidity Buffer",
            institution = "Wilmington Trust Delaware Custody",
            accountMask = "•••• 5520",
            category = VaultAccountCategory.STRATEGIC_COLLATERAL,
            balance = 1200000.0,
            apy = 4.15,
            status = "Asset Protected",
            notes = "Delaware statutory trust asset protection ring-fencing buffer.",
            isTaxExempt = false
        ),
        VaultAccount(
            id = "va_escrow",
            name = "Real Estate Tax & Escrow Safe Harbor",
            institution = "Huntington Private Bank Traverse City",
            accountMask = "•••• 6614",
            category = VaultAccountCategory.ESCROW_RESERVES,
            balance = 150000.0,
            apy = 3.90,
            status = "Escrow Dedicated",
            notes = "Pre-funded escrow buffer for the 7 family estates, taxes, and property insurance.",
            isTaxExempt = false
        ),
        VaultAccount(
            id = "va_emergency",
            name = "Sovereign Contingency Vault",
            institution = "Northern Trust Sovereign Services",
            accountMask = "•••• 8839",
            category = VaultAccountCategory.ESCROW_RESERVES,
            balance = 500000.0,
            apy = 4.00,
            status = "Instant Settlement",
            notes = "Immediate response liquidity reserve for family emergency sweeps or capital calls.",
            isTaxExempt = false
        )
    )

    private val _accountsState = MutableStateFlow(initialAccounts)
    override val accountsFlow: StateFlow<List<VaultAccount>> = _accountsState.asStateFlow()

    override fun getSummaryAccounts(): List<VaultAccount> = _accountsState.value

    override fun getAccountsByCategory(category: VaultAccountCategory): List<VaultAccount> {
        return if (category == VaultAccountCategory.ALL) {
            _accountsState.value
        } else {
            _accountsState.value.filter { it.category == category }
        }
    }

    override fun getTotalBalance(): Double = _accountsState.value.sumOf { it.balance }

    override fun getCategoryTotal(category: VaultAccountCategory): Double {
        return if (category == VaultAccountCategory.ALL) {
            getTotalBalance()
        } else {
            _accountsState.value.filter { it.category == category }.sumOf { it.balance }
        }
    }

    override fun getTotalAnnualYield(): Double = _accountsState.value.sumOf { it.annualYield }

    override fun getBlendedApy(): Double {
        val total = getTotalBalance()
        return if (total > 0) (getTotalAnnualYield() / total) * 100.0 else 0.0
    }

    override fun refreshAccounts() {
        _accountsState.value = initialAccounts
    }

    override fun updateAccount(account: VaultAccount) {
        _accountsState.value = _accountsState.value.map { if (it.id == account.id) account else it }
    }

    override fun addAccount(account: VaultAccount) {
        _accountsState.value = _accountsState.value + account
    }

    override fun deleteAccount(id: String) {
        _accountsState.value = _accountsState.value.filter { it.id != id }
    }
}
