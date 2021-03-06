package com.app.bible.knowbible.mvvm.model

enum class EnumBooksList(val bookNumber: Int, val numberOfChapters: Int) {
    //ВЕТХИЙ ЗАВЕТ
    GENESIS(10, 50),
    EXODUS(20, 40),
    LEVITICUS(30, 27),
    NUMBERS(40, 36),
    DEUTERONOMY(50, 34),
    JOSHUA(60, 24),
    JUDGES(70, 21),
    RUTH(80, 4),
    FIRST_SAMUEL(90, 31), //(1 KINGS)
    SECOND_SAMUEL(100, 24), //(2 KINGS)
    FIRST_KINGS(110, 22), //(3 KINGS)
    SECOND_KINGS(120, 25), //(4 KINGS)
    FIRST_CHRONICLES(130, 29),
    SECOND_CHRONICLES(140, 36),
    EZRA(150, 10),
    NEHEMIAH(160, 13),
    ESTHER(190, 10),
    JOB(220, 42),
    PSALMS(230, 150),
    PROVERBS(240, 31),
    ECCLESIASTES(250, 12),
    SONG_OF_SOLOMON(260, 8), //(CANTICLES)
    ISAIAH(290, 66),
    JEREMIAH(300, 52),
    LAMENTATIONS(310, 5),
    EZEKIEL(330, 48),
    DANIEL(340, 12),
    HOSEA(350, 14),
    JOEL(360, 3),
    AMOS(370, 9),
    OBADIAH(380, 1),
    JONAH(390, 4),
    MICAH(400, 7),
    NAHUM(410, 3),
    HABAKKUK(420, 3),
    ZEPHANIAH(430, 3),
    HAGGAI(440, 2),
    ZECHARIAH(450, 14),
    MALACHI(460, 4),

    //НОВЫЙ ЗАВЕТ
    MATTHEW(470, 28),
    MARK(480, 16),
    LUKE(490, 24),
    JOHN(500, 21),
    ACTS(510, 28),
    JAMES(660, 5),
    FIRST_PETER(670, 5),
    SECOND_PETER(680, 3),
    FIRST_JOHN(690, 5),
    SECOND_JOHN(700, 1),
    THIRD_JOHN(710, 1),
    JUDE(720, 1),
    ROMANS(520, 16),
    FIRST_CORINTHIANS(530, 16),
    SECOND_CORINTHIANS(540, 13),
    GALATIANS(550, 6),
    EPHESIANS(560, 6),
    PHILIPPIANS(570, 4),
    COLOSSIANS(580, 4),
    FIRST_THESSALONIANS(590, 5),
    SECOND_THESSALONIANS(600, 3),
    FIRST_TIMOTHY(610, 6),
    SECOND_TIMOTHY(620, 4),
    TITUS(630, 3),
    PHILEMON(640, 1),
    HEBREWS(650, 13),
    REVELATION(730, 22)
}