/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package net.minecraft.entity.ai.brain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import java.util.Collection;
import java.util.List;
import net.minecraft.entity.ai.brain.ScheduleRuleEntry;

public class ScheduleRule {
    private final List<ScheduleRuleEntry> entries = Lists.newArrayList();
    private int prioritizedEntryIndex;

    public ImmutableList<ScheduleRuleEntry> getEntries() {
        return ImmutableList.copyOf(this.entries);
    }

    public ScheduleRule add(int startTime, float priority) {
        this.entries.add(new ScheduleRuleEntry(startTime, priority));
        this.sort();
        return this;
    }

    public ScheduleRule add(Collection<ScheduleRuleEntry> entries) {
        this.entries.addAll(entries);
        this.sort();
        return this;
    }

    private void sort() {
        Int2ObjectAVLTreeMap int2ObjectSortedMap = new Int2ObjectAVLTreeMap();
        this.entries.forEach(scheduleRuleEntry -> int2ObjectSortedMap.put(scheduleRuleEntry.getStartTime(), scheduleRuleEntry));
        this.entries.clear();
        this.entries.addAll(int2ObjectSortedMap.values());
        this.prioritizedEntryIndex = 0;
    }

    public float getPriority(int time) {
        ScheduleRuleEntry scheduleRuleEntry3;
        if (this.entries.size() <= 0) {
            return 0.0f;
        }
        ScheduleRuleEntry scheduleRuleEntry = this.entries.get(this.prioritizedEntryIndex);
        ScheduleRuleEntry scheduleRuleEntry2 = this.entries.get(this.entries.size() - 1);
        boolean bl = time < scheduleRuleEntry.getStartTime();
        int i = bl ? 0 : this.prioritizedEntryIndex;
        float f = bl ? scheduleRuleEntry2.getPriority() : scheduleRuleEntry.getPriority();
        int j = i;
        while (j < this.entries.size() && (scheduleRuleEntry3 = this.entries.get(j)).getStartTime() <= time) {
            this.prioritizedEntryIndex = j++;
            f = scheduleRuleEntry3.getPriority();
        }
        return f;
    }
}

