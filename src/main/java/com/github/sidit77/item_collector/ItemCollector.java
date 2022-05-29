package com.github.sidit77.item_collector;

import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItemCollector implements ModInitializer {

    public static Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "item_collector";
    public static final String MOD_NAME = "Item Collector";


    @Override
    public void onInitialize() {
        log(Level.INFO, "Initializing..");
    }

    public static void log(Level level, String message){
        LOGGER.log(level, message);
    }

}
