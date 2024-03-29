package emu.grasscutter.game.managers.deforestation;

import dev.morphia.annotations.Transient;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.game.entity.EntityItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.net.proto.HitTreeNotifyOuterClass;
import emu.grasscutter.net.proto.VectorOuterClass;
import emu.grasscutter.utils.Position;

import java.util.ArrayList;
import java.util.HashMap;

public class DeforestationManager {
    final static int RECORD_EXPIRED_SECONDS = 60 * 5; // 5 min
    final static int RECORD_MAX_TIMES = 3; // max number of wood
    final static int RECORD_MAX_TIMES_OTHER_HIT_TREE = 10; // if hit 10 times other trees, reset wood

    @Transient
    private final Player player;
    @Transient
    private final ArrayList<HitTreeRecord> currentRecord;
    @Transient
    private final static HashMap<Integer, Integer> ColliderTypeToWoodItemID = new HashMap<>();

    static {
        /* define wood types which reflected to item id*/
        ColliderTypeToWoodItemID.put(1, 101301);
        ColliderTypeToWoodItemID.put(2, 101302);
        ColliderTypeToWoodItemID.put(3, 101303);
        ColliderTypeToWoodItemID.put(4, 101304);
        ColliderTypeToWoodItemID.put(5, 101305);
        ColliderTypeToWoodItemID.put(6, 101306);
        ColliderTypeToWoodItemID.put(7, 101307);
        ColliderTypeToWoodItemID.put(8, 101308);
        ColliderTypeToWoodItemID.put(9, 101309);
        ColliderTypeToWoodItemID.put(10, 101310);
        ColliderTypeToWoodItemID.put(11, 101311);
        ColliderTypeToWoodItemID.put(12, 101312);
    }

    public DeforestationManager(Player player) {
        this.player = player;
        this.currentRecord = new ArrayList<>();
    }

    public void resetWood() {
        synchronized (this.currentRecord) {
            this.currentRecord.clear();
        }
    }

    public void onDeforestationInvoke(HitTreeNotifyOuterClass.HitTreeNotify hit) {
        synchronized (this.currentRecord) {
            //Grasscutter.getLogger().info("onDeforestationInvoke! Wood records {}", currentRecord);
            VectorOuterClass.Vector hitPosition = hit.getHitPostion();
            int woodType = hit.getWoodType();
            if (ColliderTypeToWoodItemID.containsKey(woodType)) {// is a available wood type
                Scene scene = this.player.getScene();
                int itemId = ColliderTypeToWoodItemID.get(woodType);
                int positionHash = hitPosition.hashCode();
                HitTreeRecord record = this.searchRecord(positionHash);
                if (record == null) {
                    record = new HitTreeRecord(positionHash);
                } else {
                    this.currentRecord.remove(record);// move it to last position
                }
                this.currentRecord.add(record);
                if (this.currentRecord.size() > RECORD_MAX_TIMES_OTHER_HIT_TREE) {
                    this.currentRecord.remove(0);
                }
                if (record.record()) {
                    EntityItem entity = new EntityItem(scene,
                        null,
                        GameData.getItemDataMap().get(itemId),
                        new Position(hitPosition.getX(), hitPosition.getY(), hitPosition.getZ()),
                        1,
                        false);
                    scene.addEntity(entity);
                }
                //record.record()=false : too many wood they have deforested, no more wood dropped!
            } else {
                Grasscutter.getLogger().warn("No wood type {} found.", woodType);
            }
        }
        // unknown wood type
    }

    private HitTreeRecord searchRecord(int id) {
        for (HitTreeRecord record : this.currentRecord) {
            if (record.getUnique() == id) {
                return record;
            }
        }
        return null;
    }
}
