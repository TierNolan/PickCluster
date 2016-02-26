package org.tiernolan.pickcluster.net.message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.tiernolan.pickcluster.net.message.common.PingCommon;
import org.tiernolan.pickcluster.net.message.common.PongCommon;
import org.tiernolan.pickcluster.net.message.common.VerAckCommon;
import org.tiernolan.pickcluster.net.message.common.VersionCommon;
import org.tiernolan.pickcluster.types.UInt96;
import org.tiernolan.pickcluster.types.encode.Convert;
import org.tiernolan.pickcluster.types.endian.EndianDataInputStream;

public class MessageMap {
	
	private int mask;
	
	private UInt96[] commands;
	@SuppressWarnings("rawtypes")
	private MessageConstructor[] constructors;
	@SuppressWarnings("rawtypes")
	private List<MessageHandler> unknownMessageHandler = new ArrayList<MessageHandler>();
	@SuppressWarnings("rawtypes")
	private List[] handlers;
	
	private boolean locked = false;
	private boolean done = false;
	private List<UInt96> commandList = new ArrayList<UInt96>();
	@SuppressWarnings("rawtypes")
	private Map<UInt96, MessageConstructor> hashMap = new HashMap<UInt96, MessageConstructor>();
	
	protected MessageMap() {
		addAllConstructorsCommon();
		addAllConstructors();
		done();
	}
	
	protected MessageMap(MessageMap map) {
		if (!map.done) {
			throw new IllegalStateException("The done() method for the source map has not been called");
		}
		this.constructors = Arrays.copyOf(map.constructors, map.constructors.length);
		this.commands = Arrays.copyOf(map.commands, map.commands.length);
		this.mask = map.mask;
		initHandlers();
		this.commandList = null;
		this.hashMap = null;
		this.done = true;
		this.locked = false;
	}
	
	private void addAllConstructorsCommon() {
		this.add("version", new MessageConstructor<VersionCommon>() {
			@Override
			public VersionCommon getMessage(int version, EndianDataInputStream in) throws IOException {
				return new VersionCommon(version, in);
			}});
		this.add("verack", new MessageConstructor<VerAckCommon>() {
			@Override
			public VerAckCommon getMessage(int version, EndianDataInputStream in) throws IOException {
				return new VerAckCommon(version, in);
			}});
		this.add("ping", new MessageConstructor<PingCommon>() {
			@Override
			public PingCommon getMessage(int version, EndianDataInputStream in) throws IOException {
				return new PingCommon(version, in);
			}});
		this.add("pong", new MessageConstructor<PongCommon>() {
			@Override
			public PongCommon getMessage(int version, EndianDataInputStream in) throws IOException {
				return new PongCommon(version, in);
			}});
	}
	
	protected void addAllConstructors() {
	}
	
	@SuppressWarnings("rawtypes")
	public MessageMap add(String command, MessageConstructor constructor) {
		return add(Convert.commandStringToUInt96(command), constructor);
	}
	
	@SuppressWarnings("rawtypes")
	public MessageMap add(UInt96 command, MessageConstructor constructor) {
		if (done) {
			throw new IllegalArgumentException("Constructor added after done() method called");
		}
		commandList.add(command);
		hashMap.put(command, constructor);
		return this;
	}
	
	public <T extends Message> MessageMap add(UInt96 command, MessageHandler<T> handler) {
		if (!done) {
			throw new IllegalArgumentException("Handler added before done() method called");
		}
		if (locked) {
			throw new IllegalArgumentException("Handler added after map was locked");
		}
		if (command == null) {
			unknownMessageHandler.add(handler);
			return this;
		}
		int index = command.hashCode() & mask;
		if (constructors[index] == null || !commands[index].equals(command)) {
			throw new IllegalArgumentException("Unknown command " + Convert.UInt96ToCommandString(command));
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		List<MessageHandler> handlerList = handlers[index];
		handlerList.add(handler);		
		return this;
	}
	
	public MessageMap lock() {
		if (!done) {
			throw new IllegalArgumentException("Lock method called before done() method");
		}
		locked = true;
		return this;
	}
	
	public MessageMap done() {
		mask = 1;

		outerLoop:
			while (mask < 0xFFFF) {
				boolean[] hits = new boolean[mask + 1];
				for (UInt96 command : commandList) {
					int index = command.hashCode() & mask;
					if (hits[index]) {
						mask = ((mask + 1) << 1) - 1;
						continue outerLoop;
					}
					hits[index] = true;
				}
				constructors = new MessageConstructor[hits.length];
				commands = new UInt96[hits.length];
				for (UInt96 command : commandList) {
					int index = command.hashCode() & mask;
					constructors[index] = hashMap.get(command);
					commands[index] = command;
				}
				initHandlers();
				hashMap = null;
				commandList = null;
				done = true;
				return this;
			}
		throw new IllegalStateException("Unable to create MessageMap");
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Message> MessageConstructor<T> getConstructor(UInt96 command) {
		int index = command.hashCode() & mask;
		return command.equals(commands[index]) ? constructors[index] : null;
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Message> List<MessageHandler<T>> getHandler(UInt96 command) {
		int index = command.hashCode() & mask;
		return command.equals(commands[index]) ? handlers[index] : null;
	}
	
	public MessageMap copyConstructorsOnly() {
		return new MessageMap(this);
	}
	
	private void initHandlers() {
		handlers = new List[constructors.length];
		for (int i = 0; i < constructors.length; i++) {
			if (constructors[i] != null) {
				@SuppressWarnings("rawtypes")
				List<MessageHandler> handlerList = new ArrayList<MessageHandler>();
				handlers[i] = handlerList;
			}
		}
	}
}
