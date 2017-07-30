package me.mrkirby153.kcutils.structure;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.MCEditSchematicFormat;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;

public class Structure {

    private final File schematicFile;

    private boolean placed = false;
    private Location placedAt = null;

    private boolean wePresent = false;

    private WorldEditPlugin worldEdit;

    public Structure(File schematicFile) {
        this.schematicFile = schematicFile;
        worldEdit = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        wePresent = worldEdit != null;
    }

    public void place(Location block){
        if(!wePresent)
            throw new IllegalStateException("WorldEdit is required to load structures!");
        EditSession session = worldEdit.getWorldEdit().getEditSessionFactory().getEditSession((World) new BukkitWorld(block.getWorld()), Integer.MAX_VALUE);
        try {
            CuboidClipboard clipboard = MCEditSchematicFormat.getFormat(schematicFile).load(schematicFile);
            Vector origin = new Vector(block.getBlockX(), block.getBlockY(), block.getBlockZ());
            clipboard.paste(session, origin, false);
        } catch (MaxChangedBlocksException | IOException | DataException e) {
            e.printStackTrace();
        }
        this.placedAt = block.clone();
        this.placed = true;
    }

    public void reset(){
        this.placedAt = null;
        this.placed = false;
    }

    public File getSchematicFile() {
        return schematicFile;
    }

    public boolean isPlaced() {
        return placed;
    }

    public Location getPlacedAt() {
        return placedAt;
    }
}
