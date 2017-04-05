package cn.bingzhou.rpc.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

import cn.bingzhou.rpc.common.RpcRequest;
import cn.bingzhou.rpcregister.ServiceDiscovery;
import cn.bingzhou.rpcregister.ServiceRegister;

public class RpcProxy {
	
	private String address;
	public RpcProxy(String address){
		this.address=address;
	}
	
	@SuppressWarnings("unchecked")
	public  <T> T createProxy(Class<T> cls){
		
		return (T)Proxy.newProxyInstance(cls.getClassLoader(), cls.getInterfaces(), new InvocationHandler() {
			
			public Object invoke(Object proxy, Method method, Object[] args)
					throws Throwable {
				//启动netty
				RpcRequest request = new RpcRequest();
				request.setClss(method.getParameterTypes());
				request.setInterfaceName(method.getDeclaringClass().getName());
				request.setMethodName(method.getName());
				request.setParams(args);
				request.setRequestId(UUID.randomUUID().toString());
				
				ServiceDiscovery service=new ServiceDiscovery(address);
				String address=service.discover();
				String[] split = address.split(",");
				String ip = split[0];
				int port=Integer.parseInt(split[1]);
				RpcClient rpcClient = new RpcClient(ip,port);
				rpcClient.send(request);
				return rpcClient.getResponse();
			}
		});
		
	}

}
