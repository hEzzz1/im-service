package org.team324.tcp;

import org.team324.codec.config.BootstrapConfig;
import org.team324.tcp.redis.RedisManager;
import org.team324.tcp.server.LimServer;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author crystalZ
 * @date 2024/6/2
 */
public class Stater {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length > 0) {
            start(args[0]);

        }
    }

    private static void start(String path){

        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);

            new LimServer(bootstrapConfig.getLim()).start();
            new LimServer(bootstrapConfig.getLim()).start();

            RedisManager.init(bootstrapConfig);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(500);
        }

    }
}
