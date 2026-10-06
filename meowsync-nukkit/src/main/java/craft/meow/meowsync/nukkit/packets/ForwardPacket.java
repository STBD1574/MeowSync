package craft.meow.meowsync.nukkit.packets;

import cn.nukkit.network.protocol.DataPacket;
import lombok.Getter;
import lombok.ToString;

/**
 * 代理端 &lt;-&gt; 服务端之间传递的通用转发数据包 (p2s2p)。
 *
 * <p>{@code msgId} 用来关联一次请求和它的回包，{@code data} 承载原始字节。
 * 该数据包属于代理端与服务端的内部协议，绝不允许下发给玩家客户端。
 */
@ToString(exclude = "data")
public class ForwardPacket extends DataPacket {

    public static final int NETWORK_ID = 501;

    @Getter
    private int msgId;

    @Getter
    private byte[] data;

    public ForwardPacket() {
        this(0, new byte[0]);
    }

    public ForwardPacket(int msgId, byte[] data) {
        this.msgId = msgId;
        this.data = data == null ? new byte[0] : data;
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
        // 大端 4 字节 int，与代理端 ByteBuf#readInt 对应
        this.putInt(this.msgId);
        this.putByteArray(this.data);
    }

    @Override
    public void decode() {
        this.msgId = this.getInt();
        this.data = this.getByteArray();
    }
}
