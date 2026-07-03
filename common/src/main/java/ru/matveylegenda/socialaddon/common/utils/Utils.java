package ru.matveylegenda.socialaddon.common.utils;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Pattern;

public class Utils {
    public static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacySection();
    public static final Pattern TIME = Pattern.compile("\\{time}");
}
