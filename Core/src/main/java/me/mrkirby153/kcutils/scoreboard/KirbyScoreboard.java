package me.mrkirby153.kcutils.scoreboard;

import me.mrkirby153.kcutils.scoreboard.items.ElementText;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Wrapper class for quickly creating scoreboards
 */
public class KirbyScoreboard {

    private final Random r = new Random();
    private Scoreboard scoreboard;
    private Objective sideObjective;
    private ArrayList<ScoreboardElement> scoreboardElements = new ArrayList<>();
    private Set<ScoreboardTeam> teams = new HashSet<>();
    private String[] current = new String[15];

    private boolean debug = false;

    public KirbyScoreboard(String displayName) {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        sideObjective = addObjective(displayName, DisplaySlot.SIDEBAR);
    }

    /**
     * Adds a scoreboard element
     *
     * @param element The scoreboard element to add
     */
    public void add(ScoreboardElement element) {
        scoreboardElements.add(element);
    }

    /**
     * Adds a scoreboard element
     *
     * @param text The text to add
     */
    public void add(String text) {
        this.add(new ElementText(text));
    }

    /**
     * Adds a team to the scoreboard
     *
     * @param team The team to add
     */
    public void addTeam(ScoreboardTeam team) {
        teams.add(team);
    }

    /**
     * Renders the scoreboard
     */
    public final void draw() {
        if (debug) {
            System.out.println("--------------");
            System.out.println("Calling preDraw()");
        }
        preDraw();
        if (debug)
            System.out.println("Calling updateTeams()");
        updateTeams();

        if (debug)
            System.out.println("Updating scoreboard elements");
        ArrayList<String> newLines = new ArrayList<>();
        for (ScoreboardElement item : this.scoreboardElements) {
            for (String line : item.getLines()) {
                while (true) {
                    boolean matched = false;
                    for (String otherLines : newLines) {
                        if (line.equals(otherLines)) {
                            line += ChatColor.RESET;
                            matched = true;
                        }
                    }
                    if (!matched)
                        break;
                }
                newLines.add(line);
            }
        }

        HashSet<Integer> toAdd = new HashSet<>();
        HashSet<Integer> toRemove = new HashSet<>();
        for (int i = 0; i < 15; i++) {
            if (i >= newLines.size()) {
                if (current[i] != null) {
                    toRemove.add(i);
                }
                continue;
            }

            if (current[i] == null || !current[i].equals(newLines.get(i))) {
                toRemove.add(i);
                toAdd.add(i);
            }
        }
        for (int i : toRemove) {
            if (current[i] != null) {
                resetScore(current[i]);
                current[i] = null;
            }
        }
        for (int i : toAdd) {
            String newLine = newLines.get(i);
            sideObjective.getScore(newLine).setScore(15 - i);
            current[i] = newLine;
        }

        if (debug)
            System.out.println("Calling postDraw()");
        postDraw();
    }

    /**
     * Enable debug output
     */
    public void enableDebug() {
        debug = true;
    }

    /**
     * Gets the scoreboard
     *
     * @return The scoreboard
     */
    public Scoreboard getBoard() {
        return this.scoreboard;
    }

    /**
     * Removes a team from the scoreboard
     *
     * @param team The team to remove
     */
    public void remove(ScoreboardTeam team) {
        teams.remove(team);
    }

    /**
     * Resets the scoreboard (Clears all the lines)
     */
    public void reset() {
        scoreboardElements.clear();
    }

    /**
     * Called after the scoreboard has been rendered
     */
    protected void postDraw() {

    }

    /**
     * Called before the scoreboard is rendered
     */
    protected void preDraw() {

    }

    /**
     * Clears the score of a line on the scoreboard (Removes it)
     *
     * @param line The line to remove
     */
    private void resetScore(String line) {
        scoreboard.resetScores(line);
    }

    /**
     * Updates the teams on the scoreboard
     */
    private void updateTeams() {
        teams.forEach(t -> {
            if (debug)
                System.out.println("Updating team " + t.getTeamName());
            Team scoreboardTeam = null;
            if (scoreboard.getTeam(t.getFilteredName()) != null) {
                scoreboardTeam = scoreboard.getTeam(t.getFilteredName());
            }
            if (scoreboardTeam == null)
                scoreboardTeam = scoreboard.registerNewTeam(t.getFilteredName());
            String prefix = t.getColor() + "";
            if (t.showPrefix()) {
                prefix += String.format("[%s]", t.getPrefix().toUpperCase());
            }
            scoreboardTeam.setPrefix(prefix);
            scoreboardTeam.setSuffix(ChatColor.RESET + "");
            scoreboardTeam.setCanSeeFriendlyInvisibles(t.canSeeInvisibles());
            scoreboardTeam.setAllowFriendlyFire(t.isFriendlyFire());

            // Update all the players on the team
            List<String> playersOnTeam = t.getPlayers().stream().map(Bukkit::getPlayer).filter(Objects::nonNull).map(Player::getName).collect(Collectors.toList());
            Set<String> playersOnScoreboardTeam = scoreboardTeam.getEntries();

            List<String> toAdd = playersOnTeam.stream().filter(p -> !playersOnScoreboardTeam.contains(p)).collect(Collectors.toList());
            List<String> toRemove = playersOnScoreboardTeam.stream().filter(p -> !playersOnTeam.contains(p)).collect(Collectors.toList());
            if (debug && toAdd.size() > 0)
                System.out.println("[ADD] " + Arrays.toString(toAdd.toArray(new String[0])));
            if (debug && toRemove.size() > 0)
                System.out.println("[REMOVE] " + Arrays.toString(toRemove.toArray(new String[0])));

            toRemove.forEach(scoreboardTeam::removeEntry);
            toAdd.forEach(scoreboardTeam::addEntry);
        });
        // Remove teams that no longer exist
        scoreboard.getTeams().forEach(team -> {
            boolean exists = false;
            for (ScoreboardTeam t : teams) {
                if (t.getTeamName().startsWith(team.getName())) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                if (debug)
                    System.out.println("Unregistering team " + team.getName());
                team.unregister();
            }
        });
    }

    /**
     * Adds an objective to the scoreboard
     *
     * @param displayName The display name of the objective
     * @param displaySlot The slot that the objective is displayed in
     * @param criteria    The criteria of the objective
     * @return The Objective
     */
    protected Objective addObjective(String displayName, DisplaySlot displaySlot, String criteria) {
        Objective o = scoreboard.registerNewObjective("Obj-" + r.nextInt(999999), criteria);
        o.setDisplaySlot(displaySlot);
        o.setDisplayName(displayName);
        return o;
    }

    /**
     * Adds a dummy objective to the scoreboard
     *
     * @param displayName The display name of the objective
     * @param displaySlot The slot the objective is displayed in
     * @return The objective
     */
    protected Objective addObjective(String displayName, DisplaySlot displaySlot) {
        return this.addObjective(displayName, displaySlot, "dummy");
    }

}
