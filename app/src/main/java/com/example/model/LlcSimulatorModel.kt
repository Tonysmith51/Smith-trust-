package com.example.model

enum class EntityTaxClassification(val displayName: String, val description: String) {
    DISREGARDED_SINGLE_MEMBER(
        "Single-Member LLC (Disregarded)",
        "Flows 100% to Grantor Trust (IRC §671); zero entity-level tax"
    ),
    PARTNERSHIP_SUB_K(
        "Multi-Member Partnership (Subchapter K)",
        "Pass-through taxation via Form 1065 / Schedule K-1; flexible allocations"
    ),
    S_CORPORATION(
        "S-Corporation Election (Form 2553)",
        "Pass-through with reasonable salary split to optimize self-employment tax"
    )
}

enum class StateTaxNexus(val displayName: String, val ratePct: Double, val notes: String) {
    DELAWARE("Delaware (Court of Chancery)", 0.0, "0% state tax on out-of-state income; world-class asset protection"),
    MICHIGAN("Michigan Nexus", 4.25, "4.25% flat individual income tax; single business entity reporting"),
    TENNESSEE("Tennessee Haven", 0.0, "0% state personal income tax; 6.5% franchise & excise on inside profits")
}

data class LlcTaxEquitySimState(
    val entityName: String = "Smith Asset Holdings LLC",
    val hypotheticalAssetValue: Double = 15000000.0, // $15M default
    val annualNoiYieldPct: Double = 8.5, // 8.5% annual distribution yield
    val taxClassification: EntityTaxClassification = EntityTaxClassification.DISREGARDED_SINGLE_MEMBER,
    val stateNexus: StateTaxNexus = StateTaxNexus.DELAWARE,
    val depreciationShieldPct: Double = 25.0, // 25% cost segregation shield
    val trustEquityPct: Double = 70.0, // 70% Master Trust
    val managingMemberPct: Double = 20.0, // 20% Anthony Smith Personal
    val familyPoolPct: Double = 10.0, // 10% Family / Key Executive Pool
    val holdingYearsHorizon: Int = 5,
    val exitAppreciationRatePct: Double = 5.5
) {
    val totalEquityPct: Double get() = trustEquityPct + managingMemberPct + familyPoolPct

    // Annual Pre-Tax Cash Flow
    val grossAnnualDistribution: Double get() = hypotheticalAssetValue * (annualNoiYieldPct / 100.0)
    val monthlyPreTaxDistribution: Double get() = grossAnnualDistribution / 12.0

    // Tax Shield & Deductions
    // Annualized depreciation shield (accelerated bonus + cost segregation over 15-yr blended)
    val annualDepreciationDeduction: Double get() = (hypotheticalAssetValue * (depreciationShieldPct / 100.0)) / 10.0
    val taxableIncome: Double get() = (grossAnnualDistribution - annualDepreciationDeduction).coerceAtLeast(0.0)

    // Effective Tax Rates:
    // Federal: 37% top bracket or 20% qualified dividend/LTCG + 3.8% NIIT = 23.8%
    // Grantor trust pass-through enjoys bracket compression avoidance and shield pass-through
    val federalEffectiveRatePct: Double get() = when (taxClassification) {
        EntityTaxClassification.DISREGARDED_SINGLE_MEMBER -> 22.5 // Grantor trust pass-through with deduction pass-through
        EntityTaxClassification.PARTNERSHIP_SUB_K -> 24.8 // K-1 pass-through
        EntityTaxClassification.S_CORPORATION -> 21.0 // S-corp salary / distribution split
    }

    val stateTaxRatePct: Double get() = stateNexus.ratePct
    val totalEffectiveTaxRatePct: Double get() = federalEffectiveRatePct + stateTaxRatePct

    val estimatedAnnualTaxLiability: Double get() = taxableIncome * (totalEffectiveTaxRatePct / 100.0)
    val annualTaxShieldSavings: Double get() = annualDepreciationDeduction * (totalEffectiveTaxRatePct / 100.0)

    // Net After-Tax Cash Available for Equity Distributions
    val netAfterTaxDistribution: Double get() = (grossAnnualDistribution - estimatedAnnualTaxLiability).coerceAtLeast(0.0)
    val monthlyAfterTaxDistribution: Double get() = netAfterTaxDistribution / 12.0

    // Member Annual Equity Distributions ($)
    val trustAnnualDistribution: Double get() = netAfterTaxDistribution * (trustEquityPct / 100.0)
    val managingMemberAnnualDistribution: Double get() = netAfterTaxDistribution * (managingMemberPct / 100.0)
    val familyPoolAnnualDistribution: Double get() = netAfterTaxDistribution * (familyPoolPct / 100.0)

    // Member Asset Equity Book Value ($)
    val trustEquityValue: Double get() = hypotheticalAssetValue * (trustEquityPct / 100.0)
    val managingMemberEquityValue: Double get() = hypotheticalAssetValue * (managingMemberPct / 100.0)
    val familyPoolEquityValue: Double get() = hypotheticalAssetValue * (familyPoolPct / 100.0)

    // Exit / Liquidation Projection (Compound Capital Gain)
    val projectedExitValuation: Double get() = hypotheticalAssetValue * Math.pow(1.0 + (exitAppreciationRatePct / 100.0), holdingYearsHorizon.toDouble())
    val projectedCapitalGain: Double get() = (projectedExitValuation - hypotheticalAssetValue).coerceAtLeast(0.0)
    val exitTaxLiability: Double get() = projectedCapitalGain * 0.238 // 20% Fed LTCG + 3.8% NIIT
    val netExitProceeds: Double get() = projectedExitValuation - exitTaxLiability

    val trustExitProceeds: Double get() = netExitProceeds * (trustEquityPct / 100.0)
    val managingMemberExitProceeds: Double get() = netExitProceeds * (managingMemberPct / 100.0)
    val familyPoolExitProceeds: Double get() = netExitProceeds * (familyPoolPct / 100.0)
}
