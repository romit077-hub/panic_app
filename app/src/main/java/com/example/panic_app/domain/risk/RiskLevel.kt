package com.example.panic_app.domain.risk

enum class RiskLevel(val maximumScore: Int) {
    SAFE(30), WARNING(60), HIGH(80), CRITICAL(100);

    companion object {
        const val MIN_SCORE = 0
        const val MAX_SCORE = 100
        fun fromScore(score: Int): RiskLevel {
            require(score in MIN_SCORE..MAX_SCORE) { "Risk score must be between 0 and 100" }
            return entries.first { score <= it.maximumScore }
        }
    }
}
