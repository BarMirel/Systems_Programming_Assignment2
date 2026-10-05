package bgu.spl.mics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {

	private static volatile MessageBusImpl instance;
	private final ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> eventSubscriptions;
	private final ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubscriptions;
	private final ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> messageQueues;
	private final ConcurrentHashMap<Event<?>, Future<?>> eventFutures;


	private MessageBusImpl() {
		eventSubscriptions = new ConcurrentHashMap<>();
		broadcastSubscriptions = new ConcurrentHashMap<>();
		messageQueues = new ConcurrentHashMap<>();
		eventFutures = new ConcurrentHashMap<>();
	}


	public static MessageBusImpl getInstance() {
		if (instance == null) {
			synchronized (MessageBusImpl.class) {
				if (instance == null) {
					instance = new MessageBusImpl();
				}
			}
		}
		return instance;
	}
	
	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		eventSubscriptions.putIfAbsent(type, new ConcurrentLinkedQueue<>());
		eventSubscriptions.get(type).add(m);
	}
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		broadcastSubscriptions.putIfAbsent(type, new ConcurrentLinkedQueue<>());
		broadcastSubscriptions.get(type).add(m);
	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		Future<T> future = (Future<T>) eventFutures.remove(e);
		if (future != null) {
			future.resolve(result);
		}
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		ConcurrentLinkedQueue<MicroService> subscribers = broadcastSubscriptions.get(b.getClass());
		if (subscribers != null) {
			for (MicroService ms : subscribers) {
				synchronized (messageQueues) {
					try {
						if(ms != null){
							if(messageQueues.get(ms) != null) {
								messageQueues.get(ms).put(b);
								messageQueues.notifyAll();
							}
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		ConcurrentLinkedQueue<MicroService> subscribers = eventSubscriptions.get(e.getClass());
		if (subscribers == null || subscribers.isEmpty()) {
			return null;
		}
		MicroService selectedService;
		synchronized (this) {
			selectedService = subscribers.peek();
			subscribers.remove(selectedService);
			subscribers.add(selectedService);
		}
		synchronized (messageQueues) {
			try {
				if(selectedService != null) {
					if(messageQueues.get(selectedService) != null) {
						Future<T> f = new Future<>();
						eventFutures.put(e, f);
						messageQueues.get(selectedService).put(e);
						messageQueues.notifyAll();
						return f;
					}
				}
				return null;
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
	}

	@Override
	public void register(MicroService m) {
		synchronized (messageQueues) {
			messageQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
		}
	}

	@Override
	public void unregister(MicroService m) {
		synchronized (messageQueues) {
			messageQueues.remove(m);
		}
			synchronized (eventSubscriptions) {
				eventSubscriptions.values().forEach(list -> list.remove(m));
			}
			synchronized (broadcastSubscriptions) {
				broadcastSubscriptions.values().forEach(list -> list.remove(m));
			}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		synchronized (messageQueues) {
			if (!messageQueues.containsKey(m)) throw new InterruptedException("Microservice not registered");
			else if (messageQueues.get(m) != null && !messageQueues.get(m).isEmpty()) {
				return messageQueues.get(m).take();
			}
			else messageQueues.wait();
			return null;
		}
	}

	//methods for testing
	public ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> getEventSubscriptions() {
		return eventSubscriptions;
	}

	public ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> getBroadcastSubscriptions() {
		return broadcastSubscriptions;
	}

	public ConcurrentHashMap<Event<?>, Future<?>> getEventFutures() {
		return eventFutures;
	}

	public ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> getMessageQueues() {
		return messageQueues;
	}

}
