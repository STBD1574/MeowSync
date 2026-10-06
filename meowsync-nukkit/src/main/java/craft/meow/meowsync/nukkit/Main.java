package craft.meow.meowsync.nukkit;

import cn.nukkit.command.PluginCommand;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.plugin.PluginBase;
import craft.meow.meowsync.nukkit.commands.TransferCommand;
import craft.meow.meowsync.nukkit.handler.PacketHandler;
import craft.meow.meowsync.nukkit.packets.ForwardPacket;
import craft.meow.meowsync.nukkit.packets.ReconnectPacket;
import craft.meow.meowsync.nukkit.packets.ServerInfo;
import craft.meow.meowsync.nukkit.packets.ServerTransferPacket;

public class Main extends PluginBase {
    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        // 这些数据包只在 Waterdog 代理模式下才有意义
        if (!this.getServer().isWaterdogCapable()) {
            this.getLogger().warning("Must be enabled use-waterdog in conig to use MeowSync!");
            this.setEnabled(false);
            return;
        }

        this.registerPackets();
        this.getServer().getPluginManager().registerEvents(new PacketHandler(), this);

        this.registerCommands();

        this.getLogger().info("MeowSync enabled!");
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    /**
     * 注册服务端 &lt;-&gt; 代理端之间使用的自定义数据包。
     * <p>
     * 玩家的客户端协议版本可能是任意受支持的版本，因此需要为所有协议注册，
     * 否则部分玩家的连接会因为无法解析该数据包而报错。
     */
    private void registerPackets() {
        for (int protocol : ProtocolInfo.SUPPORTED_PROTOCOLS) {
            this.getServer().getNetwork().registerPacketNew(protocol, ForwardPacket.NETWORK_ID, ForwardPacket.class);
            this.getServer().getNetwork().registerPacketNew(protocol, ServerTransferPacket.NETWORK_ID, ServerTransferPacket.class);
            this.getServer().getNetwork().registerPacketNew(protocol, ReconnectPacket.NETWORK_ID, ReconnectPacket.class);
            this.getServer().getNetwork().registerPacketNew(protocol, ServerInfo.NETWORK_ID, ServerInfo.class);
        }
    }

    // 注册插件命令
    @SuppressWarnings("unchecked")
    private void registerCommands() {
        PluginCommand<Main> trsCommand = (PluginCommand<Main>) this.getCommand("trs");
        trsCommand.setExecutor(new TransferCommand());
    }

    public static Main getInstance() {
        return instance;
    }
}
