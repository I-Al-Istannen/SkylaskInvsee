package me.ialistannen.skylaskinvsee.util;

import me.ialistannen.skylaskinvsee.SkylaskInvsee;

/**
 * Some static utility functions
 */
public class Util {

    /**
     * Translates a message
     *
     * @param key The key to translate
     * @param formattingObjects The formatting objects
     *
     * @return The translated message
     */
    public static String trWithPrefix(String key, Object... formattingObjects) {
        return tr("prefix") + tr(key, formattingObjects);
    }

    /**
     * Translates a message
     *
     * @param key The key to translate
     * @param formattingObjects The formatting objects
     *
     * @return The translated message
     */
    public static String tr(String key, Object... formattingObjects) {
        return SkylaskInvsee.getInstance().getLanguage().tr(key, formattingObjects);
    }
}
