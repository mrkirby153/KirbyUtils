package me.mrkirby153.kcutils.scoreboard;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class ScoreboardTeam {

    protected String teamName;

    protected Set<UUID> players = new HashSet<>();

    protected ChatColor color;
    protected ChatColor prefixColor;

    protected String prefixFormat = "[%s]";

    protected boolean showPrefix = false;
    protected String prefix = null;

    protected boolean friendlyFire = false;
    protected boolean seeInvisible = false;

    public ScoreboardTeam(String teamName, ChatColor color) {
        this.teamName = teamName;
        this.color = color;
        this.prefixColor = color;
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
    }

    public boolean canSeeInvisibles() {
        return seeInvisible;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ScoreboardTeam && ((ScoreboardTeam) obj).getTeamName().equals(this.getTeamName());
    }

    public ChatColor getColor() {
        return color;
    }

    public String getFilteredName() {
        return getTeamName().substring(0, Math.min(getTeamName().length(), 16));
    }

    public Set<UUID> getPlayers() {
        return players;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public ChatColor getPrefixColor() {
        return prefixColor;
    }

    public void setPrefixColor(ChatColor prefixColor) {
        this.prefixColor = prefixColor;
    }

    public String getPrefixFormat() {
        return prefixFormat;
    }

    public void setPrefixFormat(String prefixFormat) {
        this.prefixFormat = prefixFormat;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
    }

    public void removePlayer(UUID uuid){
        players.remove(uuid);
    }

    public void setSeeInvisible(boolean seeInvisible) {
        this.seeInvisible = seeInvisible;
    }

    public void setShowPrefix(boolean showPrefix) {
        this.showPrefix = showPrefix;
    }

    public boolean showPrefix() {
        return showPrefix;
    }
}
