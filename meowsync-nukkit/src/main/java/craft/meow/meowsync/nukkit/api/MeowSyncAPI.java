package craft.meow.meowsync.nukkit.api;

import cn.nukkit.Player;
import craft.meow.meowsync.nukkit.Main;
import craft.meow.meowsync.nukkit.packets.ForwardPacket;
import craft.meow.meowsync.nukkit.packets.ReconnectPacket;
import craft.meow.meowsync.nukkit.packets.ServerInfo;
import craft.meow.meowsync.nukkit.packets.ServerTransferPacket;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

/**
 * MeowSync 对外 API，供其它 Nukkit 插件调用。
 *
 * <p>使用示例：
 * <pre>
 *     MeowSyncAPI.transfer(player, "lobby");
 *     MeowSyncAPI.reconnect(player);
 *     MeowSyncAPI.onServerInfo((player, info) -&gt; ...);
 * </pre>
 */
public final class MeowSyncAPI {

    private static final byte[] EMPTY = new byte[0];

    /**
     * 代理端通过 {@link ForwardPacket} 下发数据时触发的监听器。
     */
    private static final List<BiConsumer<Player, ForwardPacket>> FORWARD_LISTENERS = new CopyOnWriteArrayList<>();

    /**
     * 代理端通过 {@link ServerInfo} 下发服务器信息时触发的监听器。
     */
    private static final List<BiConsumer<Player, ServerInfo>> SERVER_INFO_LISTENERS = new CopyOnWriteArrayList<>();

    private MeowSyncAPI() {
    }

    /**
     * @return 插件是否已经在 Waterdog 模式下正常启用
     */
    public static boolean isAvailable() {
        Main main = Main.getInstance();
        return main != null && main.isEnabled();
    }

    /**
     * 注册一个转发包监听器。当代理端通过 {@link ForwardPacket} 下发数据时会被调用。
     */
    public static void onForward(BiConsumer<Player, ForwardPacket> listener) {
        if (listener != null) {
            FORWARD_LISTENERS.add(listener);
        }
    }

    /**
     * 注册一个服务器信息监听器。当代理端通过 {@link ServerInfo} 下发服务器信息时会被调用。
     */
    public static void onServerInfo(BiConsumer<Player, ServerInfo> listener) {
        if (listener != null) {
            SERVER_INFO_LISTENERS.add(listener);
        }
    }

    /**
     * 派发一个来自代理端的转发包，供 {@code PacketHandler} 内部调用。
     */
    public static void dispatchForward(Player player, ForwardPacket packet) {
        for (BiConsumer<Player, ForwardPacket> listener : FORWARD_LISTENERS) {
            try {
                listener.accept(player, packet);
            } catch (Throwable t) {
                logError("处理转发包监听器时发生异常", t);
            }
        }
    }

    /**
     * 派发一个来自代理端的服务器信息包，供 {@code PacketHandler} 内部调用。
     */
    public static void dispatchServerInfo(Player player, ServerInfo packet) {
        for (BiConsumer<Player, ServerInfo> listener : SERVER_INFO_LISTENERS) {
            try {
                listener.accept(player, packet);
            } catch (Throwable t) {
                logError("处理服务器信息监听器时发生异常", t);
            }
        }
    }

    /**
     * 请求代理端把该玩家转移到指定的下游服务器。
     *
     * @param player     要被转移的玩家
     * @param serverName 目标服务器名称（代理端 servers 配置中注册的名称）
     * @return 数据包是否成功发送
     */
    public static boolean transfer(Player player, String serverName) {
        if (!isAvailable() || player == null || serverName == null || serverName.isEmpty()) {
            return false;
        }
        return player.dataPacket(new ServerTransferPacket(serverName));
    }

    /**
     * 请求代理端让该玩家重新连接（例如服务端重载 / 数据刷新后重新进入）。
     *
     * @param player 目标玩家
     * @return 数据包是否成功发送
     */
    public static boolean reconnect(Player player) {
        if (!isAvailable() || player == null) {
            return false;
        }
        return player.dataPacket(new ReconnectPacket());
    }

    /**
     * 向代理端查询指定服务器的信息，代理端会通过 {@link #onServerInfo} 回传结果。
     *
     * @param player     目标玩家（实际上经由该玩家的连接发往代理端）
     * @param serverName 要查询的服务器名称；为空表示查询玩家当前所在服务器
     * @return 数据包是否成功发送
     */
    public static boolean requestServerInfo(Player player, String serverName) {
        if (!isAvailable() || player == null) {
            return false;
        }
        return player.dataPacket(new ServerInfo(serverName == null ? "" : serverName, ""));
    }

    /**
     * 向代理端发送一个通用转发数据包 (p2s)。
     *
     * @param player 数据包发送的目标（实际上经由该玩家的连接发往代理端）
     * @param msgId  关联 id，代理端回传时会带回同样的值
     * @param data   负载数据
     * @return 数据包是否成功发送
     */
    public static boolean sendForward(Player player, int msgId, byte[] data) {
        if (!isAvailable() || player == null) {
            return false;
        }
        return player.dataPacket(new ForwardPacket(msgId, data));
    }

    /**
     * 向代理端发送一个数据同步标记数据包。
     *
     * @param player 数据包发送的目标（实际上经由该玩家的连接发往代理端）
     * @return 数据包是否成功发送
     */
    public static boolean sendDataTransfer(Player player) {
        return sendForward(player, 0, EMPTY);
    }

    private static void logError(String message, Throwable t) {
        Main main = Main.getInstance();
        if (main != null) {
            main.getLogger().error("MeowSync: " + message, t);
        }
    }
}
