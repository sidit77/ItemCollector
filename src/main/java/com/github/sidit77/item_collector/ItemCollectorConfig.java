package com.github.sidit77.item_collector;

import com.oroarmor.config.Config;
import com.oroarmor.config.ConfigItem;
import com.oroarmor.config.ConfigItemGroup;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.util.List;

public class ItemCollectorConfig extends Config {

    public static final ConfigItem<Double> range  = new ConfigItem<>("range", 20.0, "config.item_collector.range");
    public static final ConfigItem<Double> maxAcceleration = new ConfigItem<>("max_acceleration", 0.1, "config.item_collector.max_acceleration");
    public static final ConfigItem<Double> accelerationFactor  = new ConfigItem<>("acceleration_factor", 0.03, "config.item_collector.acceleration_factor");

    public ItemCollectorConfig() {
        super(
                List.of(new ConfigItemGroup(
                        List.of(range, maxAcceleration, accelerationFactor),
                        "settings"
                )),
                new File(FabricLoader.getInstance().getConfigDir().toFile(), ItemCollector.MOD_ID + ".json"),
                ItemCollector.MOD_ID
        );
    }
}
