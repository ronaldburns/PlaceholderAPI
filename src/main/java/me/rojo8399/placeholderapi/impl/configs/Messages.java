/*
 The MIT License (MIT)

 Copyright (c) 2017 Wundero

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
package me.rojo8399.placeholderapi.impl.configs;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class Messages {

    @ConfigSerializable
    public static class Message {

        @Setting
        public String value;

        public Message(){}

        public Message(String s) {
            this.value = s;
        }

        public Text t(Object... args) {
            return Messages.t(value, args);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    @ConfigSerializable
    public static class Misc {
        @ConfigSerializable
        public static class Direction {
            @Setting
            public Message east = of("East");
            @Setting
            public Message north = of("North");
            @Setting
            public Message northeast = of("Northeast");
            @Setting
            public Message northwest = of("Northwest");
            @Setting
            public Message south = of("South");
            @Setting
            public Message southeast = of("Southeast");
            @Setting
            public Message southwest = of("Southwest");
            @Setting
            public Message west = of("West");
        }

        @Setting
        public Message by = of("by");
        @Setting
        public Direction directions = new Direction();
        @Setting("invalid")
        public Message invalid = of("&cThat is not a valid %s!");
        @Setting("no-permission")
        public Message noPerm = of("&cYou are not allowed to do that!");

        @Setting("no-value")
        public Message noValue = of("&cNo value present.");
        @Setting
        public Message suggestions = of("Suggestions: ");
        @Setting
        public Message version = of("&7version");
    }

    @ConfigSerializable
    public static class Placeholders {
        @Setting("all-placeholders-button")
        public Message allPlaceholdersButton = of("&e[SEE ALL]");
        @Setting("all-placeholders-hover")
        public Message allPlaceholdersHover = of("&bClick to see all placeholders!");
        @Setting("all-relational-placeholders-hover")
        public Message allPlaceholdersHoverRelational = of("&bClick to see all relational placeholders!");
        @Setting("all-supported-placeholders")
        public Message allSupportedPlaceholders = of("&6&lAll supported placeholders:");
        @Setting("all-supported-relational-placeholders")
        public Message allSupportedPlaceholdersRelational = of("&6&lAll supported relational placeholders:");
        @Setting("available-placeholders")
        public Message availablePlaceholders = of("&aAvailable placeholders:");
        @Setting("click-to-reload")
        public Message clickReload = of("&bClick to reload:");
        @Setting("currency-description")
        public Message curdesc = of("View information about the server's economy.");
        @Setting("improper-registration")
        public Message improperRegistration = of(
                "&cPlaceholder was not registered correctly! Please check the logs for details.");
        @Setting("info-button-hover")
        public Message infoButtonHover = of("&bClick to get more info!");
        @Setting("invalid-placeholder")
        public Message invalidPlaceholder = of("&cThat is not a valid placeholder!");
        @Setting("invalid-source-observer")
        public Message invalidSrcObs = of("&cThe provided types are invalid sources or observers!");
        @Setting("javascript-description")
        public Message jsdesc = of("Execute JavaScripts.");
        @Setting("must-specify")
        public Message mustSpecify = of("&cYou must specify a placeholder!");
        @Setting("placeholder-not-enabled")
        public Message notEnabled = of("&cPlaceholder not enabled!");
        @Setting("parse-button-hover")
        public Message parseButtonHover = of("&bClick to parse this placeholder for you!");
        @Setting("placeholder-disabled")
        public Message placeholderDisabled = of("&aPlaceholder disabled!");
        @Setting("placeholder-enabled")
        public Message placeholderEnabled = of("&aPlaceholder enabled!");
        @Setting("player-description")
        public Message playerdesc = of("View information about a player.");
        @Setting("user-description")
        public Message userdesc = of("View information about a user.");
        @Setting("rank-description")
        public Message rankdesc = of("View information about a player's rank.");
        @Setting("reload-button")
        public Message reloadButton = of("&c[RELOAD]");
        @Setting("reload-button-hover")
        public Message reloadButtonHover = of("&bClick to reload this placeholder!");
        @Setting("reload-failed")
        public Message reloadFailed = of("&cPlaceholder failed to reload!");
        @Setting("reload-success")
        public Message reloadSuccess = of("&aPlaceholder reloaded successfully!");
        @Setting("relational-player-description")
        public Message relplayerdesc = of("View information about a player relative to another player.");
        @Setting("relational-rank-description")
        public Message relrankdesc = of("View information about a player's rank relative to another player.");
        @Setting("server-description")
        public Message serverdesc = of("View information about the server.");
        @Setting("sound-description")
        public Message sounddesc = of("Play sounds to players.");
        @Setting("statistics-description")
        public Message statdesc = of("View a player's statistics.");
        @Setting("supported-placeholders")
        public Message supportedPlaceholders = of("&6Supported relational placeholders:");
        @Setting("supported-relational-placeholders")
        public Message supportedPlaceholdersRelational = of("&6Supported relational placeholders:");
        @Setting("time-description")
        public Message timedesc = of("View the current date and time.");
        @Setting("token-needed")
        public Message tokenNeeded = of("&cThis placeholder needs a token!");
    }

    @ConfigSerializable
    public static class Plugins {
        @Setting("placeholders-reloaded")
        public Message reloadCount = of("&a%s placeholders reloaded! (&c%s failed to reload.&a)");
        @Setting("reload-failed")
        public Message reloadFailed = of("&cPlaceholderAPI failed to reload!");
        @Setting("reload-success")
        public Message reloadSuccess = of("&aPlaceholderAPI reloaded successfully!");
        @Setting("service-unavailable")
        public Message serviceUnavailable = of("&cPlaceholders are unavailable!");
    }

    private static Messages inst;

    public static final TypeToken<Messages> type = TypeToken.of(Messages.class);

    public static Messages get() {
        return inst == null ? new Messages() : inst;
    }

    public static void init(Messages inst) {
        Messages.inst = inst;
    }

    private static Message of(String v) {
        return new Message(v);
    }

    public static Text t(String m, Object... args) {
        return TextSerializers.FORMATTING_CODE
                .deserialize((args == null || args.length == 0 ? m : String.format(m, args)));
    }

    @Setting
    public Misc misc = new Misc();

    @Setting
    public Placeholders placeholder = new Placeholders();

    @Setting
    public Plugins plugin = new Plugins();

}
