package live.connector.vertxui.client.transport;

import com.github.nmorel.gwtjackson.client.ObjectMapper;
import com.google.gwt.core.client.GWT;
import com.google.gwt.xhr.client.XMLHttpRequest;
import com.kfuntak.gwt.json.serialization.client.Serializer;

import elemental.events.Event;
import elemental.events.MessageEvent;
import elemental.html.ArrayBuffer;
import elemental.html.WebSocket;
import elemental.json.Json;
import elemental.json.JsonObject;
import live.connector.vertxui.client.transport.EventBus.Handler;

public class Pojofy {

	public static <I, O> void ajax(String protocol, String url, I model, ObjectMapper<I> inMapper,
			ObjectMapper<O> outMapper, Handler<O> handler) {
		XMLHttpRequest xhr = XMLHttpRequest.create();
		xhr.setOnReadyStateChange(a -> {
			if (xhr.getReadyState() == 4 && xhr.getStatus() == 200) {
				handler.handle(out(xhr.getResponseText(), outMapper));
			}
		});
		xhr.open(protocol, url);
		xhr.send(in(model, inMapper));
	}

	public final native static String toString(ArrayBuffer buf)/*-{
																return String.fromCharCode.apply(null, new Uint8Array(buf));
																}-*/;

	public static <O> boolean socketReceive(String url, Event e, ObjectMapper<O> outMapper, Handler<O> handler) {
		Object me = ((MessageEvent) e).getData();
		String meString = me.toString();
		if (meString.equals("[object ArrayBuffer]")) { // websockets
			meString = toString((ArrayBuffer) me);
		}
		if (!meString.startsWith("{\"url\":\"" + url + "\"")) {
			return false;
		}
		JsonObject json = Json.parse(meString);
		handler.handle(out(json.getString("body"), outMapper));
		return true;
	}

	protected static <I> String in(I model, ObjectMapper<I> inMapper) {
		if (model == null) {
			return null;
		} else if (model instanceof String) {
			return (String) model;
		} else {
			return inMapper.write(model);
		}
	}

	@SuppressWarnings("unchecked")
	protected static <O> O out(String message, ObjectMapper<O> outMapper) {
		if (message == null || outMapper == null) { // outMapper null: string
			return (O) message;
		} else {
			return outMapper.read(message);
		}
	}

	public static <I> void socketSend(WebSocket socket, String url, I model, ObjectMapper<I> inMapper,
			JsonObject headers) {
		JsonObject object = Json.createObject();
		object.put("url", url);
		object.put("body", in(model, inMapper));
		object.put("headers", headers);
		socket.send(object.toJson());
	}

	// will be tried
	public String toJson(Object object) {
		Serializer serializer = (Serializer) GWT.create(Serializer.class);
		return serializer.serializeToJson(object).toString();
	}

	// will be tried
	@SuppressWarnings("unchecked")
	public static <T> T fromJson(String json, String classname) {
		Serializer serializer = (Serializer) GWT.create(Serializer.class);
		return (T) serializer.deSerialize(json, classname);
	}
}
