package craft.meow.meowsync.wdpe;

import craft.meow.meowsync.wdpe.handler.PacketHandler;
import craft.meow.meowsync.wdpe.packets.PacketRegistry;
import dev.waterdog.waterdogpe.event.defaults.PlayerLoginEvent;
import dev.waterdog.waterdogpe.plugin.Plugin;

public class Main extends Plugin {

    private static Main instance;

    @Override
    public void onEnable() {
        instance = this;

        // 必须在代理构建 codec 之前注册，否则自定义数据包无法被解析
        PacketRegistry.register();

        // 玩家登录时为其挂上 MeowSync 的数据包处理器
        this.getProxy().getEventManager().subscribe(PlayerLoginEvent.class, this::onPlayerLogin);

        this.getLogger().info("MeowSync enabled!");
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    private void onPlayerLogin(PlayerLoginEvent event) {
        event.getPlayer().getPluginPacketHandlers().add(new PacketHandler(event.getPlayer()));
    }

    public static Main getInstance() {
        return instance;
    }
}
