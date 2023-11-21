package com.pawlowski.imucollector.data

enum class ActivityType(val code: String, val tensorIndex: Int) {
    CIRCLES_RIGHT("CIRCLES_RIGHT", 0),
    CIRCLES_LEFT("CIRCLES_LEFT", 1),
    TRIANGLE("TRIANGLE", 2),
    SQUARE("SQUARE", 3),
    FORWARD_BACK("FORWARD_BACK", 4),
}
