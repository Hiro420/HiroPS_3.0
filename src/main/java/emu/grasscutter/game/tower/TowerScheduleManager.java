package emu.grasscutter.game.tower;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.DataLoader;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.TowerScheduleData;
import emu.grasscutter.server.game.GameServer;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class TowerScheduleManager {
    private final GameServer gameServer;

    public GameServer getGameServer() {
        return this.gameServer;
    }

    public TowerScheduleManager(GameServer gameServer) {
        this.gameServer = gameServer;
        this.load();
    }

    private TowerScheduleConfig towerScheduleConfig;

    public synchronized void load() {
        try (Reader fileReader = new InputStreamReader(DataLoader.load("TowerSchedule.json"))) {
            this.towerScheduleConfig = Grasscutter.getGsonFactory().fromJson(fileReader, TowerScheduleConfig.class);
        } catch (Exception e) {
            Grasscutter.getLogger().error("Unable to load tower schedule config.", e);
        }
    }

    public TowerScheduleConfig getTowerScheduleConfig() {
        return this.towerScheduleConfig;
    }

    public TowerScheduleData getCurrentTowerScheduleData() {
        var data = GameData.getTowerScheduleDataMap().get(this.towerScheduleConfig.getScheduleId());
        if (data == null) {
            Grasscutter.getLogger().error("Could not get current tower schedule data by schedule id {}, please check your resource files",
                this.towerScheduleConfig.getScheduleId());
        }

        return data;
    }

    public List<Integer> getAllFloors() {
        List<Integer> floors = new ArrayList<>(this.getCurrentTowerScheduleData().getEntranceFloorId());
        floors.addAll(this.getScheduleFloors());
        return floors;
    }

    public List<Integer> getScheduleFloors() {
        return this.getCurrentTowerScheduleData().getSchedules().get(0).getFloorList();
    }

    public int getNextFloorId(int floorId) {
        var entranceFloors = this.getCurrentTowerScheduleData().getEntranceFloorId();
        var scheduleFloors = this.getScheduleFloors();
        var nextId = 0;

        // find in entrance floors first
        for (int i = 0; i < entranceFloors.size() - 1; i++) {
            if (floorId == entranceFloors.get(i)) {
                nextId = entranceFloors.get(i + 1);
            }
        }

        if (floorId == entranceFloors.get(entranceFloors.size() - 1)) {
            nextId = scheduleFloors.get(0);
        }

        if (nextId != 0) {
            return nextId;
        }

        // find in schedule floors
        for (int i = 0; i < scheduleFloors.size() - 1; i++) {
            if (floorId == scheduleFloors.get(i)) {
                nextId = scheduleFloors.get(i + 1);
            }
        }
        return nextId;
    }

    public Integer getLastEntranceFloor() {
        return this.getCurrentTowerScheduleData().getEntranceFloorId().get(this.getCurrentTowerScheduleData().getEntranceFloorId().size() - 1);
    }
}
