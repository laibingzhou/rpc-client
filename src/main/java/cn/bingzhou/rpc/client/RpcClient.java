package cn.bingzhou.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import cn.bingzhou.rpc.common.RpcDecoder;
import cn.bingzhou.rpc.common.RpcEncoder;
import cn.bingzhou.rpc.common.RpcRequest;
import cn.bingzhou.rpc.common.RpcResponse;

public class RpcClient extends SimpleChannelInboundHandler {
	
	
	//启动netty服务
	
	private String ip;
	private int port;
	
	private Object obj=new Object();
	
	
	private RpcResponse response;
	
	public RpcResponse getResponse() {
		return response;
	}


	public RpcClient(String ip,int port){
		this.ip=ip;
		this.port=port;
	}
	
	
	public void send(RpcRequest request) throws Exception{
		
		EventLoopGroup eventGroup=new NioEventLoopGroup();
		
		Bootstrap client=new Bootstrap();
		client.group(eventGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<Channel>() {

			@Override
			protected void initChannel(Channel channel) throws Exception {
				channel.pipeline().addLast(new RpcEncoder(RpcRequest.class))
								  .addLast(new RpcDecoder(RpcResponse.class))
								  .addLast(RpcClient.this);
				
			}
		}).option(ChannelOption.SO_KEEPALIVE, true);
		ChannelFuture future = client.connect(ip, port).sync();
		future.channel().writeAndFlush(request).sync();//等待连接成功后
		synchronized (obj) {
			obj.wait();
		}
		future.channel().close();
		
	}


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object response)
			throws Exception {
		this.response=(RpcResponse) response;
		synchronized (obj) {
			obj.notifyAll();
		}
	}
	
	

}
