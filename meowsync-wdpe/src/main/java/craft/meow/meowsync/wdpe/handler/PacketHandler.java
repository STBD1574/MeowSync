package craft.meow.meowsync.wdpe.handler;

import craft.meow.meowsync.wdpe.api.MeowSyncAPI;
import craft.meow.meowsync.wdpe.packets.ForwardPacket;
import craft.meow.meowsync.wdpe.packets.ReconnectPacket;
import craft.meow.meowsync.wdpe.packets.ServerTransferPacket;
import dev.waterdog.waterdogpe.network.protocol.Signals;
import dev.waterdog.waterdogpe.network.protocol.handler.PluginPacketHandler;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import org.cloudburstmc.protocol.bedrock.PacketDirection;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;
import org.cloudburstmc.protocol.common.PacketSignal;

/**
 * MeowSync 数据包处理器，每个玩家登录时注册一个实例。
 *
 * <p>处理四类自定义数据包：
 * <ul>
 *     <li>{@link ForwardPacket}：p2s2p 通用转发包，直接拦截，绝不下发给客户端；
 *         若是下游服务端回传的，则交给 {@link MeowSyncAPI} 的监听器处理。</li>
 *     <li>{@link ServerTransferPacket}：下游服务端请求转移玩家，代理端执行转移。</li>
 *     <li>{@link ReconnectPacket}：下游服务端请求让玩家重新连接。</li>
 *     <li>{@code craft.meow.meowsync.wdpe.packets.ServerInfo}：下游服务端查询服务器信息，
 *         代理端把结果回传。</li>
 * </ul>
 */
public class PacketHandler implements PluginPacketHandler {

    private final ProxiedPlayer player;

    public PacketHandler(ProxiedPlayer player) {
        this.player = player;
    }

    @Override
    public PacketSignal handlePacket(BedrockPacket packet, PacketDirection direction) {
        if (packet instanceof ForwardPacket forwardPacket) {
            // 只有来自下游服务端（CLIENT_BOUND）的回包才派发给监听器。
            if (direction == PacketDirection.CLIENT_BOUND) {
                MeowSyncAPI.dispatchForward(this.player, forwardPacket);
            }
            return Signals.CANCEL;
        }

        if (packet instanceof ServerTransferPacket transferPacket) {
            // 只有来自下游服务端的数据包才执行转移，避免客户端伪造。
            if (direction == PacketDirection.CLIENT_BOUND) {
                this.handleServerTransfer(transferPacket);
            }
            return Signals.CANCEL;
        }

        if (packet instanceof ReconnectPacket) {
            if (direction == PacketDirection.CLIENT_BOUND) {
                this.handleReconnect();
            }
            return Signals.CANCEL;
        }

        if (packet instanceof craft.meow.meowsync.wdpe.packets.ServerInfo serverInfo) {
            if (direction == PacketDirection.CLIENT_BOUND) {
                this.handleServerInfo(serverInfo);
            }
            return Signals.CANCEL;
        }

        return PacketSignal.UNHANDLED;
    }

    /**
     * 下游服务端请求把玩家转移到指定服务器。
     *
     * <p>{@link ProxiedPlayer#connect(ServerInfo)} 内部会触发 ServerTransferRequestEvent，
     * 其它插件仍然可以拦截该事件或者修改目标服务器。
     */
    private void handleServerTransfer(ServerTransferPacket packet) {
        String serverName = packet.getServerName();
        if (serverName == null || serverName.isEmpty()) {
            return;
        }

        ServerInfo target = this.player.getProxy().getServerInfo(serverName);
        if (target == null) {
            this.player.getLogger().warning("MeowSync: Player " + player.getName() + "'s transfer request was cancelled because of cannot find the server " + serverName);
            return;
        }

        if (!this.player.isConnected()) {
            return;
        }

        try {
            this.player.connect(target);
        } catch (Exception e) {
            this.player.getLogger().error("MeowSync: Player " + this.player.getName() + " was failed connect to " + serverName, e);
        }
    }

    /**
     * 下游服务端请求让玩家重新连接。
     *
     * <p>由于 {@link ProxiedPlayer#connect(ServerInfo)} 对当前服务器会直接返回，
     * 这里使用 {@link ProxiedPlayer#redirectServer(ServerInfo)} 把当前服务器的公共地址
     * 下发给客户端，让客户端重新进入代理端（进而重新连接到服务器）。
     */
    private void handleReconnect() {
        if (!this.player.isConnected()) {
            return;
        }

        ServerInfo current = this.player.getServerInfo();
        if (current == null) {
            this.player.getLogger().warning("MeowSync: Player " + player.getName() + "'s reconnect request was ignored because of the player is not connected to any server");
            return;
        }

        try {
            this.player.redirectServer(current);
        } catch (Exception e) {
            this.player.getLogger().error("MeowSync: Player " + this.player.getName() + " was failed to reconnect", e);
        }
    }

    /**
     * 下游服务端查询某个服务器的信息，代理端把结果回传 (p2s)。
     *
     * <p>当 {@code serverName} 为空时，默认查询玩家当前所在的服务器。
     */
    private void handleServerInfo(craft.meow.meowsync.wdpe.packets.ServerInfo request) {
        String serverName = request.getServerName();
        ServerInfo target = (serverName == null || serverName.isEmpty())
                ? this.player.getServerInfo()
                : this.player.getProxy().getServerInfo(serverName);

        if (target == null) {
            this.player.getLogger().warning("MeowSync: Player " + player.getName() + "'s server info request was ignored because of cannot find the server " + serverName);
            return;
        }

        MeowSyncAPI.sendServerInfo(this.player, target.getServerName(), describe(target), false);
    }

    /**
     * 把 WaterdogPE 的 {@link ServerInfo} 序列化成 MeowSync 的服务器信息字符串。
     */
    private static String describe(ServerInfo info) {
        return "name=" + info.getServerName()
                + ";type=" + info.getServerType().getIdentifier()
                + ";address=" + info.getAddress()
                + ";publicAddress=" + info.getPublicAddress()
                + ";players=" + info.getPlayers().size();
    }
}
