package org.team324.tcp;

import org.I0Itec.zkclient.ZkClient;
import org.team324.codec.config.BootstrapConfig;
import org.team324.tcp.reciver.MessageReciver;
import org.team324.tcp.redis.RedisManager;
import org.team324.tcp.register.RegistryZk;
import org.team324.tcp.register.Zkit;
import org.team324.tcp.server.LimServer;
import org.team324.tcp.utils.MqFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

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

    private static void start(String path) {

        try {
            Yaml yaml = new Yaml();
            InputStream inputStream = new FileInputStream(path);
            BootstrapConfig bootstrapConfig = yaml.loadAs(inputStream, BootstrapConfig.class);

            new LimServer(bootstrapConfig.getLim()).start();
            new LimServer(bootstrapConfig.getLim()).start();

            RedisManager.init(bootstrapConfig);
            MqFactory.init(bootstrapConfig.getLim().getRabbitmq());
            MessageReciver.init(bootstrapConfig.getLim().getBrokerId().toString());

            registerZk(bootstrapConfig);

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(500);
        }

    }

    private static void registerZk(BootstrapConfig config) throws UnknownHostException {
        String hostAddress = InetAddress.getLocalHost().getHostAddress();

        ZkClient zkClient = new ZkClient(config.getLim().getZkConfig().getZkAddr(),
                config.getLim().getZkConfig().getZkConnectTimeOut());

        Zkit zkit = new Zkit(zkClient);

        RegistryZk registryZk = new RegistryZk(zkit, hostAddress, config.getLim());

        Thread thread = new Thread(registryZk);
        thread.start();
    }

}
