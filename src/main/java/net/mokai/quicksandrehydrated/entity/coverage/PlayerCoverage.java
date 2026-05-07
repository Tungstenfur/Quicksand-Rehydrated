package net.mokai.quicksandrehydrated.entity.coverage;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.Mth.clamp;

public class PlayerCoverage {

    public List<CoverageEntry> coverageEntries;
    public boolean requiresUpdate = false;
    public boolean renderUpdate = true;
    public int renderGeneration = 0;
    public int skinRenderGeneration = -1;
    public int armorRenderGeneration = -1;

    public PlayerCoverage() {
        this.coverageEntries = new ArrayList<>();
    }

    public void markDirty() {
        requiresUpdate = true;
        renderUpdate = true;
        renderGeneration++;
    }

    public void addCoverageEntry(CoverageEntry newEntry) {
        // Optimized version that tries to combine similar entries and avoid unnecessary updates

        // both begin and end are inclusive
        newEntry.begin = clamp(newEntry.begin, 0, 31);
        newEntry.end = clamp(newEntry.end, 0, 31);
        
        // If begin and end are the same, no need to add this entry
        if (newEntry.begin >= newEntry.end) {
            return;
        }

        // first, we clear the entire region.
        clearCoverage(newEntry.begin, newEntry.end);



        // Check the entries directly above and directly below to see if we can extend them.
        CoverageEntry entryAbove = getEntryAt(newEntry.end+1);
        CoverageEntry entryBelow = getEntryAt(newEntry.begin-1);

        if (entryAbove != null && entryAbove.texture.equals(newEntry.texture)) {
            entryAbove.begin = newEntry.begin;
        }
        else if (entryBelow != null && entryBelow.texture.equals(newEntry.texture)) {
            entryBelow.end = newEntry.end;
        }
        else {
            // otherwise, we cannot extend any coverages, and can simply add the new one.
            coverageEntries.add(newEntry);
        }

        // this function always changes the coverage
        markDirty();

    }

    public void clearCoverage(int begin, int end) {
        // this function clears the coverage in the given area
        // both begin and inclusive are inclusive

        if (begin >= end) return; // Invalid range

        // keep track of entries to delete and add
        List<CoverageEntry> entriesToRemove = new ArrayList<>();
        List<CoverageEntry> entriesToAdd = new ArrayList<>();

        boolean shiftedEntry = false;

        for (CoverageEntry entry : coverageEntries) {

            // Case 1: This entry is fully contained by the range, and can be removed.
            if (entry.begin >= begin && entry.end <= end) {
                entriesToRemove.add(entry);
                continue;
            }

            // otherwise, part of the entry needs to stay behind.

            // variables for whether the bounds are strictly inside this entry
            boolean endInRange = pointInsideEntry(end, entry);
            boolean beginInRange = pointInsideEntry(begin, entry);

            // if neither are in range, we don't need to do anything.
            if (!endInRange && !beginInRange) continue;

            // Case 2: The range is fully inside this entry, and must be spliced in two.
            if (endInRange && beginInRange) {

                // top half is entry
                // next pixel up from range's end
                entry.begin = end+1;

                // bottom half
                // next pixel down from where range begins
                CoverageEntry newEntry = new CoverageEntry(entry.begin, begin-1, entry.texture);
                entriesToAdd.add(newEntry);

            }

            // Case 3: The range overlaps this entry's top
            else if (endInRange) {
                // move entry top downwards
                entry.begin = end+1; // next pixel up
                shiftedEntry = true;
            }

            // Case 4: The range overlaps this entry's bottom
            else {
                // move entry bottom upwards
                entry.end = begin-1; // next pixel down
                shiftedEntry = true;
            }

        }
        
        // Remove entries marked for removal
        coverageEntries.removeAll(entriesToRemove);
        
        // Add new entries created from splits
        coverageEntries.addAll(entriesToAdd);
        
        // If we made any changes, mark for update
        if (!entriesToRemove.isEmpty() || !entriesToAdd.isEmpty() || shiftedEntry) {
            markDirty();
        }

    }

    public CoverageEntry getEntryAt(int point) {
        for (CoverageEntry entry : coverageEntries) {
            if (pointInsideEntry(point, entry)) return entry;
        }
        return null;
    }

    // both begin and end are inclusive
    public boolean pointInsideRange(int point, int begin, int end) {
        return point >= begin && point <= end;
    }
    public boolean pointInsideEntry(int point, CoverageEntry entry) {
        return point >= entry.begin && point <= entry.end;
    }

    public void copyFrom(PlayerCoverage coverage) {
        this.coverageEntries = coverage.coverageEntries;
        markDirty();
    }

}
