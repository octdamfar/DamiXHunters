package dev.damianfarias.hunters.utils;

import dev.damianfarias.hunters.DamiXHunters;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class DamiUtils {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacyAmpersand();
    private final static int CENTER_PX = 154;

    public static String colorize(String message) {
        if (message == null) return null;

        Pattern hexPattern = Pattern.compile("(?i)#([A-Fa-f0-9]{6})");
        Matcher matcher = hexPattern.matcher(message);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String replacement = ChatColor.of("#" + hexCode).toString();
            matcher.appendReplacement(buffer, replacement);
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    public static void sendMessage(CommandSender entity, String s) {
        if (entity == null || s == null) return;

        if(s.startsWith("<center>")) s = getCenteredMessage(s.replace("<center>", ""));
        else if(s.startsWith("<underline:")){
            if(s.length() == 12){
                sendUnderline(entity, s.charAt(11));
            }else{
                sendUnderline(entity, s.replace("<underline:", ""));
            }
            return;
        }

        entity.sendMessage(colorize(s));
    }

    public static Enchantment getEnchantmentUniversal(String name) {
        if (name == null || name.isEmpty()) return null;

        String normalized = name.toLowerCase().trim();

        if (normalized.contains(":")) {
            NamespacedKey key = NamespacedKey.fromString(normalized);
            if (key != null) {
                Enchantment ench = Enchantment.getByKey(key);
                if (ench != null) return ench;
            }
        }

        Enchantment ench = Enchantment.getByKey(NamespacedKey.minecraft(normalized));
        if (ench != null) return ench;

        ench = Enchantment.getByName(normalized.toUpperCase());
        if (ench != null) return ench;

        for (Enchantment e : Enchantment.values()) {
            if (e.getKey().getKey().equalsIgnoreCase(normalized)) return e;
            if (e.getKey().toString().equalsIgnoreCase(normalized)) return e;
        }

        return null;
    }

    @SuppressWarnings({"UnstableApiUsage", "removal"})
    public static Sound getSoundUniversal(String name) {
        if (name == null || name.isEmpty()) return null;

        String normalized = name.trim()
                .replace("MINECRAFT:", "")
                .replace("minecraft:", "")
                .toLowerCase();

        NamespacedKey key = NamespacedKey.minecraft(normalized.replace('_', '.'));
        Sound sound = Registry.SOUNDS.get(key);
        if (sound != null) return sound;

        key = NamespacedKey.minecraft(normalized.replace('.', '_'));
        sound = Registry.SOUNDS.get(key);
        if (sound != null) return sound;

        try {
            return Sound.valueOf(name.toUpperCase().replace('.', '_'));
        } catch (IllegalArgumentException ignored) {}

        return null;
    }

    public static List<String> filterSuggestions(List<String> suggestions, String filter) {
        if (suggestions == null || filter == null || filter.isEmpty()) return suggestions;

        String lowerFilter = filter.toLowerCase();
        return suggestions.stream()
                .filter(suggestion -> suggestion.toLowerCase().startsWith(lowerFilter))
                .toList();
    }

    public static List<String> colorizeList(List<String> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(DamiUtils::colorize)
                .toList();
    }

    public static String locationToString(Location location){
        if (location == null) return "null";
        return String.format("%s;%s;%s;%s;%s,%s",
                location.getWorld() != null ? location.getWorld().getName() : "world",
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    public static List<String> locationToStringList(List<Location> spawns) {
        if (spawns == null) return List.of();
        return spawns.stream()
                .map(DamiUtils::locationToString)
                .toList();
    }

    public static Location stringToLocation(String string){
        if (string == null || string.isEmpty()) return null;
        String[] parts = string.split(";");
        if (parts.length != 6) return null;
        String worldName = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }

    public static void sendTitle(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        Component titleComponent = LEGACY_SERIALIZER.deserialize(colorize(title));
        Component subtitleComponent = subtitle != null ? LEGACY_SERIALIZER.deserialize(colorize(subtitle)) : Component.empty();

        Title.Times times = Title.Times.times(
                Duration.ofMillis(fadeInTicks * 50L),
                Duration.ofMillis(stayTicks * 50L),
                Duration.ofMillis(fadeOutTicks * 50L)
        );

        Title ft = Title.title(titleComponent, subtitleComponent, times);

        player.showTitle(ft);
    }

    public static void spawnFirework(Location loc, String data, String logPrefix, String playerName) {
        Firework firework = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();

        int power = 1;
        FireworkEffect.Type type = FireworkEffect.Type.BALL;
        List<Color> colors = new ArrayList<>();
        List<Color> fades = new ArrayList<>();
        boolean trail = false;
        boolean flicker = false;
        boolean silent = false;
        boolean glow = false;

        try {
            for (String part : data.split(",")) {
                String[] kv = part.split(":", 2);
                if (kv.length < 2) continue;
                String key = kv[0].trim().toLowerCase();
                String value = kv[1].trim();

                switch (key) {
                    case "power" -> {
                        try {
                            power = Integer.parseInt(value);
                        } catch (NumberFormatException ex) {
                            Bukkit.getConsoleSender().sendMessage("§c[" + logPrefix + " Actions] Power inválido en FIREWORK para " + playerName + ": " + value);
                        }
                    }
                    case "type" -> {
                        try {
                            type = FireworkEffect.Type.valueOf(value.toUpperCase());
                        } catch (IllegalArgumentException ex) {
                            Bukkit.getConsoleSender().sendMessage("§c[" + logPrefix + " Actions] Tipo inválido en FIREWORK para " + playerName + ": " + value);
                        }
                    }
                    case "colors" -> colors.addAll(parseColors(value));
                    case "fade" -> fades.addAll(parseColors(value));
                    case "trail" -> trail = Boolean.parseBoolean(value);
                    case "flicker" -> flicker = Boolean.parseBoolean(value);
                    case "silent" -> silent = Boolean.parseBoolean(value);
                    case "glow" -> glow = Boolean.parseBoolean(value);
                }
            }

            FireworkEffect effect = FireworkEffect.builder()
                    .with(type)
                    .withColor(colors.isEmpty() ? Collections.singletonList(Color.WHITE) : colors)
                    .withFade(fades)
                    .trail(trail)
                    .flicker(flicker)
                    .build();

            meta.addEffect(effect);
            meta.setPower(power);
            firework.setFireworkMeta(meta);

            firework.setSilent(silent);
            firework.setGlowing(glow);

        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage("§c[" + logPrefix + " Actions] Error en FIREWORK para " + playerName + ": " + ex.getMessage());
        }
    }

    private static List<Color> parseColors(String input) {
        List<Color> list = new ArrayList<>();
        for (String c : input.split(";")) {
            try {
                list.add((Color) Color.class.getField(c.trim().toUpperCase()).get(null));
            } catch (Exception ignored) {
                Bukkit.getConsoleSender().sendMessage("§e[FireworkSerializer] Color inválido: " + c);
            }
        }
        return list;
    }

    public static String getCenteredMessage(String message){
        message = colorize(message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for(char c : message.toCharArray()){
            if(c == '§'){
                previousCode = true;
            }else if(previousCode){
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            }else{
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while(compensated < toCompensate){
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb +message;
    }

    public static void sendUnderline(CommandSender sender, String hex) {
        sender.sendMessage(colorize(hex+"&m                                                                          "));
    }


    public static void sendUnderline(CommandSender sender, char color) {
        sender.sendMessage(colorize("&"+color+"&m                                                                          "));
    }

    public static List<Location> stringToLocationList(List<String> l) {
        if (l == null) return List.of();
        return l.stream()
                .map(DamiUtils::stringToLocation)
                .toList();
    }

    public static void sendActionBar(Player player, String message) {
        if (player != null && player.isOnline()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(colorize(message)));
        }
    }

    public static void sendSerializedSound(Player player, String string, String path) {
        String[] args = string.split(";");
        if(args.length < 3){
            DamiXHunters.getInstance().getLogger().warning(path+" has invalid amount of arguments (need 3)");
            return;
        }

        Sound sound = getSoundUniversal(args[0]);
        if(sound == null){
            DamiXHunters.getInstance().getLogger().warning(path+" has invalid sound name or key.");
            return;
        }

        float v,p;
        try{
            v = Float.parseFloat(args[1]);
            p = Float.parseFloat(args[2]);
        }catch (NumberFormatException e){
            DamiXHunters.getInstance().getLogger().warning(path+" starting-hunters-sound has invalid volumen or pitch float argument(s).");
            return;
        }

        player.playSound(player.getLocation(), sound, v, p);
    }

    @Getter
    public enum DefaultFontInfo {

        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private final char character;
        private final int length;

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public int getBoldLength() {
            if (this == DefaultFontInfo.SPACE) return this.getLength();
            return this.length + 1;
        }

        public static DefaultFontInfo getDefaultFontInfo(char c) {
            for (DefaultFontInfo dFI : DefaultFontInfo.values()) {
                if (dFI.getCharacter() == c) return dFI;
            }
            return DefaultFontInfo.DEFAULT;
        }
    }
}
