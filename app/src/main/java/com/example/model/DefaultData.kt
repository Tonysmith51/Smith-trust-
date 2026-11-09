package com.example.model

object DefaultData {

    fun getDefaultVaultLegs(): List<VaultLeg> = listOf(
        VaultLeg(
            id = "leg_tbills",
            name = "T-Bills & Short Paper",
            subtitle = "3-6 Mo US Treasury Bills & Commercial Cash Paper",
            allocation = 33000000.0,
            apy = 3.28
        ),
        VaultLeg(
            id = "leg_treasuries",
            name = "10-Year US Treasuries",
            subtitle = "Benchmark Sovereign Fixed Income Ladder",
            allocation = 33000000.0,
            apy = 3.45
        ),
        VaultLeg(
            id = "leg_munis",
            name = "AAA Michigan / Munis",
            subtitle = "State & Local Triple-Exempt Municipal Bonds",
            allocation = 34000000.0,
            apy = 3.88
        )
    )

    fun getDefaultBankAccounts(): List<BankAccount> = listOf(
        BankAccount(
            id = "bank_jpm_liquid",
            institution = "J.P. Morgan Private Bank",
            accountName = "J.P. Morgan Treasury Money Market (CapEx & Acquisitions)",
            accountNumberMask = "•••• 9821",
            balance = 31761543.0,
            apy = 4.85,
            isPrimarySweep = true,
            category = "Treasury Money Market (Liquid)",
            maturityDate = "Daily Liquidity (T+0)",
            purposeNotes = "Liquid funds to acquire houses, vehicles, aircraft, and trust capital assets while earning daily Treasury Money Market yield",
            webPortalUrl = "https://privatebank.jpmorgan.com/gl/en/log-in",
            appPackageName = "com.jpmorgan.privatebank",
            lastSyncTime = "Live Feed Connected • Just now",
            isLiveConnected = true
        ),
        BankAccount(
            id = "bank_tbills_tax_2027",
            institution = "J.P. Morgan / U.S. Treasury Direct",
            accountName = "Short-Term U.S. T-Bills Tax Escrow (Due April 15, 2027)",
            accountNumberMask = "•••• 2027",
            balance = 29101957.0,
            apy = 4.95,
            isPrimarySweep = false,
            category = "Short-Term U.S. Treasury Bills",
            maturityDate = "April 15, 2027",
            purposeNotes = "Principal tax liability escrow invested in short-term U.S. T-Bills until April 15, 2027 tax due date (state & local tax-exempt)",
            webPortalUrl = "https://www.treasurydirect.gov/RS/PWOLGate",
            appPackageName = "gov.treasury.treasurydirect",
            lastSyncTime = "Live Fed Direct Link • Just now",
            isLiveConnected = true
        ),
        BankAccount(
            id = "bank_vault_yield_tax",
            institution = "J.P. Morgan / U.S. Treasury Direct",
            accountName = "Vault Yield Tax Reinvestment (Rolling T-Bills)",
            accountNumberMask = "•••• 3700",
            balance = 1310000.0,
            apy = 4.90,
            isPrimarySweep = false,
            category = "Rolling U.S. T-Bills Sinking Fund",
            maturityDate = "Quarterly Rolling T-Bills",
            purposeNotes = "Automated 37% tax sweep off $100M vault interest income reinvested in rolling short-term T-Bills until tax remittances",
            webPortalUrl = "https://privatebank.jpmorgan.com/gl/en/log-in",
            appPackageName = "com.jpmorgan.privatebank",
            lastSyncTime = "Treasury Auto-Sweep • Just now",
            isLiveConnected = true
        ),
        BankAccount(
            id = "bank_amy_minors_custodial",
            institution = "J.P. Morgan Private Bank Custody",
            accountName = "Aunt Amy's Minors Custodial Trust Escrow (6 Children @ \$19k/yr)",
            accountNumberMask = "•••• 9812",
            balance = 114000.0,
            apy = 4.85,
            isPrimarySweep = false,
            category = "Custodial Escrow",
            maturityDate = "Annual Gift Vesting",
            purposeNotes = "Annual exclusion gifts held in custody for Aunt Amy's 6 minor children",
            webPortalUrl = "https://privatebank.jpmorgan.com/gl/en/log-in",
            appPackageName = "com.jpmorgan.privatebank",
            lastSyncTime = "Custodial Ledger • Just now",
            isLiveConnected = true
        )
    )

    fun getDefaultLoans(): List<LoanItem> = listOf(
        LoanItem(
            id = "loan_swaney",
            name = "Swaney TC Flagship Note",
            category = "Real Estate First Lien",
            collateral = "Swaney Traverse City Flagship",
            balance = 1200000.0,
            apr = 5.25,
            monthlyPayment = 6630.0,
            mode = LoanPaymentMode.AMORTIZING,
            entityObligor = "Smith Real Estate Holdings LLC",
            maturityDate = "Oct 2039",
            lender = "J.P. Morgan Private Bank"
        ),
        LoanItem(
            id = "loan_cedar",
            name = "Cedar 400-Acre Land Note",
            category = "Ranch Acquisition Note",
            collateral = "Cedar Ranch & Compound",
            balance = 650000.0,
            apr = 4.85,
            monthlyPayment = 2627.0,
            mode = LoanPaymentMode.INTEREST_ONLY,
            entityObligor = "Smith Real Estate Holdings LLC",
            maturityDate = "Jul 2034",
            lender = "Northern Trust Wealth"
        ),
        LoanItem(
            id = "loan_policy",
            name = "Whole Life Policy Loan",
            category = "ILIT Cash Value Credit",
            collateral = "Pacific Life $25M Policy Cash Value",
            balance = 350000.0,
            apr = 4.75,
            monthlyPayment = 1385.0,
            mode = LoanPaymentMode.INTEREST_ONLY,
            entityObligor = "Anthony Smith Master Trust",
            maturityDate = "Rolling / Non-Callable",
            lender = "Pacific Life Private Wealth"
        ),
        LoanItem(
            id = "loan_fleet",
            name = "Aviation & Fleet Capital Note",
            category = "Secured Equipment Line",
            collateral = "Executive Convoy & Trailer Rigs",
            balance = 140000.0,
            apr = 5.10,
            monthlyPayment = 1850.0,
            mode = LoanPaymentMode.AMORTIZING,
            entityObligor = "Smith Aviation & Fleet LLC",
            maturityDate = "Mar 2029",
            lender = "Huntington Commercial Desk"
        ),
        LoanItem(
            id = "loan_hosp",
            name = "Hospitality Renovation Credit",
            category = "Operating Credit Facility",
            collateral = "Lakeside Guest Lodges & Docks",
            balance = 210000.0,
            apr = 4.95,
            monthlyPayment = 1020.0,
            mode = LoanPaymentMode.INTEREST_ONLY,
            entityObligor = "Smith Hospitality & Lodging LLC",
            maturityDate = "Nov 2032",
            lender = "Fifth Third Private Bank"
        )
    )

    fun getDefaultFamilyCards(): List<FamilyCard> = listOf(
        FamilyCard("card_mom", "Mom", "Mother", "•••• 7101", 14200.0, 19000.0, false, "Chase Sapphire Reserve® (Visa Infinite)"),
        FamilyCard("card_dad", "Dad", "Father", "•••• 7102", 15800.0, 19000.0, false, "Chase Sapphire Reserve® (Visa Infinite)"),
        FamilyCard("card_lana", "Lana", "Family Beneficiary", "•••• 7103", 11400.0, 19000.0, false, "Chase Sapphire Reserve® (Visa Infinite)"),
        FamilyCard("card_arrin", "Arrin", "Family Beneficiary", "•••• 7104", 12900.0, 19000.0, false, "Chase Sapphire Reserve® (Visa Infinite)"),
        FamilyCard("card_nana", "Nana", "Grandmother", "•••• 7105", 8200.0, 19000.0, false, "Chase Sapphire Reserve® (Visa Infinite)"),
        FamilyCard("card_shauna", "Shauna", "Family Beneficiary", "•••• 7106", 13100.0, 19000.0, false, "Chase Sapphire Reserve® (Visa Infinite)"),
        FamilyCard("card_aunt_amy", "Aunt Amy", "Aunt • Guardian of 6 Minors", "•••• 7107", 16400.0, 19000.0, false, "Chase Sapphire Reserve® (Visa Infinite)"),
        FamilyCard("card_uncle_mike", "Uncle Mike", "Uncle", "•••• 7108", 14800.0, 19000.0, false, "Chase Sapphire Reserve® (Visa Infinite)"),
        FamilyCard("card_danny", "Danny", "Family Beneficiary", "•••• 7109", 10500.0, 19000.0, false, "Chase Sapphire Reserve® (Visa Infinite)"),
        FamilyCard("card_katie", "Katie", "Family Beneficiary", "•••• 7110", 9800.0, 19000.0, false, "Chase Sapphire Reserve® (Visa Infinite)"),
        FamilyCard("card_bethany", "Bethany", "Family Beneficiary", "•••• 7111", 12200.0, 19000.0, false, "Chase Sapphire Reserve® (Visa Infinite)")
    )

    fun getDefaultMinorCustodialFund(): MinorCustodialFund = MinorCustodialFund()

    fun getDefaultInsurancePolicies(): List<InsurancePolicy> = listOf(
        InsurancePolicy(
            id = "ins_whole_life",
            name = "Executive Whole Life ILIT",
            carrier = "Pacific Life Private Wealth",
            category = "Whole Life ILIT",
            deathBenefit = 25000000.0,
            cashValue = 2250000.0,
            annualPremium = 120000.0,
            coverageHighlights = "Irrevocable Life Insurance Trust with 90% non-taxable collateralized borrowing line ($2,025,000 credit facility)."
        ),
        InsurancePolicy(
            id = "ins_term",
            name = "30-Year Key-Person Term",
            carrier = "Lincoln Financial",
            category = "Key-Person Term",
            deathBenefit = 15000000.0,
            cashValue = 0.0,
            annualPremium = 18000.0,
            coverageHighlights = "Tier-1 Convertible key-person sovereign security covenant."
        ),
        InsurancePolicy(
            id = "ins_ppo",
            name = "Platinum Executive PPO",
            carrier = "Blue Cross Blue Shield Private",
            category = "Platinum PPO",
            deathBenefit = 0.0,
            cashValue = 0.0,
            annualPremium = 100000.0,
            coverageHighlights = "$0 in-network deductible, concierge private physician network, worldwide family coverage tier."
        ),
        InsurancePolicy(
            id = "ins_evac",
            name = "Global Medevac & War Risk",
            carrier = "Medjet Assist Global",
            category = "Global Medical & Evac",
            deathBenefit = 0.0,
            cashValue = 0.0,
            annualPremium = 12000.0,
            coverageHighlights = "Dedicated private jet air ambulance extraction from anywhere worldwide to hospital of choice."
        )
    )

    fun getDefaultProperties(): List<TrustProperty> = listOf(
        TrustProperty(
            id = "prop_swaney",
            name = "Swaney TC Flagship",
            location = "Traverse City, Michigan",
            listPrice = 2850000.0,
            closingInvested = 2980000.0,
            occupants = "Anthony Smith (Primary Residence)",
            annualEscrow = 36000.0,
            notes = "IRC §121 principal residence exemption qualified; smart security perimeter & private bay access."
        ),
        TrustProperty(
            id = "prop_cobalt",
            name = "Cobalt TC",
            location = "Traverse City, Michigan",
            listPrice = 850000.0,
            closingInvested = 892000.0,
            occupants = "Family Guest House & Trust Hospitality",
            annualEscrow = 14000.0,
            notes = "Dedicated guest lodging minutes from Grand Traverse Bay."
        ),
        TrustProperty(
            id = "prop_cookeville",
            name = "Cookeville TN Retreat",
            location = "Cookeville, Tennessee",
            listPrice = 650000.0,
            closingInvested = 682500.0,
            occupants = "Southern Family Sanctuary",
            annualEscrow = 9500.0,
            notes = "Zero state income tax haven; rolling hills estate with backup generator grid."
        ),
        TrustProperty(
            id = "prop_belding",
            name = "Belding MI Homestead",
            location = "Belding, Michigan",
            listPrice = 420000.0,
            closingInvested = 441000.0,
            occupants = "Extended Family Custodians",
            annualEscrow = 8000.0,
            notes = "Quiet estate retained for legacy family branch."
        ),
        TrustProperty(
            id = "prop_6565",
            name = "6565 Suerwier Rd Applegate",
            location = "Applegate, Michigan",
            listPrice = 550000.0,
            closingInvested = 577500.0,
            occupants = "Lake Huron Lodge 1",
            annualEscrow = 11500.0,
            notes = "Direct Lake Huron sunrise frontage, sand beach, deep-water dock."
        ),
        TrustProperty(
            id = "prop_6555",
            name = "6555 Suerwier Rd Applegate",
            location = "Applegate, Michigan",
            listPrice = 480000.0,
            closingInvested = 504000.0,
            occupants = "Lake Huron Cottage 2",
            annualEscrow = 10000.0,
            notes = "Contiguous lakeside compound buffer parcel with private boathouse."
        ),
        TrustProperty(
            id = "prop_cedar",
            name = "Cedar 400-Acre Ranch & Compound",
            location = "Cedar, Leelanau County, Michigan",
            listPrice = 3900000.0,
            closingInvested = 4100000.0,
            occupants = "Family Compound & Equestrian Sanctuary",
            annualEscrow = 61000.0,
            notes = "400 contiguous protected acres; private grass runway, equestrian stables, custom timber lodge."
        )
    )

    fun getDefaultFleet(): List<FleetVehicle> = listOf(
        FleetVehicle(
            id = "fleet_pilatus",
            yearModel = "2025 Pilatus PC-12 NGX Executive",
            dealer = "Pilatus Aircraft / West Michigan Aviation",
            preTaxPrice = 5450000.0,
            salesTaxRate = 6.0,
            annualUpkeep = 85000.0,
            notes = "Pratt & Whitney PT6E-67XP turboprop; BMW Designworks cabin interior; certified for Leelanau private grass runway.",
            assetCategory = "Aircraft",
            registrationNumber = "Tail N704AS",
            estimatedCurrentValuation = 5650000.0,
            nextMaintenanceDate = "Jan 18, 2027",
            maintenanceSchedule = "Phase 1 Annual Airframe & Avionics Check",
            maintenanceStatus = "Certified Flight-Ready",
            location = "TVC Cherry Capital Hangar 4B / Cedar Runway",
            telemetryUsage = "245 Flight Hours"
        ),
        FleetVehicle(
            id = "fleet_bell",
            yearModel = "2024 Bell 505 Jet Ranger X",
            dealer = "Bell Flight Textron",
            preTaxPrice = 1650000.0,
            salesTaxRate = 6.0,
            annualUpkeep = 36000.0,
            notes = "Turbomeca Arrius 2R turboshaft with dual-channel FADEC; rapid inter-estate transfer between Traverse Bay & Cedar Compound.",
            assetCategory = "Aircraft",
            registrationNumber = "Tail N505ST",
            estimatedCurrentValuation = 1580000.0,
            nextMaintenanceDate = "Nov 25, 2026",
            maintenanceSchedule = "100-Hr Turbomeca Rotor & Turbine Inspection",
            maintenanceStatus = "Service Due Soon",
            location = "Swaney TC Helipad & Cedar Compound",
            telemetryUsage = "140 Rotor Hours"
        ),
        FleetVehicle(
            id = "fleet_expedition",
            yearModel = "2027 Ford Expedition Max King Ranch",
            dealer = "Fox Ford Traverse City",
            preTaxPrice = 94500.0,
            salesTaxRate = 6.0,
            annualUpkeep = 14000.0,
            notes = "Executive flagship SUV; Level-3 security window tint, dual rear consoles, trust sovereign plates.",
            assetCategory = "Vehicle",
            registrationNumber = "VIN •••• 4491",
            estimatedCurrentValuation = 89500.0,
            nextMaintenanceDate = "Dec 10, 2026",
            maintenanceSchedule = "10,000-Mi Powertrain & Filter Check",
            maintenanceStatus = "Certified Active",
            location = "Swaney TC Flagship Garage",
            telemetryUsage = "7,200 Mi"
        ),
        FleetVehicle(
            id = "fleet_wagoneer",
            yearModel = "2026 Jeep Grand Wagoneer L Summit",
            dealer = "Serra Traverse City",
            preTaxPrice = 112000.0,
            salesTaxRate = 6.0,
            annualUpkeep = 14000.0,
            notes = "Midnight Obsidian 4x4 with McIntosh Reference 23-speaker Audio & Quadra-Lift adaptive air suspension.",
            assetCategory = "Vehicle",
            registrationNumber = "VIN •••• 8820",
            estimatedCurrentValuation = 104000.0,
            nextMaintenanceDate = "Feb 15, 2027",
            maintenanceSchedule = "Annual Synthetic Lube & Inspection",
            maintenanceStatus = "Certified Active",
            location = "Swaney TC Flagship Garage",
            telemetryUsage = "4,600 Mi"
        ),
        FleetVehicle(
            id = "fleet_transit",
            yearModel = "2026 Ford Transit-350 XLT Passenger",
            dealer = "Bill Marsh Auto Group",
            preTaxPrice = 62000.0,
            salesTaxRate = 6.0,
            annualUpkeep = 11000.0,
            notes = "Extended high-roof 12-passenger estate shuttle with Starlink satellite Wi-Fi for family airport sweeps.",
            assetCategory = "Vehicle",
            registrationNumber = "VIN •••• 3302",
            estimatedCurrentValuation = 58000.0,
            nextMaintenanceDate = "Mar 05, 2027",
            maintenanceSchedule = "Brake & Tire Rotation Check",
            maintenanceStatus = "Certified Active",
            location = "Cedar Ranch Compound Bay 2",
            telemetryUsage = "11,500 Mi"
        ),
        FleetVehicle(
            id = "fleet_chriscraft",
            yearModel = "2025 Chris-Craft Launch 35 GT",
            dealer = "Walstrom Marine Harbor Springs",
            preTaxPrice = 580000.0,
            salesTaxRate = 6.0,
            annualUpkeep = 24000.0,
            notes = "Dual Mercury Verado 400hp V10 outboards; custom heritage teak package; Grand Traverse Bay & Lake Huron runabout.",
            assetCategory = "Watercraft",
            registrationNumber = "HIN •••• 7741",
            estimatedCurrentValuation = 545000.0,
            nextMaintenanceDate = "Dec 01, 2026",
            maintenanceSchedule = "Winterization & Twin Mercury 400 V10 Inspection",
            maintenanceStatus = "In Maintenance Hold",
            location = "Applegate Lake Huron Private Boathouse",
            telemetryUsage = "68 Marine Hours"
        )
    )

    fun getDefaultLlcs(): List<SubsidiaryLlc> = listOf(
        SubsidiaryLlc(
            id = "llc_asset",
            name = "Smith Asset Holdings LLC",
            entityType = "Holding / Treasury Custodian",
            purpose = "Holds core liquid fixed income, bond ladders, and bank sweep relationships under Delaware asset protection statutes."
        ),
        SubsidiaryLlc(
            id = "llc_re",
            name = "Smith Real Estate Holdings LLC",
            entityType = "Title & Land Holding Entity",
            purpose = "Holds fee simple deeds for the 7 family estates, lakeside lodges, and the 400-acre Cedar ranch."
        ),
        SubsidiaryLlc(
            id = "llc_hosp",
            name = "Smith Hospitality & Lodging LLC",
            entityType = "Operating & Management Entity",
            purpose = "Manages upkeep, concierge staff, groundskeeping, and family guest reservations across all properties."
        ),
        SubsidiaryLlc(
            id = "llc_fleet",
            name = "Smith Aviation & Fleet LLC",
            entityType = "Transportation & Dry Lease Entity",
            purpose = "Holds titles for executive vehicles, trailers, runway permits, and manages insurance liability separation."
        ),
        SubsidiaryLlc(
            id = "llc_bev",
            name = "Smith Beverages LLC",
            entityType = "Operating CPG Subsidiary",
            purpose = "Producer of 'Smith's Pop' premium craft sodas, tonic waters, and botanicals distributed across the Great Lakes."
        ),
        SubsidiaryLlc(
            id = "llc_snacks",
            name = "Smith Snacks LLC",
            entityType = "Operating CPG Subsidiary",
            purpose = "Maker of 'Smith's Crisps & Bites' small-batch kettle chips, roasted nuts, and gourmet provisions."
        )
    )
}
