package com.example.ai

import android.util.Log
import com.example.data.TrustState
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeout
import java.text.NumberFormat
import java.util.Locale

private const val TAG = "TrustGeminiAssistant"

sealed interface GeminiAssistantState {
    data object Idle : GeminiAssistantState
    data object Loading : GeminiAssistantState
    data class Success(val response: String) : GeminiAssistantState
    data class Error(val message: String) : GeminiAssistantState
}

class TrustGeminiAssistant {

    private val model by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    fun buildSystemContext(state: TrustState): String {
        return buildString {
            appendLine("You are the Private Family Office AI Copilot for the Anthony Smith Master Trust.")
            appendLine("You assist Anthony Smith (Grantor & Trustee) in managing sovereign wealth, assets, family credit cards, and real estate.")
            appendLine("Current Real-Time Trust Blueprint Data:")
            appendLine("- Master Vault Corpus: ${currencyFormat.format(state.totalVaultCorpus)} across 3 Legs (Fixed Income 3.8%, Treasury Ladder 3.25%, Dividend Yield 3.88%).")
            appendLine("- Gross Annual Yield: ${currencyFormat.format(state.grossAnnualYield)} (${currencyFormat.format(state.monthlyCashSweep)}/month cash sweep).")
            appendLine("- Total Bank & T-Bills Reserves: ${currencyFormat.format(state.totalBankBalance)}:")
            appendLine("  1) J.P. Morgan Treasury Money Market: \$31,761,543.00 (Liquid CapEx fund used to acquire houses, cars, and fleet assets while earning 4.85% APY in Treasury MMAs)")
            appendLine("  2) Short-Term U.S. T-Bills Tax Escrow: \$29,101,957.00 (Invested in U.S. T-Bills maturing April 15, 2027 for settlement of tax liabilities, earning 4.95% APY state tax-free)")
            appendLine("  3) Vault Yield Tax Reinvestment: \$1,310,000.00 (Automated 37% tax sweep on \$100M vault earnings reinvested in rolling short-term T-Bills)")
            appendLine("  4) Aunt Amy's 6 Minor Children Custodial Escrow: \$114,000.00 (Dedicated J.P. Morgan account holding \$19,000/yr for each of Aunt Amy's 6 young children at 4.85% APY)")
            appendLine("- Active Family Cards: 11 authorized Chase Sapphire Reserve® cards with \$19,000/yr annual exclusion cap each (Total \$209,000/yr).")
            appendLine("  Cardholders: Mom, Dad, Lana, Arrin, Nana, Shauna, Aunt Amy, Uncle Mike, Danny, Katie, Bethany.")
            appendLine("- Real Estate Holdings: 7 Michigan estates (${currencyFormat.format(state.totalRealEstateInvested)} invested), including Swaney Flagship, Applegate Lake Huron compounds, and Cedar 400-Acre Compound & Grass Runway.")
            appendLine("- Sovereign Fleet & Aviation: ${currencyFormat.format(state.totalFleetMarketValue)} market value across 6 assets:")
            appendLine("  1) 2025 Pilatus PC-12 NGX Executive turboprop (Tail N704AS, \$5.65M value, Certified Flight-Ready, TVC Hangar 4B)")
            appendLine("  2) 2024 Bell 505 Jet Ranger X helicopter (Tail N505ST, \$1.58M value, Service Due Soon: 100-Hr Turbomeca inspection Nov 25, 2026)")
            appendLine("  3) 2027 Ford Expedition Max King Ranch (\$89.5k value)")
            appendLine("  4) 2026 Jeep Grand Wagoneer L Summit (\$104k value)")
            appendLine("  5) 2026 Ford Transit-350 XLT Passenger shuttle (\$58k value)")
            appendLine("  6) 2025 Chris-Craft Launch 35 GT watercraft (\$545k value, In Maintenance Hold)")
            appendLine("- Total Active Debts: ${currencyFormat.format(state.totalActiveDebtBalances)} (${currencyFormat.format(state.totalDebtServiceAnnual)}/yr debt service).")
            appendLine("- Net Pure Surplus: ${currencyFormat.format(state.pureSurplusAnnual)}/yr (${currencyFormat.format(state.monthlyPureSurplus)}/month).")
            appendLine()
            appendLine("Instructions:")
            appendLine("1. Give authoritative, concise, polished sovereign family office guidance.")
            appendLine("2. Use clear formatting with bullet points when helpful.")
            appendLine("3. Directly answer the user's questions about their money, cards, fleet maintenance, and taxes.")
            appendLine("4. Always maintain privacy and confidentiality appropriate for sovereign wealth.")
        }
    }

    suspend fun askGemini(state: TrustState, userQuery: String): String {
        // Try live Firebase AI Logic first
        try {
            return withTimeout(20_000L) {
                val systemContext = buildSystemContext(state)
                val fullPrompt = "$systemContext\n\nUser Question: $userQuery\n\nExecutive Copilot Response:"
                val response = model.generateContent(fullPrompt)
                val text = response.text
                if (!text.isNullOrBlank()) {
                    text.trim()
                } else {
                    generateSovereignFallbackResponse(state, userQuery)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Live cloud AI unavailable (${e.message}), engaging Sovereign Intelligence Copilot engine.")
            return generateSovereignFallbackResponse(state, userQuery)
        }
    }

    private fun generateSovereignFallbackResponse(state: TrustState, query: String): String {
        val q = query.lowercase(Locale.US)

        return when {
            q.contains("link") || q.contains("app") || q.contains("website") || q.contains("portal") || q.contains("login") -> {
                buildString {
                    appendLine("🔗 **Linked Banking Apps & Direct Web Portals:**")
                    appendLine("The app provides direct app-to-app deep linking and secure website portal access for all trust accounts:")
                    appendLine("• **J.P. Morgan Private Bank:**")
                    appendLine("  - Package: `com.jpmorgan.privatebank`")
                    appendLine("  - Web Portal: [privatebank.jpmorgan.com](https://privatebank.jpmorgan.com/gl/en/log-in)")
                    appendLine("• **Chase Mobile & Private Client:**")
                    appendLine("  - Package: `com.chase.sig.android`")
                    appendLine("  - Web Portal: [secure.chase.com](https://secure.chase.com)")
                    appendLine("• **U.S. TreasuryDirect:**")
                    appendLine("  - Package: `gov.treasury.treasurydirect`")
                    appendLine("  - Web Portal: [treasurydirect.gov](https://www.treasurydirect.gov/RS/PWOLGate)")
                    appendLine()
                    appendLine("⚡ **Live Updates:** Tap **Sync Feeds** on the Banks screen to execute real-time balance reconciliation and credit daily accrued interest across all accounts.")
                }
            }
            q.contains("bank") || q.contains("jp morgan") || q.contains("jpm") || q.contains("t-bill") || q.contains("tax") || q.contains("treasury") -> {
                buildString {
                    appendLine("🏦 **Sovereign Treasury & Bank Reserves Breakdown:**")
                    appendLine("• **Total Reserves:** ${currencyFormat.format(state.totalBankBalance)} earning +${currencyFormat.format(state.totalBankInterest)}/yr (${String.format(Locale.US, "%.2f", state.blendedBankApy)}% blended APY)")
                    appendLine()
                    appendLine("1. **J.P. Morgan Treasury Money Market:**")
                    appendLine("   - **Balance:** \$31,761,543.00 (4.85% APY)")
                    appendLine("   - **Status:** Primary Liquid Sweep Target (T+0 Daily)")
                    appendLine("   - **Purpose:** Earmarked for purchasing houses, vehicles, aircraft, and capital expenditures while earning interest.")
                    appendLine()
                    appendLine("2. **Short-Term U.S. T-Bills Tax Escrow:**")
                    appendLine("   - **Balance:** \$29,101,957.00 (4.95% APY)")
                    appendLine("   - **Maturity:** April 15, 2027 (State & local tax-exempt)")
                    appendLine("   - **Purpose:** Full settlement of principal tax liability due April 15, 2027.")
                    appendLine()
                    appendLine("3. **Vault Yield Tax Reinvestment Sinking Fund:**")
                    appendLine("   - **Balance:** \$1,310,000.00 (4.90% APY in rolling T-Bills)")
                    appendLine("   - **Purpose:** Automated 37% tax sweep off \$100M vault earnings reinvested in rolling T-Bills.")
                    appendLine()
                    appendLine("4. **Aunt Amy's Minors Custodial Escrow:**")
                    appendLine("   - **Balance:** \$114,000.00 (4.85% APY)")
                    appendLine("   - **Purpose:** Dedicated holding account for Aunt Amy's 6 minor children (\$19k/yr each).")
                }
            }
            q.contains("net worth") || q.contains("aum") || q.contains("worth") || q.contains("summary") || q.contains("balance sheet") -> {
                buildString {
                    appendLine("💎 **Anthony Smith Master Trust — Sovereign Balance Sheet:**")
                    appendLine("• **Total Protected AUM:** ${currencyFormat.format(state.trustAum)}")
                    appendLine("• **Master Vault Corpus:** ${currencyFormat.format(state.totalVaultCorpus)} (3-legged fixed income ladder)")
                    appendLine("• **Liquid Bank & T-Bills Reserves:** ${currencyFormat.format(state.totalBankBalance)}")
                    appendLine("• **Michigan Real Estate Invested:** ${currencyFormat.format(state.totalRealEstateInvested)} (7 compounds)")
                    appendLine("• **Fleet & Aviation Valuation:** ${currencyFormat.format(state.totalFleetMarketValue)} (6 assets)")
                    appendLine("• **Whole Life Insurance Cash Value:** ${currencyFormat.format(state.totalInsuranceCashValue)} (\$12.5M death benefit)")
                    appendLine("• **Total Active Debts:** ${currencyFormat.format(state.totalActiveDebtBalances)} (${currencyFormat.format(state.totalDebtServiceAnnual)}/yr debt service)")
                    appendLine("• **Net Pure Annual Surplus:** +${currencyFormat.format(state.pureSurplusAnnual)}/yr (+${currencyFormat.format(state.monthlyPureSurplus)}/month)")
                }
            }
            q.contains("card") || q.contains("sapphire") || q.contains("family") || q.contains("allowance") -> {
                buildString {
                    appendLine("💳 **Chase Sapphire Reserve® Family Cards:**")
                    appendLine("• **Total Active Cards:** 11 authorized cards (\$19,000/yr annual gift exclusion cap each)")
                    appendLine("• **Total Annual Budget:** ${currencyFormat.format(state.activeFamilyCardsAnnualCap)}/yr")
                    appendLine("• **YTD Aggregate Spend:** ${currencyFormat.format(state.totalFamilyCardsYtdSpend)}")
                    appendLine()
                    appendLine("Cardholders: Mom, Dad, Lana, Arrin, Nana, Shauna, Aunt Amy, Uncle Mike, Danny, Katie, Bethany.")
                    appendLine()
                    appendLine("• **Aunt Amy's 6 Minor Children:** Handled through a separate custodial escrow account (\$114,000 total @ \$19,000/yr each) so funds are invested safely until age of majority.")
                }
            }
            q.contains("fleet") || q.contains("plane") || q.contains("aircraft") || q.contains("pilatus") || q.contains("helicopter") || q.contains("car") || q.contains("maintenance") -> {
                buildString {
                    appendLine("✈️ **Sovereign Fleet & Aviation Status:**")
                    appendLine("• **Total Fleet Market Value:** ${currencyFormat.format(state.totalFleetMarketValue)}")
                    appendLine("• **Annual Maintenance Upkeep:** ${currencyFormat.format(state.totalFleetUpkeepAnnual)}/yr")
                    appendLine()
                    appendLine("1. **2025 Pilatus PC-12 NGX** (Tail N704AS): \$5,650,000 | Certified Flight-Ready (TVC Hangar 4B)")
                    appendLine("2. **2024 Bell 505 Jet Ranger X** (Tail N505ST): \$1,580,000 | ⚠️ Service Due Soon: 100-Hr Turbomeca inspection Nov 25, 2026")
                    appendLine("3. **2027 Ford Expedition Max King Ranch**: \$89,500 | Operational")
                    appendLine("4. **2026 Jeep Grand Wagoneer L Summit**: \$104,000 | Operational")
                    appendLine("5. **2026 Ford Transit-350 XLT Shuttle**: \$58,000 | Operational")
                    appendLine("6. **2025 Chris-Craft Launch 35 GT**: \$545,000 | Maintenance Hold (Winterized)")
                }
            }
            q.contains("estate") || q.contains("house") || q.contains("property") || q.contains("swaney") || q.contains("cedar") || q.contains("applegate") -> {
                buildString {
                    appendLine("🏡 **Michigan Real Estate Compound Holdings:**")
                    appendLine("• **Total Invested:** ${currencyFormat.format(state.totalRealEstateInvested)} across 7 estates")
                    appendLine("• **Annual Escrow & Taxes:** ${currencyFormat.format(state.totalRealEstateEscrowAnnual)}/yr")
                    appendLine()
                    appendLine("Key Flagship Properties:")
                    appendLine("• **Swaney Traverse City Flagship:** \$1.75M list | \$1.90M invested (Master Sovereign Compound)")
                    appendLine("• **Cedar 400-Acre Compound & Grass Runway:** \$1.35M list | \$1.55M invested (Private aviation & family retreat)")
                    appendLine("• **Applegate Lake Huron Compound 1 & 2:** \$1.70M combined invested (Waterfront family estates)")
                    appendLine("• **Lake Leelanau & Northport Estates:** \$2.20M combined invested")
                }
            }
            q.contains("vault") || q.contains("corpus") || q.contains("sweep") || q.contains("yield") -> {
                buildString {
                    appendLine("🛡️ **Master Vault & Cash Sweep Architecture:**")
                    appendLine("• **Vault Corpus:** ${currencyFormat.format(state.totalVaultCorpus)}")
                    appendLine("• **Gross Annual Inflow:** ${currencyFormat.format(state.grossAnnualYield)}/yr")
                    appendLine("• **Monthly Cash Sweep:** ${currencyFormat.format(state.monthlyCashSweep)}/month into J.P. Morgan Private Bank")
                    appendLine("• **Leg 1 (T-Bills & Short Paper):** \$33,000,000 @ 3.28% APY")
                    appendLine("• **Leg 2 (10-Yr US Treasuries):** \$33,000,000 @ 3.45% APY")
                    appendLine("• **Leg 3 (AAA Michigan Munis):** \$34,000,000 @ 3.88% APY (Triple Tax-Exempt)")
                }
            }
            else -> {
                buildString {
                    appendLine("🏛️ **Anthony Smith Master Trust Sovereign Briefing:**")
                    appendLine("• **Protected AUM:** ${currencyFormat.format(state.trustAum)}")
                    appendLine("• **J.P. Morgan Liquid CapEx Account:** \$31,761,543.00 (Ready for house & car acquisitions)")
                    appendLine("• **April 15, 2027 Tax Escrow:** \$29,101,957.00 (U.S. T-Bills)")
                    appendLine("• **Monthly Cash Sweep:** ${currencyFormat.format(state.monthlyCashSweep)}/month")
                    appendLine("• **Net Pure Surplus:** +${currencyFormat.format(state.pureSurplusAnnual)}/year")
                    appendLine()
                    appendLine("I am standing by to assist with any asset allocation, credit card limit checks, property acquisition sweeps, or aviation logistics.")
                }
            }
        }
    }
}
