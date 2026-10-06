package craft.meow.meowsync.nukkit.handler;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.DataPacket;
import craft.meow.meowsync.nukkit.api.MeowSyncAPI;
import craft.meow.meowsync.nukkit.packets.ForwardPacket;
import craft.meow.meowsync.nukkit.packets.ServerInfo;

/**
 * 处理代理端下发的 MeowSync 数据包。
 *
 * <p>这些数据包属于服务端与代理端之间的内部协议，必须拦截下来，
 * 避免让原版客户端收到无法识别的数据包。拦截后转发给
 * {@link MeowSyncAPI} 的监听器，供其它插件消费。
 */
public class PacketHandler implements Listener {

    @EventHandler
    public void onDataPacketReceived(DataPacketReceiveEvent event) {
        DataPacket packet = event.getPacket();

        // 通用转发包 (p2s)，由代理端 -> 服务端，不允许转发给玩家客户端
        if (packet instanceof ForwardPacket forwardPacket) {
            event.setCancelled();
            MeowSyncAPI.dispatchForward(event.getPlayer(), forwardPacket);
            return;
        }

        // 服务器信息包 (p2s)，由代理端 -> 服务端
        if (packet instanceof ServerInfo serverInfo) {
            event.setCancelled();
            MeowSyncAPI.dispatchServerInfo(event.getPlayer(), serverInfo);
        }
    }
}
