package craft.meow.meowsync.wdpe.api;

import craft.meow.meowsync.wdpe.Main;
import craft.meow.meowsync.wdpe.packets.ForwardPacket;
import craft.meow.meowsync.wdpe.packets.ServerInfo;
import dev.waterdog.waterdogpe.network.connection.client.ClientConnection;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * MeowSync 对 WaterdogPE 插件公开的 API。
 *
 * <p>使用示例：
 * <pre>
 *     // 向下游服务端发送一个转发包
 *     MeowSyncAPI.sendForward(player, 1, new byte[]{...});
 *     // 监听下游服务端回传的转发包
 *     MeowSyncAPI.onForward((player, packet) -&gt; ...);
 * </pre>
 */
public final class MeowSyncAPI {

    private static final byte[] EMPTY = new byte[0];

    /**
     * 下游服务端通过 {@link ForwardPacket} 回传数据时触发的监听器。
     */
    private static final List<BiConsumer<ProxiedPlayer, ForwardPacket>> FORWARD_LISTENERS = new CopyOnWriteArrayList<>();

    private MeowSyncAPI() {
    }

    /**
     * @return MeowSync-WDPE 插件是否已经启用
     */
    public static boolean isAvailable() {
        Main main = Main.getInstance();
        return main != null && main.isEnabled();
    }

    /**
     * 注册一个转发包监听器。当下游服务端通过 {@link ForwardPacket} 回传数据时会被调用。
     */
    public static void onForward(BiConsumer<ProxiedPlayer, ForwardPacket> listener) {
        if (listener != null) {
            FORWARD_LISTENERS.add(listener);
        }
    }

    /**
     * 派发一个来自下游服务端的转发包，供 {@code PacketHandler} 内部调用。
     */
    public static void dispatchForward(ProxiedPlayer player, ForwardPacket packet) {
        for (BiConsumer<ProxiedPlayer, ForwardPacket> listener : FORWARD_LISTENERS) {
            try {
                listener.accept(player, packet);
            } catch (Throwable t) {
                Main main = Main.getInstance();
                if (main != null) {
                    main.getLogger().error("MeowSync: 处理转发包监听器时发生异常", t);
                }
            }
        }
    }

    /**
     * 向该玩家当前所在的下游服务端发送一个数据同步标记数据包 (id 501)。
     *
     * @param player 目标玩家
     * @return 数据包是否成功发出
     */
    public static boolean sendDataTransfer(ProxiedPlayer player) {
        return sendDataTransfer(player, false);
    }

    /**
     * 向该玩家当前所在的下游服务端发送一个数据同步标记数据包 (id 501)。
     *
     * @param player      目标玩家
     * @param immediately 是否跳过发送队列立即发送
     * @return 数据包是否成功发出
     */
    public static boolean sendDataTransfer(ProxiedPlayer player, boolean immediately) {
        return sendForward(player, 0, EMPTY, immediately);
    }

    /**
     * 向该玩家当前所在的下游服务端发送一个通用转发包 (p2s)。
     *
     * @param player 目标玩家
     * @param msgId  关联 id，服务端回传时会带回同样的值
     * @param data   负载数据
     * @return 数据包是否成功发出
     */
    public static boolean sendForward(ProxiedPlayer player, int msgId, byte[] data) {
        return sendForward(player, msgId, data, false);
    }

    /**
     * 向该玩家当前所在的下游服务端发送一个通用转发包 (p2s)。
     *
     * @param player      目标玩家
     * @param msgId       关联 id，服务端回传时会带回同样的值
     * @param data        负载数据
     * @param immediately 是否跳过发送队列立即发送
     * @return 数据包是否成功发出
     */
    public static boolean sendForward(ProxiedPlayer player, int msgId, byte[] data, boolean immediately) {
        return send(player, new ForwardPacket(msgId, data), immediately, "转发包");
    }

    /**
     * 向该玩家当前所在的下游服务端发送服务器信息 (p2s)。
     *
     * @param player     目标玩家
     * @param serverName 信息所属服务器名
     * @param info       服务器信息（自由格式，建议 JSON）
     * @return 数据包是否成功发出
     */
    public static boolean sendServerInfo(ProxiedPlayer player, String serverName, String info) {
        return sendServerInfo(player, serverName, info, false);
    }

    /**
     * 向该玩家当前所在的下游服务端发送服务器信息 (p2s)。
     *
     * @param player      目标玩家
     * @param serverName  信息所属服务器名
     * @param info        服务器信息（自由格式，建议 JSON）
     * @param immediately 是否跳过发送队列立即发送
     * @return 数据包是否成功发出
     */
    public static boolean sendServerInfo(ProxiedPlayer player, String serverName, String info, boolean immediately) {
        return send(player, new ServerInfo(serverName, info), immediately, "服务器信息包");
    }

    private static boolean send(ProxiedPlayer player, BedrockPacket packet, boolean immediately, String description) {
        if (!isAvailable() || player == null || !player.isConnected()) {
            return false;
        }

        ClientConnection connection = player.getDownstreamConnection();
        if (connection == null || !connection.isConnected()) {
            return false;
        }

        try {
            if (immediately) {
                connection.sendPacketImmediately(packet);
            } else {
                connection.sendPacket(packet);
            }
            return true;
        } catch (Exception e) {
            Main main = Main.getInstance();
            if (main != null) {
                main.getLogger().error("MeowSync: 向 " + player.getName() + " 的下游服务端发送" + description + "失败", e);
            }
            return false;
        }
    }
}
