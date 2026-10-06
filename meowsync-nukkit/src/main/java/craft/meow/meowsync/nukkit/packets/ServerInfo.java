package craft.meow.meowsync.nukkit.packets;

import cn.nukkit.network.protocol.DataPacket;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 服务器信息数据包 (s2p2s)。
 *
 * <ul>
 *     <li>s2p：服务端发送 {@link #serverName}，表示想查询该服务器的信息；</li>
 *     <li>p2s：代理端把 {@link #info}（服务器信息，自由格式，建议 JSON）回传给服务端。</li>
 * </ul>
 */
@ToString
public class ServerInfo extends DataPacket {

    public static final int NETWORK_ID = 504;

    @Getter
    @Setter
    private String serverName = "";

    @Getter
    @Setter
    private String info = "";

    public ServerInfo() {
    }

    public ServerInfo(String serverName, String info) {
        this.serverName = serverName == null ? "" : serverName;
        this.info = info == null ? "" : info;
    }

    @Override
    public int packetId() {
        return NETWORK_ID;
    }

    @Override
    public byte pid() {
        return (byte) NETWORK_ID;
    }

    @Override
    public void encode() {
        this.reset();
        this.putString(this.serverName == null ? "" : this.serverName);
        this.putString(this.info == null ? "" : this.info);
    }

    @Override
    public void decode() {
        this.serverName = this.getString();
        this.info = this.getString();
    }
}
